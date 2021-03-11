package com.citrus.mCitrusTablet.view.dialog

import android.graphics.Point
import android.util.Log
import android.view.Gravity
import android.view.View
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.di.prefs
import com.citrus.mCitrusTablet.model.vo.*
import com.citrus.mCitrusTablet.util.ui.BaseDialogFragment
import com.citrus.mCitrusTablet.view.adapter.OtherSeatAdapter
import com.citrus.mCitrusTablet.view.adapter.OtherTimeAdapter
import com.citrus.mCitrusTablet.view.reservation.ReservationViewModel
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import com.google.gson.Gson
import com.savvi.rangedatepicker.CalendarPickerView
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import kotlinx.android.synthetic.main.dailog_date_picker.*
import kotlinx.android.synthetic.main.dialog_other_seat.*
import kotlinx.android.synthetic.main.dialog_search_table.*
import org.jetbrains.anko.support.v4.toast
import java.text.SimpleDateFormat


class CustomSearchTableDialog(
    private var mContext: FragmentActivity,
    private var viewModel: ReservationViewModel,
    private var onSearchListener: (date:String,cusNum:String,seat:String) -> Unit
) : BaseDialogFragment() {
    private var otherTimeAdapter = SectionedRecyclerViewAdapter()
    private var itemPerLine = 6
    private var title = mutableListOf<String>()
    private var item = mutableListOf<OrderDateDatum>()

    var type:String = "CusNum"

    override fun getLayoutId(): Int {
        return R.layout.dialog_search_table
    }

    override fun initView() {
        setWindowWidthPercent()

            searchTimeRv.apply {
            val glm = GridLayoutManager(mContext, itemPerLine)
            glm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (otherTimeAdapter.getSectionItemViewType(position) == SectionedRecyclerViewAdapter.VIEW_TYPE_HEADER) {
                        itemPerLine
                    } else {
                        1
                    }
                }
            }
            this.layoutManager = glm
        }


        rb_group.setOnCheckedChangeListener { _, i ->
            when(i){
                R.id.rb_people -> {
                    type = "CusNum"
                    et_people.visibility = View.VISIBLE
                    et_floor.visibility = View.INVISIBLE
                    et_room.visibility = View.GONE
                    et_floor.text.clear()
                    et_room.text.clear()
                    clearView()
                }
                R.id.rb_seat -> {
                    type = "Seat"
                    et_people.visibility = View.VISIBLE
                    et_floor.visibility = View.VISIBLE
                    et_room.visibility = View.VISIBLE
                    et_people.text.clear()
                    clearView()
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

            if (llDate.text.toString() == getString(R.string.select_time)) {
                YoYo.with(Techniques.Shake).duration(1000).playOn(llDate)
                toast(R.string.select_time)
                return@setOnClickListener
            }

            if (et_people.text.toString() == "") {
                YoYo.with(Techniques.Shake).duration(1000).playOn(et_people)
                toast(R.string.submitErrorMsg)
                return@setOnClickListener
            }

            if(type == "Seat"){
                if (et_floor.text.toString() == "" ||et_room.text.toString() == "") {
                    YoYo.with(Techniques.Shake).duration(1000).playOn(et_floor)
                    YoYo.with(Techniques.Shake).duration(1000).playOn(et_room)
                    toast(R.string.submitErrorMsg)
                    return@setOnClickListener
                }
            }

            var jsonStr = ""
            val inputFormat = SimpleDateFormat("yyyy/MM/dd")
            val outputFormat = SimpleDateFormat("MM-dd-yyyy")
            val date = inputFormat.parse(llDate.text.toString())
            val formattedDate = outputFormat.format(date)

            jsonStr = if(type == "Seat"){
                Gson().toJson(PostToGetOrderDateBySeat(prefs.rsno,formattedDate,et_floor.text.toString(),et_room.text.toString()))
            }else{
                Gson().toJson(PostToGetOrderDateByCusNum(prefs.rsno,formattedDate,et_people.text.toString().toInt()))
            }

            viewModel.fetchReservationTime(jsonStr)
        }

        viewModel.orderDateDatum.observe(viewLifecycleOwner, {
            otherTimeAdapter.removeAllSections()
            if (it.isNotEmpty()) {
                title.add(mContext.resources.getString(R.string.pick_time_hint))
                item = it.toMutableList()

                otherTimeAdapter.addSection(
                    OtherTimeAdapter(
                        title[0],
                        item,
                        onButtonClick = { timeStr ->
                            onSearchListener(timeStr,et_people.text.toString(), if(type=="CusNum") "0" else et_floor.text.toString()+"-"+et_room.text.toString())
                            dismiss()
                        }
                    )
                )
                searchTimeRv.adapter = otherTimeAdapter

                animation_resultNotFound.visibility = View.GONE
                searchTimeRv.visibility = View.VISIBLE
            }else{
                clearView()
                activity?.let {
                    CustomAlertDialog(
                        requireActivity(),
                        resources.getString(R.string.noTime),
                        "",
                        0,
                        onConfirmListener = {

                        }
                    ).show()
                }
            }
        })




    }

    override fun initAction() {
    }


    private fun clearView(){
        otherTimeAdapter.removeAllSections()
        animation_resultNotFound.visibility = View.VISIBLE
        searchTimeRv.visibility = View.GONE
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