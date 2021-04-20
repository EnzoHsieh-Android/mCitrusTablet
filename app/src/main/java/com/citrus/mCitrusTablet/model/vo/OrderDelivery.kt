package com.citrus.mCitrusTablet.model.vo

import com.google.gson.annotations.SerializedName

/*output*/
data class PostToGetDelivery (
    @SerializedName("RSNO")
    val rsno: String,

    val waitNo: String
)


data class PostToSetDeliveryStatus (
    @SerializedName("OrdersDelivery")
    val ordersDeliveryUpdate: OrdersDeliveryUpdate
)




data class OrdersDeliveryUpdate (
    @SerializedName("OrderNO")
    val orderNO: String,

    @SerializedName("Flag")
    val flag: String
)



/*input*/
data class OrderDelivery (
    val status: Int,
    @SerializedName("data")
    val data: DeliveryInfo
)


data class DeliveryInfo (
    @SerializedName("OrdersDelivery")
    val ordersDelivery: OrdersDelivery,

    @SerializedName("OrdersItemDelivery")
    val ordersItemDelivery: List<OrdersItemDelivery>
)


data class OrdersDelivery (
    @SerializedName("OrderNO")
    val orderNO: String,

    @SerializedName("OrderTime")
    val orderTime: String,

    @SerializedName("Subtotal")
    val subtotal: Double,

    @SerializedName("Tax")
    val tax: Double,

    @SerializedName("GrandTotal")
    val grandTotal: Double,

    @SerializedName("Pax")
    val pax: Int,

    @SerializedName("Phone")
    val phone: String,

    @SerializedName("ServiceOutStatus")
    val serviceOutStatus:String,

    @SerializedName("Flag")
    val flag:String
)


data class OrdersItemDelivery (
    @SerializedName("Gname")
    val gname: String,

    @SerializedName("Qty")
    val qty: Int,

    @SerializedName("AddName")
    val addName: String,

    @SerializedName("FlavorName")
    val flavorName: String,

    @SerializedName("Price")
    val price: Double
){
    fun isSame(other: OrdersItemDelivery): Boolean {
        return gname == other.gname
    }

    fun isContentSame(other: OrdersItemDelivery): Boolean {
        return this === other
    }
}



data class DeliveryStatusResult (
    val status: Long,

    @SerializedName("Data")
    val data: DataInfo
)


data class DataInfo (
    val data: Long
)
