package com.citrus.mCitrusTablet.view.report

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.databinding.FragmentMonthlyBinding
import com.citrus.mCitrusTablet.di.prefs
import com.citrus.mCitrusTablet.model.vo.Report
import com.citrus.mCitrusTablet.util.MyMarkView
import com.citrus.mCitrusTablet.view.adapter.ReportAdapter
import com.github.mikephil.charting.animation.Easing
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

    private val reportViewModel: ReportViewModel by activityViewModels()
    private var _binding: FragmentMonthlyBinding? = null
    private val binding get() = _binding!!
    private var resReportList = mutableListOf<Report>()
    private var titleEntity = mutableListOf<String>()
    private val reportAdapter by lazy {
        ReportAdapter(requireContext(),mutableListOf(), prefs.reportTypePos)
    }

    companion object {
        fun newInstance(): MonthlyFragment {
            return MonthlyFragment()
        }
    }

    override fun onResume() {
        reportViewModel.setLocationPageType(ReportRange.BY_MONTHLY)
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentMonthlyBinding.bind(view)
        initObserver()
        initView()

    }

    private fun initObserver() {

        reportViewModel.chartTypeChange.observe(viewLifecycleOwner,{ pos ->
            if(pos == 0){
                binding.stackedBarChart.visibility = View.VISIBLE
                binding.TextBlock.visibility = View.INVISIBLE
            }else{
                binding.stackedBarChart.visibility = View.INVISIBLE
                binding.TextBlock.visibility = View.VISIBLE
            }
        })


        reportViewModel.monthlyDetailReportData.observe(viewLifecycleOwner,{ originalList ->
            reportAdapter.setList(originalList,prefs.reportTypePos)
        })

        reportViewModel.monthlyReportTitleData.observe(viewLifecycleOwner, { titleList ->
            titleEntity = titleList
        })

        reportViewModel.monthlyReportData.observe(viewLifecycleOwner, { resDataList ->
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


    }

    private fun drawBarChart() {
        val textSp = resources.getDimensionPixelSize(R.dimen.sp_16)
        val formSize = resources.getDimensionPixelSize(R.dimen.sp_6)
        val valueSp = resources.getDimensionPixelSize(R.dimen.sp_3)
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

            /** legend- 底下色塊描述設定*/
            legend.isEnabled = true
            legend.textSize = textSp.toFloat()
            legend.formSize = formSize.toFloat()

            axisRight.isEnabled = false
            axisLeft.textSize = textSp.toFloat()
            setScaleEnabled(false)
        }

        chart.data = BarData(getBarData()).apply {
            /** 圖內value*/
            setValueFormatter { value, _, _, _ ->
                value.toInt().toString()
            }
            setDrawValues(false)
        }
        /** X軸標籤Rotation*/
        chart.xAxis.labelRotationAngle = 70f
        chart.xAxis.textSize = valueSp.toFloat()

        /** 點擊呈現MarkView相關*/
        var axisValueFormatter =  chart.xAxis.valueFormatter
        val myMarkView = MyMarkView(requireContext(),resReportList,axisValueFormatter,ReportRange.BY_MONTHLY)
        myMarkView.chartView = chart
        chart.marker = myMarkView



        chart.animateXY(0, 500)
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
        binding.apply {
            rvReport.apply {
                adapter = reportAdapter
                addItemDecoration(
                    DividerItemDecoration(
                        this.context,
                        DividerItemDecoration.VERTICAL
                    )
                )
                layoutManager = LinearLayoutManager(requireContext())
            }

            if(prefs.chartTypePos == 1){
                stackedBarChart.visibility = View.INVISIBLE
                TextBlock.visibility = View.VISIBLE
            }else{
                stackedBarChart.visibility = View.VISIBLE
                TextBlock.visibility = View.INVISIBLE
            }
        }
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
        _binding?.rvReport?.adapter = null
        _binding = null
    }

}