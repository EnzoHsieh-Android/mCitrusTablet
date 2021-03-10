package com.citrus.mCitrusTablet.util



import java.text.SimpleDateFormat
import java.util.*

object Constants {
    const val KEY_LANGUAGE = "language"
    const val SHARED_PREFERENCES_NAME = "sharedPref"
    const val KEY_DELAY_INTERVAL = "DELAY_INTERVAL"
    const val DEFAULT_TIME:Long = 1
    const val KEY_STORE_ID = "KEY_STORE_ID"
    const val KEY_RSNO = "KEY_RSNO"
    const val KEY_STORE_NAME = "KEY_STORE_NAME"
    const val KEY_SERVER_DOMAIN = "KEY_SERVER_DOMAIN"
    const val CHANGE_STATUS = "/POSServer/UploadDataWS/Service1.asmx/SetWaitData"
    const val GET_ALL_DATA = "/POSServer/UploadDataWS/Service1.asmx/GetAllReservationData"
    const val GET_FLOOR = "/POSServer/UploadDataWS/Service1.asmx/GetReservationFloor"
    const val SET_RESERVATION = "/POSServer/UploadDataWS/Service1.asmx/SetReservationData"
    const val SET_WAIT = "/POSServer/UploadDataWS/Service1.asmx/SetWaitData"
    const val GET_STORE_INFO = "/POSServer/UploadDataWS/Service1.asmx/GetStore"
    const val GET_RESERVATION_TIME = "/POSServer/UploadDataWS/Service1.asmx/GetReservationTime"
    const val GET_ORDERS_DELIVERY= "/POSServer/UploadDataWS/Service1.asmx/GetOrdersDeliveryDataByWaitNo"
    const val SEND_SMS = "http://hq.citrus.tw/citrus/Service1.asmx/SendNewsletter_Mitake"
    var dateTimeFormatSql = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
    var inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
    var dateFormatSql = SimpleDateFormat("yyyy/MM/dd")
    var defaultTimeStr: String = dateFormatSql.format(Date())

}