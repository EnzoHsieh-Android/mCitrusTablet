package com.citrus.mCitrusTablet.model.vo

import com.google.gson.annotations.SerializedName




/*output*/
data class PostToGetOrderDateBySeat (
    @SerializedName("RSNO")
    val rsno: String,

    @SerializedName("ReservationTime")
    val reservationTime: String,

    val floor: String,
    val room: String
)



data class PostToGetOrderDateByCusNum (
    @SerializedName("RSNO")
    val rsno: String,

    @SerializedName("ReservationTime")
    val reservationTime: String,

    @SerializedName("cust_num")
    val custNum: Int
)



/*input*/
data class OrderDate (
    val status: Int,
    val data: List<OrderDateDatum>
)


data class OrderDateDatum (
    @SerializedName("ResDate")
    val resDate: String,

    @SerializedName("Num")
    val num: Int
)
