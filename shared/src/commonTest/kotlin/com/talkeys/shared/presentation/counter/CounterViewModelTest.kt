package com.talkeys.shared.presentation.counter

import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class CounterViewModelTest {

    @Test
    fun initialCountIsZero() {
        val vm = CounterViewModel()
        assertEquals(0, vm.count.value)
    }

    @Test
    fun incrementUpdatesCount() = runTest {
        val vm = CounterViewModel()
        vm.increment()
        assertEquals(1, vm.count.value)
    }

    @Test
    fun multipleIncrementsAccumulate() = runTest {
        val vm = CounterViewModel()
        repeat(5) { vm.increment() }
        assertEquals(5, vm.count.value)
    }
}
