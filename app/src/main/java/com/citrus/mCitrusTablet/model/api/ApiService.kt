package com.citrus.mCitrusTablet.model.api

import com.citrus.mCitrusTablet.model.vo.AllDataObject
import com.citrus.mCitrusTablet.model.vo.ReservationUpdateStatus
import com.citrus.mCitrusTablet.model.vo.SearchSeat
import com.citrus.mCitrusTablet.model.vo.UpdateStatus
import com.skydoves.sandwich.ApiResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Headers
import retrofit2.http.POST


interface ApiService {

    companion object {
        const val BASE_URL = "https://cms.citrus.tw/"
    }

    /*變更狀態*/
    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("celaviLAB/POSServer/UploadDataWS/Service1.asmx/SetWaitData")
    suspend fun  changeStatus(@Field("jsonData") jsonData:String): ApiResponse<UpdateStatus>


    /*取得全部資料*/
    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("celaviLAB/POSServer/UploadDataWS/Service1.asmx/GetAllReservationData")
    suspend fun  getAllData(@Field("jsonData") jsonData:String): ApiResponse<AllDataObject>


    /*取得預約空桌*/
    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("celaviLAB/POSServer/UploadDataWS/Service1.asmx/GetReservationFloor")
    suspend fun  getReservationFloor(@Field("jsonData") jsonData:String): ApiResponse<SearchSeat>

    /*預約*/
    @FormUrlEncoded
    @Headers("Content-Type: application/x-www-form-urlencoded")
    @POST("celaviLAB/POSServer/UploadDataWS/Service1.asmx/SetReservationData")
    suspend fun  setReservationData(@Field("jsonData") jsonData:String): ApiResponse<ReservationUpdateStatus>

}