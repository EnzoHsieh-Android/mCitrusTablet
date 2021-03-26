package com.citrus.mCitrusTablet.view.wait


import android.util.Log
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
import com.citrus.mCitrusTablet.view.reservation.SearchViewStatus
import com.citrus.mCitrusTablet.view.reservation.TasksEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import okhttp3.internal.wait
import timber.log.Timber


enum class SortOrder { BY_LESS, BY_TIME_MORE, BY_MORE, BY_TIME_LESS }
enum class Filter {SHOW_ALL, SHOW_CANCELLED, SHOW_CONFIRM, SHOW_NOTIFIED, SHOW_WAIT}

class WaitViewModel @ViewModelInject constructor(private val model: Repository) :
    ViewModel() {

    private var serverDomain =
        "https://" + prefs.severDomain

    private lateinit var newReservationGuest:ReservationGuests
    private var newReservationCount = 0
    private var isFirstFetch = true
    private var storageList: MutableList<Wait> = mutableListOf()
    private var expendList = mutableListOf<String>()
    private var sortOrder: SortOrder = SortOrder.BY_TIME_MORE
    private var hideCheck: HideCheck = HideCheck.HIDE_TRUE
    private var nowFilter = Filter.SHOW_ALL
    private var undo = false
    private var smsQueue = arrayListOf<String>()

    private lateinit var storeChangeForDelete:Wait
    private fun isStoreChangeInit()=::storeChangeForDelete.isInitialized

    private lateinit var selectGuest:Wait
    private fun isSelectGuestInit()=::selectGuest.isInitialized

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

    private val _resHasNewData = SingleLiveEvent<ReservationGuests>()
    val resHasNewData: SingleLiveEvent<ReservationGuests>
        get() = _resHasNewData


    private val _deliveryInfo = SingleLiveEvent<List<OrdersItemDelivery>>()
    val deliveryInfo: SingleLiveEvent<List<OrdersItemDelivery>>
        get() = _deliveryInfo

    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()




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

    private fun fetchAllData() =
        viewModelScope.launch {
            model.fetchAllData(
                serverDomain + Constants.GET_ALL_DATA,
                "wait",
                PostToGetAllData(prefs.rsno, Constants.defaultTimeStr, Constants.defaultTimeStr),
                onCusCount = { cusCount ->
                    _cusCount.postValue(cusCount)
                },onReservationCount = { num,res ->
                    newReservationCount = num
                    Log.e("newReservationCount",newReservationCount.toString())
                    if (res != null) {
                        newReservationGuest = res
                    }
                },onWaitCount = { _,_ ->

                }).collect { list ->
                if (list.isNotEmpty()) {

                    prefs.storageReservationNum = if(!isFirstFetch && (prefs.storageReservationNum != newReservationCount)){
                        _resHasNewData.postValue(newReservationGuest)
                        prefs.storageReservationNum
                    }else{
                        newReservationCount
                    }
                    Log.e("prefs.storageReservationNum",prefs.storageReservationNum.toString())

                    list as MutableList<Wait>
                    storageList = list.toMutableList()

                    if(isSelectGuestInit()){
                        for(item in storageList){
                            item.isSelect = item.tkey == selectGuest.tkey
                            for(key in expendList){
                                if(key == item.tkey){
                                    item.isExpend = true
                                }
                            }
                        }
                    }

                    if(prefs.storageWaitNum < storageList.size){
                        var distance = storageList.size - prefs.storageWaitNum

                        for(index in storageList.size - distance +1 .. storageList.size){
                            storageList[index-1].isNew = true
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


    /*新增時無法取得wait url,利用smsQueue儲存新增成功時返回的單號*/
    private fun sendWaitSms() {
       if(smsQueue.size>0){
          for (guest in storageList.filter { it.status == Constants.ADD }){
              for(key in smsQueue){
                  if(guest.tkey == key){
                      if(guest.phone != null && guest.phone != "") {
                          sendSMS(
                              guest,
                              prefs.storeName + " " + prefs.messageWait + "\n" + guest.url
                          )
                      }else{
                          sendMail(guest,prefs.storeName + " " + prefs.messageWait + "\n" + guest.url)
                      }
                  }
              }
          }
           smsQueue.clear()
       }
    }


    private fun sendSMS(guest:Wait, msg:String){
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

    private fun sendMail(guest:Wait,msg:String){
        viewModelScope.launch {
            model.sendMail(
                Constants.SEND_MAIL,
                guest.email!!,
                msg
            ).collect {
                Timber.d("smsStatus%s", it.toString())
            }
        }
    }


    private fun refreshAllData(list: MutableList<Wait>) {

        var wlist: MutableList<Wait> = when(nowFilter){
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
            _cusCount.postValue(wlist.filter { it.status != Constants.CHECK }.size.toString())
            _allData.postValue(
                getSortRequirement(
                    sortOrder,
                    wlist.filter { it.status != Constants.CHECK })
            )
        } else {
            _cusCount.postValue(wlist.size.toString())
            _allData.postValue(getSortRequirement(sortOrder, wlist))
        }
    }

    fun reload() {
        fetchAllData()
    }


    fun fetchOrdersDeliver(postToGetDelivery: PostToGetDelivery) =
        viewModelScope.launch {
            model.fetchOrdersDeliveryData(
                serverDomain + Constants.GET_ORDERS_DELIVERY,
                postToGetDelivery,
                onEmpty = {
                    _deliveryInfo.postValue(mutableListOf())
                }).collect {
                _deliveryInfo.postValue(it)
            }
        }


    fun changeStatus(wait: Wait, statusStr: String) =
        viewModelScope.launch {
            model.changeStatus(
                serverDomain + Constants.CHANGE_STATUS,
                PostToChangeStatus(Reservation(wait.tkey, statusStr))
            ).collect { status ->

                when (status) {
                    1 -> {
                        if(statusStr == Constants.NOTICE && !undo){
                            var msg = prefs.storeName+ " " + prefs.messageNotice + "\n" + wait.url
                            if(wait.phone != null && wait.phone != ""){
                                sendSMS(wait, msg)
                            }else{
                                sendMail(wait,msg)
                            }
                        }

                        if(statusStr == Constants.CANCEL){
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
                    if (item.phone?.contains(status.searchStr) == true || item.email?.split("@")?.get(0)
                            ?.contains(status.searchStr) == true) {
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


    fun onDetachView(){
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
        changeStatus(guest,Constants.NOTICE)
    }

    fun changeFilter(filterType: Filter) {
        nowFilter = filterType
        _filterType.postValue(nowFilter)
        refreshAllData(storageList)
    }

    fun itemSelect(wait: Wait) {
        selectGuest = wait
        for(item in storageList){
            item.isSelect = item == wait
        }

        if(wait.isExpend){
            expendList.add(wait.tkey)
        }else{
            expendList.remove(wait.tkey)
        }

        refreshAllData(storageList)
    }

    private fun showUndoToast(wait:Wait) = viewModelScope.launch{
        tasksEventChannel.send(TasksEvent.ShowUndoDeleteTaskMessageW(wait))
    }

    fun onUndoFinish(wait: Wait) =
        viewModelScope.launch {
            Timber.d("Action: Undo all change")
            undo = true
            changeStatus(wait,storeChangeForDelete.status)
        }




}

