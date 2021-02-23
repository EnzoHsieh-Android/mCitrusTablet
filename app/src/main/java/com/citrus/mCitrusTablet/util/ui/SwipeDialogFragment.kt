package com.citrus.mCitrusTablet.util.ui

import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.labo.kaji.swipeawaydialog.SwipeDismissTouchListener
import com.labo.kaji.swipeawaydialog.SwipeDismissTouchListener.DismissCallbacks
import com.labo.kaji.swipeawaydialog.SwipeableFrameLayout

abstract class SwipeDialogFragment : DialogFragment() {
    /**
     * Get whether dialog can be swiped away.
     */
    /**
     * Set whether dialog can be swiped away.
     */
    var isSwipeEnable = true
    private var mTiltEnabled = true
    private var mSwipeLayoutGenerated = false
    private var mListener: SwipeDismissTouchListener? = null
    /**
     * Get whether tilt effect is enabled on swiping.
     */
    /**
     * Set whether tilt effect is enabled on swiping.
     */
    var isTiltEnabled: Boolean
        get() = mTiltEnabled
        set(tiltEnabled) {
            mTiltEnabled = tiltEnabled
            if (mListener != null) {
                mListener!!.setTiltEnabled(tiltEnabled)
            }
        }

    /**
     * Called when dialog is swiped away to dismiss.
     * @return true to prevent dismissing
     */
    fun onSwipedAway(toRight: Boolean): Boolean {
        return false
    }

    override fun onStart() {
        super.onStart()
        if (!mSwipeLayoutGenerated && showsDialog) {
            dialog?.window?.let {
                val decorView = it.decorView as ViewGroup
                val content = decorView.getChildAt(0)
                decorView.removeView(content)
                val layout = SwipeableFrameLayout(activity)
                layout.addView(content)
                decorView.addView(layout)
                mListener = SwipeDismissTouchListener(decorView, "layout", object : DismissCallbacks {
                    override fun canDismiss(token: Any): Boolean {
                        return isCancelable && isSwipeEnable
                    }

                    override fun onDismiss(view: View, toRight: Boolean, token: Any) {
                        if (isSwipeEnable) {
                            dismiss()
                        }
                    }
                })
                mListener!!.setTiltEnabled(mTiltEnabled)
                layout.setSwipeDismissTouchListener(mListener)
                layout.setOnTouchListener(mListener)
                layout.isClickable = true
                mSwipeLayoutGenerated = true
            }

        }
    }
}