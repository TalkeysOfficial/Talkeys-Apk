package com.example.talkeys_new.screens.events.exploreEvents

import android.util.Log
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalConfiguration
import com.example.talkeys_new.screens.events.sharedEventsListViewModel
import com.example.talkeys_new.screens.events.toAndroidEventResponse
import com.example.talkeys_new.screens.common.HomeTopBar
import com.example.talkeys_new.R
import com.example.talkeys_new.dataModels.EventResponse
import com.example.talkeys_new.screens.common.BottomBar
import com.example.talkeys_new.screens.common.Footer
import kotlinx.coroutines.delay
import com.talkeys.shared.presentation.events.EventsListUiState

/**
 * Screen for exploring events with filtering and categorization
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreEventsScreen(navController: NavController) {
    // Get screen dimensions for responsive design
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    
    // Responsive detection
    val isSmallScreen = screenWidth < 380.dp || screenHeight < 700.dp
    val isVerySmallScreen = screenWidth < 340.dp

    val viewModel = sharedEventsListViewModel()
    val state by viewModel.uiState.collectAsState()
    val content = state as? EventsListUiState.Content
    val eventList = remember(content?.events) {
        content?.events?.map { it.toAndroidEventResponse() }.orEmpty()
    }
    val isLoading = state is EventsListUiState.Loading
    val errorMessage = (state as? EventsListUiState.Error)?.message
    val showLiveEvents = content?.showLiveOnly ?: true

    // Group events by category with null safety
    val groupedEvents = remember(eventList) {
        eventList.groupBy { event ->
            event.category?.takeIf { it.isNotBlank() } ?: "Uncategorized"
        }.filterValues { it.isNotEmpty() } // Remove empty categories
    }

    // Auto-refresh mechanism (optional - can be removed if not needed)
    LaunchedEffect(key1 = Unit) {
        while (true) {
            delay(300000) // 5 minutes
            if (!isLoading && errorMessage == null) {
                viewModel.loadEvents(forceRefresh = true)
            }
        }
    }

    Scaffold(
        topBar = {
            HomeTopBar(navController = navController)
        },
        containerColor = Color.Transparent,
        contentColor = Color.White,
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.navigationBars,
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Background image
            Image(
                painter = painterResource(id = R.drawable.background),
                contentDescription = "Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Main content with pull-to-refresh
            ExploreEventsContent(
                groupedEvents = groupedEvents,
                isLoading = isLoading,
                errorMessage = errorMessage,
                showLiveEvents = showLiveEvents,
                onToggleFilter = { viewModel.toggleFilter() },
                onRetry = { viewModel.retry() },
                onRefresh = { viewModel.loadEvents(forceRefresh = true) },
                onEventClick = { event ->
                    handleEventClick(event, navController)
                },
                onClearError = { viewModel.dismissError() },
                navController = navController,
                isSmallScreen = isSmallScreen,
                isVerySmallScreen = isVerySmallScreen
            )

            // Bottom navigation
            BottomBar(
                navController = navController,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                scrollState = rememberScrollState()
            )
        }
    }
}

/**
 * Main content composable for the explore events screen
 */
