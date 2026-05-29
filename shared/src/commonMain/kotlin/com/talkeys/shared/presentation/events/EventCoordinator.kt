package com.talkeys.shared.presentation.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkeys.shared.data.events.EventSummary
import com.talkeys.shared.data.events.EventsRepository
import com.talkeys.shared.network.ApiError
import com.talkeys.shared.network.ApiResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class EventsRoute {
    EventList,
    EventCreation
}

sealed interface EventPlatformRequest {
    data class ShareEvent(
        val eventId: String,
        val subject: String,
        val text: String
    ) : EventPlatformRequest

    data class NavigateToEventDetail(val eventId: String) : EventPlatformRequest
    data class NavigateToRoute(val route: EventsRoute) : EventPlatformRequest
}

data class EventCoordinatorUiState(
    val isLoading: Boolean = false,
    val allEvents: List<EventSummary> = emptyList(),
    val filteredEvents: List<EventSummary> = emptyList(),
    val selectedEvent: EventSummary? = null,
    val showLiveEvents: Boolean = true,
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val likedEventIds: Set<String> = emptySet(),
    val registrationIntentEventIds: Set<String> = emptySet(),
    val error: String? = null
)

class EventCoordinator(
    private val repository: EventsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EventCoordinatorUiState(isLoading = true))
    val uiState: StateFlow<EventCoordinatorUiState> = _uiState.asStateFlow()

    private val _platformRequests = MutableSharedFlow<EventPlatformRequest>(extraBufferCapacity = 1)
    val platformRequests: SharedFlow<EventPlatformRequest> = _platformRequests.asSharedFlow()

    init {
        loadEvents()
    }

    fun loadEvents(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.getAllEvents(forceRefresh)) {
                is ApiResult.Success -> {
                    _uiState.value = _uiState.value
                        .copy(isLoading = false, allEvents = result.data, error = null)
                        .withAppliedFilters()
                }
                is ApiResult.Failure -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMessage(result.error)
                    )
                }
            }
        }
    }

    fun refresh() {
        loadEvents(forceRefresh = true)
    }

    fun selectEvent(eventId: String) {
        val selected = _uiState.value.allEvents.firstOrNull { it.id == eventId }
        _uiState.value = _uiState.value.copy(selectedEvent = selected)
    }

    fun toggleLiveFilter() {
        _uiState.value = _uiState.value
            .copy(showLiveEvents = !_uiState.value.showLiveEvents)
            .withAppliedFilters()
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value
            .copy(searchQuery = query)
            .withAppliedFilters()
    }

    fun updateCategory(category: String?) {
        _uiState.value = _uiState.value
            .copy(selectedCategory = category?.takeIf { it.isNotBlank() })
            .withAppliedFilters()
    }

    fun clearFilters() {
        _uiState.value = _uiState.value
            .copy(showLiveEvents = true, searchQuery = "", selectedCategory = null)
            .withAppliedFilters()
    }

    fun toggleLocalLike(eventId: String): Boolean {
        val current = _uiState.value.likedEventIds
        val next = if (eventId in current) current - eventId else current + eventId
        _uiState.value = _uiState.value.copy(likedEventIds = next)
        return eventId in next
    }

    fun requestRegistration(eventId: String) {
        _uiState.value = _uiState.value.copy(
            registrationIntentEventIds = _uiState.value.registrationIntentEventIds + eventId
        )
    }

    fun requestShare(eventId: String) {
        val event = _uiState.value.allEvents.firstOrNull { it.id == eventId } ?: return
        _platformRequests.tryEmit(
            EventPlatformRequest.ShareEvent(
                eventId = event.id,
                subject = "Check out this event: ${event.name}",
                text = event.shareText()
            )
        )
    }

    fun requestNavigateToEventDetail(eventId: String) {
        _platformRequests.tryEmit(EventPlatformRequest.NavigateToEventDetail(eventId))
    }

    fun requestNavigateToCreation() {
        _platformRequests.tryEmit(EventPlatformRequest.NavigateToRoute(EventsRoute.EventCreation))
    }

    fun requestNavigateToList() {
        _platformRequests.tryEmit(EventPlatformRequest.NavigateToRoute(EventsRoute.EventList))
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun EventCoordinatorUiState.withAppliedFilters(): EventCoordinatorUiState {
        val filtered = allEvents
            .filter { if (showLiveEvents) it.isLive else !it.isLive }
            .filter { event ->
                searchQuery.isBlank() ||
                    event.name.contains(searchQuery, ignoreCase = true) ||
                    event.eventDescription?.contains(searchQuery, ignoreCase = true) == true ||
                    event.category.contains(searchQuery, ignoreCase = true)
            }
            .filter { event -> selectedCategory == null || event.category == selectedCategory }

        return copy(filteredEvents = filtered)
    }

    private fun EventSummary.shareText(): String = buildString {
        append("I found this interesting event: ")
        append(name)
        append("\n\n")
        append(eventDescription ?: "No description available")
        append("\n\nDate: ")
        append(startDate)
        append("\nTime: ")
        append(startTime)
        append("\nLocation: ")
        append(location ?: "Online")
    }

    private fun errorMessage(error: ApiError): String = when (error) {
        is ApiError.NetworkError -> "Please check your internet connection and try again."
        is ApiError.HttpError -> "Server error (${error.status}). Please try again."
        is ApiError.ParseError -> "Could not read event data. Please try again."
        is ApiError.Unknown -> error.cause
    }
}
