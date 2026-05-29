package com.talkeys.shared.data.events

import com.talkeys.shared.logging.AppLogger
import com.talkeys.shared.network.ApiResult

/**
 * Read-only events repository. Wraps [EventsApi] and provides a small
 * in-memory cache for list and detail results.
 *
 * Observable UI state (loading/error/content) belongs in the ViewModel,
 * not here.
 */
open class EventsRepository(private val api: EventsApi) {

    // Simple in-memory caches
    private var cachedList: List<EventSummary>? = null
    private val cachedDetails = mutableMapOf<String, EventDetail>()

    /**
     * Get all events. Returns cached data unless [forceRefresh] is true.
     */
    open suspend fun getAllEvents(forceRefresh: Boolean = false): ApiResult<List<EventSummary>> {
        if (!forceRefresh) {
            cachedList?.let {
                AppLogger.d("EventsRepository", "Returning ${it.size} cached events")
                return ApiResult.Success(it)
            }
        }

        return when (val result = api.getAllEvents()) {
            is ApiResult.Success -> {
                val events = result.data.data.events
                cachedList = events
                AppLogger.d("EventsRepository", "Fetched and cached ${events.size} events")
                ApiResult.Success(events)
            }
            is ApiResult.Failure -> result
        }
    }

    /**
     * Get event detail by ID. Returns cached data unless [forceRefresh] is true.
     */
    open suspend fun getEventById(
        eventId: String,
        forceRefresh: Boolean = false
    ): ApiResult<EventDetail> {
        if (!forceRefresh) {
            cachedDetails[eventId]?.let {
                AppLogger.d("EventsRepository", "Returning cached detail for $eventId")
                return ApiResult.Success(it)
            }
        }

        return when (val result = api.getEventById(eventId)) {
            is ApiResult.Success -> {
                val detail = result.data.data
                cachedDetails[eventId] = detail
                AppLogger.d("EventsRepository", "Fetched and cached detail for ${detail.name}")
                ApiResult.Success(detail)
            }
            is ApiResult.Failure -> result
        }
    }

    /**
     * Clear all caches.
     */
    fun clearCache() {
        cachedList = null
        cachedDetails.clear()
    }
}
