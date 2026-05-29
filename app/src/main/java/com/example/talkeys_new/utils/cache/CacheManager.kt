package com.example.talkeys_new.utils.cache

import android.util.Log
import com.example.talkeys_new.dataModels.EventResponse
import com.talkeys.shared.data.dashboard.UserProfile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Centralized cache manager for the Talkeys application
 * Manages different types of caches with appropriate TTL and size limits
 */
object CacheManager {
    private val TAG = "CacheManager"
    private val cacheScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    // Cache configurations
    private const val EVENTS_CACHE_SIZE = 100
    private const val EVENTS_TTL_MINUTES = 30L
    
    private const val USER_PROFILE_CACHE_SIZE = 50
    private const val USER_PROFILE_TTL_MINUTES = 15L
    
    private const val EVENT_DETAILS_CACHE_SIZE = 50
    private const val EVENT_DETAILS_TTL_MINUTES = 60L
    
    private const val USER_EVENTS_CACHE_SIZE = 20
    private const val USER_EVENTS_TTL_MINUTES = 10L
    
    // Cache instances
    val eventsCache = LruCache<List<EventResponse>>(
        maxSize = EVENTS_CACHE_SIZE,
        ttlMillis = TimeUnit.MINUTES.toMillis(EVENTS_TTL_MINUTES)
    )
    
    val userProfileCache = LruCache<UserProfile>(
        maxSize = USER_PROFILE_CACHE_SIZE,
        ttlMillis = TimeUnit.MINUTES.toMillis(USER_PROFILE_TTL_MINUTES)
    )
    
    val eventDetailsCache = LruCache<EventResponse>(
        maxSize = EVENT_DETAILS_CACHE_SIZE,
        ttlMillis = TimeUnit.MINUTES.toMillis(EVENT_DETAILS_TTL_MINUTES)
    )
    
    val userEventsCache = LruCache<List<EventResponse>>(
        maxSize = USER_EVENTS_CACHE_SIZE,
        ttlMillis = TimeUnit.MINUTES.toMillis(USER_EVENTS_TTL_MINUTES)
    )
    
    val recentActivityCache = LruCache<Map<String, Any>>(
        maxSize = 20,
        ttlMillis = TimeUnit.MINUTES.toMillis(5L)
    )
    
    init {
        Log.d(TAG, "CacheManager initialized")
        startPeriodicCleanup()
    }
    
    /**
     * Cache keys for different data types
     */
    object Keys {
        const val ALL_EVENTS = "all_events"
        const val USER_PROFILE_PREFIX = "user_profile_"
        const val EVENT_DETAIL_PREFIX = "event_detail_"
        const val USER_EVENTS_PREFIX = "user_events_"
        const val RECENT_ACTIVITY_PREFIX = "recent_activity_"
        
        fun userProfile(userId: String) = "$USER_PROFILE_PREFIX$userId"
        fun eventDetail(eventId: String) = "$EVENT_DETAIL_PREFIX$eventId"
        fun userEvents(type: String, status: String?, period: String?) = 
            "$USER_EVENTS_PREFIX${type}_${status}_$period"
        fun recentActivity(range: String?) = "$RECENT_ACTIVITY_PREFIX$range"
    }
    
    /**
     * Clears all caches
     */
    suspend fun clearAllCaches() {
        eventsCache.clear()
        userProfileCache.clear()
        eventDetailsCache.clear()
        userEventsCache.clear()
        recentActivityCache.clear()
        Log.d(TAG, "All caches cleared")
    }
    
    /**
     * Clears user-specific caches (useful on logout)
     */
    suspend fun clearUserCaches() {
        userProfileCache.clear()
        userEventsCache.clear()
        recentActivityCache.clear()
        Log.d(TAG, "User-specific caches cleared")
    }
    
    /**
     * Gets cache statistics for debugging
     */
    fun getCacheStats(): Map<String, Int> {
        return mapOf(
            "events" to eventsCache.size(),
            "userProfile" to userProfileCache.size(),
            "eventDetails" to eventDetailsCache.size(),
            "userEvents" to userEventsCache.size(),
            "recentActivity" to recentActivityCache.size()
        )
    }
    
    /**
     * Starts periodic cleanup of expired entries
     */
    private fun startPeriodicCleanup() {
        cacheScope.launch {
            while (true) {
                try {
                    kotlinx.coroutines.delay(TimeUnit.MINUTES.toMillis(5)) // Cleanup every 5 minutes
                    
                    eventsCache.cleanupExpired()
                    userProfileCache.cleanupExpired()
                    eventDetailsCache.cleanupExpired()
                    userEventsCache.cleanupExpired()
                    recentActivityCache.cleanupExpired()
                    
                } catch (e: Exception) {
                    Log.e(TAG, "Error during cache cleanup", e)
                }
            }
        }
    }
}
