package com.talkeys.shared.presentation.events

import com.talkeys.shared.data.events.EventSummary
import com.talkeys.shared.data.events.EventsApi
import com.talkeys.shared.data.events.EventsRepository
import com.talkeys.shared.network.ApiClient
import com.talkeys.shared.network.ApiError
import com.talkeys.shared.network.ApiResult
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class EventCoordinatorTest {

    private val events = listOf(
        event(id = "1", name = "Kotlin Meetup", category = "Tech", isLive = true),
        event(id = "2", name = "Design Jam", category = "Design", isLive = true),
        event(id = "3", name = "Past Talk", category = "Tech", isLive = false)
    )

    @Test
    fun loadEvents_appliesLiveFilterByDefault() = runTest {
        val coordinator = EventCoordinator(repositoryReturning(ApiResult.Success(events)))
        advanceUntilIdle()

        val state = coordinator.uiState.value
        assertEquals(3, state.allEvents.size)
        assertEquals(listOf("1", "2"), state.filteredEvents.map { it.id })
        assertEquals(true, state.showLiveEvents)
    }

    @Test
    fun searchAndCategoryFiltersAreAppliedTogether() = runTest {
        val coordinator = EventCoordinator(repositoryReturning(ApiResult.Success(events)))
        advanceUntilIdle()

        coordinator.updateSearchQuery("kotlin")
        coordinator.updateCategory("Tech")

        assertEquals(listOf("1"), coordinator.uiState.value.filteredEvents.map { it.id })
    }

    @Test
    fun toggleLocalLike_updatesLikedIds() = runTest {
        val coordinator = EventCoordinator(repositoryReturning(ApiResult.Success(events)))
        advanceUntilIdle()

        assertTrue(coordinator.toggleLocalLike("1"))
        assertEquals(setOf("1"), coordinator.uiState.value.likedEventIds)

        assertEquals(false, coordinator.toggleLocalLike("1"))
        assertEquals(emptySet(), coordinator.uiState.value.likedEventIds)
    }

    @Test
    fun requestShare_emitsPlatformRequest() = runTest {
        val coordinator = EventCoordinator(repositoryReturning(ApiResult.Success(events)))
        advanceUntilIdle()

        val request = async { coordinator.platformRequests.first() }
        runCurrent()
        coordinator.requestShare("1")
        val emitted = request.await()

        assertIs<EventPlatformRequest.ShareEvent>(emitted)
        assertEquals("1", emitted.eventId)
        assertTrue(emitted.text.contains("Kotlin Meetup"))
    }

    @Test
    fun loadEvents_failure_setsError() = runTest {
        val coordinator = EventCoordinator(repositoryReturning(ApiResult.Failure(ApiError.NetworkError)))
        advanceUntilIdle()

        assertEquals("Please check your internet connection and try again.", coordinator.uiState.value.error)
    }

    private fun repositoryReturning(result: ApiResult<List<EventSummary>>) =
        object : EventsRepository(EventsApi(ApiClient())) {
            override suspend fun getAllEvents(forceRefresh: Boolean): ApiResult<List<EventSummary>> = result
        }

    private fun event(
        id: String,
        name: String,
        category: String,
        isLive: Boolean
    ) = EventSummary(
        id = id,
        name = name,
        category = category,
        ticketPrice = "0",
        mode = "online",
        duration = "1h",
        slots = 10,
        visibility = "public",
        startDate = "2026-01-01",
        startTime = "10:00",
        totalSeats = 100,
        isTeamEvent = false,
        isPaid = false,
        isLive = isLive,
        eventDescription = "$name description"
    )
}