@Composable
private fun ExploreEventsContent(
    groupedEvents: Map<String, List<EventResponse>>,
    isLoading: Boolean,
    errorMessage: String?,
    showLiveEvents: Boolean,
    onToggleFilter: () -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onEventClick: (EventResponse) -> Unit,
    onClearError: () -> Unit,
    navController: NavController,
    isSmallScreen: Boolean = false,
    isVerySmallScreen: Boolean = false
) {
    // Pull to refresh state
    var isRefreshing by remember { mutableStateOf(false) }
    var pullOffset by remember { mutableStateOf(0f) }
    val pullThreshold = 150f
    
    // Animation for pull offset (reduced for subtler effect)
    val animatedPullOffset by animateFloatAsState(
        targetValue = if (isRefreshing) pullThreshold * 0.4f else pullOffset * 0.4f,
        animationSpec = tween(300),
        label = "pullOffset"
    )
    
    // Handle refresh function
    val handleRefresh = {
        Log.d("ExploreEvents", "handleRefresh called - isRefreshing: $isRefreshing, isLoading: $isLoading")
        if (!isRefreshing && !isLoading) {
            Log.d("ExploreEvents", "Starting refresh with forceRefresh=true...")
            isRefreshing = true
            onRefresh()
        } else {
            Log.d("ExploreEvents", "Refresh blocked - already refreshing or loading")
        }
    }
    
    // Reset refresh state when loading completes
    LaunchedEffect(isLoading) {
        Log.d("ExploreEvents", "Loading state changed: $isLoading, isRefreshing: $isRefreshing")
        if (!isLoading && isRefreshing) {
            Log.d("ExploreEvents", "Resetting refresh state")
            isRefreshing = false
            pullOffset = 0f
        }
    }
    
    // Timeout mechanism to prevent stuck refresh state
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            Log.d("ExploreEvents", "Starting refresh timeout")
            kotlinx.coroutines.delay(5000) // 5 second timeout
            if (isRefreshing) {
                Log.w("ExploreEvents", "Refresh timeout reached, forcing reset")
                isRefreshing = false
                pullOffset = 0f
            }
        }
    }
    
    // Debug logging for states (only log significant changes)
    LaunchedEffect(isRefreshing, isLoading) {
        Log.d("ExploreEvents", "State - isRefreshing: $isRefreshing, isLoading: $isLoading")
    }
    
    // Nested scroll connection for pull to refresh
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                
                // Only consume scroll when we're releasing (scrolling back up)
                if (delta > 0 && pullOffset > 0 && source != NestedScrollSource.Drag) {
                    val consumed = pullOffset.coerceAtMost(delta)
                    pullOffset -= consumed
                    return Offset(0f, consumed)
                }
                return Offset.Zero
            }
            
            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                
                // If we're at the top and trying to scroll up (pull down), start pull to refresh
                if (delta > 0 && source == NestedScrollSource.Drag) {
                    val newOffset = (pullOffset + delta * 0.6f).coerceAtMost(pullThreshold * 1.2f)
                    Log.d("ExploreEvents", "onPostScroll - delta: $delta, oldOffset: $pullOffset, newOffset: $newOffset")
                    pullOffset = newOffset
                    return Offset(0f, delta)
                }
                return Offset.Zero
            }
            
            override suspend fun onPreFling(available: Velocity): Velocity {
                // Handle fling end - trigger refresh if threshold met
                Log.d("ExploreEvents", "onPreFling - pullOffset: $pullOffset, threshold: $pullThreshold, isRefreshing: $isRefreshing")
                if (pullOffset >= pullThreshold && !isRefreshing) {
                    Log.d("ExploreEvents", "Threshold met, triggering refresh")
                    handleRefresh()
                } else {
                    Log.d("ExploreEvents", "Threshold not met or already refreshing, resetting pullOffset")
                    pullOffset = 0f
                }
                return Velocity.Zero
            }
        }
    }
    
    when {
        errorMessage != null -> {
            ErrorContent(
                errorMessage = errorMessage,
                onRetry = onRetry,
                onClearError = onClearError
            )
        }
        else -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollConnection)
            ) {
                // Refresh indicator (positioned more subtly)
                if (animatedPullOffset > 0f || isRefreshing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .offset(y = (animatedPullOffset - 40f).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isRefreshing || pullOffset >= pullThreshold) {
                            CircularProgressIndicator(
                                color = Color(0xFF8A44CB),
                                modifier = Modifier.size(if (isVerySmallScreen) 28.dp else 32.dp)
                            )
                        } else {
                            // Show pull indicator
                            val alpha = (pullOffset / pullThreshold).coerceIn(0f, 1f)
                            CircularProgressIndicator(
                                progress = { alpha },
                                color = Color(0xFF8A44CB).copy(alpha = alpha),
                                modifier = Modifier.size(if (isVerySmallScreen) 28.dp else 32.dp)
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = animatedPullOffset.dp),
                    verticalArrangement = Arrangement.spacedBy(if (isVerySmallScreen) 12.dp else 16.dp),
                    contentPadding = WindowInsets.navigationBars.asPaddingValues()
                ) {
                // Header section
                item {
                    HeaderSection(
                        showLiveEvents = showLiveEvents,
                        onToggleFilter = onToggleFilter,
                        isSmallScreen = isSmallScreen,
                        isVerySmallScreen = isVerySmallScreen
                    )
                }

                // Content based on loading state
                if (isLoading) {
                    items(3) {
                        LoadingCategorySection(
                            isSmallScreen = isSmallScreen,
                            isVerySmallScreen = isVerySmallScreen
                        )
                    }
                } else if (groupedEvents.isEmpty()) {
                    item {
                        EmptyStateContent(
                            showLiveEvents = showLiveEvents,
                            isSmallScreen = isSmallScreen,
                            isVerySmallScreen = isVerySmallScreen
                        )
                    }
                } else {
                    // Display categorized events
                    groupedEvents.forEach { (category, events) ->
                        item {
                            CategorySection(
                                category = category,
                                events = events,
                                onEventClick = onEventClick,
                                isSmallScreen = isSmallScreen,
                                isVerySmallScreen = isVerySmallScreen
                            )
                        }
                    }
                }

                    // Footer
                    item {
                        Spacer(modifier = Modifier.height(if (isVerySmallScreen) 6.dp else 8.dp))
                        Footer(navController = navController)
                    }
                }
            }
        }
    }
}

