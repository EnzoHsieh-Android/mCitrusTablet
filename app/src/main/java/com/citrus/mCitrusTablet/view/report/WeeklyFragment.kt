package com.citrus.mCitrusTablet.view.report

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.databinding.FragmentWeeklyBinding
import com.citrus.mCitrusTablet.di.prefs
import com.citrus.mCitrusTablet.model.vo.Report
import com.citrus.mCitrusTablet.util.MyMarkView
import com.citrus.mCitrusTablet.view.adapter.ReportAdapter
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_report.*


@AndroidEntryPoint
class WeeklyFragment : Fragment(R.layout.fragment_weekly) {

    private val reportViewModel: ReportViewModel by activityViewModels()
    private var _binding: FragmentWeeklyBinding? = null
    private val binding get() = _binding!!
    private var resReportList = mutableListOf<Report>()
    private var titleEntity = mutableListOf<String>()
    private val reportAdapter by lazy {
        ReportAdapter(requireContext(), mutableListOf())
    }

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


        initObserver()
        initView()


    }

    private fun initObserver() {

        reportViewModel.chartTypeChange.observe(viewLifecycleOwner, { pos ->
            if (pos == 0) {
                binding.stackedBarChart.visibility = View.VISIBLE
                binding.TextBlock.visibility = View.INVISIBLE
            } else {
                binding.stackedBarChart.visibility = View.INVISIBLE
                binding.TextBlock.visibility = View.VISIBLE
            }
        })


        reportViewModel.weeklyReportTitleData.observe(viewLifecycleOwner, { titleList ->
            titleEntity = titleList
        })

        reportViewModel.weeklyDetailReportData.observe(viewLifecycleOwner, { originalList ->
            reportAdapter.updateList(originalList)
        })

        reportViewModel.weeklyReportData.observe(viewLifecycleOwner, { resDataList ->

            var totalNum = 0
            var checkNum = 0
            var adultNum = 0
            var childNum = 0
            var cancelNum = 0
            var notCheckNum = 0
            var waitTime = 0


            for (item in resDataList) {
                totalNum += item.total
                checkNum += item.check
                adultNum += item.adult
                childNum += item.child
                cancelNum += item.cancel
                notCheckNum += item.wait

                if(item.waitTime != 0){
                    waitTime += item.waitTime
                }

            }

            binding.TvCheckNum.text = checkNum.toString() + "組"
            binding.TvAdultNum.text = adultNum.toString() + "人"
            binding.TvChildNum.text = childNum.toString() + "人"
            binding.TvCancelNum.text = cancelNum.toString() + "組"
            binding.TvUnCheckNum.text = notCheckNum.toString() + "組"


            if (waitTime != 0) {
                binding.waitTimeBlock.visibility = View.VISIBLE
                binding.TvWaitTime.text = (waitTime / checkNum).toString() + " 分"
            } else {
                binding.waitTimeBlock.visibility = View.GONE
            }

            resReportList = resDataList
            binding.stackedBarChart.clear()
            drawBarChart()
            drawPieChart(totalNum, checkNum, notCheckNum, cancelNum)
        })

    }

    private fun drawPieChart(totalNum: Int, checkNum: Int, notCheckNum: Int, cancelNum: Int) {
        val strings: MutableList<PieEntry> = ArrayList()
        strings.add(
            PieEntry(
                checkNum.toFloat(),
                if (checkNum != 0) resources.getString(R.string.checked) else ""
            )
        )
        strings.add(
            PieEntry(
                notCheckNum.toFloat(),
                if (notCheckNum != 0) resources.getString(R.string.Waiting) else ""
            )
        )
        strings.add(
            PieEntry(
                cancelNum.toFloat(),
                if (cancelNum != 0) resources.getString(R.string.delete) else ""
            )
        )

        val dataSet = PieDataSet(strings, "")
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f

        val colors = ArrayList<Int>()
        colors.add(resources.getColor(R.color.chart_color_check))
        colors.add(resources.getColor(R.color.chart_color_wait))
        colors.add(resources.getColor(R.color.chart_color_cancel))
        dataSet.colors = colors


        dataSet.valueLinePart1OffsetPercentage = 80f
        dataSet.valueLinePart1Length = 0.2f
        dataSet.valueLinePart2Length = 0.4f

        dataSet.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE


        var tf = Typeface.createFromAsset(resources.assets, "OpenSans-Regular.ttf")
        val pieData = PieData(dataSet)
        pieData.setValueTextColor(Color.BLACK)
        pieData.setValueTypeface(tf)

        val textSp = resources.getDimensionPixelSize(R.dimen.sp_6)
        val labelSp = resources.getDimensionPixelSize(R.dimen.sp_4)
        val formSize = resources.getDimensionPixelSize(R.dimen.sp_4)

        binding.picChart.data = pieData
        val description = Description()
        description.text=""
        binding.picChart.setExtraOffsets(20f, 0f, 20f, 0f)
        binding.picChart.dragDecelerationFrictionCoef = 0.95f

        binding.picChart.isDrawHoleEnabled = true
        binding.picChart.setHoleColor(Color.WHITE)

        binding.picChart.setTransparentCircleColor(Color.WHITE)
        binding.picChart.setTransparentCircleAlpha(110)

        binding.picChart.holeRadius = 58f
        binding.picChart.transparentCircleRadius = 61f

        binding.picChart.description = description
        binding.picChart.setDrawCenterText(true)
        binding.picChart.setEntryLabelTextSize(labelSp.toFloat())
        binding.picChart.setEntryLabelColor(Color.BLACK)
        binding.picChart.data.setValueTextSize(textSp.toFloat())

        binding.picChart.data.setValueFormatter { value, entry, dataSetIndex, viewPortHandler ->
            var drawValue = "0"
            if(value>0.0){
                drawValue = value.toInt().toString()
            }

            drawValue
        }


        binding.picChart.setCenterTextTypeface(
            Typeface.createFromAsset(
                resources.assets,
                "OpenSans-Regular.ttf"
            )
        )
        binding.picChart.centerText = "總計：$totalNum"
        binding.picChart.setCenterTextSize(textSp.toFloat())

        val l: Legend = binding.picChart.legend
        l.isEnabled = false

        binding.picChart.animateY(500, Easing.EaseInOutQuad)
        // undo all highlights
        binding.picChart.highlightValues(null)

        binding.picChart.invalidate()

    }

    private fun drawBarChart() {
        val textSp = resources.getDimensionPixelSize(R.dimen.sp_12)
        val formSize = resources.getDimensionPixelSize(R.dimen.sp_6)
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
            legend.formSize = formSize.toFloat()
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

        val myMarkView = MyMarkView(
            requireContext(),
            resReportList,
            axisValueFormatter,
            ReportRange.BY_WEEKLY
        )
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
                binding.stackedBarChart.visibility = View.INVISIBLE
                binding.TextBlock.visibility = View.VISIBLE
            }else{
                binding.stackedBarChart.visibility = View.VISIBLE
                binding.TextBlock.visibility = View.INVISIBLE
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