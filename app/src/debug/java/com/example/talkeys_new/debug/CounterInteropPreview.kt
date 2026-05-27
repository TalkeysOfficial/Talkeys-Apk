package com.example.talkeys_new.debug

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.talkeys.shared.presentation.counter.CounterViewModel

/**
 * Debug-only Compose preview proving that the shared [CounterViewModel]
 * can be observed from Android via collectAsState().
 *
 * This is NOT a production screen. It lives under src/debug and is not
 * reachable from the real app navigation.
 */
@Composable
fun CounterInteropDemo(vm: CounterViewModel = viewModel()) {
    val count by vm.count.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Shared Counter: $count", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = { vm.increment() }) {
            Text("Increment")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CounterInteropPreview() {
    MaterialTheme {
        CounterInteropDemo()
    }
}
