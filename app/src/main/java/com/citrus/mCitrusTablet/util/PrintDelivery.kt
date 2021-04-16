package com.citrus.mCitrusTablet.util

import androidx.fragment.app.FragmentActivity
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.di.prefs
import com.citrus.mCitrusTablet.model.vo.DeliveryInfo
import com.citrus.mCitrusTablet.util.Constants.dfShow
import java.io.DataOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket

class PrintDelivery(
    private val context: FragmentActivity,
    private val deliveryInfo: DeliveryInfo,
    private val onResult: (isSuccess: Boolean, err: String?) -> Unit
) {
    private var mOutputStream: OutputStream? = null
    private var socket: Socket? = null


    fun startPrint() {
        var deliveryItemList = deliveryInfo.ordersItemDelivery

        var data: ByteArray
        data = initCmd()
        data = b(data, fontSizeCmd(FontSize.Big))
        data = b(data, boldCmd(true))
        data = b(data, setLineSpace(55))
        if (prefs.storeName.isNotEmpty()) data = b(data, text(prefs.storeName))
        data = b(data, fontSizeCmd(FontSize.Normal))
        data = b(data, boldCmd(false))
        data = b(data, text("預點時間: " + deliveryInfo.ordersDelivery.orderTime))
        data = b(data, text("單號 " + deliveryInfo.ordersDelivery.orderNO))
        data = b(data, text("列印時間" + ": " + Constants.getCurrentTime()))

        data = b(data, dashLine(false))

        var sum = 0
        for (item in deliveryItemList) {

            var itemTitle = item.qty.toString() + "x " + item.gname

            if (getStringPixLength(itemTitle + dfShow.format(item.price), 12, 24) / 12 > 33) {
                data = b(data, text(itemTitle))
                data = b(data, twoColumn("", dfShow.format(item.price), false))
            } else {
                data = b(data, twoColumn(itemTitle, dfShow.format(item.price), false))
            }

            val flavorAdd =
                if (!item.addName.isNullOrEmpty() && !item.flavorName.isNullOrEmpty()) item.addName + "/" + item.flavorName
                else if (!item.addName.isNullOrEmpty()) item.addName
                else if (!item.flavorName.isNullOrEmpty()) item.flavorName
                else null

            flavorAdd?.let { data = b(data, text("    #$it")) }
        }
        data = b(data, dashLine(false))
        data = b(data, boldCmd(true))
        data = b(data, fontSizeCmd(FontSize.Big))
        data = b(data, twoColumn("總計    " + sum + "項", deliveryInfo.ordersDelivery.subtotal.toString(), false))
        data = b(data, text("\n"))

        if (data.isEmpty()) {
            onResult(true, null)
        } else {
            data = b(data, cutPaperCmd())
            send(data)
        }
    }



    private fun send(buffer: ByteArray) {
        socket = Socket()
        socket?.run {
            connect(InetSocketAddress(prefs.printerIP, prefs.printerPort.toInt()), 5000)
            mOutputStream = DataOutputStream(getOutputStream())

            if (isConnected(socket)) {
                mOutputStream?.write(buffer)
                mOutputStream?.flush()
                closeIOAndSocket()
                onResult(true, null)
            } else {
                onError("Socket is not connected")
            }

        } ?: run {
            onError("Socket is null")
            return
        }
    }

    private fun onError(message: String) {
        onResult(false, message)
    }

    @Throws(IOException::class)
    fun closeIOAndSocket() {
        mOutputStream?.close()
        socket?.close()
    }
}