package com.citrus.mCitrusTablet.di

import android.app.Application
import com.citrus.mCitrusTablet.model.api.GlobalResponseOperator
import com.citrus.mCitrusTablet.util.ErrorManager
import com.citrus.mCitrusTablet.util.Prefs
import com.skydoves.sandwich.SandwichInitializer
import com.yariksoffice.lingver.Lingver
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber



val prefs: Prefs by lazy {
    CitrusTabletApplication.prefs!!
}

val errorManager:ErrorManager by lazy {
    CitrusTabletApplication.errorManager!!
}


@HiltAndroidApp
class CitrusTabletApplication:Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        Lingver.init(this)
        prefs = Prefs(applicationContext)
        errorManager =  ErrorManager(applicationContext)
        SandwichInitializer.sandwichOperator = GlobalResponseOperator<Any>(this)
    }


    companion object {
        private lateinit var instance: CitrusTabletApplication
        var prefs: Prefs? = null
        var errorManager:ErrorManager? = null
    }
}

