package com.talkeys.shared.data.dashboard

import com.talkeys.shared.data.events.EventSummary
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class UserProfile(
    @SerialName("_id") val id: String,
    val name: String,
    val email: String,
    val displayName: String? = null,
    val about: String? = null,
    val pronouns: String? = null,
    val avatarUrl: String? = null,
    val likedEvents: List<String> = emptyList()
)

@Serializable
data class UpdateUserProfileRequest(
    val displayName: String? = null,
    val about: String? = null,
    val pronouns: String? = null,
    val avatarUrl: String? = null
) {
    fun toPatchMap(): Map<String, String> = buildMap {
        displayName?.let { put("displayName", it) }
        about?.let { put("about", it) }
        pronouns?.let { put("pronouns", it) }
        avatarUrl?.let { put("avatarUrl", it) }
    }
}

@Serializable
data class UserEventsResponse(
    val events: List<EventSummary>
)

/**
 * The active Android client treated dashboard activity as Map<String, Any>.
 * Until the backend provides a stable typed activity schema, keep the raw JSON
 * object explicit instead of pretending we know the contract.
 */
@Serializable
data class RecentActivity(
    val range: String,
    val payload: JsonObject
)

enum class UserEventType(val wireValue: String) {
    Registered("registered"),
    Bookmarked("bookmarked"),
    Hosted("hosted")
}

enum class UserEventStatus(val wireValue: String) {
    Past("past"),
    Upcoming("upcoming")
}

enum class DashboardPeriod(val wireValue: String) {
    OneMonth("1m"),
    SixMonths("6m"),
    OneYear("1y")
}
