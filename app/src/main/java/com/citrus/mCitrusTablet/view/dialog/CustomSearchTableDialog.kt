package com.citrus.mCitrusTablet.view.dialog

import android.graphics.Point
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.fragment.app.FragmentActivity
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.di.prefs
import com.citrus.mCitrusTablet.model.vo.PostToGetOrderDateByCusNum
import com.citrus.mCitrusTablet.model.vo.PostToGetOrderDateBySeat
import com.citrus.mCitrusTablet.util.ui.BaseDialogFragment
import com.citrus.mCitrusTablet.view.reservation.ReservationViewModel
import com.google.gson.Gson
import com.savvi.rangedatepicker.CalendarPickerView
import kotlinx.android.synthetic.main.dialog_search_table.*
import java.text.SimpleDateFormat


class CustomSearchTableDialog(
    private var mContext: FragmentActivity,
    private var viewModel: ReservationViewModel,
    private var onSearchListener: (postData: String) -> Unit
) : BaseDialogFragment() {

    var type:String = "CusNum"

    override fun getLayoutId(): Int {
        return R.layout.dialog_search_table
    }

    override fun initView() {
        setWindowWidthPercent()


        rb_group.setOnCheckedChangeListener { _, i ->
            when(i){
                R.id.rb_people -> {
                    type = "CusNum"
                    et_people.visibility = View.VISIBLE
                    et_seat.visibility = View.GONE
                }
                R.id.rb_seat -> {
                    type = "Seat"
                    et_people.visibility = View.GONE
                    et_seat.visibility = View.VISIBLE
                }
            }
        }

        llDate.setOnClickListener {
            mContext?.let {
                CustomDatePickerDialog(
                    it,
                    CalendarType.NoTimePickerForSearchReservation,
                    CalendarPickerView.SelectionMode.SINGLE,
               "",
                  ""
                ) { _, startTime, endTime, _ ->
                    llDate.text = startTime
                }.show(it.supportFragmentManager, "CustomDatePickerDialog")
            }
        }


        btn_Search.setOnClickListener {
            var jsonStr = ""
            val inputFormat = SimpleDateFormat("yyyy/MM/dd")
            val outputFormat = SimpleDateFormat("MM-dd-yyyy")
            val date = inputFormat.parse(llDate.text.toString())
            val formattedDate = outputFormat.format(date)

            jsonStr = if(type == "Seat"){
                Gson().toJson(PostToGetOrderDateBySeat(prefs.rsno,formattedDate,"A","A1"))
            }else{
                Gson().toJson(PostToGetOrderDateByCusNum(prefs.rsno,formattedDate,et_people.text.toString().toInt()))
            }

            viewModel.fetchReservationTime(jsonStr)
        }

        viewModel.orderDateDatum.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                Log.e("list",it.toString())
            }
        })



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