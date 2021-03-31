package com.citrus.mCitrusTablet.view.report

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.databinding.FragmentWaitChildBinding


class WaitChildFragment : Fragment(R.layout.fragment_wait_child) {

    private var _binding: FragmentWaitChildBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentWaitChildBinding.bind(view)

        binding.apply {
            initView()
        }

    }

    private fun initView() {


    }



}