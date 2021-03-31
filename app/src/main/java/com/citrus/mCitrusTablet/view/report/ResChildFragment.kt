package com.citrus.mCitrusTablet.view.report

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.databinding.FragmentResChildBinding


class ResChildFragment : Fragment(R.layout.fragment_res_child) {

    private var _binding: FragmentResChildBinding? = null
    private val binding get() = _binding!!

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentResChildBinding.bind(view)

        binding.apply {
            initView()
        }

    }

    private fun initView() {


    }

}