package com.example.talkeys_new.screens.events

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.talkeys.shared.data.events.EventsRepository
import com.talkeys.shared.presentation.events.EventCoordinator
import com.talkeys.shared.presentation.events.EventCoordinatorFactory
import com.talkeys.shared.presentation.events.EventCreationViewModel
import com.talkeys.shared.presentation.events.EventCreationViewModelFactory
import com.talkeys.shared.presentation.events.EventDetailViewModel
import com.talkeys.shared.presentation.events.EventDetailViewModelFactory
import com.talkeys.shared.presentation.events.EventsListViewModel
import com.talkeys.shared.presentation.events.EventsListViewModelFactory
import org.koin.compose.koinInject

/**
 * Composable helpers for obtaining shared KMP ViewModels on Android.
 *
 * Usage:
 *   val listVm = sharedEventsListViewModel()
 *   val detailVm = sharedEventDetailViewModel()
 *
 * These pull [EventsRepository] from the Koin graph (registered in
 * [com.talkeys.shared.di.sharedModule]) and create the ViewModel via
 * the shared KMP factory, so Android and iOS get identical behaviour.
 *
 * The current event read pipeline is shared KMP. Android keeps platform UI and
 * action handling, while events list/detail data comes from shared repositories
 * and shared ViewModels.
 */

@Composable
fun sharedEventsListViewModel(
    repository: EventsRepository = koinInject()
): EventsListViewModel = viewModel(
    factory = EventsListViewModelFactory(repository)
)

@Composable
fun sharedEventDetailViewModel(
    repository: EventsRepository = koinInject()
): EventDetailViewModel = viewModel(
    factory = EventDetailViewModelFactory(repository)
)

@Composable
fun sharedEventCoordinator(
    repository: EventsRepository = koinInject()
): EventCoordinator = viewModel(
    factory = EventCoordinatorFactory(repository)
)

@Composable
fun sharedEventCreationViewModel(): EventCreationViewModel = viewModel(
    factory = EventCreationViewModelFactory()
)
