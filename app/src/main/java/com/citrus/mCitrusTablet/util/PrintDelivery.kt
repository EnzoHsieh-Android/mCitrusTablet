package com.citrus.mCitrusTablet.util

import androidx.fragment.app.FragmentActivity
import com.citrus.mCitrusTablet.di.prefs
import com.citrus.mCitrusTablet.model.vo.DeliveryInfo
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
        data = b(data, pageMode())
        data = b(data, fontSizeCmd(FontSize.Big))
        data = b(data, setLineSpace(55))
        var storeName = prefs.storeName
        val storeNameLen = getStringPixLength(storeName, 1, 2)
        if (storeNameLen < 15) {
            storeName = String.format("%" + (((16 - storeNameLen) / 2) + storeName.length) + "s", storeName)
        }
        data = b(data, text(storeName))
        data = b(data, text("預點時間: " + deliveryInfo.ordersDelivery.orderTime))
        data = b(data, text("單號 " + deliveryInfo.ordersDelivery.orderNO))
        data = b(data, dashLine(false))

        var sum = 0
        for (item in deliveryItemList) {
            sum += item.qty
            val title = item.qty.toString() + "x " + item.gname + " " +item.addName+ " "+item.flavorName
            data = b(data, twoColumn(title, item.price.toString(), false))
        }
        data = b(data, dashLine(false))
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