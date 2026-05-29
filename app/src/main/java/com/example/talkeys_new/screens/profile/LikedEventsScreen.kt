package com.example.talkeys_new.screens.profile

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.talkeys_new.R
import com.example.talkeys_new.screens.dashboard.sharedDashboardViewModel
import com.example.talkeys_new.screens.events.toAndroidEventResponse
import com.example.talkeys_new.screens.events.exploreEvents.EventCard
import com.example.talkeys_new.screens.events.exploreEvents.SkeletonEventCard
import com.talkeys.shared.data.dashboard.UserEventType
import com.talkeys.shared.presentation.dashboard.SharedDashboardViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun LikedEventsScreen(
    navController: NavController,
    viewModel: SharedDashboardViewModel = sharedDashboardViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val likedEvents = remember(uiState.events) {
        uiState.events.map { it.toAndroidEventResponse() }
    }
    val isLoading = uiState.isLoading
    val error = uiState.error
    
    // Group liked events by category
    val groupedLikedEvents = remember(likedEvents) {
        likedEvents.groupBy { event ->
            event.category?.takeIf { it.isNotBlank() } ?: "Uncategorized"
        }.filterValues { it.isNotEmpty() }
    }
    
    // Fetch liked/bookmarked events from the shared dashboard repository.
    LaunchedEffect(Unit) {
        viewModel.loadEvents(UserEventType.Bookmarked)
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
                text = "Liked Events",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Content
        when {
            isLoading -> {
                // Loading state with skeleton categories
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = WindowInsets.navigationBars.asPaddingValues()
                ) {
                    items(3) {
                        LoadingCategorySection()
                    }
                }
            }

            error != null -> {
                // Error state
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = error ?: "An unknown error occurred",
                        color = Color.Red,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { viewModel.loadEvents(UserEventType.Bookmarked) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8A44CB)
                        )
                    ) {
                        Text("Retry", color = Color.White)
                    }
                }
            }

            likedEvents.isEmpty() -> {
                // Empty state
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No liked events yet",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Like events to see them here",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }

            else -> {
                // Display events grouped by category
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = WindowInsets.navigationBars.asPaddingValues()
                ) {
                    // Display each category
                    groupedLikedEvents.forEach { (category, events) ->
                        item {
                            CategorySection(
                                category = category,
                                events = events,
                                navController = navController
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CategorySection(
    category: String,
    events: List<com.example.talkeys_new.dataModels.EventResponse>,
    navController: NavController
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Category header
        Text(
            text = category,
            style = TextStyle(
                fontSize = 18.sp,
                fontFamily = FontFamily(Font(R.font.urbanist_semibold)),
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(bottom = 12.dp)
        )

        // Horizontal scrollable list
        val lazyListState = rememberLazyListState()

        LazyRow(
            state = lazyListState,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 80.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(
                items = events,
                key = { _, event -> event._id ?: event.hashCode() }
            ) { index, event ->
                EventCard(
                    event = event,
                    onClick = {
                        navController.navigate("eventDetail/${event._id}")
                    },
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LoadingCategorySection() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Loading category title
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(20.dp)
                .background(
                    Color(0xFF404040).copy(alpha = 0.6f),
                    RoundedCornerShape(4.dp)
                )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Loading cards row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 80.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(3) {
                SkeletonEventCard(
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }
    }
}
