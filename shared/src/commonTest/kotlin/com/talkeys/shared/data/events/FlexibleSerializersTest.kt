package com.talkeys.shared.data.events

import kotlinx.serialization.json.Json
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class FlexibleSerializersTest {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    // ── FlexibleStringSerializer ────────────────────────────────────

    @Test
    fun flexibleString_jsonString() {
        val input = """{"_id":"1","name":"E","category":"C","ticketPrice":"250","mode":"online","duration":"1h","slots":10,"visibility":"public","startDate":"2025-01-01","startTime":"10:00","totalSeats":50,"isTeamEvent":false,"isPaid":true,"isLive":true}"""
        val event = json.decodeFromString<EventSummary>(input)
        assertEquals("250", event.ticketPrice)
    }

    @Test
    fun flexibleString_jsonNumber() {
        val input = """{"_id":"1","name":"E","category":"C","ticketPrice":500,"mode":"online","duration":"1h","slots":10,"visibility":"public","startDate":"2025-01-01","startTime":"10:00","totalSeats":50,"isTeamEvent":false,"isPaid":true,"isLive":true}"""
        val event = json.decodeFromString<EventSummary>(input)
        assertEquals("500", event.ticketPrice)
    }

    @Test
    fun flexibleString_zero() {
        val input = """{"_id":"1","name":"E","category":"C","ticketPrice":0,"mode":"online","duration":"1h","slots":10,"visibility":"public","startDate":"2025-01-01","startTime":"10:00","totalSeats":50,"isTeamEvent":false,"isPaid":false,"isLive":true}"""
        val event = json.decodeFromString<EventSummary>(input)
        assertEquals("0", event.ticketPrice)
    }

    @Test
    fun flexibleString_decimalNumber() {
        val input = """{"_id":"1","name":"E","category":"C","ticketPrice":99.99,"mode":"online","duration":"1h","slots":10,"visibility":"public","startDate":"2025-01-01","startTime":"10:00","totalSeats":50,"isTeamEvent":false,"isPaid":true,"isLive":true}"""
        val event = json.decodeFromString<EventSummary>(input)
        assertEquals("99.99", event.ticketPrice)
    }

    // ── FlexibleIntSerializer ───────────────────────────────────────

    @Test
    fun flexibleInt_jsonInt() {
        val input = """{"_id":"1","name":"E","category":"C","ticketPrice":"0","mode":"online","duration":"1h","slots":10,"visibility":"public","startDate":"2025-01-01","startTime":"10:00","totalSeats":100,"isTeamEvent":false,"isPaid":false,"isLive":true}"""
        val event = json.decodeFromString<EventSummary>(input)
        assertEquals(100, event.totalSeats)
    }

    @Test
    fun flexibleInt_jsonString() {
        val input = """{"_id":"1","name":"E","category":"C","ticketPrice":"0","mode":"online","duration":"1h","slots":10,"visibility":"public","startDate":"2025-01-01","startTime":"10:00","totalSeats":"200","isTeamEvent":false,"isPaid":false,"isLive":true}"""
        val event = json.decodeFromString<EventSummary>(input)
        assertEquals(200, event.totalSeats)
    }

    @Test
    fun flexibleInt_invalidString_failsParsing() {
        val input = """{"_id":"1","name":"E","category":"C","ticketPrice":"0","mode":"online","duration":"1h","slots":10,"visibility":"public","startDate":"2025-01-01","startTime":"10:00","totalSeats":"abc","isTeamEvent":false,"isPaid":false,"isLive":true}"""
        assertFailsWith<SerializationException> {
            json.decodeFromString<EventSummary>(input)
        }
    }
}
