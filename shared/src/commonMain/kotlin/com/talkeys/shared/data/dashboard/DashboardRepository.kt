package com.talkeys.shared.data.dashboard

import com.talkeys.shared.auth.TokenStorage
import com.talkeys.shared.data.events.EventSummary
import com.talkeys.shared.network.ApiError
import com.talkeys.shared.network.ApiResult

open class DashboardRepository(
    private val api: DashboardApi,
    private val tokenStorage: TokenStorage
) {
    private var cachedProfile: UserProfile? = null
    private val cachedEvents = mutableMapOf<String, List<EventSummary>>()
    private val cachedActivity = mutableMapOf<String, RecentActivity>()

    open suspend fun getUserProfile(forceRefresh: Boolean = false): ApiResult<UserProfile> {
        if (!forceRefresh) cachedProfile?.let { return ApiResult.Success(it) }
        val token = validTokenOrFailure() ?: return ApiResult.Failure(ApiError.HttpError(401, "Missing or expired auth token"))
        return when (val result = api.getUserProfile(token)) {
            is ApiResult.Success -> {
                cachedProfile = result.data
                result
            }
            is ApiResult.Failure -> result
        }
    }

    open suspend fun updateUserProfile(request: UpdateUserProfileRequest): ApiResult<UserProfile> {
        val token = validTokenOrFailure() ?: return ApiResult.Failure(ApiError.HttpError(401, "Missing or expired auth token"))
        return when (val result = api.updateUserProfile(token, request.toPatchMap())) {
            is ApiResult.Success -> {
                cachedProfile = result.data
                result
            }
            is ApiResult.Failure -> result
        }
    }

    open suspend fun getUserEvents(
        type: UserEventType,
        status: UserEventStatus? = null,
        period: DashboardPeriod? = null,
        forceRefresh: Boolean = false
    ): ApiResult<List<EventSummary>> {
        val key = eventsCacheKey(type, status, period)
        if (!forceRefresh) cachedEvents[key]?.let { return ApiResult.Success(it) }
        val token = validTokenOrFailure() ?: return ApiResult.Failure(ApiError.HttpError(401, "Missing or expired auth token"))
        return when (val result = api.getUserEvents(token, type.wireValue, status?.wireValue, period?.wireValue)) {
            is ApiResult.Success -> {
                cachedEvents[key] = result.data.events
                ApiResult.Success(result.data.events)
            }
            is ApiResult.Failure -> result
        }
    }

    open suspend fun getRecentActivity(
        range: DashboardPeriod = DashboardPeriod.OneMonth,
        forceRefresh: Boolean = false
    ): ApiResult<RecentActivity> {
        if (!forceRefresh) cachedActivity[range.wireValue]?.let { return ApiResult.Success(it) }
        val token = validTokenOrFailure() ?: return ApiResult.Failure(ApiError.HttpError(401, "Missing or expired auth token"))
        return when (val result = api.getRecentActivity(token, range.wireValue)) {
            is ApiResult.Success -> {
                val activity = RecentActivity(range = range.wireValue, payload = result.data)
                cachedActivity[range.wireValue] = activity
                ApiResult.Success(activity)
            }
            is ApiResult.Failure -> result
        }
    }

    fun clearCache() {
        cachedProfile = null
        cachedEvents.clear()
        cachedActivity.clear()
    }

    private suspend fun validTokenOrFailure(): String? {
        val token = tokenStorage.getToken()
        return if (!token.isNullOrBlank() && !tokenStorage.isTokenExpired()) token else null
    }

    private fun eventsCacheKey(
        type: UserEventType,
        status: UserEventStatus?,
        period: DashboardPeriod?
    ): String = listOf(type.wireValue, status?.wireValue.orEmpty(), period?.wireValue.orEmpty()).joinToString(":")
}
