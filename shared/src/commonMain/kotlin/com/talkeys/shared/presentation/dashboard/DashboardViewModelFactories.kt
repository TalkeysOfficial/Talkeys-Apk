package com.talkeys.shared.presentation.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.talkeys.shared.data.dashboard.DashboardRepository
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import kotlin.reflect.KClass

class ProfileViewModelFactory(
    private val repository: DashboardRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        require(modelClass == ProfileViewModel::class) {
            "ProfileViewModelFactory cannot create ${modelClass.simpleName}"
        }
        return ProfileViewModel(repository) as T
    }
}

class SharedDashboardViewModelFactory(
    private val repository: DashboardRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        require(modelClass == SharedDashboardViewModel::class) {
            "SharedDashboardViewModelFactory cannot create ${modelClass.simpleName}"
        }
        return SharedDashboardViewModel(repository) as T
    }
}

private object KoinHelper : KoinComponent

val profileViewModelFactory: ViewModelProvider.Factory
    get() = ProfileViewModelFactory(KoinHelper.get())

val sharedDashboardViewModelFactory: ViewModelProvider.Factory
    get() = SharedDashboardViewModelFactory(KoinHelper.get())
