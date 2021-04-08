package com.citrus.mCitrusTablet.view.report

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.databinding.FragmentDailyBinding
import com.citrus.mCitrusTablet.model.vo.Report
import com.citrus.mCitrusTablet.model.vo.ReservationGuests
import com.citrus.mCitrusTablet.model.vo.Wait
import com.citrus.mCitrusTablet.util.Constants
import com.citrus.mCitrusTablet.view.adapter.ReportAdapter
import com.citrus.mCitrusTablet.view.adapter.WaitAdapter
import com.citrus.mCitrusTablet.view.dialog.CustomOrderDeliveryDialog


class DailyFragment : Fragment(R.layout.fragment_daily) {
    private val reportViewModel: ReportViewModel by activityViewModels()
    private var _binding: FragmentDailyBinding? = null
    private val binding get() = _binding!!
    private var reportType:ReportType = ReportType.RESERVATION
    private val reportAdapter by lazy {
        ReportAdapter(mutableListOf(), reportType)
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

        reportViewModel.reportType.reObserve(viewLifecycleOwner,{ reportType ->
            this.reportType = reportType
        })


        reportViewModel.dailyDetailReportData.reObserve(viewLifecycleOwner,{ originalList ->
            reportAdapter.setList(originalList,reportType)
        })

        reportViewModel.dailyReportData.reObserve(viewLifecycleOwner, { resDataList ->
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

        reportViewModel.locationPageType.reObserve(viewLifecycleOwner, {
            Log.e("fragment hash code",this.hashCode().toString())
            reportViewModel.reFetch()
        })
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inline fun <T> LiveData<T>.reObserve(owner: LifecycleOwner, crossinline func: (T) -> (Unit)) {
        removeObservers(owner)
        observe(owner, { t -> func(t) })
    }
}