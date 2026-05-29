package com.example.talkeys_new.screens.dashboard

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.talkeys.shared.data.dashboard.DashboardRepository
import com.talkeys.shared.presentation.dashboard.ProfileViewModel
import com.talkeys.shared.presentation.dashboard.ProfileViewModelFactory
import com.talkeys.shared.presentation.dashboard.SharedDashboardViewModel
import com.talkeys.shared.presentation.dashboard.SharedDashboardViewModelFactory
import org.koin.compose.koinInject

@Composable
fun sharedProfileViewModel(
    repository: DashboardRepository = koinInject()
): ProfileViewModel = viewModel(
    factory = ProfileViewModelFactory(repository)
)

@Composable
fun sharedDashboardViewModel(
    repository: DashboardRepository = koinInject()
): SharedDashboardViewModel = viewModel(
    factory = SharedDashboardViewModelFactory(repository)
)
