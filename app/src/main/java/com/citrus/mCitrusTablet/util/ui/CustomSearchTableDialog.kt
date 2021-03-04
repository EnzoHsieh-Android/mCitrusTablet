package com.citrus.mCitrusTablet.util.ui

import android.graphics.Point
import android.view.Gravity
import androidx.fragment.app.FragmentActivity
import com.citrus.mCitrusTablet.R
import com.savvi.rangedatepicker.CalendarPickerView
import kotlinx.android.synthetic.main.dialog_search_table.*


class CustomSearchTableDialog(
    private var mContext: FragmentActivity,
    onConfirmListener: () -> Unit
) : BaseDialogFragment() {

    override fun getLayoutId(): Int {
        return R.layout.dialog_search_table
    }

    override fun initView() {
        setWindowWidthPercent()

        llDate.setOnClickListener {
            mContext?.let {
                CustomDatePickerDialog(
                    it,
                    CalendarType.NoTimePickerForSearchReservation,
                    CalendarPickerView.SelectionMode.SINGLE,
               "",
                  ""
                ) { _, startTime, endTime, _ ->
                    llDate.text = startTime
                }.show(it.supportFragmentManager, "CustomDatePickerDialog")
            }
        }
    }

    override fun initAction() {
    }

    private fun setWindowWidthPercent() {
        dialog?.window?.let {
            val size = Point()
            val display = it.windowManager.defaultDisplay
            display.getSize(size)

            val width = size.x
            val height = size.y

            it.setLayout((width * 0.65).toInt(), (height * 0.75).toInt())
            it.setGravity(Gravity.CENTER)
        }
    }
}