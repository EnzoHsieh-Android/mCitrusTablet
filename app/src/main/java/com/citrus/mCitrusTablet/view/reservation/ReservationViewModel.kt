package com.citrus.mCitrusTablet.view.reservation



import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citrus.mCitrusTablet.di.prefs
import com.citrus.mCitrusTablet.model.Repository
import com.citrus.mCitrusTablet.model.vo.*
import com.citrus.mCitrusTablet.util.Constants
import com.citrus.mCitrusTablet.util.Constants.TimeStrForDelete
import com.citrus.mCitrusTablet.util.Constants.defaultTimeStr
import com.citrus.mCitrusTablet.util.Constants.inputFormat
import com.citrus.mCitrusTablet.util.HideCheck
import com.citrus.mCitrusTablet.util.SingleLiveEvent
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import timber.log.Timber
import java.text.SimpleDateFormat


enum class SortOrder { BY_LESS, BY_TIME, BY_MORE }
enum class CancelFilter {SHOW_CANCELLED, HIDE_CANCELLED}
class ReservationViewModel @ViewModelInject constructor(private val model: Repository) :
    ViewModel() {

    private var serverDomain =
        "https://" + prefs.severDomain

    private lateinit var newWaitGuest:Wait
    private var newWaitGuestCount = 0
    private var isFirstFetch = true
    private var isReload = true
    private var hideCheck: HideCheck = HideCheck.HIDE_TRUE
    private var hideCancelled:CancelFilter = CancelFilter.SHOW_CANCELLED
    private var delayTime = Constants.DEFAULT_TIME
    private var storageList = mutableListOf<ReservationGuests>()
    private var expendList = mutableListOf<String>()
    private lateinit var fetchJob: Job

    private lateinit var selectGuest:ReservationGuests
            private fun isSelectGuestInit()=::selectGuest.isInitialized

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

    private val _isFirst = SingleLiveEvent<Boolean>()
    val isFirst: SingleLiveEvent<Boolean>
        get() = _isFirst


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
                emit(fetchAllData(defaultTimeStr, defaultTimeStr))
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
        fetchReservationFloor(bookingPostData, searchStr)
    }

    fun setDateArray(data: Array<String>) {
        _dateRange.value = data
        fetchAllData(data[0], data[1])
        prefs.storageWaitNum = 0
        isFirstFetch = true
    }

    fun reload() {
        isReload = true
        if (_dateRange.value == null) {
            fetchAllData(defaultTimeStr, defaultTimeStr)
        } else {
            _dateRange.value?.get(0)?.let { fetchAllData(it, _dateRange.value?.get(1)!!) }
        }
    }


    fun setDateArrayReservation(data: Array<String>) {
        _orderDate.value = data
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


    private fun fetchAllData(startTime: String, endTime: String) =
            viewModelScope.launch {
                model.fetchAllData(
                    serverDomain + Constants.GET_ALL_DATA,
                    "reservation",
                    PostToGetAllData(prefs.rsno, startTime, endTime),
                    onCusCount = { cusCount ->
                        _cusCount.postValue(cusCount)
                    },onWaitCount = {  num,guest ->
                        newWaitGuestCount = num
                        newWaitGuest = guest
                    },onReservationCount = { _,_ ->

                    }).collect { list ->
                    if (list.isNotEmpty()) {
                        prefs.storageWaitNum = if(!isFirstFetch && (prefs.storageWaitNum != newWaitGuestCount)){
                            _waitHasNewData.postValue(newWaitGuest)
                            prefs.storageWaitNum
                        }else{
                            newWaitGuestCount
                        }

                       var newList = list as MutableList<ReservationGuests>
                        if(isSelectGuestInit()){
                            for(item in newList){
                                item.isSelect = item.tkey == selectGuest.tkey
                                for(key in expendList){
                                    if(key == item.tkey){
                                        item.isExpend = true
                                    }
                                }
                            }
                        }

                        if(newList != storageList){
                            storageList = newList
                            isReload = true
                            allDataReorganization(getSortRequirement(SortOrder.BY_TIME, storageList))
                        }else{
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


    private fun fetchStoreInfo() =
        viewModelScope.launch {
            model.fetchStoreInfo(
                serverDomain + Constants.GET_STORE_INFO,
                prefs.storeId
            ).collect {
                prefs.storeName = it[0].storeName
                prefs.rsno = it[0].rsno
                prefs.messageRes = it[0].message1
                prefs.messageWait = it[0].message2
                prefs.messageNotice = it[0].message3
                _isFirst.postValue(true)
            }
        }



    fun itemSelect(guest:ReservationGuests){
        selectGuest = guest
        for(item in storageList){
            item.isSelect = item == guest
        }

        if(guest.isExpend){
            expendList.add(guest.tkey)
        }else{
            expendList.remove(guest.tkey)
        }

        allDataReorganization(storageList)
    }

    fun deleteNone(){
        allDataReorganization(storageList)
    }


    /*拆分原始DATA組成SectionRecyclerView需求的樣式，分成Title List以及Data List*/
    private fun allDataReorganization(list: MutableList<ReservationGuests>) {
        var sortGuests =
            if (list.isNotEmpty()) getSortRequirement(SortOrder.BY_TIME, list) else list
        var isFirst = true
        var tempValue = ""
        val timeTitle = mutableSetOf<String>()
        var tempItem = mutableListOf<ReservationGuests>()
        val groupItem = mutableListOf<List<ReservationGuests>>()
        var totalList: MutableList<Any> = mutableListOf()

        sortGuests = if (hideCancelled == CancelFilter.HIDE_CANCELLED) sortGuests.filter { it.status != Constants.CANCEL } as MutableList<ReservationGuests> else sortGuests
        sortGuests = if (hideCheck != HideCheck.HIDE_TRUE) sortGuests.filter { it.status != Constants.CHECK } as MutableList<ReservationGuests> else sortGuests

        if (sortGuests.isNotEmpty()) {
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


            /*用於SectionRecyclerView Swipe取得物件使用*/
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

        if(isReload){
            _allData.postValue(groupItem)
            isReload = false
        }else{
            _holdData.postValue(groupItem)
        }

    }


    fun searchForStr(status: SearchViewStatus) {
        isReload = true
        when (status) {
            is SearchViewStatus.IsEmpty -> {
                allDataReorganization(storageList)
            }

            is SearchViewStatus.NeedChange -> {
                var tempGuestsList = mutableListOf<ReservationGuests>()
                for (item: ReservationGuests in storageList) {
                    if (item.phone?.contains(status.searchStr) == true || item.email?.split("@")?.get(0)
                            ?.contains(status.searchStr) == true) {
                        tempGuestsList.add(item)
                    }
                }
                allDataReorganization(tempGuestsList)
            }
        }
    }


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
                            storageList[index].updateDate = TimeStrForDelete
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


    fun uploadReservation(dataPostToSet: PostToSetReservation) =
        viewModelScope.launch {
            model.uploadReservationData(serverDomain + Constants.SET_RESERVATION, dataPostToSet)
                .collect {
                    if (it.status == 1 && it.data == 1) {
                        tasksEventChannel.send(TasksEvent.ShowSuccessMessage)
                        var time = dataPostToSet.reservation.reservationTime.split(" ")
                        fetchAllData(time[0], time[0])
                        sendSMS(
                            "celaviLAB",
                            dataPostToSet.reservation.phone,
                            prefs.storeName + " " + dataPostToSet.reservation.reservationTime + " "+ prefs.messageRes
                        )
                    } else {
                        tasksEventChannel.send(TasksEvent.ShowFailMessage)
                    }
                }
        }

    private suspend fun sendSMS(project: String, phone: String, body: String) =
        model.sendSMS(Constants.SEND_SMS, project, phone, body).collect {

        }


    fun fetchReservationTime(postData: String) =
        viewModelScope.launch {
            model.fetchReservationTime(
                serverDomain + Constants.GET_RESERVATION_TIME,
                postData,
                onEmpty = {
                    _orderDateDatum.postValue(mutableListOf())
                }).collect {
                _orderDateDatum.postValue(it)
            }
        }

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

    fun hideCancelled(cancelFilter: CancelFilter) {
        hideCancelled = cancelFilter
        isReload = !isReload
        allDataReorganization(storageList)
    }

    private fun showUndoToast(guest:ReservationGuests) = viewModelScope.launch{
        tasksEventChannel.send(TasksEvent.ShowUndoDeleteTaskMessageR(guest))
    }

    fun onUndoFinish(guest: ReservationGuests) =
        viewModelScope.launch {
            Timber.d("Action: Undo all change")
            changeStatus(guest,Constants.ADD)
        }



    fun onDetachView(){
        onCleared()
    }

    override fun onCleared() {
        stopFetchJob()
        super.onCleared()
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
}
