package com.citrus.mCitrusTablet.view.setting

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import com.citrus.mCitrusTablet.BuildConfig
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.databinding.FragmentSettingBinding
import com.citrus.mCitrusTablet.di.prefs
import com.citrus.mCitrusTablet.view.SharedViewModel
import com.daimajia.androidanimations.library.Techniques
import com.daimajia.androidanimations.library.YoYo
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SettingFragment : DialogFragment(R.layout.fragment_setting) {
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private val requestStorageCode = 888
    var isLanChange = false
    var hasChange = true
    var chooseLan = prefs.languagePos


    override fun onResume() {
        super.onResume()
        val languages = resources.getStringArray(R.array.language)
        val lanArrayAdapter = ArrayAdapter(requireContext(),R.layout.dropdown_item,languages)

        val printLan = resources.getStringArray(R.array.printLan)
        val printArrayAdapter = ArrayAdapter(requireContext(),R.layout.dropdown_item,printLan)
        binding.languagePicker.setAdapter(lanArrayAdapter)
        binding.printLan.setAdapter(printArrayAdapter)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (dialog != null && dialog?.window != null) {
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentSettingBinding.bind(view)
        loadFromSharedPref()
        isCancelable = false



        binding.apply {
            tvVersion.text = "2021 © Citrus Solutions Co., Ltd. Version " + BuildConfig.VERSION_NAME
            tvVersion.setOnClickListener {
                val permissionCheck =
                    activity?.let { it1 ->
                        ContextCompat.checkSelfPermission(
                            it1,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    }

                if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                    activity?.let { it1 ->
                        ActivityCompat.requestPermissions(
                            it1,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            requestStorageCode
                        )
                    }
                } else {
                    sharedViewModel.updateDialog()
                }
            }

            binding.btnOK.setOnClickListener {
                if (applyChangesToSharedPref()) {
                    if (hasChange) {
                        sharedViewModel.hasSetLanguage()
                        prefs.storeName = ""
                    }
                    dismiss()
                }
            }


            languagePicker.setOnItemClickListener { _, _, position, _ ->
                var lang = -1
                isLanChange = true
                when (position) {
                    0 -> {
                        lang = 0
                    }
                    1 -> {
                        lang = 1
                    }
                    2 -> {
                        lang = 2
                    }
                }
                chooseLan = lang
            }


        }
    }


    private fun loadFromSharedPref() {
        val storeId = prefs.storeId
        val server = prefs.severDomain
        val printerIP = prefs.printerIP
        val printerPort = prefs.printerPort
        val languages = resources.getStringArray(R.array.language)


        binding.printLan.setText(prefs.charSet,false)

        if(prefs.languagePos != -1){
            binding.languagePicker.setText(languages[prefs.languagePos],false)
        }else{
            var lanStr = resources.configuration.locale.country
            var index = when(lanStr){
                "CN" -> {
                    0
                }
                "TW" -> {
                    1
                }
                else -> {
                    2
                }
            }
            binding.languagePicker.setText(languages[index],false)
        }



        if (storeId == "" || server == "") {
            isCancelable = false
        }

        binding.etStoreId!!.setText(storeId,false)
        binding.etServerIp!!.setText(server,false)
        binding.printIp!!.setText(printerIP,false)
        binding.printPort!!.setText(printerPort,false)
    }

    private fun applyChangesToSharedPref(): Boolean {

        val storeIdText = binding.etStoreId!!.text.trim().toString()
        val serverText = binding.etServerIp!!.text.trim().toString()
        val printLan = binding.printLan!!.text.trim().toString()
        val printIp = binding.printIp!!.text.trim().toString()
        val printPort = binding.printPort!!.text.trim().toString()

        if (storeIdText.isEmpty()) {
            YoYo.with(Techniques.Shake).duration(1000).playOn(binding.storeIdInputLayout)
            YoYo.with(Techniques.Shake).duration(1000).playOn(binding.etStoreId)
            return false
        }
        if (serverText.isEmpty()) {
            YoYo.with(Techniques.Shake).duration(1000).playOn(binding.serverIpTextInputLayout)
            YoYo.with(Techniques.Shake).duration(1000).playOn(binding.etServerIp)
            return false
        }

        hasChange =
            !(storeIdText == prefs.storeId && serverText == prefs.severDomain && chooseLan == prefs.languagePos)

        prefs.storeId = storeIdText
        prefs.severDomain = serverText
        prefs.languagePos = chooseLan
        prefs.charSet = printLan

        if(printIp.isNotEmpty()){
            prefs.printerIP = printIp
        }

        if(printPort.isNotEmpty()){
            prefs.printerPort = printPort
        }

        return true
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    override fun dismiss() {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(requireActivity().window.decorView.windowToken, 0)
        super.dismiss()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return object : Dialog(requireContext(), theme) {
            override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
                if (ev.action == MotionEvent.ACTION_DOWN) {
                    val v = currentFocus
                    if (v is EditText) {
                        val outRect = Rect()
                        v.getGlobalVisibleRect(outRect)
                        if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                            v.clearFocus()
                            val imm =
                                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            imm.hideSoftInputFromWindow(v.windowToken, 0)
                        }
                    }
                }
                return super.dispatchTouchEvent(ev)
            }
        }
    }
}

