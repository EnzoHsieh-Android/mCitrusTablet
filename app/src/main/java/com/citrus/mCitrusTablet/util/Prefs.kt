package com.citrus.mCitrusTablet.util

import android.content.Context
import android.content.SharedPreferences



class Prefs(private val context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(Constants.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE)

    var languagePos: Int
        get() = prefs.getInt(Constants.KEY_LANGUAGE, -1)
        set(value) = prefs.edit().putInt(Constants.KEY_LANGUAGE, value).apply()


    var rsno:String
        get() = prefs.getString(Constants.KEY_RSNO, "")?: ""
        set(value) = prefs.edit().putString(Constants.KEY_RSNO, value).apply()

    var severDomain:String
        get() = prefs.getString(Constants.KEY_SERVER_DOMAIN, "")?: ""
        set(value) = prefs.edit().putString(Constants.KEY_SERVER_DOMAIN, value).apply()

}
