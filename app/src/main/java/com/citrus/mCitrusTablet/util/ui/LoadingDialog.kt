package com.citrus.mCitrusTablet.util.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.citrus.mCitrusTablet.R


class LoadingDialog : DialogFragment() {
    private lateinit var mContext: Context

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.CustomProgressDialog)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val decorView = dialog?.window?.decorView
        decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
        dialog?.setCanceledOnTouchOutside(false)
        isCancelable = false
        return inflater.inflate(R.layout.dialog_loading, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        Glide.with(mContext).load("http://" + prefs.ip + "/AdvImages/loadingIcon.png")
//            .diskCacheStrategy(DiskCacheStrategy.NONE).skipMemoryCache(true).centerInside()
//            .error(R.drawable.ic_loading).into(ivLoading)
//
//        YoYo.with(Techniques.Flash).duration(3000).repeat(Animation.INFINITE).playOn(ivLoading)
    }

    companion object {
        fun newInstance(): LoadingDialog {
            return LoadingDialog()
        }
    }
}