package com.citrus.mCitrusTablet.view.report

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.databinding.FragmentReportBinding
import com.citrus.mCitrusTablet.view.reservation.ReservationViewModel
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ReportFragment : Fragment(R.layout.fragment_report) {
    private val reportViewModel: ReportViewModel by viewModels()
    private var _binding: FragmentReportBinding? = null
    private val binding get() = _binding!!
    private lateinit var titles:Array<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentReportBinding.bind(view)

        reportViewModel.fetchAllData("2021/03/22","2021/03/28")

        binding.apply {
            initView()
        }
    }

    private fun initView() {
        binding.viewPager.adapter = CollectionAdapter(this)

        TabLayoutMediator(binding.tabLayout, binding.viewPager
        ) { tab: TabLayout.Tab, position: Int ->
            titles = arrayOf("單日統計","週統計","月統計")
            tab.text = titles[position]
        }.attach()
    }


    inner class CollectionAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {

        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            // Return a NEW fragment instance in createFragment(int)
            when (position) {
                0 -> return ResChildFragment()
                1 -> return WaitChildFragment()
            }
            return ResChildFragment()
        }
    }

}