package com.citrus.mCitrusTablet.model

import android.util.Log
import com.citrus.mCitrusTablet.model.api.ApiService
import com.citrus.mCitrusTablet.model.vo.*
import com.google.gson.Gson
import com.skydoves.sandwich.suspendOnSuccess
import kotlinx.coroutines.flow.flow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class Repository @Inject constructor(private val apiService: ApiService) {


    fun changeStatus(url: String, postToChangeStatus: PostToChangeStatus) = flow {
        val jsonString = Gson().toJson(postToChangeStatus)
        apiService.changeStatus(url, jsonString)
            .suspendOnSuccess {
                data?.let {
                    it.status?.let { result ->
                        emit(result)
                    }
                }
            }
    }


    fun fetchAllData(
        url: String,
        fetchType: String,
        postToGetAllData: PostToGetAllData,
        onCusCount: (String) -> Unit
    ) = flow {
        val jsonString = Gson().toJson(postToGetAllData)
        apiService.getAllData(url, jsonString)
            .suspendOnSuccess {
                data?.let {
                    it.data?.let { list ->
                        if (fetchType == "reservation") {
                            onCusCount(list.reservation.size.toString())
                            emit(list.reservation)
                        } else {
                            onCusCount(list.wait.size.toString())
                            emit(list.wait)
                        }
                    }
                }
            }
    }



    fun fetchReservationFloor(url: String, postToGetSeats: PostToGetSeats, onEmpty: () -> Unit) =
        flow {
            val jsonString = Gson().toJson(postToGetSeats)
            apiService.getReservationFloor(url, jsonString)
                .suspendOnSuccess {
                    if (data?.status == 0) {
                        onEmpty()
                    } else {
                        data?.let { searchSeat ->
                            emit(searchSeat.data)
                        }
                    }
                }
        }


    fun fetchStoreInfo(url: String, storeId: String) = flow {
        apiService.getStoreInfo(url, storeId).suspendOnSuccess {
            if (data?.status != 0) {
                emit(data!!.data)
            }
        }
    }


    fun uploadReservationData(url: String, postToSetReservation: PostToSetReservation) = flow {
        val jsonString = Gson().toJson(postToSetReservation)
        apiService.setReservationData(url, jsonString)
            .suspendOnSuccess {
                data?.let {
                    emit(it)
                }
            }
    }

    fun sendSMS(url: String, project: String, phone: String, body: String) = flow {
        apiService.sendSMS(url, project, phone, body)
            .suspendOnSuccess {
                data?.let {
                    if (it.status == 1) {
                        emit(1)
                    }
                }
            }
    }

     fun fetchReservationTime(url: String, PostData:String) = flow {
        Log.e("fetchOrdersDeliverDate",PostData)
        apiService.getReservationTime(url,PostData)
            .suspendOnSuccess {
            if(data?.status != 0){
                emit(data!!.data.filter { it.num != 0 })
            }
        }
    }


    fun setWaitData(url: String , postToSetWaiting: PostToSetWaiting) = flow {
        val jsonString = Gson().toJson(postToSetWaiting)
        apiService.setWaitData(url,jsonString).suspendOnSuccess {
            if(data?.status != 0){
                emit(data!!)
            }
        }
    }



}