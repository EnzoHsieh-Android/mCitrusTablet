package com.citrus.mCitrusTablet.model.vo

import com.google.gson.annotations.SerializedName
import java.io.Serializable


/*output*/
data class PostToGetAllData(var RSNO:String, var startDate:String, var endDate:String):Serializable


/*input*/
data class AllDataObject (
    val status: Long,
    val data: Data
):Serializable


data class Data (
    @SerializedName("Reservation")
    val reservation: List<ReservationGuests>,

    @SerializedName("Wait")
    val wait: List<Wait>
):Serializable


data class ReservationGuests(
    val tkey: String,

    @SerializedName("StoreID")
    val storeID: Int,

    @SerializedName("ReservationTime")
    val reservationTime: String,

    @SerializedName("cust_num")
    val custNum: Int,

    @SerializedName("CustNo")
    val custNo: String,

    val mName: String,

    @SerializedName("Phone")
    val phone: String,

    @SerializedName("Memo")
    var memo: Any? = null,

    @SerializedName("Status")
    var status: String,

    @SerializedName("floor_name")
    val floorName: String,

    @SerializedName("room_name")
    val roomName: String
):Serializable


data class Wait (
    val tkey: String,

    @SerializedName("StoreID")
    val storeID: Long,

    @SerializedName("ReservationTime")
    val reservationTime: String,

    @SerializedName("cust_num")
    val custNum: Long,

    val mName: String,

    @SerializedName("Phone")
    val phone: String,

    @SerializedName("Memo")
    val memo: String,

    @SerializedName("Status")
    var status: String,

    @SerializedName("MaleCount")
    val maleCount: Long,

    @SerializedName("FemaleCount")
    val femaleCount: Long,

    @SerializedName("AdultCount")
    val adultCount: Long,

    @SerializedName("KidCount")
    val kidCount: Long,

    @SerializedName("Email")
    val email: String,

    @SerializedName("Gender")
    val gender: String,

    @SerializedName("WaitNumber")
    val waitNumber: Int,

    @SerializedName("OrderNO")
    val orderNo: String,

    val url: String,

    @SerializedName("UpdateDate")
    var updateDate:String
    ,
    var isOverTime:Boolean
):Serializable{
    fun isSame(other: Wait): Boolean {
        return tkey == other.tkey
    }

    fun isContentSame(other: Wait): Boolean {
        return this == other
    }
}



