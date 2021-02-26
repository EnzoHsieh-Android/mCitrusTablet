package com.citrus.mCitrusTablet.util.ui.timeRangePicker

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import com.citrus.mCitrusTablet.databinding.TimeRangePickerBottomSheetDialogBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


/**
 * TimeRangePicker based on BottomSheetDialogFragment.
 * If you want to use TimeRangePickerBottomSheet, you need to add Material Components for Android.
 * Note the following link : https://material.io/develop/android/docs/getting-started/
 */
class TimeRangePickerBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: TimeRangePickerBottomSheetDialogBinding

    var onTimeRangeSelectedListener: OnTimeRangeSelectedListener? = null

    // OK button is disabled if end time is earlier than start time
    var oneDayMode = true

    // Minute time interval. default value is 10
    var interval = TimeRangePicker.defaultInterval

    // default range is {current hour}:00 ~ {current hour + 1}:00
    var timeRange = TimePickerUtils.getCurrentTimeRange()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = TimeRangePickerBottomSheetDialogBinding.inflate(inflater)
        initView()
        return binding.root
    }

    fun show(fragmentActivity: FragmentActivity) {
        show(fragmentActivity.supportFragmentManager, TAG)
    }

    private fun initView() {
        with(binding) {
            tpStart.setIs24HourView(true)
            tpEnd.setIs24HourView(true)
            btnOk.setUsable(isOkBtnUsable())
            tpStart.setOnTimeChangedListener { _, _, _ -> btnOk.setUsable(isOkBtnUsable()) }
            tpEnd.setOnTimeChangedListener { _, _, _ -> btnOk.setUsable(isOkBtnUsable()) }
            btnOk.setOnClickListener {
                onTimeRangeSelectedListener?.onTimeSelected(getSelectedTimeRange())
                dismiss()
            }
        }
    }

    private fun getSelectedTimeRange() = with(binding) {
        TimeRange(
            tpStart.getDisplayedHour(),
            tpStart.getDisplayedMinute(),
            tpEnd.getDisplayedHour(),
            tpEnd.getDisplayedMinute()
        )
    }

    private fun isOkBtnUsable(): Boolean {
        if (!oneDayMode) {
            return true
        }

        return TimePickerUtils.isCorrectSequence(getSelectedTimeRange())
    }

    class Builder : Buildable<TimeRangePickerBottomSheet>, TimeRangeSelectable<Builder> {
        private var listener: OnTimeRangeSelectedListener? = null
        private var defaultTimeInterval = TimeRangePicker.defaultInterval
        private var defaultTimeRange = TimePickerUtils.getCurrentTimeRange()
        private var defaultOnDayMode = true

        override fun setOnDayMode(OnDayMode: Boolean): Builder {
            defaultOnDayMode = OnDayMode
            return this
        }

        override fun setTimeInterval(timeInterval: Int): Builder {
            defaultTimeInterval = timeInterval
            return this
        }

        override fun setOnTimeRangeSelectedListener(timeRangeSelectedListener: OnTimeRangeSelectedListener): Builder {
            listener = timeRangeSelectedListener
            return this
        }

        fun setOnTimeRangeSelectedListener(onSelected: (timeRange: TimeRange) -> Unit): Builder {
            listener = object : OnTimeRangeSelectedListener {
                override fun onTimeSelected(timeRange: TimeRange) {
                    onSelected(timeRange)
                }
            }
            return this
        }

        override fun setTimeRange(timeRange: TimeRange): Builder {
            defaultTimeRange = timeRange
            return this
        }

        override fun build() = TimeRangePickerBottomSheet().apply {
            onTimeRangeSelectedListener = listener
            interval = defaultTimeInterval
            oneDayMode = defaultOnDayMode
            timeRange = defaultTimeRange
        }
    }

    companion object {
        private val TAG = TimeRangePickerBottomSheet::class.java.name

        fun getInstance(
        ): TimeRangePickerBottomSheet {
            val pickBottomSheetFragment = TimeRangePickerBottomSheet()
            return pickBottomSheetFragment
        }
    }
}