package com.citrus.mCitrusTablet.view.reservation


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
import com.citrus.mCitrusTablet.util.Constants.defaultTimeStr
import com.citrus.mCitrusTablet.util.Constants.inputFormat
import com.citrus.mCitrusTablet.util.SingleLiveEvent
import com.citrus.mCitrusTablet.view.wait.HideCheck
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import java.text.SimpleDateFormat

enum class SortOrder { BY_LESS, BY_TIME, BY_MORE }
enum class HideCheck { HIDE_TRUE, HIDE_FALSE}

class ReservationViewModel @ViewModelInject constructor(private val model: Repository) :
    ViewModel() {

    private var serverDomain =
        "https://" + prefs.severDomain
    var hideCheck: HideCheck = HideCheck.HIDE_TRUE
    private var delayTime = Constants.DEFAULT_TIME
    var storageList:MutableList<ReservationGuests> = mutableListOf()
    private val job = SupervisorJob()

    private var guests = mutableListOf<ReservationGuests>()

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

    private val _dateRange = MutableLiveData<Array<String>>()
    val dateRange: LiveData<Array<String>>
        get() = _dateRange

    private val _orderDate = MutableLiveData<Array<String>>()
    val orderDate: LiveData<Array<String>>
        get() = _orderDate

    private val _cusCount = MutableLiveData<String>()
    val cusCount: LiveData<String>
        get() = _cusCount

    private val _isFirst = SingleLiveEvent<Boolean>()
    val isFirst: SingleLiveEvent<Boolean>
        get() = _isFirst

    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()




    private var scope = viewModelScope.launch(Dispatchers.IO + job) {
        while (true) {
            if(_dateRange.value == null){
                fetchAllData(defaultTimeStr, defaultTimeStr)
            }else{
                _dateRange.value?.get(0)?.let { fetchAllData(it, _dateRange.value?.get(1)!!) }
            }
            delay(delayTime * 60 * 1000)
        }
    }

    private fun stopJob() {
        scope.cancel()
    }

    init{  viewModelScope.launch { getStoreInformation() } }

    fun setSearchVal(
        rsno: String,
        reservationTime: String,
        customNum: Int
    ) {
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm")
        var date = sdf.parse(reservationTime)
        val sdf2 = SimpleDateFormat("yyyy-MM-dd HH:mm")
        var searchStr = sdf2.format(date)
        val changeForUpdate = SimpleDateFormat("MM-dd-yyyy HH:mm")
        val updateChange = changeForUpdate.format(date)

        var bookingPostData = PostToGetSeats(rsno, updateChange, customNum)
        viewModelScope.launch {
            fetchReservationFloor(bookingPostData,searchStr)
        }
    }

    fun setDateArray(data: Array<String>) {
        _dateRange.value = data

        viewModelScope.launch {
            fetchAllData(data[0], data[1])
        }
    }

    fun reload(){
        viewModelScope.launch {
            if(_dateRange.value == null){
                fetchAllData(defaultTimeStr, defaultTimeStr)
            }else{
                _dateRange.value?.get(0)?.let { fetchAllData(it, _dateRange.value?.get(1)!!) }
            }
        }
    }


    fun setDateArrayReservation(data: Array<String>) {
        _orderDate.value = data
    }


    private suspend fun fetchReservationFloor(postToGetSeats: PostToGetSeats, searchStr: String) =
        model.fetchReservationFloor(serverDomain+Constants.GET_FLOOR,postToGetSeats, onEmpty = {
            _seatData.postValue(mutableListOf())
        }).collect { datumList ->

            var hasDate = false
            var seatData:List<Floor> = mutableListOf()

            for(datum in datumList){
                if(datum.resDate == searchStr){
                    seatData = datum.floor
                    hasDate = true
                }
            }

            if(hasDate){
                _seatData.postValue(seatData)
            }else{
                _datumData.postValue(datumList)
            }


        }


     private suspend fun fetchAllData(startTime: String, endTime: String) {
        var dataOutput = PostToGetAllData(prefs.rsno, startTime, endTime)
         if (dataOutput != null) {
             model.fetchAllData(serverDomain+Constants.GET_ALL_DATA,"reservation",dataOutput, onCusCount = { cusCount ->
                 _cusCount.postValue(cusCount)
             }).collect { list ->
                 if (list.isNotEmpty()) {
                     storageList = list as MutableList<ReservationGuests>
                     guests = getSortRequirement(SortOrder.BY_TIME, list)
                     allDataReorganization(guests)
                 } else {
                     _titleData.postValue(listOf())
                     _allData.postValue(mutableListOf())
                 }
             }
         }
    }

    private suspend fun fetchStoreInfo() = model.fetchStoreInfo(serverDomain+Constants.GET_STORE_INFO,
        prefs.storeId).collect {
        prefs.storeName = it[0].storeName
        prefs.rsno = it[0].rsno
        _isFirst.postValue(true)
    }

    private suspend fun getStoreInformation() {
        if(prefs.storeName == ""){
            fetchStoreInfo()
        }
    }

    private fun allDataReorganization(list: MutableList<ReservationGuests>){
        var sortGuests = if(list.isNotEmpty()) getSortRequirement(SortOrder.BY_TIME, list) else list
        var isFirst = true
        var tempValue = ""
        val timeTitle = mutableSetOf<String>()
        var tempItem = mutableListOf<ReservationGuests>()
        val groupItem = mutableListOf<List<ReservationGuests>>()

        if(sortGuests.isNotEmpty()) {
            for (item in  if(hideCheck != HideCheck.HIDE_TRUE) sortGuests.filter { it.status == "A" } else sortGuests ) {
                val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss").parse(item.reservationTime)
                val formattedDate = SimpleDateFormat("MM/dd HH:mm").format(date)
                var dateStr = formattedDate.split(" ")
                var timeStr = dateStr[1].split(":")
                timeTitle.add(timeStr[0] + ":00")

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
        }
        _titleData.postValue(timeTitle.toList())
        _allData.postValue(groupItem)
    }


    fun searchForStr(status:SearchViewStatus){
        when(status){
            is SearchViewStatus.IsEmpty -> allDataReorganization( storageList)

            is SearchViewStatus.NeedChange -> {
                var tempGuestsList = mutableListOf<ReservationGuests>()
                for (item:ReservationGuests in storageList) {
                    if (item.phone.contains(status.searchStr)) {
                        tempGuestsList.add(item)
                    }
                }
                allDataReorganization(tempGuestsList)
            }
        }
    }

    fun changeStatus(guest: ReservationGuests){
        viewModelScope.launch {
            changeStatusData(guest)
        }
    }


    private suspend fun changeStatusData(guest: ReservationGuests) = model.changeStatus(serverDomain+Constants.CHANGE_STATUS,PostToChangeStatus(Reservation(guest.tkey,"C"))).collect { status ->

       when(status){
           1 -> {
                var index = storageList.indexOf(guest)
               storageList[index].status = "C"
               allDataReorganization(storageList)
           }
           0 -> {

           }
       }
    }

    fun uploadReservation(dataPostToSet: PostToSetReservation) {
        viewModelScope.launch {
            uploadReservationData(dataPostToSet)
        }
    }


    private suspend fun uploadReservationData(dataPostToSet: PostToSetReservation) =
        model.uploadReservationData(serverDomain+Constants.SET_RESERVATION,dataPostToSet).collect {
            if (it.status == 1 && it.data == 1) {
                tasksEventChannel.send(TasksEvent.ShowSuccessMessage)
                var time = dataPostToSet.reservation.reservationTime.split(" ")
                fetchAllData(time[0], time[0])
                sendSMS("celaviLAB",dataPostToSet.reservation.phone, prefs.storeName +" "+dataPostToSet.reservation.reservationTime+" 已完成預約")
            }else{
                tasksEventChannel.send(TasksEvent.ShowFailMessage)
            }
        }



    private suspend fun sendSMS(project:String,phone:String,body:String) = model.sendSMS(Constants.SEND_SMS,project,phone,body).collect {

    }


    fun fetchReservationTime(postData: String){
        viewModelScope.launch {
            fetchReservationTimeData(postData)
        }
    }


    private suspend fun fetchReservationTimeData(postData: String) =
        model.fetchReservationTime(serverDomain +Constants.GET_RESERVATION_TIME,postData,onEmpty = {
            _orderDateDatum.postValue(mutableListOf())
        }).collect {
                _orderDateDatum.postValue(it)
        }




    private fun getSortRequirement(
        sortStatus: SortOrder,
        originalList: List<ReservationGuests>
    ): MutableList<ReservationGuests> {
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
                            first.floorName+first.roomName < second.floorName+second.roomName -> {
                                -1
                            }
                            first.floorName+first.roomName > second.floorName+second.roomName -> {
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

    fun hideChecked(isHide:Boolean) {
        hideCheck = if(isHide){
            HideCheck.HIDE_TRUE
        }else{
            HideCheck.HIDE_FALSE
        }
        allDataReorganization(guests)
    }

    override fun onCleared() {
        super.onCleared()
        stopJob()
    }
}


sealed class SearchViewStatus {
    object IsEmpty : SearchViewStatus()
    data class NeedChange(val searchStr: String) : SearchViewStatus()
}

sealed class TasksEvent {
    object ShowSuccessMessage : TasksEvent()
    object ShowFailMessage : TasksEvent()
}
