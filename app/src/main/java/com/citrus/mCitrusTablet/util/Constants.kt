package com.citrus.mCitrusTablet.util



import com.citrus.mCitrusTablet.util.ui.CalendarType
import java.text.SimpleDateFormat
import java.util.*

object Constants {
    const val SHARED_PREFERENCES_NAME = "sharedPref"
    const val KEY_DELAY_INTERVAL = "DELAY_INTERVAL"
    const val DEFAULT_TIME:Long = 1
    var dateTimeFormatSql = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
    var inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    var dateFormatSql = SimpleDateFormat("yyyy/MM/dd")
    var defaultTimeStr: String = dateFormatSql.format(Date())

}