package com.citrus.mCitrusTablet.view.wait


import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citrus.mCitrusTablet.di.prefs
import com.citrus.mCitrusTablet.model.Repository
import com.citrus.mCitrusTablet.model.vo.*
import com.citrus.mCitrusTablet.util.Constants
import com.citrus.mCitrusTablet.util.HideCheck
import com.citrus.mCitrusTablet.util.SingleLiveEvent
import com.citrus.mCitrusTablet.view.reservation.CusNumType
import com.citrus.mCitrusTablet.view.reservation.SearchViewStatus
import com.citrus.mCitrusTablet.view.reservation.TasksEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import timber.log.Timber


enum class SortOrder { BY_LESS, BY_TIME_MORE, BY_MORE, BY_TIME_LESS }
enum class Filter { SHOW_ALL, SHOW_CANCELLED, SHOW_CONFIRM, SHOW_NOTIFIED, SHOW_WAIT }

class WaitViewModel @ViewModelInject constructor(private val model: Repository) :
    ViewModel() {

    private var serverDomain =
        "https://" + prefs.severDomain

    private lateinit var newReservationGuest: ReservationGuests
    private var newReservationCount = 0

    /**是否第一次撈取資料*/
    private var isFirstFetch = true

    /**記憶體暫存 Data List*/
    private var storageList: MutableList<Wait> = mutableListOf()

    /**記憶體暫存memo展開的item*/
    private var expendList = mutableListOf<String>()
    private var sortOrder: SortOrder = SortOrder.BY_TIME_MORE
    private var hideCheck: HideCheck = HideCheck.HIDE_TRUE
    private var nowFilter = Filter.SHOW_ALL
    private var undo = false

    /**待發送訊息貯列*/
    private var smsQueue = arrayListOf<String>()

    private lateinit var storeChangeForDelete: Wait
    private fun isStoreChangeInit() = ::storeChangeForDelete.isInitialized

    private lateinit var selectGuest: Wait
    private fun isSelectGuestInit() = ::selectGuest.isInitialized

    private var delayTime = Constants.DEFAULT_TIME
    private val job = SupervisorJob()
    lateinit var fetchJob: Job

    private val _cusCount = MutableLiveData<String>()
    val cusCount: LiveData<String>
        get() = _cusCount

    private val _allData = MutableLiveData<List<Wait>>()
    val allData: LiveData<List<Wait>>
        get() = _allData

    private val _filterType = MutableLiveData<Filter>()
    val filterType: LiveData<Filter>
        get() = _filterType

    private val _hideCheckType = MutableLiveData<HideCheck>()
    val hideCheckType: LiveData<HideCheck>
        get() = _hideCheckType

    private val _sortType = MutableLiveData<SortOrder>()
    val sortType: LiveData<SortOrder>
        get() = _sortType

    private val _cusNumType = MutableLiveData<CusNumType>()
    val cusNumType: LiveData<CusNumType>
        get() = _cusNumType

    private val _resHasNewData = SingleLiveEvent<ReservationGuests>()
    val resHasNewData: SingleLiveEvent<ReservationGuests>
        get() = _resHasNewData

    private val _isDeliveryStatusChange = SingleLiveEvent<Boolean>()
    val isDeliveryStatusChange: SingleLiveEvent<Boolean>
        get() = _isDeliveryStatusChange


    private val _deliveryInfo = SingleLiveEvent<DeliveryInfo>()
    val deliveryInfo: SingleLiveEvent<DeliveryInfo>
        get() = _deliveryInfo

    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()


    init {
        _cusNumType.value = CusNumType.SHOW_DETAIL
    }


    private fun createFetchJob(): Flow<Job> = flow {
        while (true) {
            emit(fetchAllData())
            delay(delayTime * 60 * 1000)
        }
    }

    fun startFetchJob() {
        fetchJob = viewModelScope.launch {
            createFetchJob().onEach { Timber.d("Wait_onEach: $it") }
                .collect()
        }
    }

    private fun stopFetchJob() {
        fetchJob.cancel()
    }

    /**撈取今日wait資料*/
    private fun fetchAllData() =
        viewModelScope.launch {
            model.fetchAllData(
                serverDomain + Constants.GET_ALL_DATA,
                "wait",
                PostToGetAllData(prefs.rsno, Constants.defaultTimeStr, Constants.defaultTimeStr),
                onCusCount = { cusCount ->
                    _cusCount.postValue(cusCount)
                }, onReservationCount = { num, res ->
                    newReservationCount = num
                    if (res != null) {
                        newReservationGuest = res
                    }
                }, onWaitCount = { _, _ ->

                }).collect { list ->
                if (list.isNotEmpty()) {
                    /**第二次撈取後以儲存的當日訂位人數來比對是否有來自外部的新增資料*/
                    prefs.storageReservationNum =
                        if (!isFirstFetch && (prefs.storageReservationNum != newReservationCount)) {
                            _resHasNewData.postValue(newReservationGuest)
                            prefs.storageReservationNum
                        } else {
                            newReservationCount
                        }

                    list as MutableList<Wait>
                    storageList = list.toMutableList()

                    /**新的List keep上一次選中及展開的狀態*/
                    if (isSelectGuestInit()) {
                        for (item in storageList) {
                            item.isSelect = item.tkey == selectGuest.tkey
                            for (key in expendList) {
                                if (key == item.tkey) {
                                    item.isExpend = true
                                }
                            }
                        }
                    }

                    /**來自外部的新資料加上new hint*/
                    if (prefs.storageWaitNum < storageList.size) {
                        var distance = storageList.size - prefs.storageWaitNum

                        for (index in storageList.size - distance + 1..storageList.size) {
                            storageList[index - 1].isNew = true
                        }
                        prefs.storageWaitNum = storageList.size
                    }

                    sendWaitSms()
                    refreshAllData(storageList)
                    isFirstFetch = false
                } else {
                    storageList = mutableListOf()
                    _allData.postValue(storageList)
                }
            }
        }


    /**新增時無法取得客戶所需的候位網站連結，利用smsQueue儲存新增成功時返回的單號，在reload的時候進行發送*/
    private fun sendWaitSms() {
        if (smsQueue.size > 0) {
            for (guest in storageList.filter { it.status == Constants.ADD }) {
                for (key in smsQueue) {
                    if (guest.tkey == key) {
                        if (guest.phone != null && guest.phone != "") {
                            shortURL(guest, guest.url, "P", Constants.ADD)
                        } else {
                            shortURL(guest, guest.url, "M", Constants.ADD)
                        }
                    }
                }
            }
            smsQueue.clear()
        }
    }


    /**傳送簡訊*/
    private fun sendSMS(guest: Wait, msg: String) {
        viewModelScope.launch {
            model.sendSMS(
                Constants.SEND_SMS,
                "celaviLAB",
                guest.phone!!,
                msg
            ).collect {
                Timber.d("smsStatus%s", it.toString())
            }
        }
    }

    /**傳送mail*/
    private fun sendMail(guest: Wait, msg: String, subTitle: String) {
        viewModelScope.launch {
            model.sendMail(
                Constants.SEND_MAIL,
                guest.email!!,
                msg,
                subTitle,
                "Citrus"
            ).collect {
                Timber.d("smsStatus%s", it.toString())
            }
        }
    }

    /**縮短網址*/
    private fun shortURL(guest: Wait, address: String, sendType: String, sendStatus: String) {
        viewModelScope.launch {
            model.getShortURL(serverDomain + Constants.GET_SHORT_URL, address).collect { shortURL ->
                if (shortURL != null && shortURL != "") {
                    var msg = ""
                    var subTitle = ""

                    when (sendStatus) {
                        Constants.ADD -> {
                            subTitle = prefs.messageWait
                            msg = prefs.storeName + " " + subTitle + "\n" + shortURL
                        }
                        Constants.NOTICE -> {
                            subTitle = prefs.messageNotice
                            msg = prefs.storeName + " " + subTitle + "\n" + shortURL
                        }
                    }


                    when (sendType) {
                        "P" -> {
                            sendSMS(guest, msg)
                        }
                        "M" -> {
                            sendMail(guest, msg, subTitle)
                        }
                    }
                }
            }
        }
    }


    private fun refreshAllData(list: MutableList<Wait>) {

        var wList: MutableList<Wait> = when (nowFilter) {
            Filter.SHOW_ALL -> {
                list
            }
            Filter.SHOW_CANCELLED -> {
                list.filter { it.status == Constants.CANCEL } as MutableList<Wait>
            }
            Filter.SHOW_NOTIFIED -> {
                list.filter { it.status == Constants.NOTICE } as MutableList<Wait>
            }
            Filter.SHOW_CONFIRM -> {
                list.filter { it.status == Constants.CONFIRM } as MutableList<Wait>
            }
            Filter.SHOW_WAIT -> {
                list.filter { it.status == Constants.ADD } as MutableList<Wait>
            }
        }

        if (hideCheck != HideCheck.HIDE_TRUE) {
            _cusCount.postValue(wList.filter { it.status != Constants.CHECK }.size.toString())
            _allData.postValue(
                getSortRequirement(
                    sortOrder,
                    wList.filter { it.status != Constants.CHECK })
            )
        } else {
            _cusCount.postValue(wList.size.toString())
            _allData.postValue(getSortRequirement(sortOrder, wList))
        }
    }

    fun reload() {
        fetchAllData()
    }


    /**撈取預點單資料*/
    fun fetchOrdersDeliver(postToGetDelivery: PostToGetDelivery) =
        viewModelScope.launch {
            model.fetchOrdersDeliveryData(
                serverDomain + Constants.GET_ORDERS_DELIVERY,
                postToGetDelivery,
                onEmpty = {

                }).collect {
                _deliveryInfo.postValue(it)
            }
        }

    /**轉單*/
    fun setOrdersDeliverStatus(orderNo: String) {
        viewModelScope.launch {
            model.setDeliveryStatus(
                serverDomain + Constants.SET_DELIVERY_STATUS,
                PostToSetDeliveryStatus(OrdersDeliveryUpdate(orderNo, "A", "")),
                prefs.rsno
            ).collect {
                if(it){
                    _isDeliveryStatusChange.postValue(it)
                }
            }
        }
    }


    /**改變狀態 (A:新增候位、I:已通知、O:已確認、D:已取消,Ｃ:已入席)*/
    fun changeStatus(wait: Wait, statusStr: String) =
        viewModelScope.launch {
            model.changeStatus(
                serverDomain + Constants.CHANGE_STATUS,
                PostToChangeStatus(Reservation(wait.tkey, statusStr))
            ).collect { status ->

                when (status) {
                    1 -> {
                        if (statusStr == Constants.NOTICE && !undo) {
                            if (wait.phone != null && wait.phone != "") {
                                shortURL(wait, wait.url, "P", Constants.NOTICE)
                            } else {
                                shortURL(wait, wait.url, "M", Constants.NOTICE)
                            }
                        }

                        if (statusStr == Constants.CANCEL) {
                            showUndoToast(wait)
                            storeChangeForDelete = wait
                        }

                        undo = false
                        fetchAllData()
                    }
                    0 -> {

                    }
                }
            }
        }

    /**新增候位資料*/
    fun uploadWait(dataPostToSet: PostToSetWaiting) =
        viewModelScope.launch {
            model.setWaitData(serverDomain + Constants.SET_WAIT, dataPostToSet).collect {
                if (it.status != 0) {
                    smsQueue.add(it.data)
                    prefs.storageWaitNum = prefs.storageWaitNum + 1
                    fetchAllData()
                    tasksEventChannel.send(TasksEvent.ShowSuccessMessage)
                } else {
                    tasksEventChannel.send(TasksEvent.ShowFailMessage)
                }
            }
        }

    /**字串搜尋功能*/
    fun searchForStr(status: SearchViewStatus) {
        when (status) {
            is SearchViewStatus.IsEmpty -> _allData.postValue(
                getSortRequirement(
                    sortOrder,
                    storageList
                )
            )

            is SearchViewStatus.NeedChange -> {
                var tempGuestsList = mutableListOf<Wait>()
                for (item: Wait in storageList) {
                    if (item.phone?.contains(status.searchStr) == true || item.email?.split("@")
                            ?.get(0)
                            ?.contains(status.searchStr) == true
                    ) {
                        tempGuestsList.add(item)
                    }
                }
                refreshAllData(tempGuestsList)
            }
        }
    }


    private fun getSortRequirement(
        sortStatus: SortOrder,
        originalList: List<Wait>
    ): MutableList<Wait> {
        if (originalList.isEmpty()) {
            return mutableListOf()
        }

        return when (sortStatus) {
            SortOrder.BY_LESS -> originalList.sortedWith { first, second ->
                when {
                    first.custNum == second.custNum -> {
                        when {
                            Constants.inputFormat.parse(first.reservationTime) < Constants.inputFormat.parse(
                                second.reservationTime
                            ) -> {
                                -1
                            }
                            Constants.inputFormat.parse(first.reservationTime) > Constants.inputFormat.parse(
                                second.reservationTime
                            ) -> {
                                1
                            }
                            else -> {
                                0
                            }
                        }
                    }
                    first.custNum < second.custNum -> -1
                    else -> 1
                }
            } as MutableList<Wait>

            SortOrder.BY_TIME_LESS -> originalList.sortedWith { first, second ->
                when {
                    Constants.inputFormat.parse(first.reservationTime) == Constants.inputFormat.parse(
                        second.reservationTime
                    ) -> 0

                    Constants.inputFormat.parse(first.reservationTime) < Constants.inputFormat.parse(
                        second.reservationTime
                    ) -> -1
                    else -> 1
                }
            } as MutableList<Wait>

            SortOrder.BY_TIME_MORE -> originalList.sortedWith { first, second ->
                when {
                    Constants.inputFormat.parse(first.reservationTime) == Constants.inputFormat.parse(
                        second.reservationTime
                    ) -> 0

                    Constants.inputFormat.parse(first.reservationTime) < Constants.inputFormat.parse(
                        second.reservationTime
                    ) -> 1
                    else -> -1
                }
            } as MutableList<Wait>

            SortOrder.BY_MORE -> originalList.sortedWith { first, second ->
                when {
                    first.custNum == second.custNum -> {
                        when {
                            Constants.inputFormat.parse(first.reservationTime) < Constants.inputFormat.parse(
                                second.reservationTime
                            ) -> {
                                -1
                            }
                            Constants.inputFormat.parse(first.reservationTime) > Constants.inputFormat.parse(
                                second.reservationTime
                            ) -> {
                                1
                            }
                            else -> {
                                0
                            }
                        }
                    }
                    first.custNum < second.custNum -> 1
                    else -> -1
                }
            } as MutableList<Wait>
        }
    }


    fun onDetachView() {
        onCleared()
    }


    override fun onCleared() {
        stopFetchJob()
        super.onCleared()
    }

    fun sortList(sort: SortOrder) {
        sortOrder = sort
        _sortType.postValue(sortOrder)
        refreshAllData(storageList)
    }

    fun hideChecked(isHide: Boolean) {
        hideCheck = if (isHide) {
            HideCheck.HIDE_TRUE
        } else {
            HideCheck.HIDE_FALSE
        }

        _hideCheckType.postValue(hideCheck)
        refreshAllData(storageList)
    }


    fun sendNotice(guest: Wait) {
        changeStatus(guest, Constants.NOTICE)
    }

    fun changeFilter(filterType: Filter) {
        nowFilter = filterType
        _filterType.postValue(nowFilter)
        refreshAllData(storageList)
    }

    fun itemSelect(wait: Wait) {
        selectGuest = wait
        for (item in storageList) {
            item.isSelect = item == wait
        }

        if (wait.isExpend) {
            expendList.add(wait.tkey)
        } else {
            expendList.remove(wait.tkey)
        }

        refreshAllData(storageList)
    }

    private fun showUndoToast(wait: Wait) = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.ShowUndoDeleteTaskMessageW(wait))
    }

    fun onUndoFinish(wait: Wait) =
        viewModelScope.launch {
            Timber.d("Action: Undo all change")
            undo = true
            changeStatus(wait, storeChangeForDelete.status)
        }

    fun changeCusNumType(type: CusNumType) {
        _cusNumType.postValue(type)
    }


}

