package com.citrus.mCitrusTablet.view.report

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.databinding.FragmentDailyBinding
import com.citrus.mCitrusTablet.model.vo.Report
import com.citrus.mCitrusTablet.model.vo.ReservationGuests
import com.citrus.mCitrusTablet.model.vo.Wait


class DailyFragment : Fragment(R.layout.fragment_daily) {
    private val reportViewModel: ReportViewModel by activityViewModels()
    private var _binding: FragmentDailyBinding? = null
    private val binding get() = _binding!!
    private var reportType:ReportType = ReportType.RESERVATION


    override fun onResume() {
        reportViewModel.setLocationPageType(ReportRange.BY_DAILY)
        reportViewModel.reFetch()
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



        }
    }

    private fun initObserver() {

        reportViewModel.reportType.observe(viewLifecycleOwner,{ reportType ->
            this.reportType = reportType
        })


        reportViewModel.dailyDetailReportData.observe(viewLifecycleOwner,{ originalList ->
            when(reportType){
                ReportType.RESERVATION -> {
                    originalList as MutableList<ReservationGuests>
                }

                ReportType.WAIT -> {
                    originalList as MutableList<Wait>
                }
            }
        })

        reportViewModel.dailyReportData.observe(viewLifecycleOwner, { resDataList ->
            var guestsData = Report(0, 0, 0, 0, 0, 0, "")
            if(resDataList.isNotEmpty()) {
                 guestsData = resDataList[0]
            }
                binding.TvTotalNum.text = guestsData.total.toString() + "組"
                binding.TvCheckNum.text = guestsData.check.toString() + "組"
                binding.TvAdultNum.text = guestsData.adult.toString() + "人"
                binding.TvChildNum.text = guestsData.child.toString() + "人"
                binding.TvCancelNum.text = guestsData.cancel.toString() + "組"
                binding.TvUnCheckNum.text = guestsData.wait.toString() + "組"

        })

        reportViewModel.locationPageType.observe(viewLifecycleOwner, {
            reportViewModel.reFetch()
        })
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}