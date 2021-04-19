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
import com.citrus.mCitrusTablet.databinding.FragmentDailyBinding
import com.citrus.mCitrusTablet.model.vo.Report
import com.citrus.mCitrusTablet.view.adapter.ReportAdapter
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry


class DailyFragment : Fragment(R.layout.fragment_daily) {
    private val reportViewModel: ReportViewModel by activityViewModels()
    private var _binding: FragmentDailyBinding? = null
    private val binding get() = _binding!!
    private val reportAdapter by lazy {
        ReportAdapter(requireContext(), mutableListOf())
    }


    companion object {
        fun newInstance(): DailyFragment {
            return DailyFragment()
        }
    }


    override fun onResume() {
        reportViewModel.setLocationPageType(ReportRange.BY_DAILY)
        super.onResume()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentDailyBinding.bind(view)
        initView()
        initObserver()
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


        }
    }

    private fun initObserver() {


        reportViewModel.dailyDetailReportData.observe(viewLifecycleOwner, { originalList ->
            reportAdapter.updateList(originalList)
        })

        reportViewModel.dailyReportData.observe(viewLifecycleOwner, { resDataList ->
            var guestsData = Report(0, 0, 0, 0, 0, 0, "", 0)
            if (resDataList.isNotEmpty()) {
                guestsData = resDataList[0]
                drawPieChart(guestsData)
            } else {
                binding.picChart.clear()
            }

            binding.TvCheckNum.text = " "+guestsData.check.toString() + resources.getString(R.string.report_group)
            binding.TvAdultNum.text = " "+guestsData.adult.toString() + resources.getString(R.string.report_person)
            binding.TvChildNum.text = " "+guestsData.child.toString() + resources.getString(R.string.report_person)
            binding.TvCancelNum.text = " "+guestsData.cancel.toString() + resources.getString(R.string.report_group)
            binding.TvUnCheckNum.text = " "+guestsData.wait.toString() + resources.getString(R.string.report_group)

            if (guestsData.waitTime != 0) {
                binding.waitTimeBlock.visibility = View.VISIBLE
                binding.TvWaitTime.text = " "+(guestsData.waitTime / guestsData.check).toString() +" "+resources.getString(R.string.report_min)
            } else {
                binding.waitTimeBlock.visibility = View.GONE
            }
        })


    }

    private fun drawPieChart(guestsData: Report) {
        val strings: MutableList<PieEntry> = ArrayList()
        strings.add(
            PieEntry(
                guestsData.check.toFloat(), if (guestsData.check != 0) resources.getString(
                    R.string.checked
                ) else ""
            )
        )
        strings.add(
            PieEntry(
                guestsData.wait.toFloat(),
                if (guestsData.wait != 0) resources.getString(R.string.chart_wait) else ""
            )
        )
        strings.add(
            PieEntry(
                guestsData.cancel.toFloat(), if (guestsData.cancel != 0) resources.getString(
                    R.string.delete
                ) else ""
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
        binding.picChart.centerText = resources.getString(R.string.TotalForTheDay)+ guestsData.total
        binding.picChart.setCenterTextSize(textSp.toFloat())

        val l: Legend = binding.picChart.legend
        l.isEnabled = false

        binding.picChart.animateY(500, Easing.EaseInOutQuad)

        // undo all highlights
        binding.picChart.highlightValues(null)

        binding.picChart.invalidate()



    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding?.rvReport?.adapter = null
        _binding = null
    }

//    private inline fun <T> LiveData<T>.reObserve(owner: LifecycleOwner, crossinline func: (T) -> (Unit)) {
//        removeObservers(owner)
//        observe(owner, { t -> func(t) })
//    }
}