package com.citrus.mCitrusTablet.model.api

import android.app.Application
import android.widget.Toast
import com.citrus.mCitrusTablet.di.errorManager
import com.citrus.mCitrusTablet.util.TriggerMode
import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.StatusCode
import com.skydoves.sandwich.map
import com.skydoves.sandwich.message
import com.skydoves.sandwich.operators.ApiResponseSuspendOperator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class GlobalResponseOperator<T> constructor(
    private val application: Application
) : ApiResponseSuspendOperator<T>() {

    // The body is empty, because we will handle the success case manually.
    override suspend fun onSuccess(apiResponse: ApiResponse.Success<T>) { }

    // handles error cases when the API request gets an error response.
    // e.g., internal server error.
    override suspend fun onError(apiResponse: ApiResponse.Failure.Error<T>) {
        withContext(Dispatchers.Main) {
            apiResponse.run {
                errorManager.setTriggerMode(TriggerMode.START)
                Timber.d(message())
                // handling error based on status code.
                when (statusCode) {
                    StatusCode.InternalServerError -> toast("InternalServerError")
                    StatusCode.BadGateway -> toast("BadGateway")
                    StatusCode.GatewayTimeout -> toast("GatewayTimeout")
                    else -> Timber.d("$statusCode(${statusCode.code}): ${message()}")
                }

                // map the ApiResponse.Failure.Error to a customized error model using the mapper.
                map(FetchErrorResponseMapper) {
                    Timber.d("[Code: $code]: $message")
                }
            }
        }
    }

    // handles exceptional cases when the API request gets an exception response.
    // e.g., network connection error.
    override suspend fun onException(apiResponse: ApiResponse.Failure.Exception<T>) {
        withContext(Dispatchers.Main) {
            errorManager.setTriggerMode(TriggerMode.START)
            apiResponse.run {
                Timber.d(message())
                toast(message())
            }
        }
    }

    private fun toast(message: String) {
        Toast.makeText(application, message, Toast.LENGTH_SHORT).show()
    }
}