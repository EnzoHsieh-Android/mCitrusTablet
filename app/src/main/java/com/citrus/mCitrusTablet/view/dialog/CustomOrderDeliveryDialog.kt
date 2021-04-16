package com.citrus.mCitrusTablet.view.dialog

import android.graphics.Point
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.di.prefs
import com.citrus.mCitrusTablet.model.vo.DeliveryInfo
import com.citrus.mCitrusTablet.model.vo.PostToGetDelivery
import com.citrus.mCitrusTablet.model.vo.Wait
import com.citrus.mCitrusTablet.util.PrintDelivery
import com.citrus.mCitrusTablet.util.ui.BaseDialogFragment
import com.citrus.mCitrusTablet.view.adapter.OrderDeliveryAdapter
import com.citrus.mCitrusTablet.view.wait.WaitViewModel
import kotlinx.android.synthetic.main.dialog_order_delivery.*
import java.text.SimpleDateFormat


class CustomOrderDeliveryDialog(
    var context: FragmentActivity,
    private var wait: Wait,
    private val waitViewModel: WaitViewModel,
) : BaseDialogFragment() {

    private val orderDeliveryAdapter by lazy { OrderDeliveryAdapter() }
    lateinit var deliveryInfo: DeliveryInfo



    override fun getLayoutId(): Int {
        return R.layout.dialog_order_delivery
    }

    override fun initView() {
        setWindowWidthPercent()

        deliveryRv.apply {
            adapter = orderDeliveryAdapter
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(
                DividerItemDecoration(this.context,
                    DividerItemDecoration.VERTICAL)
            )
        }



        waitViewModel.fetchOrdersDeliver(PostToGetDelivery(prefs.rsno,wait.tkey))

        waitViewModel.deliveryInfo.observe(viewLifecycleOwner,{
            deliveryInfo = it
            title.text = context.resources.getString(R.string.order_delivery) +" "+ it.ordersDelivery.orderNO

            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val outputFormat = SimpleDateFormat("yyyy/MM/dd HH:mm")
            val date = inputFormat.parse(it.ordersDelivery.orderTime)
            val formattedDate = outputFormat.format(date)
            describe.text = formattedDate
            orderDeliveryAdapter.update(it.ordersItemDelivery)
            totalPrice.text = context.resources.getString(R.string.TotalForTheDay) + "$"+it.ordersDelivery.subtotal.toInt().toString()

            if(deliveryInfo.ordersDelivery.serviceOutStatus == "A"){
                updateToPost.visibility = View.GONE
                hasUpdatePost.visibility = View.VISIBLE
            }else{
                updateToPost.visibility = View.VISIBLE
                hasUpdatePost.visibility = View.GONE
            }
        })

        waitViewModel.isDeliveryStatusChange.observe(viewLifecycleOwner,{
            if(it){
                updateToPost.visibility = View.GONE
                hasUpdatePost.visibility = View.VISIBLE
            }else{
                Toast.makeText(context,"Update Fail",Toast.LENGTH_SHORT).show()
            }
        })


        printDelivery.setOnClickListener {
            if(prefs.printerIP!="" && prefs.printerPort!="") {
                PrintDelivery(context, deliveryInfo) { isSuccess, err ->
                    Log.e("isSuccess", isSuccess.toString())
                    if (err != null) {
                        Log.e("err", err)
                    }
                }.startPrint()
            }else{
                Toast.makeText(context,"Please check setting and make sure printer IP & Port has enter!",Toast.LENGTH_LONG).show()
            }
        }

        updateToPost.setOnClickListener {
            waitViewModel.setOrdersDeliverStatus(deliveryInfo.ordersDelivery.orderNO)
        }


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