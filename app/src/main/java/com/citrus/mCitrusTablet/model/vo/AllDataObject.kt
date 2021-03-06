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
    var reservationTime: String,

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
    val roomName: String,

    @SerializedName("AdultCount")
    val adultCount: Int,

    @SerializedName("KidCount")
    val kidCount: Int,

    var isSelect:Boolean = false,

    var isExpend:Boolean = false,

    @SerializedName("Email")
    var email:String,

    @SerializedName("CreateDate")
    var createDate:String,

    @SerializedName("UpdateDate")
    var updateDate:String,

    var isNew:Boolean = false

):Serializable


data class Wait (
    val tkey: String,

    @SerializedName("StoreID")
    val storeID: Long,

    @SerializedName("ReservationTime")
    var reservationTime: String,

    @SerializedName("cust_num")
    val custNum: Int,

    val mName: String,

    @SerializedName("Phone")
    val phone: String?,

    @SerializedName("Memo")
    val memo: String,

    @SerializedName("Status")
    var status: String,

    @SerializedName("MaleCount")
    val maleCount: Int,

    @SerializedName("FemaleCount")
    val femaleCount: Int,

    @SerializedName("AdultCount")
    val adultCount: Int,

    @SerializedName("KidCount")
    val kidCount: Int,

    @SerializedName("Email")
    val email: String?,

    @SerializedName("Gender")
    val gender: String,

    @SerializedName("WaitNumber")
    val waitNumber: Int,

    @SerializedName("OrderNO")
    val orderNo: String,

    val url: String,

    @SerializedName("UpdateDate")
    var updateDate:String,

    var isOverTime:Boolean = false,
    var isSelect:Boolean = false,
    var isExpend:Boolean = false,
    var isNew:Boolean = false,
    var waitTime:Int = -1
):Serializable



