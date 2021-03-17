package com.citrus.mCitrusTablet.model.vo

import com.google.gson.annotations.SerializedName


data class StoreInfo (
    val status: Long,
    val data: List<Info>
)


data class Info (
    @SerializedName("StoreID")
    val storeID: Int,

    @SerializedName("StoreName")
    val storeName: String,

    @SerializedName("TEL")
    val tel: String,

    @SerializedName("Business_StartHours")
    val businessStartHours: String,

    @SerializedName("Business_EndHours")
    val businessEndHours: String,

    @SerializedName("Longitude")
    val longitude: Long,

    @SerializedName("Latitude")
    val latitude: Long,

    @SerializedName("Note")
    val note: String,

    @SerializedName("Address")
    val address: String,

    @SerializedName("Delivery")
    val delivery: String,

    @SerializedName("SelfCollect")
    val selfCollect: String,

    @SerializedName("IsPosition")
    val isPosition: String,

    @SerializedName("Pic")
    val pic: String,

    @SerializedName("Delivery_Distance")
    val deliveryDistance: Int,

    @SerializedName("Delivery_StartHour")
    val deliveryStartHour: String,

    @SerializedName("Delivery_EndHour")
    val deliveryEndHour: String,

    @SerializedName("Email")
    val email: String,

    @SerializedName("RSNO")
    val rsno: String,

    @SerializedName("BusinessType")
    val businessType: String,

    @SerializedName("Message1")
    val message1:String,

    @SerializedName("Message2")
    val message2:String,

    @SerializedName("Message3")
    val message3:String
)
