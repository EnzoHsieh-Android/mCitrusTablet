package com.citrus.mCitrusTablet.util.ui

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Point
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.citrus.mCitrusTablet.R
import com.citrus.util.ui.LoadingDialog


import org.jetbrains.anko.backgroundResource
import javax.inject.Inject


abstract class BaseAlertDialog(mContext: Context, style: Int) : AlertDialog(mContext, style) {


    abstract fun getLayoutId(): Int

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setFullScreen()
        window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
        setContentView(getLayoutId())
        setCancelable(false)
        initView()
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    ownerActivity?.let {
                        val imm = it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(v.windowToken, 0)
                    }
                }
            }
        }
        setFullScreen()
        return super.dispatchTouchEvent(event)
    }

    abstract fun initView()

    private fun setFullScreen() {
        val decorView = window?.decorView
        decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }
}




abstract class BaseDialogFragmentVM<VM : ViewModel>(val viewModelClass: Class<VM>) : BaseDialogFragment(){
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var viewModel: VM
}

abstract class BaseDialogFragment : SwipeDialogFragment() {

    var activity: AppCompatActivity? = null
    private var isActive = false
    private val loadingDialog = LoadingDialog.newInstance()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        activity = context as AppCompatActivity
    }

    override fun onDetach() {
        super.onDetach()
        activity = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isActive = true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(getLayoutId(), container, false)
    }

    override fun onDismiss(dialog: DialogInterface) {
        isActive = false
        super.onDismiss(dialog)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        view.backgroundResource = R.drawable.custom_dialog_bg
        setFullScreen() // if add this, click editText, window will not pop to top
        setWindowWidthPercent()
        initView()
        initAction()
    }

    abstract fun getLayoutId(): Int
    abstract fun initView()
    abstract fun initAction()

    fun showAlertDialog(title: String, message: String) {
        activity?.let {
            val customDialog = CustomAlertDialog(
                it, title, message,
                R.drawable.ic_warning
            )
            customDialog.show()
        }
    }

    fun showSuccessDialog(title: String, message: String) {
        activity?.let {
            val customDialog = CustomAlertDialog(
                it, title, message,
                R.drawable.ic_check
            )
            customDialog.show()
        }
    }


    fun showLoadingDialog() {
        if (isActive) {
            if (loadingDialog.isAdded) {
                parentFragmentManager.beginTransaction().remove(loadingDialog).commit()
            }
            loadingDialog.show(parentFragmentManager, "LoadingDialog")
        }
    }

    fun hideLoadingDialog() {
        if (isActive) {
            if (loadingDialog.isAdded ) {
                loadingDialog.dismiss()
            }
        }
    }

    fun setFullScreen() {
        val decorView = dialog?.window?.decorView
        decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_LOW_PROFILE or
                View.SYSTEM_UI_FLAG_FULLSCREEN or
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
    }

    private fun setWindowWidthPercent() {
        dialog?.window?.let {
            val size = Point()
            val display = it.windowManager.defaultDisplay
            display.getSize(size)

            val width = size.x
            val height = size.y

            it.setLayout((width * 0.95).toInt(), (height * 0.95).toInt())
            it.setGravity(Gravity.CENTER)
        }
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
                            ownerActivity?.let {
                                val imm = it.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                                imm.hideSoftInputFromWindow(v.windowToken, 0)
                            }
                        }
                    }
                }
                setFullScreen()
                return super.dispatchTouchEvent(ev)
            }
        }
    }
}