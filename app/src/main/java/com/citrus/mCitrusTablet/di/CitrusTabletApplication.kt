package com.citrus.mCitrusTablet.di

import android.app.Application
import com.citrus.mCitrusTablet.model.api.GlobalResponseOperator
import com.citrus.mCitrusTablet.util.Prefs
import com.skydoves.sandwich.SandwichInitializer
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber


val prefs: Prefs by lazy {
    CitrusTabletApplication.prefs!!
}

@HiltAndroidApp
class CitrusTabletApplication:Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        prefs = Prefs(applicationContext)
        SandwichInitializer.sandwichOperator = GlobalResponseOperator<Any>(this)
    }


    companion object {
        private lateinit var instance: CitrusTabletApplication
        var prefs: Prefs? = null

        fun getInstance(): CitrusTabletApplication {
            return instance
        }
    }

}

