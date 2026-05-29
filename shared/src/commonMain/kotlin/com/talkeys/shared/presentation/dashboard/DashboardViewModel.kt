package com.talkeys.shared.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkeys.shared.data.dashboard.DashboardPeriod
import com.talkeys.shared.data.dashboard.DashboardRepository
import com.talkeys.shared.data.dashboard.RecentActivity
import com.talkeys.shared.data.dashboard.UserEventStatus
import com.talkeys.shared.data.dashboard.UserEventType
import com.talkeys.shared.data.dashboard.UserProfile
import com.talkeys.shared.data.events.EventSummary
import com.talkeys.shared.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = false,
    val profile: UserProfile? = null,
    val events: List<EventSummary> = emptyList(),
    val activity: RecentActivity? = null,
    val error: String? = null
)

class SharedDashboardViewModel(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState(isLoading = true))
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            val profileResult = repository.getUserProfile(forceRefresh = true)
            val eventsResult = repository.getUserEvents(
                type = UserEventType.Registered,
                status = null,
                period = null,
                forceRefresh = true
            )
            val activityResult = repository.getRecentActivity(
                range = DashboardPeriod.OneMonth,
                forceRefresh = true
            )

            val firstFailure = listOf(profileResult, eventsResult, activityResult)
                .filterIsInstance<ApiResult.Failure>()
                .firstOrNull()

            _uiState.value = DashboardUiState(
                isLoading = false,
                profile = (profileResult as? ApiResult.Success)?.data,
                events = (eventsResult as? ApiResult.Success)?.data.orEmpty(),
                activity = (activityResult as? ApiResult.Success)?.data,
                error = firstFailure?.let { errorMessage(it.error) }
            )
        }
    }

    fun loadEvents(
        type: UserEventType,
        status: UserEventStatus? = null,
        period: DashboardPeriod? = null
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            when (val result = repository.getUserEvents(type, status, period, forceRefresh = true)) {
                is ApiResult.Success -> _uiState.value = _uiState.value.copy(isLoading = false, events = result.data)
                is ApiResult.Failure -> _uiState.value = _uiState.value.copy(isLoading = false, error = errorMessage(result.error))
            }
        }
    }
}
