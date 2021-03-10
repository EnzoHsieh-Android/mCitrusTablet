package com.citrus.mCitrusTablet.view.dialog

import android.content.Context
import android.graphics.Point
import android.view.Gravity
import androidx.recyclerview.widget.GridLayoutManager
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.model.vo.Datum
import com.citrus.mCitrusTablet.model.vo.Floor
import com.citrus.mCitrusTablet.util.ui.BaseDialogFragment
import com.citrus.mCitrusTablet.view.adapter.OtherSeatAdapter
import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter
import kotlinx.android.synthetic.main.dialog_other_seat.*


class CustomOtherSeatDialog(
    private var mContext: Context,
    private var dataList: List<Datum>,
    private var onBtnClick: (String, String) -> Unit
) : BaseDialogFragment() {
    private var otherSeatAdapter = SectionedRecyclerViewAdapter()
    private var itemPerLine = 6
    private var title = mutableListOf<String>()
    private var item = mutableListOf<List<Floor>>()

    override fun getLayoutId(): Int {
        return R.layout.dialog_other_seat
    }

    override fun initView() {
        setWindowWidthPercent()

        for (datum in dataList!!) {
            title.add(datum.resDate)
            item.add(datum.floor)
        }


        reservation_rv.apply {
            val glm = GridLayoutManager(mContext, itemPerLine)
            glm.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(position: Int): Int {
                    return if (otherSeatAdapter.getSectionItemViewType(position) == SectionedRecyclerViewAdapter.VIEW_TYPE_HEADER) {
                        itemPerLine
                    } else {
                        1
                    }
                }
            }
            this.layoutManager = glm
        }


        for (index in 0 until title.size) {
            otherSeatAdapter.addSection(
                OtherSeatAdapter(
                    title[index],
                    item[index],
                    onButtonClick = { floor, dateStr ->
                        onBtnClick(floor.floorName + "-" + floor.roomName, dateStr)
                        dismiss()
                    }
                )
            )
        }

        reservation_rv.adapter = otherSeatAdapter
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


    override fun onDestroy() {
        otherSeatAdapter.removeAllSections()
        super.onDestroy()
    }
}