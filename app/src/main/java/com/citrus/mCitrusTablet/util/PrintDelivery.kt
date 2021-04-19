package com.citrus.mCitrusTablet.util

import androidx.fragment.app.FragmentActivity
import com.citrus.mCitrusTablet.R
import com.citrus.mCitrusTablet.di.prefs
import com.citrus.mCitrusTablet.model.vo.DeliveryInfo
import com.citrus.mCitrusTablet.util.Constants.dfShow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.DataOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.Exception
import java.net.InetSocketAddress
import java.net.Socket
import java.net.SocketException

class PrintDelivery(
    private val context: FragmentActivity,
    private val deliveryInfo: DeliveryInfo,
    private val onResult: (isSuccess: Boolean, err: String?) -> Unit
) {
    private var mOutputStream: OutputStream? = null
    private var socket: Socket? = null


    fun startPrint() {
        val is80mm = prefs.printerIs80mm
        var deliveryItemList = deliveryInfo.ordersItemDelivery

        var data: ByteArray
        data = initCmd()
        data = b(data, fontSizeCmd(FontSize.Big))
        data = b(data, boldCmd(true))
        data = b(data, setLineSpace(55))
        if (prefs.storeName.isNotEmpty()) data = b(data, text(prefs.storeName))
        data = b(data, fontSizeCmd(FontSize.Normal))
        data = b(data, boldCmd(false))
        data = b(data, text(context.resources.getString(R.string.orderTime) + deliveryInfo.ordersDelivery.orderTime))
        data = b(data, text(context.resources.getString(R.string.orderNo) + deliveryInfo.ordersDelivery.orderNO))
        data = b(data, text(context.resources.getString(R.string.printTime)  + Constants.getCurrentTime()))

        data = b(data, dashLine(is80mm))
        if (is80mm) {
            data = b(data, boldCmd(true))
            data = b(data, twoColumn(context.getString(R.string.item), context.getString(R.string.qty) + "    " + context.getString(R.string.TotalForTheDay), is80mm))
            data = b(data, boldCmd(false))
            data = b(data, dashLine(is80mm))
        }

        var sum = 0
        for (item in deliveryItemList) {
            sum += item.qty

            if (is80mm) {
                val priceStr = String.format("%7s", dfShow.format(item.price))
                val qtyStr = String.format("%-3s", item.qty)

                //最多48個字 48-11=37
                if (item.gname.toByteArray(charset("GBK")).size <= 37) {
                    data = b(data, twoColumn(item.gname, qtyStr + priceStr, is80mm))
                } else {
                    data = b(data, text(item.gname))
                    data = b(data, twoColumn("", qtyStr + priceStr, is80mm))
                }
            } else {
                var itemTitle = item.qty.toString() + "x " + item.gname

                if (getStringPixLength(itemTitle + dfShow.format(item.price), 12, 24) / 12 > 33) {
                    data = b(data, text(itemTitle))
                    data = b(data, twoColumn("", dfShow.format(item.price), is80mm))
                } else {
                    data = b(data, twoColumn(itemTitle, dfShow.format(item.price), is80mm))
                }
            }

            val flavorAdd =
                if (!item.addName.isNullOrEmpty() && !item.flavorName.isNullOrEmpty()) item.addName + "/" + item.flavorName
                else if (!item.addName.isNullOrEmpty()) item.addName
                else if (!item.flavorName.isNullOrEmpty()) item.flavorName
                else null

            flavorAdd?.let { data = b(data, text((if (is80mm) "  #" else "    #") + it)) }
        }
        data = b(data, dashLine(is80mm))

        var orgAmtStr = String.format("%7s", dfShow.format(deliveryInfo.ordersDelivery.subtotal))
        val qtyStr = String.format("%-3s", sum)

        data = if (is80mm) {
            b(data, twoColumn(context.getString(R.string.TotalForTheDay), qtyStr + orgAmtStr, is80mm))
        } else {
            b(data, twoColumn(context.getString(R.string.TotalForTheDay), "$qtyStr $orgAmtStr", is80mm))
        }
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
            try {
                connect(InetSocketAddress(prefs.printerIP, prefs.printerPort.toInt()), 5000)
            }catch (e:Exception){
                e.message?.let { onError(it) }
                return
            }
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