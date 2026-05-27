package com.talkeys.shared.presentation.counter

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Demo-only shared ViewModel proving reactive StateFlow observation
 * from both Android Compose and SwiftUI (via SKIE AsyncSequence).
 *
 * This is infrastructure proof, not a production feature.
 * Later phases will replace this with real feature ViewModels.
 */
class CounterViewModel : ViewModel() {

    private val _count = MutableStateFlow(0)
    val count: StateFlow<Int> = _count.asStateFlow()

    fun increment() {
        _count.value++
    }
}
