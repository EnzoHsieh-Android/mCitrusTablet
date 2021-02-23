package com.citrus.mCitrusTablet.util.ui

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import java.util.*

class TimePickerFragment(private val initTime: String?, private val listener: (String) -> Unit) : DialogFragment(),
    TimePickerDialog.OnTimeSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        var hour = c.get(Calendar.HOUR_OF_DAY)
        var minute = c.get(Calendar.MINUTE)

        if (initTime?.length == 5 && initTime.contains(":")) {
            try {
                hour = initTime.substring(0, 2).toInt()
                minute = initTime.substring(3, 5).toInt()
            } catch (e: Exception) {
            }
        }

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(activity, this, hour, minute, true)
    }

    override fun onTimeSet(view: TimePicker, hourOfDay: Int, minute: Int) {
        // Do something with the time chosen by the user
        listener("${String.format("%02d", hourOfDay)}:${String.format("%02d", minute)}")
    }
}