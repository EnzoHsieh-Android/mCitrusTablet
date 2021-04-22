package com.citrus.mCitrusTablet.view.dialog

import android.content.DialogInterface
import android.graphics.Point
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.asLiveData
import androidx.recyclerview.widget.GridLayoutManager
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.di.errorManager
import com.citrus.mCitrusTablet.di.prefs
import com.citrus.mCitrusTablet.model.vo.*
import com.citrus.mCitrusTablet.util.Constants
import com.citrus.mCitrusTablet.util.TriggerMode
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.jetbrains.anko.support.v4.toast
import timber.log.Timber
import java.text.SimpleDateFormat


class CustomSearchTableDialog(
    private var mContext: FragmentActivity,
    private var viewModel: ReservationViewModel,
    private var onSearchListener: (date: String, cusNum: String, seat: String, adultCount: String, childCount: String) -> Unit
) : BaseDialogFragment() {
    private var otherTimeAdapter = SectionedRecyclerViewAdapter()
    private var itemPerLine = 6
    private var title = mutableListOf<String>()
    private var item = mutableListOf<OrderDateDatum>()

    var type: String = "CusNum"
    var adultCount: String = "0"
    var childCount: String = "0"

    override fun getLayoutId(): Int {
        return R.layout.dialog_search_table
    }

    override fun initView() {
        setWindowWidthPercent()

//        val name = resources.getStringArray(R.array.OtherDemo)
//        val arrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, name)
//        language_picker.setAdapter(arrayAdapter)
//
//        val count = resources.getStringArray(R.array.OtherDemo2)
//        val arrayAdapter2 = ArrayAdapter(requireContext(), R.layout.dropdown_item, count)
//        people_picker.setAdapter(arrayAdapter2)


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
            when (i) {
                R.id.rb_people -> {
                    type = "CusNum"
                    floorBlock.visibility = View.GONE
                    roomBlock.visibility = View.GONE
                    et_floor.text.clear()
                    et_room.text.clear()
                    clearView()
                }
                R.id.rb_seat -> {
                    type = "Seat"
                    floorBlock.visibility = View.VISIBLE
                    roomBlock.visibility = View.VISIBLE
                    clearView()
                }
            }
        }


//        people_picker.setOnItemClickListener { _, _, position, _ ->
//
//            when (position) {
//                0 -> {
//                    seat.setText("1", false)
//                }
//                1 -> {
//                    seat.setText("2", false)
//                }
//                2 -> {
//                    seat.setText("3", false)
//                }
//                3 -> {
//                    seat.setText("4", false)
//                }
//            }
//        }


        llDate.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                activity?.let {
                    CustomDatePickerDialog(
                        it,
                        CalendarType.NoTimePickerForSearchReservation,
                        CalendarPickerView.SelectionMode.SINGLE,
                        Constants.getCurrentDate(),
                        ""
                    ) { _, startTime, _, _, _, _ ->
                        llDate.setText(startTime)
                    }.show(it.supportFragmentManager, "CustomDatePickerDialog")
                }
            }
            llDate.clearFocus()
        }


        seat.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                CustomNumberPickerDialog { adultCount, childCount, totalCount ->
                    seat.setText(totalCount, false)
                    this.adultCount = adultCount
                    this.childCount = childCount
                }.show(requireActivity().supportFragmentManager, "CustomNumberPickerDialog")
                seat.clearFocus()
            }
        }

//        language_picker.setOnItemClickListener { _, _, position, _ ->
//            type = "Seat"
//            when (position) {
//                0 -> {
//                    et_floor.setText("A")
//                    et_room.setText("Angela")
//                }
//                1 -> {
//                    et_floor.setText("A")
//                    et_room.setText("Bill")
//                }
//                2 -> {
//                    et_floor.setText("A")
//                    et_room.setText("Shirley")
//                }
//            }
//        }


        btn_Search.setOnClickListener {

            if (llDate.text.toString() == "") {
                YoYo.with(Techniques.Shake).duration(1000).playOn(timeTextInputLayout)
                YoYo.with(Techniques.Shake).duration(1000).playOn(llDate)
                toast(R.string.select_time)
                return@setOnClickListener
            }

            if (seat.text.toString() == getString(R.string.number)) {
                YoYo.with(Techniques.Shake).duration(1000).playOn(seatTextInputLayout)
                YoYo.with(Techniques.Shake).duration(1000).playOn(seat)
                toast(R.string.submitErrorMsg)
                return@setOnClickListener
            }

            if (type == "Seat") {
                if (et_floor.text.toString() == "" || et_room.text.toString() == "") {
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

            jsonStr = if (type == "Seat") {
                Gson().toJson(
                    PostToGetOrderDateBySeat(
                        prefs.rsno,
                        formattedDate,
                        et_floor.text.toString(),
                        et_room.text.toString()
                    )
                )
            } else {
                Gson().toJson(
                    PostToGetOrderDateByCusNum(
                        prefs.rsno,
                        formattedDate,
                        seat.text.toString().toInt()
                    )
                )
            }

            loading.visibility = View.VISIBLE
            viewModel.fetchReservationTime(jsonStr)
        }

        viewModel.orderDateDatum.observe(viewLifecycleOwner, {
            loading.visibility = View.GONE
            otherTimeAdapter.removeAllSections()
            if (it.isNotEmpty()) {
                title.add(mContext.resources.getString(R.string.pick_time_hint))
                item = it.toMutableList()

                otherTimeAdapter.addSection(
                    OtherTimeAdapter(
                        title[0],
                        item,
                        onButtonClick = { timeStr ->
                            onSearchListener(
                                timeStr,
                                seat.text.toString(),
                                if (type == "CusNum") "0" else et_floor.text.toString() + "-" + et_room.text.toString(),
                                adultCount,
                                childCount
                            )
                            dismiss()
                        }
                    )
                )
                searchTimeRv.adapter = otherTimeAdapter

                animation_resultNotFound.visibility = View.GONE
                searchTimeRv.visibility = View.VISIBLE
            } else {
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


        errorManager.uiModeFlow.asLiveData().observe(viewLifecycleOwner,{ triggerMode ->
            when(triggerMode){
                TriggerMode.START -> {
                    loading.visibility = View.GONE
                    MainScope().launch(Dispatchers.IO){
                        errorManager.setTriggerMode(TriggerMode.END)
                    }
                }
                TriggerMode.END -> {
                    Timber.d("Has deal")
                }
            }
        })


    }

    override fun initAction() {
    }


    private fun clearView() {
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