package com.citrus.mCitrusTablet.view.reservation


import android.content.Context
import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.di.prefs
import com.citrus.mCitrusTablet.model.Repository
import com.citrus.mCitrusTablet.model.vo.*
import com.citrus.mCitrusTablet.util.Constants
import com.citrus.mCitrusTablet.util.Constants.inputFormat
import com.citrus.mCitrusTablet.util.HideCheck
import com.citrus.mCitrusTablet.util.MailContentBuild
import com.citrus.mCitrusTablet.util.SingleLiveEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.text.SimpleDateFormat


enum class SortOrder { BY_LESS, BY_TIME, BY_MORE }
enum class CancelFilter { SHOW_CANCELLED, HIDE_CANCELLED }
enum class CusNumType { SHOW_TOTAL, SHOW_DETAIL }
class ReservationViewModel @ViewModelInject constructor(
    private val model: Repository
) :
    ViewModel() {

    private lateinit var context:Context

    private var serverDomain =
        "https://" + prefs.severDomain

    private lateinit var newWaitGuest: Wait
    private var newWaitGuestCount = 0

    /**是否第一次撈取*/
    private var isFirstFetch = true

    /**是否需要畫面重建*/
    private var isReload = true
    private var hideCheck: HideCheck = HideCheck.HIDE_TRUE
    private var hideCancelled: CancelFilter = CancelFilter.SHOW_CANCELLED
    private var delayTime = Constants.DEFAULT_TIME

    /**記憶體暫存 Data List*/
    private var storageList = mutableListOf<ReservationGuests>()

    /**記憶體暫存memo展開的item*/
    private var expendList = mutableListOf<String>()
    private lateinit var fetchJob: Job

    /**記憶體暫存highLight的item*/
    private lateinit var selectGuest: ReservationGuests
    private fun isSelectGuestInit() = ::selectGuest.isInitialized

    private val _highCheckEvent = MutableLiveData<HideCheck>()
    val highCheckEvent: LiveData<HideCheck>
        get() = _highCheckEvent

    private val _seatData = SingleLiveEvent<List<Floor>>()
    val seatData: SingleLiveEvent<List<Floor>>
        get() = _seatData

    private val _orderDateDatum = SingleLiveEvent<List<OrderDateDatum>>()
    val orderDateDatum: SingleLiveEvent<List<OrderDateDatum>>
        get() = _orderDateDatum

    private val _datumData = SingleLiveEvent<List<Datum>>()
    val datumData: SingleLiveEvent<List<Datum>>
        get() = _datumData

    private val _titleData = MutableLiveData<List<String>>()
    val titleData: LiveData<List<String>>
        get() = _titleData

    private val _allData = MutableLiveData<List<List<ReservationGuests>>>()
    val allData: LiveData<List<List<ReservationGuests>>>
        get() = _allData

    private val _holdData = MutableLiveData<List<List<ReservationGuests>>>()
    val holdData: LiveData<List<List<ReservationGuests>>>
        get() = _holdData

    private val _forDeleteData = MutableLiveData<List<Any>>()
    val forDeleteData: LiveData<List<Any>>
        get() = _forDeleteData

    private val _dateRange = MutableLiveData<Array<String>>()
    val dateRange: LiveData<Array<String>>
        get() = _dateRange

    private val _orderDate = MutableLiveData<Array<String>>()
    val orderDate: LiveData<Array<String>>
        get() = _orderDate

    private val _waitHasNewData = SingleLiveEvent<Wait>()
    val waitHasNewData: SingleLiveEvent<Wait>
        get() = _waitHasNewData

    private val _isLoading = SingleLiveEvent<Boolean>()
    val isLoading: SingleLiveEvent<Boolean>
        get() = _isLoading

    private val _isFirst = SingleLiveEvent<Boolean>()
    val isFirst: SingleLiveEvent<Boolean>
        get() = _isFirst

    private val _noNeedChange = SingleLiveEvent<Boolean>()
    val noNeedChange: SingleLiveEvent<Boolean>
        get() = _noNeedChange


    private val _cusCount = MutableLiveData<String>()
    val cusCount: LiveData<String>
        get() = _cusCount

    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()


    init {
        if (prefs.storeName == "") {
            fetchStoreInfo()
        }
    }


    private fun createFetchJob(): Flow<Job> = flow {
        while (true) {
            if (_dateRange.value == null) {
                emit(fetchAllData(Constants.getCurrentDate(), Constants.getCurrentDate()))
            } else {
                _dateRange.value?.get(0)?.let { emit(fetchAllData(it, _dateRange.value?.get(1)!!)) }
            }
            delay(delayTime * 60 * 1000)
        }
    }

    fun startFetchJob() {
        fetchJob = viewModelScope.launch {
            createFetchJob().onEach { Timber.d("Res_onFetch: $it") }
                .collect()
        }
    }

    private fun stopFetchJob() {
        fetchJob.cancel()
        isFirstFetch = true
    }

    /**資料刷新，預期畫面重建，實際由fetchAllData重組List後判斷是否需要*/
    fun reload() {
        isReload = true
        if (_dateRange.value == null) {
            fetchAllData(Constants.getCurrentDate(), Constants.getCurrentDate())
        } else {
            _dateRange.value?.get(0)?.let { fetchAllData(it, _dateRange.value?.get(1)!!) }
        }
    }

    /**畫面上方選擇日期*/
    fun setDateArray(data: Array<String>) {
        _dateRange.value = data
        fetchAllData(data[0], data[1])
        prefs.storageWaitNum = 0
        isFirstFetch = true
    }


    /**畫面左方選擇日期*/
    fun setDateArrayReservation(data: Array<String>) {
        _orderDate.value = data
    }


    /**撈取座位資料*/
    fun setSearchVal(rsno: String, reservationTime: String, customNum: Int) {
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm")
        var date = sdf.parse(reservationTime)
        val sdf2 = SimpleDateFormat("yyyy-MM-dd HH:mm")
        var searchStr = sdf2.format(date)
        val changeForUpdate = SimpleDateFormat("MM-dd-yyyy HH:mm")
        val updateChange = changeForUpdate.format(date)

        var bookingPostData = PostToGetSeats(rsno, updateChange, customNum)
        fetchReservationFloor(bookingPostData, searchStr)
    }


    private fun fetchReservationFloor(postToGetSeats: PostToGetSeats, searchStr: String) =
        viewModelScope.launch {
            model.fetchReservationFloor(
                serverDomain + Constants.GET_FLOOR,
                postToGetSeats,
                onEmpty = {
                    _seatData.postValue(mutableListOf())
                }).collect { datumList ->

                var hasDate = false
                var seatData: List<Floor> = mutableListOf()

                for (datum in datumList) {
                    if (datum.resDate == searchStr) {
                        seatData = datum.floor
                        hasDate = true
                    }
                }

                if (hasDate) {
                    _seatData.postValue(seatData)
                } else {
                    _datumData.postValue(datumList)
                }
            }
        }


    /**撈取今日reservation資料*/
    private fun fetchAllData(startTime: String, endTime: String) =
        viewModelScope.launch {
            _isLoading.postValue(true)
            model.fetchAllData(
                serverDomain + Constants.GET_ALL_DATA,
                "reservation",
                PostToGetAllData(prefs.rsno, startTime, endTime),
                onCusCount = { cusCount ->
                    _cusCount.postValue(cusCount)
                }, onWaitCount = { num, guest ->
                    if (num != -1) {
                        newWaitGuestCount = num
                        if (guest != null) {
                            newWaitGuest = guest
                        }
                    }
                }, onReservationCount = { _, _ ->

                }).collect { list ->
                _isLoading.postValue(false)
                if (list.isNotEmpty()) {

                    /**第二次撈取後以儲存的候位人數來比對是否有來自外部的新增資料*/
                    prefs.storageWaitNum =
                        if (!isFirstFetch && (prefs.storageWaitNum != newWaitGuestCount)) {
                            _waitHasNewData.postValue(newWaitGuest)
                            prefs.storageWaitNum
                        } else {
                            newWaitGuestCount
                        }


                    /**新的List keep上一次選中及展開的狀態*/
                    var newList = list as MutableList<ReservationGuests>
                    if (isSelectGuestInit()) {
                        for (item in newList) {
                            item.isSelect = item.tkey == selectGuest.tkey
                            for (key in expendList) {
                                if (key == item.tkey) {
                                    item.isExpend = true
                                }
                            }
                        }
                    }


                    /**如果有不一樣才走removeSection，否則單純刷新畫面*/
                    if (newList != storageList) {
                        storageList = newList

                        /**判斷日期為本日才具備新增提醒功能*/
                        if (startTime == Constants.getCurrentDate()) {
                            if (prefs.storageReservationNum < storageList.size) {
                                var distance = storageList.size - prefs.storageReservationNum

                                for (index in storageList.size - distance + 1..storageList.size) {
                                    storageList[index - 1].isNew = true
                                }
                                prefs.storageReservationNum = storageList.size
                            }
                        }

                        isReload = true
                        allDataReorganization(getSortRequirement(SortOrder.BY_TIME, storageList))
                    } else {
                        _noNeedChange.postValue(true)
                        isReload = false
                    }

                    isFirstFetch = false

                } else {
                    storageList = mutableListOf()
                    _titleData.postValue(listOf())
                    _allData.postValue(mutableListOf())
                }
            }
        }


    /**撈取店鋪資料*/
    private fun fetchStoreInfo() =
        viewModelScope.launch {
            model.fetchStoreInfo(
                serverDomain + Constants.GET_STORE_INFO,
                prefs.storeId
            ).collect {
                prefs.storePic = it[0].pic
                prefs.storeName = it[0].storeName
                prefs.rsno = it[0].rsno
                prefs.messageRes = it[0].message1
                prefs.messageWait = it[0].message2
                prefs.messageNotice = it[0].message3
                _isFirst.postValue(true)
            }
        }


    /** highLight功能 */
    fun itemSelect(guest: ReservationGuests) {
        selectGuest = guest
        for (item in storageList) {
            item.isSelect = item == guest
        }

        if (guest.isExpend) {
            expendList.add(guest.tkey)
        } else {
            expendList.remove(guest.tkey)
        }

        allDataReorganization(storageList)
    }

    fun deleteNone() {
        allDataReorganization(storageList)
    }

    /**拆分原始DATA組成SectionRecyclerView需求的樣式，分成Title List以及Data List*/
    private fun allDataReorganization(list: MutableList<ReservationGuests>) {
        var sortGuests =
            if (list.isNotEmpty()) getSortRequirement(SortOrder.BY_TIME, list) else list
        var isFirst = true
        var tempValue = ""
        val timeTitle = mutableSetOf<String>()
        var tempItem = mutableListOf<ReservationGuests>()
        val groupItem = mutableListOf<List<ReservationGuests>>()
        var totalList: MutableList<Any> = mutableListOf()

        /**是否過濾入席、已取消*/
        sortGuests =
            if (hideCancelled == CancelFilter.HIDE_CANCELLED) sortGuests.filter { it.status != Constants.CANCEL } as MutableList<ReservationGuests> else sortGuests
        sortGuests =
            if (hideCheck != HideCheck.HIDE_TRUE) sortGuests.filter { it.status != Constants.CHECK } as MutableList<ReservationGuests> else sortGuests

        if (sortGuests.isNotEmpty()) {
            for (item in sortGuests) {
                val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(item.reservationTime)
                val formattedDate = SimpleDateFormat("MM/dd HH:mm").format(date)
                var dateStr = formattedDate.split(" ")
                var timeStr = dateStr[1].split(":")
                timeTitle.add(timeStr[0] + ":00")

                /**是不是第一個title*/
                if (isFirst) {
                    tempValue = timeStr[0]
                }

                if (timeStr[0] != tempValue) {
                    groupItem.add(tempItem.toMutableList())
                    tempItem.clear()
                }
                tempItem.add(item)
                isFirst = false
                tempValue = timeStr[0]
            }
            groupItem.add(tempItem.toMutableList())


            /**用於SectionRecyclerView Swipe取得物件使用*/
            var title = timeTitle.toList()
            for (index in title.indices) {
                totalList.add(title[index])
                for (item in groupItem[index]) {
                    totalList.add(item)
                }
            }
        }

        _cusCount.postValue(sortGuests.size.toString())
        _forDeleteData.postValue(totalList)
        _titleData.postValue(timeTitle.toList())

        if (isReload) {
            _allData.postValue(groupItem)
            isReload = false
        } else {
            _holdData.postValue(groupItem)
        }

    }


    /**字串搜尋功能，需要重建畫面*/
    fun searchForStr(status: SearchViewStatus) {
        isReload = true
        when (status) {
            is SearchViewStatus.IsEmpty -> {
                allDataReorganization(storageList)
            }

            is SearchViewStatus.NeedChange -> {
                var tempGuestsList = mutableListOf<ReservationGuests>()
                for (item: ReservationGuests in storageList) {
                    if (item.phone?.contains(status.searchStr) == true || item.email?.split("@")
                            ?.get(0)
                            ?.contains(status.searchStr) == true
                    ) {
                        tempGuestsList.add(item)
                    }
                }
                allDataReorganization(tempGuestsList)
            }
        }
    }


    /**改變狀態 (A:新增訂位、D:已取消、Ｃ:已入席)*/
    fun changeStatus(guest: ReservationGuests, status: String) =
        viewModelScope.launch {
            model.changeStatus(
                serverDomain + Constants.CHANGE_STATUS,
                PostToChangeStatus(Reservation(guest.tkey, status))
            ).collect { result ->
                when (result) {
                    1 -> {
                        var index = storageList.indexOf(guest)
                        if (status == Constants.CANCEL) {
                            storageList[index].updateDate = Constants.getSpecCurrentTime()
                            showUndoToast(storageList[index])
                        }

                        storageList[index].status = status
                        allDataReorganization(storageList)
                    }
                    0 -> {

                    }
                }
            }
        }

    /**新增預約資料*/
    fun uploadReservation(dataPostToSet: PostToSetReservation) =
        viewModelScope.launch {
            model.uploadReservationData(serverDomain + Constants.SET_RESERVATION, dataPostToSet)
                .collect {
                    if (it.status == 1 && it.data == 1) {
                        prefs.storageReservationNum = prefs.storageReservationNum + 1
                        tasksEventChannel.send(TasksEvent.ShowSuccessMessage)
                        var time = dataPostToSet.reservation.reservationTime.split(" ")
                        fetchAllData(time[0], time[0])

                        if (dataPostToSet.reservation.phone != null && dataPostToSet.reservation.phone != "") {
                            sendSMS(
                                "celaviLAB",
                                dataPostToSet.reservation.phone,
                                prefs.storeName + " " + dataPostToSet.reservation.reservationTime + " " + prefs.messageRes
                            )
                        } else {
                            var dateStr = dataPostToSet.reservation.reservationTime.split(" ")
                            var subject = prefs.storeName + " " + dataPostToSet.reservation.reservationTime + " " + dataPostToSet.reservation.custNum + " " + context.resources.getString(
                                R.string.gustertip
                            ) + " " + context.resources.getString(
                                R.string.AlreadyRes
                            )
                            var mailText = MailContentBuild(context).genericMsg(
                                "$serverDomain/images/" + prefs.storePic,
                                prefs.storeName,
                                dataPostToSet.reservation.mName,
                                dataPostToSet.reservation.day,
                                dateStr[0].replace("/", "-"),
                                dataPostToSet.reservation.adultCount.toString(),
                                dataPostToSet.reservation.kidCount.toString(),
                                dataPostToSet.reservation.memo,
                                dateStr[1]
                            )

                            sendMail(
                                dataPostToSet.reservation.email,
                                mailText,
                                subject,
                                "Citrus"
                            )
                        }

                    } else {
                        tasksEventChannel.send(TasksEvent.ShowFailMessage)
                    }
                }
        }


    /**傳送mail*/
    private fun sendMail(email: String, msg: String, subject: String ,fromName:String) {
        viewModelScope.launch {
            model.sendMail(
                Constants.SEND_MAIL,
                email,
                msg,
                subject,
                fromName
            ).collect {
                Timber.d("smsStatus%s", it.toString())
            }
        }
    }


    /**傳送簡訊*/
    private suspend fun sendSMS(project: String, phone: String, body: String) =
        model.sendSMS(Constants.SEND_SMS, project, phone, body).collect {
            /**Do nothing*/
        }


    /**依條件搜尋可供預約的時間*/
    fun fetchReservationTime(postData: String) =
        viewModelScope.launch {
            model.fetchReservationTime(
                serverDomain + Constants.GET_RESERVATION_TIME,
                postData,
                onEmpty = {
                    _orderDateDatum.postValue(mutableListOf())
                },OnErrorReceive = {

                }).collect {
                _orderDateDatum.postValue(it)
            }
        }

    /**排序*/
    private fun getSortRequirement(
        sortStatus: SortOrder,
        originalList: List<ReservationGuests>
    ): MutableList<ReservationGuests> {
        if (originalList.isEmpty()) {
            return mutableListOf()
        }
        return when (sortStatus) {

            SortOrder.BY_LESS -> originalList.sortedWith { first, second ->
                when {
                    first.custNum == second.custNum -> {
                        when {
                            inputFormat.parse(first.reservationTime) < inputFormat.parse(second.reservationTime) -> {
                                -1
                            }
                            inputFormat.parse(first.reservationTime) > inputFormat.parse(second.reservationTime) -> {
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
            } as MutableList<ReservationGuests>

            SortOrder.BY_TIME -> originalList.sortedWith { first, second ->
                when {
                    inputFormat.parse(first.reservationTime) == inputFormat.parse(second.reservationTime) -> {
                        when {
                            first.floorName + first.roomName < second.floorName + second.roomName -> {
                                -1
                            }
                            first.floorName + first.roomName > second.floorName + second.roomName -> {
                                1
                            }
                            else -> {
                                0
                            }
                        }
                    }
                    inputFormat.parse(first.reservationTime) < inputFormat.parse(second.reservationTime) -> -1
                    else -> 1
                }
            } as MutableList<ReservationGuests>

            SortOrder.BY_MORE -> originalList.sortedWith { first, second ->
                when {
                    first.custNum == second.custNum -> {
                        when {
                            inputFormat.parse(first.reservationTime) < inputFormat.parse(second.reservationTime) -> {
                                -1
                            }
                            inputFormat.parse(first.reservationTime) > inputFormat.parse(second.reservationTime) -> {
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
            } as MutableList<ReservationGuests>
        }
    }

    /**隱藏已入席*/
    fun hideChecked(isHide: Boolean) {
        hideCheck = if (isHide) {
            HideCheck.HIDE_TRUE
        } else {
            HideCheck.HIDE_FALSE
        }

        _highCheckEvent.postValue(hideCheck)
        isReload = !isReload
        allDataReorganization(storageList)
    }

    /**隱藏已取消*/
    fun hideCancelled(cancelFilter: CancelFilter) {
        hideCancelled = cancelFilter
        isReload = !isReload
        allDataReorganization(storageList)
    }

    /**刪除後顯示回復按鈕*/
    private fun showUndoToast(guest: ReservationGuests) = viewModelScope.launch {
        tasksEventChannel.send(TasksEvent.ShowUndoDeleteTaskMessageR(guest))
    }

    /**從刪除狀態回復*/
    fun onUndoFinish(guest: ReservationGuests) =
        viewModelScope.launch {
            Timber.d("Action: Undo all change")
            changeStatus(guest, Constants.ADD)
        }


    fun onDetachView() {
        onCleared()
    }

    override fun onCleared() {
        stopFetchJob()
        super.onCleared()
    }

    fun setContext(context:Context){
        this.context = context
    }

    fun changeCusNum() {
        isReload = true
        allDataReorganization(storageList)
    }

}


sealed class SearchViewStatus {
    object IsEmpty : SearchViewStatus()
    data class NeedChange(val searchStr: String) : SearchViewStatus()
}

sealed class TasksEvent {
    data class ShowUndoDeleteTaskMessageR(val guest: ReservationGuests) : TasksEvent()
    data class ShowUndoDeleteTaskMessageW(val wait: Wait) : TasksEvent()
    object ShowSuccessMessage : TasksEvent()
    object ShowFailMessage : TasksEvent()
    object ShowPrintSuccessMessage : TasksEvent()
    data class ShowPrintFailMessage(val str:String) : TasksEvent()
}
