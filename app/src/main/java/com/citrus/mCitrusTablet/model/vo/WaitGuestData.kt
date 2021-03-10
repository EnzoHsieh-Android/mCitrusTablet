package com.citrus.mCitrusTablet.model.vo

import com.google.gson.annotations.SerializedName


data class PostToSetWaiting (
    @SerializedName("Reservation")
    val waitGuestData: WaitGuestData
)


data class WaitGuestData (
    @SerializedName("StoreID")
    val storeID: Int,

    @SerializedName("cust_num")
    val custNum: Int,

    val mName: String,

    @SerializedName("Phone")
    val phone: String,

    @SerializedName("Memo")
    val memo: String,

    @SerializedName("Status")
    val status: String
)
