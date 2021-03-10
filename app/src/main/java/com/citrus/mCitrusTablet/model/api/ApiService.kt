package com.citrus.mCitrusTablet.model.api

import com.citrus.mCitrusTablet.model.vo.*
import com.skydoves.sandwich.ApiResponse
import retrofit2.http.*


interface ApiService {

    companion object {
        const val BASE_URL = "https://cms.citrus.tw/"
    }

    /*變更狀態*/
    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST
    suspend fun changeStatus(
        @Url url: String,
        @Field("jsonData") jsonData: String
    ): ApiResponse<UpdateStatus>

    /*取得全部資料*/
    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST
    suspend fun getAllData(
        @Url url: String,
        @Field("jsonData") jsonData: String
    ): ApiResponse<AllDataObject>

    /*取得分店資料*/
    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST
    suspend fun getStoreInfo(
        @Url url: String,
        @Field("storeId") storeId: String
    ): ApiResponse<StoreInfo>

    /*取得預約空桌*/
    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST
    suspend fun getReservationFloor(
        @Url url: String,
        @Field("jsonData") jsonData: String
    ): ApiResponse<SearchSeat>

    /*依條件取得可預約時段*/
    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST
    suspend fun getReservationTime(
        @Url url: String,
        @Field("jsonData") jsonData: String
    ): ApiResponse<OrderDate>

    /*預約*/
    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST
    suspend fun setReservationData(
        @Url url: String,
        @Field("jsonData") jsonData: String
    ): ApiResponse<ReservationUpdateStatus>

    /*發送簡訊*/
    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST
    suspend fun sendSMS(
        @Url url: String,
        @Field("project") project: String,
        @Field("phone") phone: String,
        @Field("body") body: String
    ): ApiResponse<ReservationUpdateStatus>

    /*新增候位*/
    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST
    suspend fun setWaitData(@Url url: String, @Field("jsonData") jsonData: String):ApiResponse<UpdateStatus>


    /*取得預點單內容*/
    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST
    suspend fun getOrdersDeliveryData(@Url url: String, @Field("jsonData")jsonData: String):ApiResponse<OrderDelivery>
}