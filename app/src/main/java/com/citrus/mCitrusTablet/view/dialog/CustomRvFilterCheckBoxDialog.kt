package com.citrus.mCitrusTablet.view.dialog

import android.content.Context
import android.view.View
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.model.vo.Wait
import com.citrus.mCitrusTablet.util.Constants
import com.citrus.mCitrusTablet.util.ui.BaseAlertDialog
import com.citrus.mCitrusTablet.util.ui.BaseDialogFragment
import com.citrus.mCitrusTablet.view.reservation.CancelFilter
import com.citrus.mCitrusTablet.view.wait.Filter
import kotlinx.android.synthetic.main.dialog_filter_check.*
import kotlinx.android.synthetic.main.dialog_filter_check.rb_cancelled
import kotlinx.android.synthetic.main.dialog_rv_filter_check.*
import kotlinx.android.synthetic.main.dialog_search_table.*
import kotlinx.android.synthetic.main.dialog_search_table.rb_group


class CustomRvFilterCheckBoxDialog(
    private var mContext: Context,
    private var type: CancelFilter,
    private var onCheckChange : (CancelFilter) -> Unit,
) : BaseAlertDialog(mContext, R.style.CustomDialogTheme){
    var newFilter = type

    override fun getLayoutId(): Int {
        return R.layout.dialog_rv_filter_check
    }



    override fun initView() {
        setCancelable(true)

        when(type){
           CancelFilter.SHOW_CANCELLED -> {
               rb_cancelled.isChecked = true
           }
           CancelFilter.HIDE_CANCELLED -> {
               rb_cancelled_h.isChecked = true
           }
        }

        rb_group.setOnCheckedChangeListener { _, i ->
            when(i){
                R.id.rb_cancelled -> {
                    newFilter = CancelFilter.SHOW_CANCELLED
                    closeDialog()
                }
                R.id.rb_cancelled_h -> {
                    newFilter = CancelFilter.HIDE_CANCELLED
                    closeDialog()
                }
            }
        }



    }

    private fun closeDialog(){
        onCheckChange(newFilter)
        dismiss()
    }



}