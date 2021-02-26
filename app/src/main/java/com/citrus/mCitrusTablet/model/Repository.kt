package com.citrus.mCitrusTablet.model

import com.citrus.mCitrusTablet.model.api.ApiService
import com.citrus.mCitrusTablet.model.vo.*
import com.google.gson.Gson
import com.skydoves.sandwich.suspendOnSuccess
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class Repository @Inject constructor(private val apiService: ApiService) {


    fun changeStatus(url: String, changeStatus: ChangeStatus) = flow {
        val jsonString = Gson().toJson(changeStatus)
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
        fetchAllData: FetchAllData,
        onCusCount: (String) -> Unit
    ) = flow {
        val jsonString = Gson().toJson(fetchAllData)
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


    fun fetchReservationFloor(url: String, bookingPostData: BookingPostData, onEmpty: () -> Unit) =
        flow {
            val jsonString = Gson().toJson(bookingPostData)
            apiService.getReservationFloor(url, jsonString)
                .suspendOnSuccess {
                    if (data?.status == 0) {
                        onEmpty()
                    }
                    data?.let { searchSeat ->
                        searchSeat.data?.let { list ->
                            emit(list[0].floor.filter { it.isLock != "Y" })
                        }
                    }
                }
        }


    fun uploadReservationData(url: String, reservationUpload: ReservationUpload) = flow {
        val jsonString = Gson().toJson(reservationUpload)
        apiService.setReservationData(url, jsonString)
            .suspendOnSuccess {
                data?.let {
                    emit(it)
                }
            }
    }


}