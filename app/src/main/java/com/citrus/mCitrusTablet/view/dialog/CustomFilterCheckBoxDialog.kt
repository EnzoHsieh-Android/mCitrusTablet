package com.citrus.mCitrusTablet.view.dialog

import android.content.Context
import android.view.View
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.model.vo.Wait
import com.citrus.mCitrusTablet.util.Constants
import com.citrus.mCitrusTablet.util.ui.BaseAlertDialog
import com.citrus.mCitrusTablet.util.ui.BaseDialogFragment
import com.citrus.mCitrusTablet.view.wait.Filter
import kotlinx.android.synthetic.main.dialog_filter_check.*
import kotlinx.android.synthetic.main.dialog_search_table.*
import kotlinx.android.synthetic.main.dialog_search_table.rb_group


class CustomFilterCheckBoxDialog(
    private var mContext: Context,
    private var type:Filter,
    private var onCheckChange : (Filter) -> Unit,
) : BaseAlertDialog(mContext, R.style.CustomDialogTheme){
    var newFilter = type

    override fun getLayoutId(): Int {
        return R.layout.dialog_filter_check
    }



    override fun initView() {


        when(type){
            Filter.SHOW_ALL -> { rb_all.isChecked = true}
            Filter.SHOW_CANCELLED -> { rb_cancelled.isChecked = true}
            Filter.SHOW_NOTIFIED -> { rb_notified.isChecked = true}
            Filter.SHOW_CONFIRM -> { rb_confirm.isChecked = true}
            Filter.SHOW_WAIT -> { rb_wait.isChecked = true}
        }

        rb_group.setOnCheckedChangeListener { _, i ->
            when(i){
                R.id.rb_all -> {
                    newFilter = Filter.SHOW_ALL
                    closeDialog()
                }
                R.id.rb_cancelled -> {
                    newFilter = Filter.SHOW_CANCELLED
                    closeDialog()
                }
                R.id.rb_confirm -> {
                    newFilter = Filter.SHOW_CONFIRM
                    closeDialog()
                }
                R.id.rb_notified -> {
                    newFilter = Filter.SHOW_NOTIFIED
                    closeDialog()
                }
                R.id.rb_wait -> {
                    newFilter = Filter.SHOW_WAIT
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