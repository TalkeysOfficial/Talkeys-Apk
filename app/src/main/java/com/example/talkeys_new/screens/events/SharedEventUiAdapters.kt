package com.example.talkeys_new.screens.events

import com.example.talkeys_new.dataModels.EventResponse
import com.talkeys.shared.data.events.EventDetail
import com.talkeys.shared.data.events.EventSummary

/**
 * Temporary Android rendering adapter.
 *
 * Existing Compose cards still accept the Android DTO. The event read pipeline
 * is owned by the shared KMP layer; this mapper lets that state feed the
 * current UI without moving unrelated rendering and action code in Phase 4.
 */
fun EventSummary.toAndroidEventResponse(): EventResponse = EventResponse(
    _id = id,
    name = name,
    category = category,
    ticketPrice = ticketPrice,
    mode = mode,
    location = location,
    duration = duration,
    slots = slots,
    visibility = visibility,
    startDate = startDate,
    startTime = startTime,
    endRegistrationDate = endRegistrationDate,
    totalSeats = totalSeats,
    eventDescription = eventDescription,
    photographs = photographs,
    prizes = prizes,
    isTeamEvent = isTeamEvent,
    isPaid = isPaid,
    isLive = isLive,
    organizerName = organizerName,
    organizerEmail = organizerEmail,
    organizerContact = organizerContact
)

fun EventDetail.toAndroidEventResponse(): EventResponse = EventResponse(
    _id = id,
    name = name,
    category = category,
    ticketPrice = ticketPrice,
    mode = mode,
    location = location,
    duration = duration,
    slots = slots,
    visibility = visibility,
    startDate = startDate,
    startTime = startTime,
    endRegistrationDate = endRegistrationDate,
    totalSeats = totalSeats,
    eventDescription = eventDescription,
    photographs = photographs,
    prizes = prizes,
    isTeamEvent = isTeamEvent,
    isPaid = isPaid,
    isLive = isLive,
    organizerName = organizerName,
    organizerEmail = organizerEmail,
    organizerContact = organizerContact
)
