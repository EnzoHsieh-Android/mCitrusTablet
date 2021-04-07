package com.citrus.mCitrusTablet.util

import android.content.Context
import android.graphics.drawable.Drawable

class Resource(private val context: Context) {
    private val resource = context.resources

    fun getString(ResId:Int):String = resource.getString(ResId)

    fun getDrawable(ResId:Int):Drawable = resource.getDrawable(ResId)

}