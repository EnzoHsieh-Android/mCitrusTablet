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
import timber.log.Timber


enum class SortOrder { BY_LESS, BY_TIME_MORE, BY_MORE, BY_TIME_LESS }
enum class Filter {SHOW_ALL, SHOW_CANCELLED, SHOW_CONFIRM, SHOW_NOTIFIED, SHOW_WAIT}

class WaitViewModel @ViewModelInject constructor(private val model: Repository) :
    ViewModel() {

    private var serverDomain =
        "https://" + prefs.severDomain
    var storageList: MutableList<Wait> = mutableListOf()
    var sortOrder: SortOrder = SortOrder.BY_TIME_LESS
    var hideCheck: HideCheck = HideCheck.HIDE_TRUE
    var nowFilter = Filter.SHOW_ALL
    var smsQueue = arrayListOf<String>()
    private var delayTime = Constants.DEFAULT_TIME
    private val job = SupervisorJob()
    lateinit var fetchJob: Job

    private val _cusCount = MutableLiveData<String>()
    val cusCount: LiveData<String>
        get() = _cusCount

    private val _allData = MutableLiveData<List<Wait>>()
    val allData: LiveData<List<Wait>>
        get() = _allData


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
                }).collect { list ->
                if (list.isNotEmpty()) {
                    list as MutableList<Wait>
                    storageList = list.toMutableList()
                    sendWaitSms()
                    refreshAllData(storageList)
                } else {
                    storageList = mutableListOf()
                    _allData.postValue(mutableListOf())
                }
            }
        }


    private fun sendWaitSms() {
        Log.e("smsQueue",smsQueue.toString())
       if(smsQueue.size>0){
          for (guest in storageList.filter { it.status == Constants.ADD }){
              for(key in smsQueue){
                  if(guest.tkey == key){
                      sendSMS(guest,prefs.storeName+" "+ prefs.messageWait +"\n"+ guest.url)
                  }
              }
          }
       }
        smsQueue.clear()
    }


    private fun sendSMS(guest:Wait, msg:String){
        Log.e("phone",guest.phone)
        viewModelScope.launch {
            model.sendSMS(
                Constants.SEND_SMS,
                "celaviLAB",
                guest.phone,
                msg
            ).collect {
            Log.e("smsStatus",it.toString())
            }
        }
    }


    private fun refreshAllData( list: MutableList<Wait>) {

        var wlist = list

        when(nowFilter){
            Filter.SHOW_ALL -> {}
            Filter.SHOW_CANCELLED -> { wlist = wlist.filter { it.status == Constants.CANCEL } as MutableList<Wait> }
            Filter.SHOW_NOTIFIED -> { wlist = wlist.filter { it.status == Constants.NOTICE } as MutableList<Wait> }
            Filter.SHOW_CONFIRM -> { wlist = wlist.filter { it.status == Constants.CONFIRM } as MutableList<Wait> }
            Filter.SHOW_WAIT -> { wlist = wlist.filter { it.status == Constants.ADD } as MutableList<Wait> }
        }

        if (hideCheck != HideCheck.HIDE_TRUE) {
            _allData.postValue(
                getSortRequirement(
                    sortOrder,
                    wlist.filter { it.status != Constants.CHECK })
            )
        } else {
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
                        if(statusStr == Constants.NOTICE){
                            sendSMS(wait, prefs.storeName+ " " + prefs.messageNotice + "\n" + wait.url)
                        }
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
                    if (item.phone.contains(status.searchStr)) {
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


    override fun onCleared() {
        stopFetchJob()
        super.onCleared()
    }

    fun sortList(sort: SortOrder) {
        sortOrder = sort
        refreshAllData(storageList)
    }

    fun hideChecked(isHide: Boolean) {
        hideCheck = if (isHide) {
            HideCheck.HIDE_TRUE
        } else {
            HideCheck.HIDE_FALSE
        }
        refreshAllData(storageList)
    }


    fun sendNotice(guest: Wait) {
        changeStatus(guest,Constants.NOTICE)
    }

    fun changeFilter(filterType: Filter) {
        nowFilter = filterType
        refreshAllData(storageList)
    }


}

