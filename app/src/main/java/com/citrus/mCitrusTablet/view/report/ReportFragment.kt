package com.citrus.mCitrusTablet.view.report

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.databinding.FragmentReportBinding
import com.citrus.mCitrusTablet.util.Constants
import com.citrus.mCitrusTablet.view.dialog.CalendarType
import com.citrus.mCitrusTablet.view.dialog.CustomDatePickerDialog
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.savvi.rangedatepicker.CalendarPickerView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReportFragment : Fragment(R.layout.fragment_report) {
    private val reportViewModel: ReportViewModel by activityViewModels()
    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!
    private lateinit var titles: Array<String>
    private lateinit var collectionAdapter:CollectionAdapter

    override fun onResume() {
        super.onResume()
        val type = resources.getStringArray(R.array.reportType)
        val showType = resources.getStringArray(R.array.showType)
        val typeArrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, type)
        val showTypeArrayAdapter = ArrayAdapter(requireContext(), R.layout.dropdown_item, showType)

        binding.reportType.setAdapter(typeArrayAdapter)
        binding.showType.setAdapter(showTypeArrayAdapter)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentReportBinding.bind(view)
        initView()
        initObserver()
    }

    private fun initObserver() {
        reportViewModel.locationPageType.observe(viewLifecycleOwner, { reportRange ->
            when (reportRange) {
                ReportRange.BY_DAILY -> {
                    binding.showTypeBlock.visibility = View.GONE
                }
                else -> {
                    binding.showTypeBlock.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun initView() {
        collectionAdapter = CollectionAdapter(this)

        binding.apply {
            binding.reportTitle.text = resources.getString(R.string.resReport)
            binding.viewPager.offscreenPageLimit = 1
            binding.viewPager.adapter = collectionAdapter

            TabLayoutMediator(
                binding.tabLayout, binding.viewPager
            ) { tab: TabLayout.Tab, position: Int ->
                titles = arrayOf("單日統計", "週統計", "月統計")
                tab.text = titles[position]
            }.attach()

            val typeArray = resources.getStringArray(R.array.reportType)
            val showTypeArray = resources.getStringArray(R.array.showType)
            reportType.setText(typeArray[0], false)
            showType.setText(showTypeArray[0], false)
            time.setText(Constants.defaultTimeStr, false)
            reportViewModel.fetchReportData(Constants.defaultTimeStr, Constants.defaultTimeStr)


            reportType.setOnItemClickListener { _, _, position, _ ->
                when (position) {
                    0 -> {
                        binding.reportTitle.text = resources.getString(R.string.resReport)
                        reportViewModel.setReportType(ReportType.RESERVATION)
                    }
                    1 -> {
                        binding.reportTitle.text = resources.getString(R.string.waitReport)
                        reportViewModel.setReportType(ReportType.WAIT)
                    }
                }
            }


            showType.setOnItemClickListener { _, _, position, _ ->
                when (position) {
                    0 -> {
                        showTypeTextInputLayout.startIconDrawable =
                            resources.getDrawable(R.drawable.ic_baseline_stacked_bar_chart_24,null)
                        reportViewModel.setShowType(ShowType.BY_CHART)
                    }
                    1 -> {
                        showTypeTextInputLayout.startIconDrawable =
                            resources.getDrawable(R.drawable.ic_baseline_text_format_24,null)
                        reportViewModel.setShowType(ShowType.BY_TEXT)
                    }
                }
            }

            time.setOnFocusChangeListener { _, hasFocus ->
                if (hasFocus) {
                    activity?.let {
                        CustomDatePickerDialog(
                            it,
                            CalendarType.NeedHistoryForDate,
                            CalendarPickerView.SelectionMode.SINGLE,
                            Constants.defaultTimeStr,
                            ""
                        ) { _, startTime, _, _, _, _ ->
                            time.setText(startTime)
                            reportViewModel.setTime(startTime)
                        }.show(it.supportFragmentManager, "CustomDatePickerDialog")
                    }
                }
                time.clearFocus()
            }


        }
    }


    inner class CollectionAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {


        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> DailyFragment.newInstance()
                1 -> WeeklyFragment.newInstance()
                2 -> MonthlyFragment.newInstance()
                else -> DailyFragment.newInstance()
            }
        }

    }


    override fun onDestroyView() {
        reportViewModel.setReportType(ReportType.RESERVATION)
        reportViewModel.setTime(Constants.defaultTimeStr)
        _binding = null
        super.onDestroyView()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}