package com.citrus.mCitrusTablet.model

import android.util.Log
import com.citrus.mCitrusTablet.model.api.ApiService
import com.citrus.mCitrusTablet.model.vo.*
import com.citrus.mCitrusTablet.util.Constants
import com.google.gson.Gson
import com.skydoves.sandwich.suspendOnSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onCompletion
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
    }.flowOn(Dispatchers.IO)

    fun fetchAllData(
        url: String,
        fetchType: String,
        postToGetAllData: PostToGetAllData,
        onCusCount: (String) -> Unit,
        onReservationCount: (Int,ReservationGuests?) -> Unit,
        onWaitCount: (Int,Wait?) -> Unit
    ) = flow {
        val jsonString = Gson().toJson(postToGetAllData)
        apiService.getAllData(url, jsonString)
            .suspendOnSuccess {
                data?.let { oData ->
                    oData.data?.let { list ->
                        if (fetchType == "reservation") {
                            /**只提示今天的外部新增候位通知*/
                            if(postToGetAllData.startDate == Constants.defaultTimeStr){
                                onWaitCount(list.wait.size,if (list.wait.isNotEmpty()) list.wait.last() else null)
                            }else{
                                onWaitCount(-1,null)
                            }
                            onCusCount(list.reservation.size.toString())
                            emit(list.reservation)
                        } else if(fetchType == "wait"){
                            onReservationCount(list.reservation.size, if (list.reservation.isNotEmpty()) list.reservation.last() else null)
                            onCusCount(list.wait.size.toString())
                            var distance = 0L
                            var updateTimeStr = ""
                            for (wait in list.wait) {
                                if (wait.updateDate != null && wait.updateDate != "" ) {
                                    val updateDateFormat = Constants.outputFormat.format(
                                        Constants.inputFormat.parse(wait.updateDate)
                                    )
                                    updateTimeStr = updateDateFormat.split(" ")[1]
                                    val updateDate =
                                        Constants.inputFormat.parse(wait.updateDate)
                                    distance = ((Date().time - updateDate.time) / 1000) / 60
                                }
                                if (distance > 9) wait.isOverTime = true else false
                                wait.updateDate = updateTimeStr
                            }
                            emit(list.wait)
                        }
                    }
                }
            }
    }.flowOn(Dispatchers.IO)

    fun fetchAllDataForReport(
        url: String,
        postToGetAllData: PostToGetAllData,
    ) = flow {
        val jsonString = Gson().toJson(postToGetAllData)
        apiService.getAllData(url, jsonString)
            .suspendOnSuccess {
                data?.let { oData ->
                    oData.data?.let { list ->
                        emit(list)
                    }
                }
            }
    }.flowOn(Dispatchers.IO)

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
        }.flowOn(Dispatchers.IO)

    fun fetchStoreInfo(url: String, storeId: String) = flow {
        apiService.getStoreInfo(url, storeId).suspendOnSuccess {
            if (data?.status != 0) {
                emit(data!!.data)
            }
        }
    }.flowOn(Dispatchers.IO)

    fun uploadReservationData(url: String, postToSetReservation: PostToSetReservation) = flow {
        val jsonString = Gson().toJson(postToSetReservation)
        apiService.setReservationData(url, jsonString)
            .suspendOnSuccess {
                data?.let {
                    emit(it)
                }
            }
    }.flowOn(Dispatchers.IO)

    fun sendSMS(url: String, project: String, phone: String, body: String) = flow {
        apiService.sendSMS(url, project, phone, body)
            .suspendOnSuccess {
                data?.let {
                    if (it.status == 1) {
                        emit(1)
                    }else{
                        emit(0)
                    }
                }
            }
    }.flowOn(Dispatchers.IO)

    fun sendMail(url: String,email: String,htmlBody: String,subject:String) = flow {
        apiService.sendMail(url,email,htmlBody,subject)
            .suspendOnSuccess {
                data?.let {
                    if (it.status == 1) {
                        emit(1)
                    }else{
                        emit(0)
                    }
                }
            }
    }.flowOn(Dispatchers.IO)

    fun fetchReservationTime(url: String, PostData: String, onEmpty: () -> Unit) = flow {
        apiService.getReservationTime(url, PostData)
            .suspendOnSuccess {
                if (data?.status != 0) {
                    emit(data!!.data.filter { it.num != 0 })
                } else {
                    onEmpty()
                }
            }
    }.flowOn(Dispatchers.IO)

    fun setWaitData(url: String, postToSetWaiting: PostToSetWaiting) = flow {
        val jsonString = Gson().toJson(postToSetWaiting)
        apiService.setWaitData(url, jsonString).suspendOnSuccess {
            if (data?.status != 0) {
                emit(data!!)
            }
        }
    }.flowOn(Dispatchers.IO)




    fun fetchOrdersDeliveryData(
        url: String,
        postToGetDelivery: PostToGetDelivery,
        onEmpty: () -> Unit
    ) = flow {
        val jsonString = Gson().toJson(postToGetDelivery)
        apiService.getOrdersDeliveryData(url, jsonString).suspendOnSuccess {
            if (data?.status != 0) {
                emit(data?.data?.ordersItemDelivery)
            } else {
                onEmpty()
            }
        }
    }.flowOn(Dispatchers.IO)


}