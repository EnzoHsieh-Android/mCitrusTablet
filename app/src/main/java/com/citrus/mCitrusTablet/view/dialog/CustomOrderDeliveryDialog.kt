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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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


            var sum:Int = 0

            for(item in deliveryInfo.ordersItemDelivery){
                sum += item.qty
            }

            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            val outputFormat = SimpleDateFormat("yyyy/MM/dd HH:mm")
            val date = inputFormat.parse(it.ordersDelivery.orderTime)
            val formattedDate = outputFormat.format(date)
            describe.text = context.resources.getString(R.string.orderTime) +" "+ formattedDate
            orderDeliveryAdapter.update(it.ordersItemDelivery)
            tvTotalSum.text = sum.toString()
            totalPrice.text = "$"+it.ordersDelivery.subtotal.toString()



            when(deliveryInfo.ordersDelivery.flag){
                "A" -> {
                    updateToPost.visibility = View.GONE
                    hasUpdatePost.visibility = View.VISIBLE
                    hasUpdatePost.text = "已轉單"
                }
                "E" -> {
                    updateToPost.visibility = View.GONE
                    hasUpdatePost.visibility = View.VISIBLE
                    when(deliveryInfo.ordersDelivery.serviceOutStatus){
                        "W" -> hasUpdatePost.text = "已接單"
                        "B" -> hasUpdatePost.text = "已取消"
                        "C" -> hasUpdatePost.text = "已結帳"
                    }
                }
                else -> {
                    updateToPost.visibility = View.VISIBLE
                    hasUpdatePost.visibility = View.GONE
                }
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
                MainScope().launch(Dispatchers.IO) {
                    PrintDelivery(context, deliveryInfo) { isSuccess, err ->
                        if (!isSuccess) {
                            launch(Dispatchers.Main) {
                                if (err != null) {
                                    waitViewModel.sendPrintFail(err)
                                }
                            }
                        }else{
                            launch(Dispatchers.Main) {
                                waitViewModel.sendPrintSuccessful()
                            }
                        }
                    }.startPrint()
                }
            }else{
                Toast.makeText(context,context.resources.getString(R.string.checkPrint),Toast.LENGTH_LONG).show()
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