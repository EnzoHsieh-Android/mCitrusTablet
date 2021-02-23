package com.citrus.mCitrusTablet.model.api


import com.skydoves.sandwich.ApiErrorModelMapper
import com.skydoves.sandwich.ApiResponse
import com.skydoves.sandwich.message

/**
 * A mapper for mapping [ApiResponse.Failure.Error] response as custom [FetchErrorResponse] instance.
 *
 * @see [ApiErrorModelMapper](https://github.com/skydoves/sandwich#apierrormodelmapper)
 */
object ErrorResponseMapper : ApiErrorModelMapper<FetchErrorResponse> {

  /**
   * maps the [ApiResponse.Failure.Error] to the [FetchErrorResponse] using the mapper.
   *
   * @param apiErrorResponse The [ApiResponse.Failure.Error] error response from the network request.
   * @return A customized [FetchErrorResponse] error response.
   */
  override fun map(apiErrorResponse: ApiResponse.Failure.Error<*>): FetchErrorResponse {
    return FetchErrorResponse(apiErrorResponse.statusCode.code, apiErrorResponse.message())
  }
}
