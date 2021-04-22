package com.citrus.mCitrusTablet.di

import android.app.Application
import com.citrus.mCitrusTablet.model.api.GlobalResponseOperator
import com.citrus.mCitrusTablet.util.ErrorManager
import com.citrus.mCitrusTablet.util.Prefs
import com.citrus.mCitrusTablet.util.Resource
import com.skydoves.sandwich.SandwichInitializer
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber


val prefs: Prefs by lazy {
    CitrusTabletApplication.prefs!!
}

val errorManager:ErrorManager by lazy {
    CitrusTabletApplication.errorManager!!
}

val resource: Resource = CitrusTabletApplication.resource!!

@HiltAndroidApp
class CitrusTabletApplication:Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        prefs = Prefs(applicationContext)
        errorManager =  ErrorManager(applicationContext)
        resource = Resource(applicationContext)
        SandwichInitializer.sandwichOperator = GlobalResponseOperator<Any>(this)
    }


    companion object {
        private lateinit var instance: CitrusTabletApplication
        var prefs: Prefs? = null
        var errorManager:ErrorManager? = null
        var resource: Resource? = null

        fun getInstance(): CitrusTabletApplication {
            return instance
        }
    }

}

