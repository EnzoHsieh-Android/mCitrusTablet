package com.citrus.mCitrusTablet.view.dialog

import android.content.Context
import android.graphics.Point
import android.view.Gravity
import android.view.View
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.model.vo.ReservationGuests
import com.citrus.mCitrusTablet.util.ui.BaseDialogFragment
import kotlinx.android.synthetic.main.dialog_guest_detail.*
import java.text.SimpleDateFormat


class CustomGuestDetailDialog(
    private var mContext: Context,
    private var guest: ReservationGuests,
) : BaseDialogFragment() {

    override fun getLayoutId(): Int {
        return R.layout.dialog_guest_detail
    }

    override fun initView() {
        setWindowWidthPercent()
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

        if(guest.memo != null && guest.memo != "") {
            tv_memo.text =
                mContext.resources.getString(R.string.memo) + " : " + guest.memo.toString()
        }else{
            tv_memo.visibility = View.GONE
            ivIcon.visibility = View.GONE
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

            it.setLayout((width * 0.5).toInt(), (height * 0.6).toInt())
            it.setGravity(Gravity.CENTER)
        }
    }
}