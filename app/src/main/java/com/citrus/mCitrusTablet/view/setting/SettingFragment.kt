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
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class SettingFragment : DialogFragment(R.layout.fragment_setting) {
    private val sharedViewModel: SharedViewModel by activityViewModels()
    private var _binding: FragmentSettingBinding? = null
    private val binding get() = _binding!!
    private val requestStorageCode = 888
    var data = arrayOf("繁體中文", "English")
    var isLanChange = false
    var hasChange = true
    var chooseLan = prefs.languagePos


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
                    }
                    dismiss()
                }
            }


            languagePicker?.minValue = 1
            languagePicker?.maxValue = data.size
            languagePicker?.displayedValues = data


            languagePicker!!.setOnValueChangedListener { _, _, newVal ->
                var lang = -1
                isLanChange = true
                when (newVal) {
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
        val rsno = prefs.rsno
        val server = prefs.severDomain


        when (prefs.languagePos) {
            1 -> {
                binding.languagePicker?.value = 1
            }
            2 -> {
                binding.languagePicker?.value = 2
            }
            else -> {
                var lanStr = resources.configuration.locale.language
                if (lanStr == "zh") {
                    binding.languagePicker?.value = 1
                } else {
                    binding.languagePicker?.value = 2
                }
            }
        }

        if (rsno == "" || server == "") {
            isCancelable = false
        }
        binding.etRsno!!.setText(rsno)
        binding.etServerIp!!.setText(server)
    }

    private fun applyChangesToSharedPref(): Boolean {

        val rsnoText = binding.etRsno!!.text.trim().toString()
        val serverText = binding.etServerIp!!.text.trim().toString()
        if (rsnoText.isEmpty() || serverText.isEmpty()) {
            return false
        }

        hasChange =
            !(rsnoText == prefs.rsno && serverText == prefs.severDomain && chooseLan == prefs.languagePos)

        prefs.rsno = rsnoText
        prefs.severDomain = serverText
        prefs.languagePos = chooseLan

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

