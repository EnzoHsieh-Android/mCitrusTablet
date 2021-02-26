package com.citrus.mCitrusTablet.util.ui.timeRangePicker

interface TimeRangeSelectable<T : Buildable<*>> {

    fun setOnDayMode(OnDayMode: Boolean): T

    fun setTimeInterval(timeInterval: Int): T

    fun setOnTimeRangeSelectedListener(timeRangeSelectedListener: OnTimeRangeSelectedListener): T

    fun setTimeRange(timeRange: TimeRange): T
}
