package com.citrus.mCitrusTablet.util

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.model.vo.Report
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF


class MyMarkView(
    context: Context?,
    private val resReportList: MutableList<Report>,
    private val axisValueFormatter: IAxisValueFormatter
) : MarkerView(context, R.layout.mark_view) {
    private var tv1: TextView? = null
    private var root: RelativeLayout? =null

    init {
        initView()
    }

    private fun initView() {
        tv1 = findViewById(R.id.tvMark)
        root = findViewById(R.id.root)
    }


    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        val barEntry = e as BarEntry
        var index = highlight?.stackIndex.toString()
        val values = barEntry.yVals
        var msg =""


        when(index){
            "2" -> {
                root?.visibility = View.VISIBLE
                var tag = axisValueFormatter.getFormattedValue(e.getX(), null).toString()
                for(data in resReportList){
                    if(data.date == tag){
                        msg = context.resources.getString(R.string.adult)+" "+data.adult+" "+context.resources.getString(R.string.child)+" "+data.child
                    }
                }
                tv1?.text = msg
            }

            "1" -> {
                root?.visibility = View.GONE
            }

            "0" -> {
                root?.visibility = View.GONE
            }

        }



        super.refreshContent(e, highlight)
    }

    private var mOffset: MPPointF? = null
    override fun getOffset(): MPPointF {
        if (mOffset == null) {
            mOffset = MPPointF((-(width / 2)).toFloat(), (-height).toFloat())
        }
        return mOffset!!
    }

}