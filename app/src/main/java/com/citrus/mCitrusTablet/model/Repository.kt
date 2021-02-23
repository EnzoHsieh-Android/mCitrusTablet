package com.citrus.mCitrusTablet.model

import com.citrus.mCitrusTablet.model.api.ApiService
import com.citrus.mCitrusTablet.model.vo.*
import com.google.gson.Gson
import com.skydoves.sandwich.suspendOnSuccess
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class Repository @Inject constructor(private val apiService: ApiService){


    fun changeStatus(changeStatus: ChangeStatus) = flow {
        val jsonString = Gson().toJson(changeStatus)
        apiService.changeStatus(jsonString)
                .suspendOnSuccess {
                    data?.let {
                        it.status?.let { result ->
                            emit(result)
                        }
                    }
                }
    }.flowOn(Dispatchers.IO)


    fun fetchAllData(fetchType:String, fetchAllData: FetchAllData, onCusCount: (String) -> Unit) = flow {
        val jsonString = Gson().toJson(fetchAllData)
        apiService.getAllData(jsonString)
                .suspendOnSuccess {
                    data?.let {
                        it.data?.let { list ->
                            if(fetchType == "reservation") {
                                onCusCount(list.reservation.size.toString())
                                emit(list.reservation)
                            }else{
                                onCusCount(list.wait.size.toString())
                                emit(list.wait)
                            }
                        }
                    }
                }
    }.flowOn(Dispatchers.IO)


    fun fetchReservationFloor(bookingPostData: BookingPostData, onEmpty: () -> Unit) = flow {
        val jsonString = Gson().toJson(bookingPostData)
        apiService.getReservationFloor(jsonString)
                .suspendOnSuccess {
                    if(data?.status == 0){
                        onEmpty()
                    }
                        data?.let { searchSeat ->
                            searchSeat.data?.let { list ->
                                emit(list[0].floor.filter { it.isLock != "Y" })
                            }
                        }
                }
    }.flowOn(Dispatchers.IO)


    fun uploadReservationData (reservationUpload: ReservationUpload) = flow {
        val jsonString = Gson().toJson(reservationUpload)
        apiService.setReservationData(jsonString)
                .suspendOnSuccess {
                    data?.let {
                        emit(it)
                    }
                }
    }.flowOn(Dispatchers.IO)


}