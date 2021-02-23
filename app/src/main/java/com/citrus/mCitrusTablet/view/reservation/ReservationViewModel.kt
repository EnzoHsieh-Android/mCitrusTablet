package com.citrus.mCitrusTablet.view.reservation

import android.content.SharedPreferences
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citrus.mCitrusTablet.model.Repository
import com.citrus.mCitrusTablet.model.vo.*
import com.citrus.mCitrusTablet.util.Constants.DEFAULT_TIME
import com.citrus.mCitrusTablet.util.Constants.KEY_DELAY_INTERVAL
import com.citrus.mCitrusTablet.util.Constants.defaultTimeStr
import com.citrus.mCitrusTablet.util.Constants.inputFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

enum class SortOrder { BY_LESS, BY_TIME, BY_MORE }

class ReservationViewModel @ViewModelInject constructor(private val model: Repository, private val sharedPreferences: SharedPreferences) :
    ViewModel() {
    private var delayTime = sharedPreferences.getLong(KEY_DELAY_INTERVAL, DEFAULT_TIME)
    var storageList:MutableList<ReservationGuests> = mutableListOf()
    private val job = SupervisorJob()

    private var guests = mutableListOf<ReservationGuests>()

    private val _seatData = MutableLiveData<List<Floor>>()
    val seatData: LiveData<List<Floor>>
        get() = _seatData

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

    fun stopJob() {
        scope.cancel()
    }


    fun setSearchVal(
        rsno: String,
        reservationTime: String,
        customNum: Int
    ) {
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm")
        var date = sdf.parse(reservationTime)
        val changeForUpdate = SimpleDateFormat("MM-dd-yyyy HH:mm")
        val updateChange = changeForUpdate.format(date)

        var bookingPostData = BookingPostData("S00096", updateChange, customNum)
        viewModelScope.launch {
            fetchReservationFloor(bookingPostData)
        }
    }

    fun setDateArray(data: Array<String>) {
        _dateRange.value = data

        viewModelScope.launch {
            fetchAllData(data[0], data[1])
        }
    }

    fun setDateArrayReservation(data: Array<String>) {
        _orderDate.value = data
    }


    private suspend fun fetchReservationFloor(bookingPostData: BookingPostData) =
        model.fetchReservationFloor(bookingPostData, onEmpty = {
            _seatData.postValue(mutableListOf())
        }).collect { floorList ->
            _seatData.postValue(floorList)
        }


     private suspend fun fetchAllData(startTime: String, endTime: String) {
        var dataOutput = FetchAllData("S00096", startTime, endTime)
         model.fetchAllData("reservation",dataOutput, onCusCount = { cusCount ->
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



    private fun allDataReorganization(list: MutableList<ReservationGuests>){
        var sortGuests = if(list.isNotEmpty()) getSortRequirement(SortOrder.BY_TIME, list) else list
        var isFirst = true
        var tempValue = ""
        val timeTitle = mutableSetOf<String>()
        var tempItem = mutableListOf<ReservationGuests>()
        val groupItem = mutableListOf<List<ReservationGuests>>()

        if(sortGuests.isNotEmpty()) {
            for (item in sortGuests) {
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


    private suspend fun changeStatusData(guest: ReservationGuests) = model.changeStatus(ChangeStatus(Reservation(guest.tkey,"C"))).collect { status ->

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

    fun uploadReservation(uploadData: ReservationUpload) {
        viewModelScope.launch {
            uploadReservationData(uploadData)
        }
    }


    private suspend fun uploadReservationData(uploadData: ReservationUpload) =
        model.uploadReservationData(uploadData).collect {
            if (it.status == 1 && it.data == 1) {
                var time = uploadData.reservation.reservationTime.split(" ")
                fetchAllData(time[0], time[0])
                tasksEventChannel.send(TasksEvent.ShowSuccessMessage)
            } else {
                tasksEventChannel.send(TasksEvent.ShowFailMessage)
            }
        }


    private fun getSortRequirement(
        sortStatus: SortOrder,
        originalList: List<ReservationGuests>
    ): MutableList<ReservationGuests> {
        var nowData: MutableList<ReservationGuests>
        nowData = when (sortStatus) {

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
                    inputFormat.parse(first.reservationTime) == inputFormat.parse(second.reservationTime) -> 0
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
        return nowData
    }


    sealed class TasksEvent {
        object ShowSuccessMessage : TasksEvent()
        object ShowFailMessage : TasksEvent()
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

