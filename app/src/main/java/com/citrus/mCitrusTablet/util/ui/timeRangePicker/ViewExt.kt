package com.citrus.mCitrusTablet.util.ui.timeRangePicker

import android.graphics.Color
import android.widget.Button
import com.citrus.mCitrusTablet.R

fun Button.setUsable(
    usable: Boolean,
    usableColorResId: Int = Color.BLACK,
    disableColorResId: Int = context.getColor(R.color.color_4D000000)
) {
    isEnabled = usable
    setTextColor(if (usable) usableColorResId else disableColorResId)
}