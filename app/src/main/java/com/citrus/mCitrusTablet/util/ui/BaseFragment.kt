package com.citrus.mCitrusTablet.util.ui

import android.util.TypedValue
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.citrus.mCitrusTablet.R
import com.google.android.material.snackbar.Snackbar

open class BaseFragment:Fragment() {
    fun adjustSnackBar(mSnackBar: Snackbar): Snackbar {
        val snackBarView = mSnackBar.view
        val snackTextView = snackBarView.findViewById(R.id.snackbar_text) as TextView
        val snackActionTextView = snackBarView.findViewById(R.id.snackbar_action) as TextView
        val dimen = resources.getDimensionPixelSize(R.dimen.sp_20)
        snackTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, dimen.toFloat())
        snackActionTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, dimen.toFloat())

        return mSnackBar
    }
}