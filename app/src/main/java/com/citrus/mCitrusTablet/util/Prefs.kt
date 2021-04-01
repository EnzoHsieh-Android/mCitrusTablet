package com.citrus.mCitrusTablet.util

import android.content.Context
import android.content.SharedPreferences
import com.citrus.mCitrusTablet.util.Constants.KEY_RESERVATION_NUM
import com.citrus.mCitrusTablet.util.Constants.KEY_WAIT_NUM


class Prefs(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    var storageWaitNum: Int
        get() = prefs.getInt(KEY_WAIT_NUM, 0)
        set(value) = prefs.edit().putInt(KEY_WAIT_NUM, value).apply()

    var storageReservationNum: Int
        get() = prefs.getInt(KEY_RESERVATION_NUM, 0)
        set(value) = prefs.edit().putInt(KEY_RESERVATION_NUM, value).apply()

    var languagePos: Int
        get() = prefs.getInt(Constants.KEY_LANGUAGE, -1)
        set(value) = prefs.edit().putInt(Constants.KEY_LANGUAGE, value).apply()

    var storeId: String
        get() = prefs.getString(Constants.KEY_STORE_ID, "")?: ""
        set(value) = prefs.edit().putString(Constants.KEY_STORE_ID, value).apply()

    var storeName: String
        get() = prefs.getString(Constants.KEY_STORE_NAME, "")?: ""
        set(value) = prefs.edit().putString(Constants.KEY_STORE_NAME, value).apply()

    var storePic: String
        get() = prefs.getString(Constants.KEY_STORE_PIC, "")?: ""
        set(value) = prefs.edit().putString(Constants.KEY_STORE_PIC, value).apply()

    var rsno:String
        get() = prefs.getString(Constants.KEY_RSNO, "")?: ""
        set(value) = prefs.edit().putString(Constants.KEY_RSNO, value).apply()

    var severDomain:String
        get() = prefs.getString(Constants.KEY_SERVER_DOMAIN, "")?: ""
        set(value) = prefs.edit().putString(Constants.KEY_SERVER_DOMAIN, value).apply()

    var messageRes:String
        get() = prefs.getString(Constants.KEY_MESSAGE_RES, "")?: ""
        set(value) = prefs.edit().putString(Constants.KEY_MESSAGE_RES, value).apply()

    var messageWait:String
        get() = prefs.getString(Constants.KEY_MESSAGE_WAIT, "")?: ""
        set(value) = prefs.edit().putString(Constants.KEY_MESSAGE_WAIT, value).apply()

    var messageNotice:String
        get() = prefs.getString(Constants.KEY_MESSAGE_NOTICE, "")?: ""
        set(value) = prefs.edit().putString(Constants.KEY_MESSAGE_NOTICE, value).apply()


}
