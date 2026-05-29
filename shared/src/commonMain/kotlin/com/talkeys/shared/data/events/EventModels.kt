package com.talkeys.shared.data.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Shared event models matching the confirmed backend JSON contract.
 *
 * Source: Android Retrofit `EventApiService` endpoints:
 *   - GET getEvents        → [EventListResponse]
 *   - GET getEventById/{id} → [EventDetailResponse]
 *
 * Loose-field handling:
 *   - `ticketPrice` arrives as JSON number OR string depending on endpoint;
 *     exposed as String to avoid floating-point precision loss on monetary values.
 *   - `totalSeats` arrives as JSON number OR string in list endpoint, always Int
 *     in detail; exposed as Int via [FlexibleIntSerializer].
 */

// ── List endpoint response ──────────────────────────────────────────

@Serializable
data class EventListResponse(
    val status: String,
    val data: EventListData
)

@Serializable
data class EventListData(
    val events: List<EventSummary>,
    val pagination: Pagination
)

@Serializable
data class Pagination(
    val total: Int,
    val page: Int,
    val pages: Int,
    val limit: Int
)

/**
 * Event item as returned in the list endpoint (`GET getEvents`).
 *
 * Field names use [SerialName] where they differ from Kotlin conventions.
 * Nullable fields match observed backend behaviour (null or missing).
 */
@Serializable
data class EventSummary(
    @SerialName("_id") val id: String,
    val name: String,
    val category: String,
    @Serializable(with = FlexibleStringSerializer::class)
    val ticketPrice: String,
    val mode: String,
    val location: String? = null,
    val duration: String,
    val slots: Int,
    val visibility: String,
    val startDate: String,
    val startTime: String,
    val endRegistrationDate: String? = null,
    @Serializable(with = FlexibleIntSerializer::class)
    val totalSeats: Int,
    val eventDescription: String? = null,
    val photographs: List<String>? = null,
    val prizes: String? = null,
    val isTeamEvent: Boolean,
    val isPaid: Boolean,
    val isLive: Boolean,
    val organizerName: String? = null,
    val organizerEmail: String? = null,
    val organizerContact: String? = null
)

// ── Detail endpoint response ────────────────────────────────────────

@Serializable
data class EventDetailResponse(
    val status: String,
    val data: EventDetail
)

/**
 * Full event detail as returned by `GET getEventById/{id}`.
 *
 * Contains additional fields not present in the list response:
 * endDate, endTime, registrationCount, sponserImages (sic — backend typo),
 * registrationLink, availableSeats.
 */
@Serializable
data class EventDetail(
    @SerialName("_id") val id: String,
    val name: String,
    val category: String,
    val mode: String,
    val location: String? = null,
    val duration: String,
    @Serializable(with = FlexibleStringSerializer::class)
    val ticketPrice: String,
    @Serializable(with = FlexibleIntSerializer::class)
    val totalSeats: Int,
    val slots: Int,
    val visibility: String,
    val prizes: String? = null,
    val photographs: List<String>? = null,
    val startDate: String,
    val startTime: String,
    val endDate: String? = null,
    val endTime: String? = null,
    val endRegistrationDate: String? = null,
    val eventDescription: String? = null,
    val isLive: Boolean,
    val isPaid: Boolean,
    val isTeamEvent: Boolean,
    val organizerContact: String? = null,
    val organizerEmail: String? = null,
    val organizerName: String? = null,
    val registrationCount: Int = 0,
    // Backend field name has a typo ("sponser" instead of "sponsor")
    val sponserImages: List<String>? = null,
    val registrationLink: String? = null,
    val availableSeats: Int = 0
)
