package com.talkeys.shared.presentation.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.talkeys.shared.data.events.EventsRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.reflect.KClass

/**
 * Factory for [EventsListViewModel]. Usable by both Android
 * (via ViewModelProvider) and Swift (via the generic
 * IosViewModelStoreOwner.viewModel<T> bridge).
 */
class EventsListViewModelFactory(
    private val repository: EventsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        require(modelClass == EventsListViewModel::class) {
            "EventsListViewModelFactory cannot create ${modelClass.simpleName}"
        }
        return EventsListViewModel(repository) as T
    }
}

/**
 * Factory for [EventDetailViewModel].
 */
class EventDetailViewModelFactory(
    private val repository: EventsRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        require(modelClass == EventDetailViewModel::class) {
            "EventDetailViewModelFactory cannot create ${modelClass.simpleName}"
        }
        return EventDetailViewModel(repository) as T
    }
}

/**
 * Pre-wired factory accessors that pull [EventsRepository] from the Koin
 * graph. Callable from Swift as:
 *
 *     EventsViewModelFactoriesKt.eventsListViewModelFactory
 *     EventsViewModelFactoriesKt.eventDetailViewModelFactory
 */
private object KoinHelper : KoinComponent

val eventsListViewModelFactory: ViewModelProvider.Factory
    get() = EventsListViewModelFactory(KoinHelper.get())

val eventDetailViewModelFactory: ViewModelProvider.Factory
    get() = EventDetailViewModelFactory(KoinHelper.get())
