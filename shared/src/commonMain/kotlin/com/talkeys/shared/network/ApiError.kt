package com.talkeys.shared.network

/**
 * Typed error model for API failures.
 * Used by [ApiResult.Failure] to carry structured error information
 * through UI state without throwing exceptions across Kotlin/Native boundaries.
 */
sealed interface ApiError {
    data object NetworkError : ApiError
    data class HttpError(val status: Int, val body: String?) : ApiError
    data class ParseError(val cause: String) : ApiError
    data class Unknown(val cause: String) : ApiError
}
