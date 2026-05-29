package com.talkeys.shared.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkeys.shared.data.dashboard.DashboardRepository
import com.talkeys.shared.data.dashboard.UpdateUserProfileRequest
import com.talkeys.shared.data.dashboard.UserProfile
import com.talkeys.shared.network.ApiError
import com.talkeys.shared.network.ApiResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ProfileUiState {
    data object Loading : ProfileUiState
    data class Content(val profile: UserProfile) : ProfileUiState
    data class Error(val message: String) : ProfileUiState
}

class ProfileViewModel(
    private val repository: DashboardRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    fun loadProfile(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            when (val result = repository.getUserProfile(forceRefresh)) {
                is ApiResult.Success -> _uiState.value = ProfileUiState.Content(result.data)
                is ApiResult.Failure -> _uiState.value = ProfileUiState.Error(errorMessage(result.error))
            }
        }
    }

    fun updateProfile(request: UpdateUserProfileRequest) {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            when (val result = repository.updateUserProfile(request)) {
                is ApiResult.Success -> _uiState.value = ProfileUiState.Content(result.data)
                is ApiResult.Failure -> _uiState.value = ProfileUiState.Error(errorMessage(result.error))
            }
        }
    }

    fun retry() {
        loadProfile(forceRefresh = true)
    }
}

internal fun errorMessage(error: ApiError): String = when (error) {
    is ApiError.NetworkError -> "Please check your internet connection and try again."
    is ApiError.HttpError -> if (error.status == 401) "Please sign in again." else "Server error (${error.status}). Please try again."
    is ApiError.ParseError -> "Could not read server data. Please try again."
    is ApiError.Unknown -> error.cause
}
