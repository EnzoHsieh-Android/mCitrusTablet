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
enum class ShowType { BY_CHART, BY_TEXT }
enum class ReportType { RESERVATION, WAIT }
class ReportViewModel @ViewModelInject constructor(private val model: Repository) :
    ViewModel() {
    private var serverDomain = "https://" + prefs.severDomain
    private var storageTime = Constants.defaultTimeStr

    private val _locationPageType = MutableLiveData<ReportRange>()
    val locationPageType: LiveData<ReportRange>
        get() = _locationPageType

    private val _showType = MutableLiveData<ShowType>()
    val showType: LiveData<ShowType>
        get() = _showType

    private val _reportType = MutableLiveData<ReportType>()
    val reportType: LiveData<ReportType>
        get() = _reportType

    private val _dailyDetailReportData = MutableLiveData<MutableList<Any>>()
    val dailyDetailReportData: LiveData<MutableList<Any>>
        get() = _dailyDetailReportData

    private val _dailyReportData = MutableLiveData<MutableList<Report>>()
    val dailyReportData: LiveData<MutableList<Report>>
        get() = _dailyReportData

    private val _dailyReportTitleData = MutableLiveData<MutableList<String>>()
    val dailyReportTitleData: LiveData<MutableList<String>>
        get() = _dailyReportTitleData

    private val _weeklyReportData = MutableLiveData<MutableList<Report>>()
    val weeklyReportData: LiveData<MutableList<Report>>
        get() = _weeklyReportData

    private val _weeklyReportTitleData = MutableLiveData<MutableList<String>>()
    val weeklyReportTitleData: LiveData<MutableList<String>>
        get() = _weeklyReportTitleData

    private val _monthlyReportData = MutableLiveData<MutableList<Report>>()
    val monthlyReportData: LiveData<MutableList<Report>>
        get() = _monthlyReportData

    private val _monthlyReportTitleData = MutableLiveData<MutableList<String>>()
    val monthlyReportTitleData: LiveData<MutableList<String>>
        get() = _monthlyReportTitleData


    init {
        _reportType.postValue(ReportType.RESERVATION)
    }


    fun fetchReportData(startTime: String, endTime: String) =
        viewModelScope.launch {
            model.fetchAllDataForReport(
                serverDomain + Constants.GET_ALL_DATA,
                PostToGetAllData(prefs.rsno, startTime, endTime)
            ).collect { allObject ->
                var originalList: Any?
                var tempDate = ""
                var index = 0
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
                            reportList.add(index, Report(0, 0, 0, 0, 0, 0, ""))
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
                                reportList[index].wait + if (guest.status == Constants.ADD) 1 else 0
                            reportList[index].check =
                                reportList[index].check + if (guest.status == Constants.CHECK) 1 else 0
                            reportList[index].date = tempDate
                        }
                    }
                }


                when (locationPageType.value) {
                    ReportRange.BY_DAILY -> {
                            _dailyDetailReportData.postValue(originalList!! as MutableList<Any>)
                            _dailyReportTitleData.postValue(titleEntity)
                            _dailyReportData.postValue(reportList)
                    }
                    ReportRange.BY_WEEKLY -> {
                            _weeklyReportTitleData.postValue(titleEntity)
                            _weeklyReportData.postValue(reportList)
                    }
                    ReportRange.BY_MONTHLY -> {
                            _monthlyReportTitleData.postValue(titleEntity)
                            _monthlyReportData.postValue(reportList)
                    }
                }
            }
        }


    /**報表每頁通知當頁處理類型，以此判別fetchReportData發送的liveData*/
    fun setLocationPageType(type: ReportRange) {
        _locationPageType.postValue(type)
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
                endTime = countDays(Constants.dateFormatSql.parse(startTime), 15)
            }
        }

        fetchReportData(startTime, endTime)
    }

    private fun isTypeRes(): Boolean {
        return reportType.value == ReportType.RESERVATION
    }

    fun setShowType(showType: ShowType) {
        _showType.postValue(showType)
    }

    fun setReportType(reportType: ReportType) {
        _reportType.postValue(reportType)
        reFetch()
    }

    private fun countDays(date: Date?, days: Int): String {
        val cal: Calendar = Calendar.getInstance()
        cal.time = date
        cal.add(Calendar.DAY_OF_MONTH, days)
        val date = cal.time
        Constants.dateFormatSql.format(date)
        return Constants.dateFormatSql.format(date)
    }

}