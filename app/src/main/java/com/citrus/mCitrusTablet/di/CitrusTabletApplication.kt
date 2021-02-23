package com.citrus.mCitrusTablet.di

import android.app.Application
import com.citrus.mCitrusTablet.model.api.GlobalResponseOperator
import com.skydoves.sandwich.SandwichInitializer
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class CitrusTabletApplication:Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        SandwichInitializer.sandwichOperator = GlobalResponseOperator<Any>(this)
    }
}