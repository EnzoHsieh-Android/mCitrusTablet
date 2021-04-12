package com.citrus.mCitrusTablet.util

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.model.vo.Report
import com.citrus.mCitrusTablet.view.report.ReportRange
import com.citrus.mCitrusTablet.view.report.ReportType
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF


class MyMarkView(
    context: Context?,
    private val resReportList: MutableList<Report>,
    private val axisValueFormatter: IAxisValueFormatter,
    private val type: ReportRange,
) : MarkerView(context, R.layout.mark_view) {
    private var tv1: TextView? = null
    private var root: ConstraintLayout? =null

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

        var tag = axisValueFormatter.getFormattedValue(e.getX(), null).toString()
        when(index){
            "2" -> {
                for(data in resReportList){
                    if(data.date == tag){
                        if(data.adult + data.child != 0){
                            root?.visibility = View.VISIBLE
                        }else{
                            root?.visibility = View.GONE
                        }
                        msg = context.resources.getString(R.string.check)+" "+data.check+ "組"+"\n"+ context.resources.getString(R.string.adult)+" "+data.adult+" "+context.resources.getString(R.string.child)+" "+data.child
                    }
                }
                tv1?.text = msg
            }

            "1" -> {
                for(data in resReportList){
                    if(data.date == tag){
                        if(data.wait != 0){
                            root?.visibility = View.VISIBLE
                        }else{
                            root?.visibility = View.GONE
                        }
                        msg = context.resources.getString(R.string.Waiting)+" "+data.wait+" "+"組"
                    }
                }
                tv1?.text = msg
            }

            "0" -> {
                for(data in resReportList){
                    if(data.date == tag){
                        if(data.cancel != 0){
                            root?.visibility = View.VISIBLE
                        }else{
                            root?.visibility = View.GONE
                        }
                        msg = context.resources.getString(R.string.cancel)+" "+data.cancel+" "+"組"
                    }
                }
                tv1?.text = msg
            }
            else -> {
                root?.visibility = View.GONE
            }

        }



        super.refreshContent(e, highlight)
    }

    private var mOffset: MPPointF? = null
    override fun getOffset(): MPPointF {
        when(type){
            ReportRange.BY_WEEKLY -> {
                if (mOffset == null) {
                    mOffset = MPPointF((-(width / 2)).toFloat(), (-height/2).toFloat())
                }
            }
            ReportRange.BY_MONTHLY -> {
                if (mOffset == null) {
                    mOffset = MPPointF((-(width/1.5)).toFloat(), (-height).toFloat())
                }
            }
        }

        return mOffset!!
    }

}