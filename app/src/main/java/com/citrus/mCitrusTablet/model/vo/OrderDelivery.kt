package com.citrus.mCitrusTablet.model.vo

import com.google.gson.annotations.SerializedName

/*output*/
data class PostToGetDelivery (
    @SerializedName("RSNO")
    val rsno: String,

    val waitNo: String
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
    val subtotal: Int,

    @SerializedName("Tax")
    val tax: Int,

    @SerializedName("GrandTotal")
    val grandTotal: Int,

    @SerializedName("Pax")
    val pax: Int,

    @SerializedName("Phone")
    val phone: String
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
    val price: Int
){
    fun isSame(other: OrdersItemDelivery): Boolean {
        return gname == other.gname
    }

    fun isContentSame(other: OrdersItemDelivery): Boolean {
        return this === other
    }
}
