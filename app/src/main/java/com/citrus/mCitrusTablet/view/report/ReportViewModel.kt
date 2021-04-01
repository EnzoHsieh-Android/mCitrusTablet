package com.citrus.mCitrusTablet.view.report


import androidx.hilt.lifecycle.ViewModelInject
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.citrus.mCitrusTablet.di.prefs
import com.citrus.mCitrusTablet.model.Repository
import com.citrus.mCitrusTablet.model.vo.PostToGetAllData
import com.citrus.mCitrusTablet.model.vo.Report
import com.citrus.mCitrusTablet.util.Constants
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

class ReportViewModel @ViewModelInject constructor(private val model: Repository) :
    ViewModel() {
    private var serverDomain = "https://" + prefs.severDomain


    private val _resReportData = MutableLiveData<MutableList<Report>>()
    val resReportData: LiveData<MutableList<Report>>
        get() = _resReportData

    private val _resReportTitleData = MutableLiveData<MutableList<String>>()
    val resReportTitleData: LiveData<MutableList<String>>
        get() = _resReportTitleData



    fun fetchAllData(startTime: String, endTime: String) =
        viewModelScope.launch {
            model.fetchAllDataForReport(
                serverDomain + Constants.GET_ALL_DATA,
                PostToGetAllData(prefs.rsno, startTime, endTime)
            ).collect { allObject ->
                var tempDate = ""
                var index = 0
                var resList = allObject.reservation.sortedBy { it.reservationTime }
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
                val outputFormat = SimpleDateFormat("MM/dd")
                var resReportList = mutableListOf<Report>()
                var titleEntity = mutableListOf<String>()

                /**Reservation - 以日期區分好date List 和 data List*/
                for (guest in resList) {
                    val date = inputFormat.parse(guest.reservationTime)
                    val formattedDate = outputFormat.format(date)
                    guest.reservationTime = formattedDate
                    if (tempDate != formattedDate) {
                        tempDate = formattedDate
                        titleEntity.add(tempDate)
                        index = titleEntity.indexOf(tempDate)
                        resReportList.add(index, Report(0, 0, 0, 0, 0, 0,""))
                    }

                    resReportList[index].adult = if (guest.status == Constants.CHECK) resReportList[index].adult + guest.adultCount else resReportList[index].adult
                    resReportList[index].child = if (guest.status == Constants.CHECK) resReportList[index].child + guest.kidCount else resReportList[index].child
                    resReportList[index].total = resReportList[index].total + 1
                    resReportList[index].cancel =
                        resReportList[index].cancel + if (guest.status == Constants.CANCEL) 1 else 0
                    resReportList[index].wait =
                        resReportList[index].wait + if (guest.status == Constants.ADD) 1 else 0
                    resReportList[index].check =
                        resReportList[index].check + if (guest.status == Constants.CHECK) 1 else 0
                    resReportList[index].date = tempDate
                }

                _resReportTitleData.postValue(titleEntity)
                _resReportData.postValue(resReportList)
            }
        }


}