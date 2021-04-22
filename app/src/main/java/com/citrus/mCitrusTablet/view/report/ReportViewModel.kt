package com.citrus.mCitrusTablet.view.report


import android.util.Log
import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citrus.mCitrusTablet.di.prefs
import com.citrus.mCitrusTablet.model.Repository
import com.citrus.mCitrusTablet.model.vo.PostToGetAllData
import com.citrus.mCitrusTablet.model.vo.Report
import com.citrus.mCitrusTablet.model.vo.ReservationGuests
import com.citrus.mCitrusTablet.model.vo.Wait
import com.citrus.mCitrusTablet.util.Constants
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*


enum class ReportRange { BY_DAILY, BY_WEEKLY, BY_MONTHLY }
class ReportViewModel @ViewModelInject constructor(private val model: Repository) :
    ViewModel() {
    private var serverDomain = "https://" + prefs.severDomain
    private var storageTime = Constants.getCurrentDate()


    private val _chartTypeChange = MutableLiveData<Int>()
    val chartTypeChange:LiveData<Int>
        get() = _chartTypeChange

    private val _locationPageType = MutableLiveData<ReportRange>()
    val locationPageType: LiveData<ReportRange>
        get() = _locationPageType

    private val _dailyDetailReportData = MutableLiveData<MutableList<Any>>()
    val dailyDetailReportData: LiveData<MutableList<Any>>
        get() = _dailyDetailReportData

    private val _dailyReportData = MutableLiveData<MutableList<Report>>()
    val dailyReportData: LiveData<MutableList<Report>>
        get() = _dailyReportData

    private val _weeklyDetailReportData = MutableLiveData<MutableList<Any>>()
    val weeklyDetailReportData: LiveData<MutableList<Any>>
        get() = _weeklyDetailReportData


    private val _weeklyReportData = MutableLiveData<MutableList<Report>>()
    val weeklyReportData: LiveData<MutableList<Report>>
        get() = _weeklyReportData

    private val _weeklyReportTitleData = MutableLiveData<MutableList<String>>()
    val weeklyReportTitleData: LiveData<MutableList<String>>
        get() = _weeklyReportTitleData

    private val _monthlyDetailReportData = MutableLiveData<MutableList<Any>>()
    val monthlyDetailReportData: LiveData<MutableList<Any>>
        get() = _monthlyDetailReportData

    private val _monthlyReportData = MutableLiveData<MutableList<Report>>()
    val monthlyReportData: LiveData<MutableList<Report>>
        get() = _monthlyReportData

    private val _monthlyReportTitleData = MutableLiveData<MutableList<String>>()
    val monthlyReportTitleData: LiveData<MutableList<String>>
        get() = _monthlyReportTitleData



    private fun fetchReportData(startTime: String, endTime: String) =
        viewModelScope.launch {
            model.fetchAllDataForReport(
                serverDomain + Constants.GET_ALL_DATA,
                PostToGetAllData(prefs.rsno, startTime, endTime)
            ).collect { allObject ->
                var originalList: Any?
                var tempDate = ""
                var index = 0
                var checkCount = 0
                originalList = if (isTypeRes()) {
                    allObject.reservation.sortedBy { it.reservationTime }
                } else {
                    allObject.wait.sortedBy { it.reservationTime }
                }

                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                val outputFormat = SimpleDateFormat("MM/dd")
                var reportList = mutableListOf<Report>()
                var titleEntity = mutableListOf<String>()


                if(originalList.isNotEmpty()) {
                    /**以日期區分好date List 和 data List*/
                    for (guest in if (isTypeRes()) originalList as MutableList<ReservationGuests> else originalList as MutableList<Wait>) {

                        var date: Date? = if (isTypeRes()) {
                            inputFormat.parse((guest as ReservationGuests).reservationTime)
                        } else {
                            inputFormat.parse((guest as Wait).reservationTime)
                        }

                        if(!isTypeRes()){
                            if((guest as Wait).status == Constants.CHECK) {
                                checkCount++
                                var date2: Date = inputFormat.parse(guest.updateDate)
                                var int = countTimeDifference(date!!, date2)
                                guest.waitTime = int
                            }
                        }



                        val formattedDate = outputFormat.format(date)
                        if (isTypeRes()) {
                            (guest as ReservationGuests).reservationTime = formattedDate
                        } else {
                            (guest as Wait).reservationTime = formattedDate
                        }

                        if (tempDate != formattedDate) {
                            tempDate = formattedDate
                            titleEntity.add(tempDate)
                            index = titleEntity.indexOf(tempDate)
                            reportList.add(index, Report(0, 0, 0, 0, 0, 0, "", 0))
                        }


                        if (isTypeRes()) {
                            reportList[index].adult =
                                if ((guest as ReservationGuests).status == Constants.CHECK) reportList[index].adult + guest.adultCount else reportList[index].adult
                            reportList[index].child =
                                if (guest.status == Constants.CHECK) reportList[index].child + guest.kidCount else reportList[index].child
                            reportList[index].total = reportList[index].total + 1
                            reportList[index].cancel =
                                reportList[index].cancel + if (guest.status == Constants.CANCEL) 1 else 0
                            reportList[index].wait =
                                reportList[index].wait + if (guest.status == Constants.ADD) 1 else 0
                            reportList[index].check =
                                reportList[index].check + if (guest.status == Constants.CHECK) 1 else 0
                            reportList[index].date = tempDate
                        } else {
                            reportList[index].adult =
                                if ((guest as Wait).status == Constants.CHECK) reportList[index].adult + guest.adultCount else reportList[index].adult
                            reportList[index].child =
                                if (guest.status == Constants.CHECK) reportList[index].child + guest.kidCount else reportList[index].child
                            reportList[index].total = reportList[index].total + 1
                            reportList[index].cancel =
                                reportList[index].cancel + if (guest.status == Constants.CANCEL) 1 else 0
                            reportList[index].cancel + if (guest.status == Constants.CANCEL) 1 else 0
                            reportList[index].wait =
                                reportList[index].wait + if (guest.status == Constants.ADD || guest.status == Constants.NOTICE || guest.status == Constants.CONFIRM) 1 else 0
                            reportList[index].check =
                                reportList[index].check + if (guest.status == Constants.CHECK) 1 else 0
                            reportList[index].date = tempDate
                            reportList[index].waitTime =  reportList[index].waitTime + if (guest.waitTime != -1) guest.waitTime else 0

                        }
                    }
                }


                when (locationPageType.value) {
                    ReportRange.BY_DAILY -> {
                        if (originalList.isNotEmpty()) {
                            _dailyDetailReportData.postValue(originalList!! as MutableList<Any>)
                        } else {
                            _dailyDetailReportData.postValue(mutableListOf())
                        }
                        _dailyReportData.postValue(reportList)
                    }
                    ReportRange.BY_WEEKLY -> {
                        if (originalList.isNotEmpty()) {
                            _weeklyDetailReportData.postValue(originalList!! as MutableList<Any>)
                        } else {
                            _weeklyDetailReportData.postValue(mutableListOf())
                        }
                        _weeklyReportTitleData.postValue(titleEntity)
                        _weeklyReportData.postValue(reportList)
                    }
                    ReportRange.BY_MONTHLY -> {
                        if (originalList.isNotEmpty()) {
                            _monthlyDetailReportData.postValue(originalList!! as MutableList<Any>)
                        } else {
                            _monthlyDetailReportData.postValue(mutableListOf())
                        }
                        _monthlyReportTitleData.postValue(titleEntity)
                        _monthlyReportData.postValue(reportList)
                    }
                }

            }
        }




    /**報表切換每頁送回當頁處理類型，讓model以此判別fetchReportData發送的liveData*/
    fun setLocationPageType(type: ReportRange) {
        if(_locationPageType.value != type) {
            _locationPageType.postValue(type)
        }
    }

    /**改變時間時重新撈取資料*/
    fun setTime(timeStr: String) {
        storageTime = timeStr
        reFetch()
    }

    fun reFetch() {
        var startTime = ""
        var endTime = ""
        when (locationPageType.value) {
            ReportRange.BY_DAILY -> {
                startTime = storageTime
                endTime = storageTime
            }
            ReportRange.BY_WEEKLY -> {
                var standardTime = storageTime
                startTime = countDays(Constants.dateFormatSql.parse(standardTime), -3)
                endTime = countDays(Constants.dateFormatSql.parse(standardTime), 3)
            }
            ReportRange.BY_MONTHLY -> {
                var standardTime = storageTime
                startTime = countDays(Constants.dateFormatSql.parse(standardTime), -15)
                endTime = countDays(Constants.dateFormatSql.parse(standardTime), 15)
            }
        }

        fetchReportData(startTime, endTime)
    }

    /**判斷報表類型是否為訂位*/
    private fun isTypeRes(): Boolean {
        var reportType = prefs.reportTypePos

        return when(reportType){
            0, -1 -> true
            1 -> false
            else -> true
        }
    }


    /**改變報表類型時重新撈取資料*/
    fun setReportTypePos(position: Int) {
        if(prefs.reportTypePos != position) {
            prefs.reportTypePos = position
            reFetch()
        }
    }

    /**圖表類型切換*/
    fun setShowTypePos(position: Int) {
        if(prefs.chartTypePos != position) {
            prefs.chartTypePos = position
            _chartTypeChange.postValue(position)
        }
    }

    private fun countDays(date: Date?, days: Int): String {
        val cal: Calendar = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.DAY_OF_MONTH, days)
        val date = cal.time
        Constants.dateFormatSql.format(date)
        return Constants.dateFormatSql.format(date)
    }

    private fun countTimeDifference(start: Date, end: Date): Int {
        val between = end.time - start.time
        val min = between / (60 * 1000)
        return min.toInt()
    }


}