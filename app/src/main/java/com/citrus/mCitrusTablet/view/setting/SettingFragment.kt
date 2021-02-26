package com.citrus.mCitrusTablet.view.setting

import android.Manifest
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.citrus.mCitrusTablet.BuildConfig
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.databinding.FragmentReservationBinding
import com.citrus.mCitrusTablet.databinding.FragmentSettingBinding
import com.citrus.mCitrusTablet.di.prefs
import com.citrus.mCitrusTablet.util.Constants.KEY_LANGUAGE
import com.citrus.mCitrusTablet.util.Constants.KEY_RSNO
import com.citrus.mCitrusTablet.util.Constants.KEY_SERVER_DOMAIN
import com.citrus.mCitrusTablet.util.ui.CustomNumberPickerDialog
import com.citrus.mCitrusTablet.util.ui.timeRangePicker.TimeRangePickerBottomSheet
import com.citrus.mCitrusTablet.view.SharedViewModel
import com.dylanc.viewbinding.bindView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingFragment : DialogFragment(R.layout.fragment_setting) {
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private val RequestStorageCode = 888
    var data = arrayOf("繁體中文", "English")
    var isLanChange = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingBinding.bind(view)
        loadFromSharedPref()

        binding.apply {
          tvVersion.text = "2021 © Citrus Solutions Co., Ltd. Version " + BuildConfig.VERSION_NAME
            tvVersion.setOnClickListener {
                val permissionCheck =
                    activity?.let { it1 -> ContextCompat.checkSelfPermission(it1, Manifest.permission.WRITE_EXTERNAL_STORAGE) }

                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    activity?.let { it1 ->
                        ActivityCompat.requestPermissions(
                            it1,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            RequestStorageCode
                        )
                    }
                } else {
                    sharedViewModel.updateDialog()
                }
            }

            binding.btnOK.setOnClickListener {
                if(applyChangesToSharedPref()){
                    sharedViewModel.hasSetLanguage()
                    dismiss()
                }
            }


            languagePicker?.minValue = 1
            languagePicker?.maxValue = data.size
            languagePicker?.displayedValues = data


            languagePicker!!.setOnValueChangedListener { _, _, newVal ->
                var lang = -1
                isLanChange = true
                when(newVal){
                    1 -> {
                        lang = 1
                    }
                    2 -> {
                        lang = 0
                    }
                }
               prefs.languagePos = lang
            }




        }




    }


    private fun loadFromSharedPref() {
        val RSNO = prefs.rsno
        val server =  prefs.severDomain


        when(prefs.languagePos){
            1 -> {
                binding.languagePicker?.value = 1
            }
            0 -> {
                binding.languagePicker?.value = 2
            }
            else ->{
              var lanStr =   resources.configuration.locale.language
                if(lanStr == "zh"){
                    binding.languagePicker?.value = 1
                }else{
                    binding.languagePicker?.value = 2
                }
            }
        }

        if(RSNO==""||server==""){
            isCancelable = false
        }
        binding.etRsno!!.setText(RSNO)
        binding.etServerIp!!.setText(server)
    }

    private fun applyChangesToSharedPref(): Boolean {
        val rsnoText = binding.etRsno!!.text.trim().toString()
        val serverText = binding.etServerIp!!.text.trim().toString()
        if(rsnoText.isEmpty() || serverText.isEmpty() ) {
            return false
        }

        prefs.rsno = rsnoText
        prefs.severDomain = serverText

        return true
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}