package com.citrus.mCitrusTablet.util.ui

import android.content.Context
import android.view.View
import androidx.fragment.app.activityViewModels
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.util.onSafeClick
import com.citrus.mCitrusTablet.view.SharedViewModel
import kotlinx.android.synthetic.main.dialog_alert.*
import kotlinx.android.synthetic.main.dialog_alert.btnCancel
import kotlinx.android.synthetic.main.dialog_alert.btnOK
import kotlinx.android.synthetic.main.dialog_alert.tvTitle
import kotlinx.android.synthetic.main.dialog_number_picker.*


class CustomNumberPickerDialog(
    mContext: Context,
    private var array: MutableList<Int>,
    private var title: String,
    private val onConfirmListener: (String) -> Unit
) : BaseAlertDialog(mContext, R.style.CustomDialogTheme) {

    lateinit var value:String

    constructor(mContext: Context,array:MutableList<Int>, title: String) : this(mContext, array, title, {})

    override fun getLayoutId(): Int {
        return R.layout.dialog_number_picker
    }

    override fun initView() {
        btnCancel.visibility = View.GONE


        tvTitle.text = title
        value = array[0].toString()

        if(array != null){
            number_picker?.minValue = 1
            number_picker?.maxValue = array.size
            number_picker?.displayedValues = array.map { it.toString() }.toTypedArray()
        }
        number_picker.setOnValueChangedListener { _, _, newVal ->
            value = array[newVal-1].toString()
        }



        btnOK.onSafeClick {
            onConfirmListener(value)
            dismiss()
        }

    }

}