package com.citrus.mCitrusTablet.util.ui

import android.content.Context
import android.content.res.Resources
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.model.vo.ReservationGuests
import com.citrus.mCitrusTablet.util.onSafeClick
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import kotlinx.android.synthetic.main.dialog_alert.*
import kotlinx.android.synthetic.main.dialog_guest_detail.*
import java.text.SimpleDateFormat


class CustomGuestDetailDialog(
    private var mContext: Context,
    private var guest: ReservationGuests,
    private val onConfirmListener: () -> Unit
) : BaseAlertDialog(mContext, R.style.CustomDialogTheme) {


    constructor( mContext: Context,  guest: ReservationGuests) : this( mContext, guest, {})

    override fun getLayoutId(): Int {
        return R.layout.dialog_guest_detail
    }

    override fun initView() {
        tv_name.text =  mContext.resources.getString(R.string.name) + " : " +guest.mName
        tv_phone.text = mContext.resources.getString(R.string.phone_number) + " : " +guest.phone
        tv_cusNum.text = mContext.resources.getString(R.string.person) + " : " +guest.custNum.toString()
        tv_seat.text = mContext.resources.getString(R.string.selectSeat) + " : " +guest.floorName+"-"+guest.roomName

        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val outputFormat = SimpleDateFormat("MM/dd HH:mm")
        val date = inputFormat.parse(guest.reservationTime)
        val formattedDate = outputFormat.format(date)
        var timeStr = formattedDate.split(" ")
        tv_time.text = mContext.resources.getString(R.string.time) + " : " +timeStr[1]
        tv_memo.text = mContext.resources.getString(R.string.memo) + " : " +guest.memo.toString()


        btnClose.setOnClickListener {
            dismiss()
        }
    }



}