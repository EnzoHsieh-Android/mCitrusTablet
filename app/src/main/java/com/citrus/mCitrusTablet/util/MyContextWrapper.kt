package com.citrus.mCitrusTablet.util

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import android.os.LocaleList
import java.util.*

/**
 * Created by user on 2018/3/13.
 */
class MyContextWrapper(base: Context) : ContextWrapper(base) {
    companion object {
        fun wrap(context: Context, newLocale: Locale?): MyContextWrapper {
            var context = context
            val res = context.resources
            val configuration = res.configuration
            context = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                configuration.setLocale(newLocale)
                val localeList = LocaleList(newLocale)
                LocaleList.setDefault(localeList)
                configuration.setLocales(localeList)
                context.createConfigurationContext(configuration)
            } else {
                configuration.setLocale(newLocale)
                context.createConfigurationContext(configuration)
            }
            return MyContextWrapper(context)
        }
    }
}