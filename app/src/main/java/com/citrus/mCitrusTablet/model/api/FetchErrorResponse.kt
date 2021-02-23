package com.citrus.mCitrusTablet.model.api

import com.skydoves.sandwich.ApiErrorModelMapper
import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.message

/**
 * A customized error response.
 *
 * @param code A network response code.
 * @param message A network error message.
 */
data class FetchErrorResponse(
  val code: Int,
  val message: String?
)

object FetchErrorResponseMapper : ApiErrorModelMapper<FetchErrorResponse> {

  override fun map(apiErrorResponse: ApiResponse.Failure.Error<*>): FetchErrorResponse {
    return FetchErrorResponse(apiErrorResponse.statusCode.code, apiErrorResponse.message())
  }
}