/**
 * Header section with title and filter buttons
 */
@Composable
private fun HeaderSection(
    showLiveEvents: Boolean,
    onToggleFilter: () -> Unit,
    isSmallScreen: Boolean = false,
    isVerySmallScreen: Boolean = false
) {
    Column {
        Text(
            text = "Explore Events",
            style = TextStyle(
                fontSize = if (isVerySmallScreen) 18.sp else if (isSmallScreen) 20.sp else 22.sp,
                fontFamily = FontFamily(Font(R.font.urbanist_semibold)),
                color = Color.White,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier.padding(
                top = if (isVerySmallScreen) 8.dp else 12.dp, 
                start = if (isVerySmallScreen) 16.dp else 19.dp
            )
        )

        Spacer(modifier = Modifier.height(if (isVerySmallScreen) 12.dp else 16.dp))

        // Filter buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = if (isVerySmallScreen) 12.dp else 16.dp),
            horizontalArrangement = Arrangement.spacedBy(if (isVerySmallScreen) 8.dp else 12.dp)
        ) {
            FilterButton(
                text = "Live Events",
                isSelected = showLiveEvents,
                onClick = { if (!showLiveEvents) onToggleFilter() },
                modifier = Modifier.weight(1f),
                isSmallScreen = isSmallScreen,
                isVerySmallScreen = isVerySmallScreen
            )

            FilterButton(
                text = "Past Events",
                isSelected = !showLiveEvents,
                onClick = { if (showLiveEvents) onToggleFilter() },
                modifier = Modifier.weight(1f),
                isSmallScreen = isSmallScreen,
                isVerySmallScreen = isVerySmallScreen
            )
        }
    }
}

/**
 * Filter button component
 */
@Composable
private fun FilterButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSmallScreen: Boolean = false,
    isVerySmallScreen: Boolean = false
) {
    Box(
        modifier = modifier
            .height(if (isVerySmallScreen) 36.dp else 40.dp)
            .background(
                color = if (isSelected) Color(0xFF8A44CB) else Color(0x40FFFFFF),
                shape = RoundedCornerShape(if (isVerySmallScreen) 18.dp else 20.dp)
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (isVerySmallScreen) 12.sp else 14.sp
        )
    }
}

/**
 * Error content display
 */
