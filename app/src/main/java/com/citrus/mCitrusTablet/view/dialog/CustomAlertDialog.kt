package com.citrus.mCitrusTablet.view.dialog

import android.content.Context
import android.view.View
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.util.onSafeClick
import com.citrus.mCitrusTablet.util.ui.BaseAlertDialog
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import kotlinx.android.synthetic.main.dialog_alert.*


class CustomAlertDialog(
    mContext: Context,
    private var title: String,
    private var msg: String,
    private val icon: Int,
    private val onConfirmListener: () -> Unit
) : BaseAlertDialog(mContext, R.style.CustomDialogTheme) {

    constructor(mContext: Context, title: String, msg: String, icon: Int) : this(mContext, title, msg, icon, {})

    override fun getLayoutId(): Int {
        return R.layout.dialog_alert
    }

    override fun initView() {
        if (icon == R.drawable.ic_check) {
            setCancelable(false)
            setCanceledOnTouchOutside(false)
        }

        if(icon !=0) {
            YoYo.with(Techniques.FlipInX).delay(100).duration(1200).pivot(YoYo.CENTER_PIVOT, YoYo.CENTER_PIVOT).playOn(ivIcon)
            ivIcon.setImageResource(icon)
        }else{
            ivIcon.visibility = View.GONE
        }

        tvTitle.text = title

        if (msg.isNotEmpty()) {
            tv_message.visibility = View.VISIBLE
            tv_message.text = msg
        }

        btnOK.onSafeClick {
            onConfirmListener()
            dismiss()
        }
    }
}