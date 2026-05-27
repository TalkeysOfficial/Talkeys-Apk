package com.talkeys.shared.network

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class ApiResultTest {

    @Test
    fun successHoldsData() {
        val result: ApiResult<String> = ApiResult.Success("hello")
        assertIs<ApiResult.Success<String>>(result)
        assertEquals("hello", result.data)
    }

    @Test
    fun failureHoldsNetworkError() {
        val result: ApiResult<String> = ApiResult.Failure(ApiError.NetworkError)
        assertIs<ApiResult.Failure>(result)
        assertIs<ApiError.NetworkError>(result.error)
    }

    @Test
    fun failureHoldsHttpError() {
        val result: ApiResult<String> = ApiResult.Failure(ApiError.HttpError(404, "Not Found"))
        assertIs<ApiResult.Failure>(result)
        val error = result.error
        assertIs<ApiError.HttpError>(error)
        assertEquals(404, error.status)
        assertEquals("Not Found", error.body)
    }

    @Test
    fun failureHoldsParseError() {
        val result: ApiResult<Int> = ApiResult.Failure(ApiError.ParseError("bad json"))
        assertIs<ApiResult.Failure>(result)
        assertIs<ApiError.ParseError>(result.error)
    }

    @Test
    fun failureHoldsUnknownError() {
        val result: ApiResult<Int> = ApiResult.Failure(ApiError.Unknown("unexpected"))
        assertIs<ApiResult.Failure>(result)
        assertIs<ApiError.Unknown>(result.error)
        assertEquals("unexpected", (result.error as ApiError.Unknown).cause)
    }
}
