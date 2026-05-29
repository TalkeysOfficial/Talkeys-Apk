package com.talkeys.shared.presentation.events

import com.talkeys.shared.data.events.EventSummary
import com.talkeys.shared.data.events.EventsRepository
import com.talkeys.shared.data.events.EventsApi
import com.talkeys.shared.data.events.EventListResponse
import com.talkeys.shared.data.events.EventListData
import com.talkeys.shared.data.events.Pagination
import com.talkeys.shared.data.events.EventDetail
import com.talkeys.shared.data.events.EventDetailResponse
import com.talkeys.shared.network.ApiClient
import com.talkeys.shared.network.ApiError
import com.talkeys.shared.network.ApiResult
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

@OptIn(ExperimentalCoroutinesApi::class)
class EventsListViewModelTest {

    private val sampleEvents = listOf(
        EventSummary(
            id = "1", name = "Live Event", category = "Tech",
            ticketPrice = "0", mode = "online", duration = "1h",
            slots = 5, visibility = "public", startDate = "2025-06-01",
            startTime = "10:00", totalSeats = 50, isTeamEvent = false,
            isPaid = false, isLive = true
        ),
        EventSummary(
            id = "2", name = "Past Event", category = "Art",
            ticketPrice = "100", mode = "offline", duration = "2h",
            slots = 10, visibility = "public", startDate = "2024-01-01",
            startTime = "14:00", totalSeats = 30, isTeamEvent = true,
            isPaid = true, isLive = false
        )
    )

    private fun fakeRepository(result: ApiResult<List<EventSummary>>): EventsRepository {
        return object : EventsRepository(
            // Pass a dummy EventsApi — we override getAllEvents
            EventsApi(ApiClient())
        ) {
            override suspend fun getAllEvents(forceRefresh: Boolean): ApiResult<List<EventSummary>> {
                return result
            }
        }
    }

    @Test
    fun loadEvents_success_showsLiveEventsByDefault() = runTest {
        val repo = fakeRepository(ApiResult.Success(sampleEvents))
        val vm = EventsListViewModel(repo)

        // After init, loadEvents is called
        advanceUntilIdle()

        val state = vm.uiState.value
        assertIs<EventsListUiState.Content>(state)
        assertEquals(1, state.events.size)
        assertEquals("Live Event", state.events[0].name)
        assertEquals(true, state.showLiveOnly)
    }

    @Test
    fun toggleFilter_switchesToPastEvents() = runTest {
        val repo = fakeRepository(ApiResult.Success(sampleEvents))
        val vm = EventsListViewModel(repo)
        advanceUntilIdle()

        vm.toggleFilter()

        val state = vm.uiState.value
        assertIs<EventsListUiState.Content>(state)
        assertEquals(1, state.events.size)
        assertEquals("Past Event", state.events[0].name)
        assertEquals(false, state.showLiveOnly)
    }

    @Test
    fun loadEvents_failure_showsError() = runTest {
        val repo = fakeRepository(ApiResult.Failure(ApiError.NetworkError))
        val vm = EventsListViewModel(repo)
        advanceUntilIdle()

        val state = vm.uiState.value
        assertIs<EventsListUiState.Error>(state)
    }

    @Test
    fun retry_afterError_reloadsSuccessfully() = runTest {
        var callCount = 0
        val repo = object : EventsRepository(EventsApi(ApiClient())) {
            override suspend fun getAllEvents(forceRefresh: Boolean): ApiResult<List<EventSummary>> {
                callCount++
                return if (callCount == 1) {
                    ApiResult.Failure(ApiError.NetworkError)
                } else {
                    ApiResult.Success(sampleEvents)
                }
            }
        }

        val vm = EventsListViewModel(repo)
        advanceUntilIdle()
        assertIs<EventsListUiState.Error>(vm.uiState.value)

        vm.retry()
        advanceUntilIdle()

        val state = vm.uiState.value
        assertIs<EventsListUiState.Content>(state)
        assertEquals(1, state.events.size)
    }
}
