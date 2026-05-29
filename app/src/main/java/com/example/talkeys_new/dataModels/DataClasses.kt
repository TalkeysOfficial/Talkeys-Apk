package com.example.talkeys_new.dataModels

data class EventResponse(
    val _id: String,
    val name: String,
    val category: String,
    val ticketPrice: Any, // Changed from Int to Any to handle both Int and String
    val mode: String,
    val location: String?,
    val duration: String,
    val slots: Int,
    val visibility: String,
    val startDate: String,
    val startTime: String,
    val endRegistrationDate: String?, // Changed to nullable to handle null values from backend
    val totalSeats: Any, // Changed from Int to Any to handle both Int and String
    val eventDescription: String?,
    val photographs: List<String>?,
    val prizes: String?,
    val isTeamEvent: Boolean,
    val isPaid: Boolean,
    val isLive: Boolean,
    val organizerName: String?,
    val organizerEmail: String?,
    val organizerContact: String?,
)

// Fixed data class to match the actual API response structure
data class EventListResponse(
    val status: String,
    val data: EventData
)

data class EventData(
    val events: List<EventResponse>,
    val pagination: Pagination
)

data class Pagination(
    val total: Int,
    val page: Int,
    val pages: Int,
    val limit: Int
)
data class EventDetailResponse(
    val status: String,
    val data: EventDetail
)

data class EventDetail(
    val _id: String,
    val name: String,
    val category: String,
    val mode: String,
    val location: String,
    val duration: String,
    val ticketPrice: String,
    val totalSeats: Int,
    val slots: Int,
    val visibility: String,
    val prizes: String,
    val photographs: List<String>?,
    val startDate: String,
    val startTime: String,
    val endDate: String,
    val endTime: String,
    val endRegistrationDate: String?, // Changed to nullable to handle null values from backend
    val eventDescription: String?,
    val isLive: Boolean,
    val isPaid: Boolean,
    val isTeamEvent: Boolean,
    val organizerContact: String,
    val organizerEmail: String,
    val organizerName: String,
    val registrationCount: Int,
    val sponserImages: List<String>,
    val registrationLink: String,
    val availableSeats: Int
)
