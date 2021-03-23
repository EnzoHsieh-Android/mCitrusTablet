package com.citrus.mCitrusTablet.model.vo

import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class PostToSetReservation (
        @SerializedName("RSNO")
        val rsno: String,
        @SerializedName("Reservation")
        val reservation: ReservationClass
): Serializable


data class ReservationClass (
        @SerializedName("ReservationTime")
        val reservationTime: String,

        @SerializedName("cust_num")
        val custNum: Int,

        @SerializedName("CustNo")
        val custNo: String,

        val mName: String,

        @SerializedName("Phone")
        val phone: String,

        @SerializedName("Email")
        val email: String,

        @SerializedName("Memo")
        val memo: String,

        @SerializedName("Status")
        val status: String,

        @SerializedName("floor_name")
        val floorName: String,

        @SerializedName("room_name")
        val roomName: String,

        @SerializedName("AdultCount")
        val adultCount: Int,

        @SerializedName("KidCount")
        val kidCount: Int
):Serializable


/*input*/
data class ReservationUpdateStatus (val status: Int, val data: Int):Serializable