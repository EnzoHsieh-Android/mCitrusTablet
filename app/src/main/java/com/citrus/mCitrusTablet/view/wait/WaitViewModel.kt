package com.citrus.mCitrusTablet.view.wait

import android.content.SharedPreferences
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citrus.mCitrusTablet.model.Repository
import com.citrus.mCitrusTablet.model.vo.FetchAllData
import com.citrus.mCitrusTablet.model.vo.ReservationGuests
import com.citrus.mCitrusTablet.model.vo.Wait
import com.citrus.mCitrusTablet.util.Constants
import com.citrus.mCitrusTablet.view.reservation.SortOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class WaitViewModel @ViewModelInject constructor(private val model: Repository, private val sharedPreferences: SharedPreferences):
    ViewModel(){
    private var delayTime = sharedPreferences.getLong(
        Constants.KEY_DELAY_INTERVAL,
        Constants.DEFAULT_TIME
    )
    private val job = SupervisorJob()

    private val _cusCount = MutableLiveData<String>()
    val cusCount: LiveData<String>
        get() = _cusCount

    private val _allData = MutableLiveData<List<Wait>>()
    val allData: LiveData<List<Wait>>
        get() = _allData

    private var scope = viewModelScope.launch(Dispatchers.IO + job) {
        while (true) {
            fetchAllData(Constants.defaultTimeStr, Constants.defaultTimeStr)

            delay(delayTime * 60 * 1000)
        }
    }

    fun stopJob() {
        scope.cancel()
    }

    private suspend fun fetchAllData(startTime: String, endTime: String) {
        var dataOutput = FetchAllData("S00096", startTime, endTime)
        model.fetchAllData("wait",dataOutput, onCusCount = { cusCount ->
            _cusCount.postValue(cusCount)
        }).collect { list ->
            if (list.isNotEmpty()) {
                _allData.postValue(list as MutableList<Wait>)
            } else {
                _allData.postValue(mutableListOf())
            }
        }
    }


}