package com.citrus.mCitrusTablet.view.dialog

import android.graphics.Point
import android.view.Gravity
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.di.prefs
import com.citrus.mCitrusTablet.model.vo.PostToGetDelivery
import com.citrus.mCitrusTablet.model.vo.Wait
import com.citrus.mCitrusTablet.util.ui.BaseDialogFragment
import com.citrus.mCitrusTablet.view.adapter.OrderDeliveryAdapter
import com.citrus.mCitrusTablet.view.wait.WaitViewModel
import kotlinx.android.synthetic.main.dialog_order_delivery.*


class CustomOrderDeliveryDialog(
    var context: FragmentActivity,
    private var wait: Wait,
    private val waitViewModel: WaitViewModel,
) : BaseDialogFragment() {

    private val orderDeliveryAdapter by lazy { OrderDeliveryAdapter() }


    override fun getLayoutId(): Int {
        return R.layout.dialog_order_delivery
    }

    override fun initView() {
        setWindowWidthPercent()
        title.text = context.resources.getString(R.string.order_delivery) +" "+ wait.orderNo

        deliveryRv.apply {
            adapter = orderDeliveryAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }


        waitViewModel.fetchOrdersDeliver(PostToGetDelivery(prefs.rsno,wait.tkey))

        waitViewModel.deliveryInfo.observe(viewLifecycleOwner,{
            orderDeliveryAdapter.update(it)
        })

    }

    override fun initAction() {
    }


    private fun setWindowWidthPercent() {
        dialog?.window?.let {
            val size = Point()
            val display = it.windowManager.defaultDisplay
            display.getSize(size)

            val width = size.x
            val height = size.y

            it.setLayout((width * 0.65).toInt(), (height * 0.75).toInt())
            it.setGravity(Gravity.CENTER)
        }
    }


}