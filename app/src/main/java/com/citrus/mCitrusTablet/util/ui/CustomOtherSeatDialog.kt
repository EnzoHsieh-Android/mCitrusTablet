package com.citrus.mCitrusTablet.util.ui

import android.content.Context
import android.graphics.Point
import android.view.Gravity
import com.citrus.mCitrusTablet.R



class CustomOtherSeatDialog(
    private var mContext: Context,
) : BaseDialogFragment() {

    override fun getLayoutId(): Int {
        return R.layout.dialog_other_seat
    }

    override fun initView() {
        setWindowWidthPercent()
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