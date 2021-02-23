package com.citrus.mCitrusTablet.model.vo

import com.google.gson.annotations.SerializedName
import java.io.Serializable


/*output*/
data class BookingPostData (
        @SerializedName("RSNO")
        val rsno: String,

        @SerializedName("ReservationTime")
        val reservationTime: String,

        @SerializedName("cust_num")
        val custNum: Int
):Serializable




/*input*/
data class SearchSeat (
        val status: Long,
        val data: List<Datum>
):Serializable


data class Datum (
        @SerializedName("ResDate")
        val resDate: String,

        @SerializedName("Floor")
        val floor: List<Floor>
):Serializable


data class Floor (
        @SerializedName("floor_name")
        val floorName: String,

        @SerializedName("room_name")
        val roomName: String,

        @SerializedName("IsLock")
        val isLock: String
):Serializable
