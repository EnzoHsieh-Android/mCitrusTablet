package com.citrus.mCitrusTablet.view.report

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.viewModels
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.databinding.FragmentMonthlyBinding
import com.citrus.mCitrusTablet.model.vo.Report
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber


@AndroidEntryPoint
class MonthlyFragment : Fragment(R.layout.fragment_monthly) {

    private val reportViewModel: ReportViewModel by viewModels()
    private var _binding: FragmentMonthlyBinding? = null
    private val binding get() = _binding!!
    private var resReportList = mutableListOf<Report>()
    private var titleEntity = mutableListOf<String>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMonthlyBinding.bind(view)

        binding.apply {
            initObserver()
            initView()
        }
        reportViewModel.fetchAllData("2021/03/1","2021/03/31")
    }

    private fun initObserver() {
        reportViewModel.resReportTitleData.observe(viewLifecycleOwner, { titleList ->
            titleEntity = titleList
            Timber.d(titleEntity.toString())
        })

        reportViewModel.resReportData.observe(viewLifecycleOwner, { resDataList ->
            resReportList = resDataList
            Timber.d(resReportList.toString())
            drawBarChart()
        })
    }

    private fun drawBarChart() {
        val textSp = resources.getDimensionPixelSize(R.dimen.sp_16)
        val valueSp = resources.getDimensionPixelSize(R.dimen.sp_4)
        val chart = binding.stackedBarChart
        chart.xAxis.apply {
            textColor = getResourceColor(R.color.primaryColor)
            valueFormatter = IndexAxisValueFormatter(titleEntity)
            labelCount = titleEntity.size
            position = XAxis.XAxisPosition.BOTTOM_INSIDE
            setDrawLabels(true)
            setDrawGridLines(false)
        }


        chart.apply {
            setDrawValueAboveBar(true)
            description.isEnabled = false
            isClickable = true
            legend.isEnabled = true
            legend.textSize = textSp.toFloat()
            axisRight.isEnabled = false
            axisLeft.textSize = textSp.toFloat()
            setScaleEnabled(false)
        }

        chart.data = BarData(getBarData()).apply {
            setValueFormatter { value, _, _, _ ->
                value.toInt().toString()
            }
            setValueTextSize(valueSp.toFloat())
        }

        chart.invalidate()
    }

    private fun getBarData(): ArrayList<IBarDataSet> {
        var entries = mutableListOf<BarEntry>()

        for (i in 0 until resReportList.size) {
            entries.add(
                BarEntry(
                    i.toFloat(),
                    floatArrayOf(
                        resReportList[i].cancel.toFloat(),
                        resReportList[i].wait.toFloat(),
                        resReportList[i].check.toFloat(),
                    )
                )
            )
        }

        val dataSet = BarDataSet(entries, "").apply {
            valueFormatter = IValueFormatter { value, _, _, _ ->
                value.toInt().toString()
            }

            colors = getChartColors()
            stackLabels = getCusStackLabels()
        }

        val bars = ArrayList<IBarDataSet>()
        bars.add(dataSet)

        return bars
    }

    private fun initView() {
        // drawBarChart()
    }

    private fun getCusStackLabels(): Array<String>? {
        return arrayOf(
            getString(R.string.cancel),
            getString(R.string.Waiting),
            getString(R.string.check)
        )
    }

    private fun getChartColors(): MutableList<Int> {
        return mutableListOf(
            getResourceColor(R.color.chart_color_cancel),
            getResourceColor(R.color.chart_color_wait),
            getResourceColor(R.color.chart_color_check)
        )
    }

    private fun getResourceColor(resID: Int): Int {
        return resources.getColor(resID)
    }



}