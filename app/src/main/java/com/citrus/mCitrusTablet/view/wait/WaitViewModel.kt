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
import com.citrus.mCitrusTablet.view.reservation.SearchViewStatus
import com.citrus.mCitrusTablet.view.reservation.TasksEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class SortOrder { BY_LESS, BY_TIME_MORE, BY_MORE,BY_TIME_LESS }
enum class HideCheck { HIDE_TRUE, HIDE_FALSE}

class WaitViewModel @ViewModelInject constructor(private val model: Repository):
    ViewModel(){

    private var serverDomain =
        "https://" + prefs.severDomain
    var storageList:MutableList<Wait> = mutableListOf()
    var sortOrder:SortOrder = SortOrder.BY_TIME_LESS
    var hideCheck:HideCheck = HideCheck.HIDE_FALSE
    private var delayTime = Constants.DEFAULT_TIME
    private val job = SupervisorJob()

    private val _cusCount = MutableLiveData<String>()
    val cusCount: LiveData<String>
        get() = _cusCount

    private val _allData = MutableLiveData<List<Wait>>()
    val allData: LiveData<List<Wait>>
        get() = _allData

    private val tasksEventChannel = Channel<TasksEvent>()
    val tasksEvent = tasksEventChannel.receiveAsFlow()

    private var scope = viewModelScope.launch(Dispatchers.IO + job) {
        while (true) {
            fetchAllData()
            delay(delayTime * 60 * 1000)
        }
    }

    private fun stopJob() {
        scope.cancel()
    }

    private suspend fun fetchAllData() {
        var dataOutput = PostToGetAllData(prefs.rsno, Constants.defaultTimeStr, Constants.defaultTimeStr)
        model.fetchAllData(serverDomain + Constants.GET_ALL_DATA,"wait",dataOutput, onCusCount = { cusCount ->
            _cusCount.postValue(cusCount)
        }).collect { list ->
            if (list.isNotEmpty()) {
                list as MutableList<Wait>
                storageList = list.toMutableList()
                _allData.postValue(getSortRequirement(sortOrder,list))
            } else {
                _allData.postValue(mutableListOf())
            }
        }
    }

    fun changeStatus(wait: Wait){
        viewModelScope.launch {
            changeStatusData(wait)
        }
    }


    private suspend fun changeStatusData(wait: Wait) = model.changeStatus(serverDomain+Constants.CHANGE_STATUS,
        PostToChangeStatus(Reservation(wait.tkey,"C"))
    ).collect { status ->

        when(status){
            1 -> {
               fetchAllData()
            }
            0 -> {

            }
        }
    }


        fun uploadWait(dataPostToSet: PostToSetWaiting) {
        viewModelScope.launch {
            uploadWaitData(dataPostToSet)
        }
    }

    private suspend fun uploadWaitData(dataPostToSet: PostToSetWaiting) =
    model.setWaitData(serverDomain + Constants.SET_WAIT , dataPostToSet).collect {
        if(it.status != 0){
            fetchAllData()
            tasksEventChannel.send(TasksEvent.ShowSuccessMessage)
        }else{
            tasksEventChannel.send(TasksEvent.ShowFailMessage)
        }
    }


    fun searchForStr(status: SearchViewStatus){
        when(status){
            is SearchViewStatus.IsEmpty ->  _allData.postValue(getSortRequirement(sortOrder,storageList))

            is SearchViewStatus.NeedChange -> {
                var tempGuestsList = mutableListOf<Wait>()
                for (item:Wait in storageList) {
                    if (item.phone.contains(status.searchStr)) {
                        tempGuestsList.add(item)
                    }
                }
                _allData.postValue(getSortRequirement(sortOrder,tempGuestsList))
            }
        }
    }


    private fun getSortRequirement(
        sortStatus: SortOrder,
        originalList: List<Wait>
    ): MutableList<Wait> {

        return when (sortStatus) {
            SortOrder.BY_LESS -> originalList.sortedWith { first, second ->
                when {
                    first.custNum == second.custNum -> {
                        when {
                            Constants.inputFormat.parse(first.reservationTime) < Constants.inputFormat.parse(second.reservationTime) -> {
                                -1
                            }
                            Constants.inputFormat.parse(first.reservationTime) > Constants.inputFormat.parse(second.reservationTime) -> {
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
                    Constants.inputFormat.parse(first.reservationTime) == Constants.inputFormat.parse(second.reservationTime) -> 0

                    Constants.inputFormat.parse(first.reservationTime) < Constants.inputFormat.parse(second.reservationTime) -> -1
                    else -> 1
                }
            } as MutableList<Wait>

            SortOrder.BY_TIME_MORE -> originalList.sortedWith { first, second ->
                when {
                    Constants.inputFormat.parse(first.reservationTime) == Constants.inputFormat.parse(second.reservationTime) -> 0

                    Constants.inputFormat.parse(first.reservationTime) < Constants.inputFormat.parse(second.reservationTime) -> 1
                    else -> -1
                }
            } as MutableList<Wait>

            SortOrder.BY_MORE -> originalList.sortedWith { first, second ->
                when {
                    first.custNum == second.custNum -> {
                        when {
                            Constants.inputFormat.parse(first.reservationTime) < Constants.inputFormat.parse(second.reservationTime) -> {
                                -1
                            }
                            Constants.inputFormat.parse(first.reservationTime) > Constants.inputFormat.parse(second.reservationTime) -> {
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
        super.onCleared()
        stopJob()
    }

    fun sortList(sort: SortOrder) {
        sortOrder = sort
        _allData.postValue(getSortRequirement(sortOrder,storageList))
    }

    fun hideChecked(isHide:Boolean) {
        hideCheck = if(isHide){
            HideCheck.HIDE_TRUE
        }else{
            HideCheck.HIDE_FALSE
        }


        if(hideCheck != HideCheck.HIDE_TRUE){
            _allData.postValue(getSortRequirement(sortOrder,storageList.filter { it.status == "A" }))
        }else{
            _allData.postValue(getSortRequirement(sortOrder,storageList))
        }

    }


}

