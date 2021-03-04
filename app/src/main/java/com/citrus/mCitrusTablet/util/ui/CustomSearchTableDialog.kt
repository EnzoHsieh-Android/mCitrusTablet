package com.citrus.mCitrusTablet.util.ui

import android.content.Context
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.model.vo.ReservationGuests


class CustomSearchTableDialog(
    private var mContext: Context,
    private val onConfirmListener: () -> Unit
) : BaseAlertDialog(mContext, R.style.CustomDialogTheme) {


    constructor( mContext: Context,  guest: ReservationGuests) : this( mContext, {})

    override fun getLayoutId(): Int {
        return R.layout.dialog_search_table
    }

    override fun initView() {



    }



}