package com.example.talkeys_new.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.talkeys_new.screens.common.ErrorCard
import com.example.talkeys_new.screens.dashboard.sharedDashboardViewModel
import com.example.talkeys_new.screens.events.toAndroidEventResponse
import com.example.talkeys_new.screens.events.exploreEvents.EventCard
import com.example.talkeys_new.screens.events.exploreEvents.SkeletonEventCard
import com.talkeys.shared.data.dashboard.UserEventType
import com.talkeys.shared.presentation.dashboard.SharedDashboardViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HostedEventsScreen(
    navController: NavController,
    viewModel: SharedDashboardViewModel = sharedDashboardViewModel()
) {
    // State collection
    val uiState by viewModel.uiState.collectAsState()
    val events = remember(uiState.events) {
        uiState.events.map { it.toAndroidEventResponse() }
    }
    val isLoading = uiState.isLoading
    val error = uiState.error

    // Fetch hosted events when screen is first displayed
    LaunchedEffect(Unit) {
        viewModel.loadEvents(UserEventType.Hosted)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Top Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(16.dp)
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            Text(
                text = "Hosted Events",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            when {
                isLoading -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(6) {
                            SkeletonEventCard()
                        }
                    }
                }

                error != null -> {
                    ErrorCard(
                        message = error ?: "An unknown error occurred",
                        onRetry = {
                            viewModel.loadEvents(UserEventType.Hosted)
                        }
                    )
                }

                events.isEmpty() -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No hosted events found",
                            color = Color.Gray,
                            fontSize = 16.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Create events to see them here",
                            color = Color.Gray,
                            fontSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { navController.navigate("create_event") },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF8A44CB)
                            )
                        ) {
                            Text(
                                text = "Create Event",
                                color = Color.White
                            )
                        }
                    }
                }

                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(events) { event ->
                            EventCard(
                                event = event,
                                onClick = {
                                    navController.navigate("eventDetail/${event._id}")
                                },
                                isFocused = true
                            )
                        }
                    }
                }
            }
        }
    }
}