@Composable
private fun ErrorContent(
    errorMessage: String,
    onRetry: () -> Unit,
    onClearError: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Unable to load events",
            color = Color.White,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = errorMessage,
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onClearError,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color.White
                )
            ) {
                Text("Dismiss")
            }

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF8A44CB)
                )
            ) {
                Text("Retry")
            }
        }
    }
}

/**
 * Empty state content
 */
@Composable
private fun EmptyStateContent(
    showLiveEvents: Boolean,
    isSmallScreen: Boolean = false,
    isVerySmallScreen: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(if (isVerySmallScreen) 24.dp else 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (showLiveEvents) "No live events available" else "No past events available",
            color = Color.White,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = if (isVerySmallScreen) 14.sp else 16.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(if (isVerySmallScreen) 6.dp else 8.dp))

        Text(
            text = "Check back later for new events",
            color = Color.White.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = if (isVerySmallScreen) 12.sp else 14.sp
            ),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Category section with horizontal scrollable event list
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategorySection(
    category: String,
    events: List<EventResponse>,
    onEventClick: (EventResponse) -> Unit,
    isSmallScreen: Boolean = false,
    isVerySmallScreen: Boolean = false
) {
    if (events.isEmpty()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isVerySmallScreen) 12.dp else 16.dp)
    ) {
        // Category header
        Text(
            text = category,
            style = TextStyle(
                fontSize = if (isVerySmallScreen) 16.sp else 18.sp,
                fontFamily = FontFamily(Font(R.font.urbanist_semibold)),
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            ),
            modifier = Modifier.padding(bottom = if (isVerySmallScreen) 8.dp else 12.dp)
        )

        // Horizontal scrollable list
        val lazyListState = rememberLazyListState()

        LazyRow(
            state = lazyListState,
            horizontalArrangement = Arrangement.spacedBy(if (isVerySmallScreen) 8.dp else 12.dp),
            contentPadding = PaddingValues(start = 0.dp, end = if (isVerySmallScreen) 60.dp else 80.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            itemsIndexed(
                items = events,
                key = { _, event -> event._id ?: event.hashCode() }
            ) { index, event ->
                EventCard(
                    event = event,
                    onClick = { onEventClick(event) },
                    modifier = Modifier
                        .animateItemPlacement() // Smooth animations
                )
            }
        }
    }
}

/**
 * Loading shimmer for category sections
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoadingCategorySection(
    isSmallScreen: Boolean = false,
    isVerySmallScreen: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isVerySmallScreen) 12.dp else 16.dp)
    ) {
        // Loading category title
        Box(
            modifier = Modifier
                .width(if (isVerySmallScreen) 100.dp else 120.dp)
                .height(if (isVerySmallScreen) 16.dp else 20.dp)
                .background(
                    Color(0xFF404040).copy(alpha = 0.6f),
                    RoundedCornerShape(4.dp)
                )
        )

        Spacer(modifier = Modifier.height(if (isVerySmallScreen) 8.dp else 12.dp))

        // Loading cards row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(if (isVerySmallScreen) 8.dp else 12.dp),
            contentPadding = PaddingValues(start = 0.dp, end = if (isVerySmallScreen) 60.dp else 80.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(3) { index ->
                SkeletonEventCard(
                    modifier = Modifier.animateItemPlacement()
                )
            }
        }
    }
}
/**
 * Handles event click with proper error handling and navigation
 */
private fun handleEventClick(event: EventResponse, navController: NavController) {
    try {
        val eventId = event._id
        if (eventId.isNullOrBlank()) {
            Log.e("ExploreEventsScreen", "Event ID is null or blank for event: ${event.name}")
            return
        }

        Log.d("ExploreEventsScreen", "Event clicked: ${event.name}, ID: $eventId")
        Log.d("ExploreEventsScreen", "Navigating to: eventDetail/$eventId")

        navController.navigate("eventDetail/$eventId") {
            // Add proper navigation options
            launchSingleTop = true
            restoreState = true
        }

        Log.d("ExploreEventsScreen", "Navigation called successfully")
    } catch (e: Exception) {
        Log.e("ExploreEventsScreen", "Error handling event click", e)
    }
}
