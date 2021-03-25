package com.citrus.mCitrusTablet.view.dialog

import android.content.Context
import android.graphics.Point
import android.view.Gravity
import android.view.View
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.model.vo.ReservationGuests
import com.citrus.mCitrusTablet.util.ui.BaseDialogFragment
import kotlinx.android.synthetic.main.dialog_guest_detail.*
import kotlinx.android.synthetic.main.dialog_number_picker.*
import java.text.SimpleDateFormat


class CustomNumberPickerDialog(val onBtnClick: (String, String, String) -> Unit) :
    BaseDialogFragment() {

    var child = 0
    var adult = 0

    override fun getLayoutId(): Int {
        return R.layout.dialog_number_picker
    }

    override fun initView() {
        setWindowWidthPercent()

        adult_picker.setOnValueChangedListener { _, _, newVal ->
            adult = newVal
        }

        child_picker.setOnValueChangedListener { _, _, newVal ->
            child = newVal
        }

        btnOK.setOnClickListener {
            if(adult + child == 0 ){
                return@setOnClickListener
            }
            onBtnClick(adult.toString(),child.toString(),(adult+child).toString())
            dismiss()
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