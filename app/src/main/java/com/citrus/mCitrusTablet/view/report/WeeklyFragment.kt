package com.citrus.mCitrusTablet.view.report

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.databinding.FragmentWeeklyBinding
import com.citrus.mCitrusTablet.model.vo.Report
import com.citrus.mCitrusTablet.util.MyMarkView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class WeeklyFragment : Fragment(R.layout.fragment_weekly) {

    private val reportViewModel: ReportViewModel by activityViewModels()
    private var _binding: FragmentWeeklyBinding? = null
    private val binding get() = _binding!!
    private var resReportList = mutableListOf<Report>()
    private var titleEntity = mutableListOf<String>()

    companion object {
        fun newInstance(): WeeklyFragment {
            return WeeklyFragment()
        }
    }

    override fun onResume() {
        reportViewModel.setLocationPageType(ReportRange.BY_WEEKLY)
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWeeklyBinding.bind(view)

        binding.apply {
            initObserver()
            initView()
        }

    }

    private fun initObserver() {

        reportViewModel.weeklyReportTitleData.observe(viewLifecycleOwner, { titleList ->
            titleEntity = titleList
        })

        reportViewModel.weeklyReportData.observe(viewLifecycleOwner, { resDataList ->

            var totalNum = 0
            var checkNum = 0
            var adultNum = 0
            var childNum = 0
            var cancelNum = 0
            var notCheckNum = 0

            for(item in resDataList){
                totalNum += item.total
                checkNum += item.check
                adultNum += item.adult
                childNum += item.child
                cancelNum += item.cancel
                notCheckNum += item.wait
            }


            binding.TvTotalNum.text = totalNum.toString() + "組"
            binding.TvCheckNum.text = checkNum.toString() + "組"
            binding.TvAdultNum.text = adultNum.toString() + "人"
            binding.TvChildNum.text = childNum.toString() + "人"
            binding.TvCancelNum.text = cancelNum.toString() + "組"
            binding.TvUnCheckNum.text = notCheckNum.toString() + "組"

            resReportList = resDataList
            binding.stackedBarChart.clear()
            drawBarChart()
        })

        reportViewModel.locationPageType.observe(viewLifecycleOwner,{
            reportViewModel.reFetch()
        })

        reportViewModel.showType.observe(viewLifecycleOwner,{ showType ->
            when(showType){
                ShowType.BY_CHART -> {
                    binding.stackedBarChart.visibility = View.VISIBLE
                    binding.showTextBlock.visibility = View.INVISIBLE
                }
                ShowType.BY_TEXT -> {
                    binding.stackedBarChart.visibility = View.INVISIBLE
                    binding.showTextBlock.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun drawBarChart() {
        val textSp = resources.getDimensionPixelSize(R.dimen.sp_12)
        val valueSp = resources.getDimensionPixelSize(R.dimen.sp_4)
        val chart = binding.stackedBarChart
        chart.xAxis.apply {
            textSize  = textSp.toFloat()
            textColor = getResourceColor(R.color.primaryColor)
            valueFormatter = IndexAxisValueFormatter(titleEntity)
            labelCount = titleEntity.size
            position = XAxis.XAxisPosition.BOTTOM_INSIDE
            setDrawLabels(true)
            setDrawGridLines(false)
        }


        chart.apply {
            setDrawValueAboveBar(false)
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
                var drawValue = ""
                if(value>0.0){
                    drawValue = value.toInt().toString()
                }

                drawValue
            }
            setValueTextSize(valueSp.toFloat())
            setDrawValues(false)
        }
        var axisValueFormatter =  chart.xAxis.valueFormatter

        val myMarkView = MyMarkView(requireContext(),resReportList,axisValueFormatter)
        myMarkView.chartView = chart
        chart.marker = myMarkView

        chart.notifyDataSetChanged()
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}