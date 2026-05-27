package com.talkeys.shared.network

/**
 * Typed result wrapper for API calls.
 * Named `ApiResult` to avoid conflict with `kotlin.Result`.
 *
 * ViewModels expose [ApiResult] in UI state rather than throwing exceptions,
 * which is critical for safe consumption across the Kotlin/Native boundary
 * (exceptions in `for await` loops crash the Swift process).
 */
sealed interface ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>
    data class Failure(val error: ApiError) : ApiResult<Nothing>
}
