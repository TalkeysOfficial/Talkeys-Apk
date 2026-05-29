package com.example.talkeys_new.screens.home

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color

import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.platform.LocalConfiguration
import android.util.Log
import com.example.talkeys_new.screens.common.BottomBar
import com.example.talkeys_new.screens.common.Footer
import com.example.talkeys_new.screens.common.HomeTopBar
import com.example.talkeys_new.screens.events.exploreEvents.EventCard
import com.example.talkeys_new.screens.events.sharedEventsListViewModel
import com.example.talkeys_new.screens.events.toAndroidEventResponse
import com.example.talkeys_new.R
import com.talkeys.shared.presentation.events.EventsListUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    // Get screen dimensions for responsive design with better detection
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // More conservative responsive detection to handle different device densities and font scales
    val isSmallScreen = screenWidth < 380.dp || screenHeight < 700.dp
    val isVerySmallScreen = screenWidth < 340.dp

    val eventViewModel = sharedEventsListViewModel()
    val eventState by eventViewModel.uiState.collectAsState()
    val events = remember(eventState) {
        (eventState as? EventsListUiState.Content)
            ?.events
            ?.map { it.toAndroidEventResponse() }
            .orEmpty()
    }
    val isLoading = eventState is EventsListUiState.Loading

    // Pull to refresh state
    var isRefreshing by remember { mutableStateOf(false) }
    var pullOffset by remember { mutableStateOf(0f) }
    val pullThreshold = 120f

    // Animation for pull offset (reduced for subtler effect)
    val animatedPullOffset by animateFloatAsState(
        targetValue = if (isRefreshing) pullThreshold * 0.4f else pullOffset * 0.4f,
        animationSpec = tween(300),
        label = "pullOffset"
    )

    // Handle refresh function
    val onRefresh = {
        Log.d("HomeScreen", "onRefresh called - isRefreshing: $isRefreshing, isLoading: $isLoading")
        if (!isRefreshing && !isLoading) {
            Log.d("HomeScreen", "Starting refresh with forceRefresh=true...")
            isRefreshing = true
            eventViewModel.loadEvents(forceRefresh = true)
        } else {
            Log.d("HomeScreen", "Refresh blocked - already refreshing or loading")
        }
    }

    // Reset refresh state when loading completes
    LaunchedEffect(isLoading) {
        Log.d("HomeScreen", "Loading state changed: $isLoading, isRefreshing: $isRefreshing")
        if (!isLoading && isRefreshing) {
            Log.d("HomeScreen", "Resetting refresh state")
            isRefreshing = false
            pullOffset = 0f
        }
    }

    // Timeout mechanism to prevent stuck refresh state
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            Log.d("HomeScreen", "Starting refresh timeout")
            kotlinx.coroutines.delay(5000) // 5 second timeout
            if (isRefreshing) {
                Log.w("HomeScreen", "Refresh timeout reached, forcing reset")
                isRefreshing = false
                pullOffset = 0f
            }
        }
    }

    // Debug logging for states (only log significant changes)
    LaunchedEffect(isRefreshing, isLoading) {
        Log.d("HomeScreen", "State - isRefreshing: $isRefreshing, isLoading: $isLoading")
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
                    Log.d("HomeScreen", "onPostScroll - delta: $delta, oldOffset: $pullOffset, newOffset: $newOffset")
                    pullOffset = newOffset
                    return Offset(0f, delta)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                // Handle fling end - trigger refresh if threshold met
                Log.d("HomeScreen", "onPreFling - pullOffset: $pullOffset, threshold: $pullThreshold, isRefreshing: $isRefreshing")
                if (pullOffset >= pullThreshold && !isRefreshing) {
                    Log.d("HomeScreen", "Threshold met, triggering refresh")
                    onRefresh()
                } else {
                    Log.d("HomeScreen", "Threshold not met or already refreshing, resetting pullOffset")
                    pullOffset = 0f
                }
                return Velocity.Zero
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Background image (fixed, not inside scrolling area)
        Image(
            painter = painterResource(id = R.drawable.background),
            contentDescription = "Background Image",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Scaffold(
            topBar = { HomeTopBar(navController = navController) },
            containerColor = Color.Transparent, // Make scaffold background transparent
            contentColor = Color.White,
            modifier = Modifier.fillMaxSize(),
            contentWindowInsets = WindowInsets.navigationBars,
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .nestedScroll(nestedScrollConnection)
            ) {
                // LazyColumn with LazyListState for scroll tracking
                val lazyListState = rememberLazyListState()

                // Refresh indicator
                if (animatedPullOffset > 0f || isRefreshing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(65.dp)
                            .offset(y = (animatedPullOffset - 65f).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isRefreshing || pullOffset >= pullThreshold) {
                            CircularProgressIndicator(
                                color = Color(0xFF8A44CB),
                                modifier = Modifier.size(32.dp)
                            )
                        } else {
                            // Show pull indicator
                            val alpha = (pullOffset / pullThreshold).coerceIn(0f, 1f)
                            CircularProgressIndicator(
                                progress = { alpha },
                                color = Color(0xFF8A44CB).copy(alpha = alpha),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }

                // Main content with offset
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = animatedPullOffset.dp),
                    verticalArrangement = Arrangement.spacedBy(if (isSmallScreen) 12.dp else 16.dp),
                    contentPadding = WindowInsets.navigationBars.asPaddingValues() // Padding for system navigation bar
                ) {
                        item { BannerSection(navController) }
                        item { CategoryTitle("Live Events") }
                        item {
                            if (isLoading && !isRefreshing) {
                                LoadingEventRow()
                            } else {
                                EventRow(events.filter { it.isLive }, navController)
                            }
                        }
                        item { CategoryTitle("Featured Communities") }
                        item { CommunityRow(navController) }
                        item { CategoryTitle("Influencers Shaping the Community") }
                        item { InfluencerRow() }
                    item { HostYourOwnEvent(navController) }
                    item {
                        Footer(
                            modifier = Modifier,
                            navController = navController
                        )
                    }
                }

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
}

@Composable
fun HostYourOwnEvent(navController: NavController) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // More conservative responsive detection
    val isSmallScreen = screenWidth < 380.dp || screenHeight < 700.dp
    val isVerySmallScreen = screenWidth < 340.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (isSmallScreen) 12.dp else 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = if (isVerySmallScreen) "Host your own EVENT!" else "Host your own EVENT!!!",
                style = TextStyle(
                    fontSize = if (isVerySmallScreen) 16.sp else if (isSmallScreen) 18.sp else 22.sp,
                    fontFamily = FontFamily(Font(R.font.urbanist_bold)),
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFFCFCFC),
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(if (isSmallScreen) 12.dp else 16.dp))

            Text(
                text = if (isVerySmallScreen)
                    "Create events, invite your community, manage everything easily."
                else if (isSmallScreen)
                    "Create an event, invite your community, and manage everything in one place."
                else
                    "Create an event, invite your community, and manage everything in one place.",
                style = TextStyle(
                    fontSize = if (isVerySmallScreen) 12.sp else if (isSmallScreen) 14.sp else 16.sp,
                    fontFamily = FontFamily(Font(R.font.urbanist_medium)),
                    fontWeight = FontWeight.Normal,
                    color = Color.White,
                    textAlign = TextAlign.Start,
                ),
                modifier = Modifier.fillMaxWidth(if (isVerySmallScreen) 0.95f else if (isSmallScreen) 0.9f else 0.8f),
                maxLines = if (isVerySmallScreen) 3 else 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(if (isSmallScreen) 20.dp else 28.dp))

            // Updated button with click handler for unified create event
            Box(
                modifier = Modifier
                    .width(if (isSmallScreen) 110.dp else 130.dp)
                    .height(if (isSmallScreen) 40.dp else 45.dp)
                    .background(color = Color(0xFF8A44CB), shape = RoundedCornerShape(8.dp))
                    .clickable {
                        // Navigate to create event screen
                        navController.navigate("create_event_1")
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Host Event",
                    fontSize = if (isSmallScreen) 14.sp else 16.sp,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Image(
            painter = painterResource(id = R.drawable.hostevent_sticker),
            contentDescription = "Host Event Sticker",
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(if (isSmallScreen) 160.dp else 200.dp)
        )
    }
}

@Composable
fun BannerSection(navController: NavController) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // More conservative responsive detection
    val isSmallScreen = screenWidth < 380.dp || screenHeight < 700.dp
    val isVerySmallScreen = screenWidth < 340.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (isSmallScreen) 250.dp else 300.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.banner),
            contentDescription = "Banner",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .align(Alignment.Center)
                .background(
                    Color.Black.copy(alpha = 0.8f),
                    RoundedCornerShape(if (isSmallScreen) 10.dp else 12.dp)
                )
                .padding(if (isSmallScreen) 12.dp else 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Explore Shows and \nevents with ease.",
                    color = Color.White,
                    fontSize = if (isSmallScreen) 16.sp else 20.sp,
                    fontFamily = FontFamily(Font(R.font.urbanist_bold)),
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(if (isSmallScreen) 6.dp else 4.dp))

                Text(
                    text = "Connect with fellow enthusiasts in our \nchat rooms. Share experiences and ideas\nanonymously.",
                    color = Color(0xFF8A44CB),
                    fontSize = if (isSmallScreen) 12.sp else 14.sp,
                    fontFamily = FontFamily(Font(R.font.urbanist_medium)),
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(if (isSmallScreen) 8.dp else 12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 6.dp else 8.dp, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { navController.navigate("events") },
                        modifier = Modifier
                            .weight(1f)
                            .height(if (isSmallScreen) 40.dp else 48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A44CB))
                    ) {
                        Text(
                            text = if (isVerySmallScreen) "Events" else "Explore Events",
                            fontSize = if (isVerySmallScreen) 11.sp else if (isSmallScreen) 12.sp else 14.sp,
                            fontFamily = FontFamily(Font(R.font.urbanist_medium)),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Button(
                        onClick = { navController.navigate("screen_not_found") },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(if (isSmallScreen) 40.dp else 48.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A44CB))
                    ) {
                        Text(
                            text = if (isVerySmallScreen) "Communities" else "Explore Communities",
                            fontSize = if (isVerySmallScreen) 10.sp else if (isSmallScreen) 11.sp else 13.sp,
                            fontFamily = FontFamily(Font(R.font.urbanist_medium)),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryTitle(title: String) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // More conservative responsive detection
    val isSmallScreen = screenWidth < 380.dp || screenHeight < 700.dp

    Text(
        text = title,
        style = TextStyle(
            fontSize = if (isSmallScreen) 16.sp else 20.sp,
            fontFamily = FontFamily(Font(R.font.urbanist_bold)),
            fontWeight = FontWeight.Bold,
            color = Color.White
        ),
        modifier = Modifier.padding(horizontal = if (isSmallScreen) 12.dp else 16.dp),
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
fun EventRow(events: List<com.example.talkeys_new.dataModels.EventResponse>, navController: NavController) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // More conservative responsive detection
    val isSmallScreen = screenWidth < 380.dp || screenHeight < 700.dp

    LazyRow(
        contentPadding = PaddingValues(horizontal = if (isSmallScreen) 12.dp else 16.dp),
        horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 12.dp else 16.dp)
    ) {
        items(events) { event ->
            EventCard(
                event = event,
                onClick = {
                    navController.navigate("eventDetail/${event._id}")
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LoadingEventRow() {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // More conservative responsive detection
    val isSmallScreen = screenWidth < 380.dp || screenHeight < 700.dp

    LazyRow(
        contentPadding = PaddingValues(horizontal = if (isSmallScreen) 12.dp else 16.dp),
        horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 12.dp else 16.dp)
    ) {
        items(4) { index ->
            com.example.talkeys_new.screens.events.exploreEvents.SkeletonEventCard(
                modifier = Modifier.animateItemPlacement(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessLow
                    )
                )
            )
        }
    }
}

// Sample data classes for Communities and Influencers
data class CommunityItem(
    val name: String,
    val description: String,
    val imageRes: Int,
    val memberCount: String
)

data class InfluencerItem(
    val name: String,
    val profession: String,
    val imageRes: Int,
    val followers: String
)

@Composable
fun CommunityRow(navController: NavController) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // More conservative responsive detection
    val isSmallScreen = screenWidth < 380.dp || screenHeight < 700.dp

    val communities = listOf(
        CommunityItem("Tech Enthusiasts", "Join fellow developers and tech lovers", R.drawable.community_banner, "1.2K"),
        CommunityItem("Gaming Community", "Connect with gamers worldwide", R.drawable.community_banner, "850"),
        CommunityItem("Art & Design", "Creative minds unite", R.drawable.community_banner, "2.1K"),
        CommunityItem("Music Lovers", "Share your passion for music", R.drawable.community_banner, "1.8K")
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = if (isSmallScreen) 12.dp else 16.dp),
        horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 12.dp else 16.dp)
    ) {
        items(communities) { community ->
            CommunityCard(
                name = community.name,
                imageRes = community.imageRes,
                description = community.description,
                navController = navController
            )
        }
    }
}

@Composable
fun CommunityCard(name: String, imageRes: Int, description: String, navController: NavController) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // More conservative responsive detection
    val isSmallScreen = screenWidth < 380.dp || screenHeight < 700.dp

    // ✅ Wrapper Box with Background and Shadows
    Box(
        modifier = Modifier
            .shadow(elevation = 4.dp,
                ambientColor = Color(0xFF20201F)
            )
            .width(if (isSmallScreen) 130.dp else 148.dp)
            .clickable { navController.navigate("screen_not_found") }
            .height(if (isSmallScreen) 170.dp else 200.dp)
            .background(color = Color(0xFF212020), shape = RoundedCornerShape(size = if (isSmallScreen) 12.dp else 15.dp))
            .padding(if (isSmallScreen) 4.dp else 6.dp)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(if (isSmallScreen) 3.dp else 4.dp, Alignment.Top),
            horizontalAlignment = Alignment.Start
        ) {
            // ✅ Community Image
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "Community Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(if (isSmallScreen) 122.dp else 136.dp)
                    .height(if (isSmallScreen) 120.dp else 136.dp)
                    .background(
                        color = Color(0xFFD9D9D9),
                        shape = RoundedCornerShape(topStart = if (isSmallScreen) 16.dp else 20.dp, bottomEnd = if (isSmallScreen) 16.dp else 20.dp)
                    )
                    .clip(RoundedCornerShape(topStart = if (isSmallScreen) 16.dp else 20.dp, bottomEnd = if (isSmallScreen) 16.dp else 20.dp))
            )

            // ✅ Community Name - Changed to "Coming Soon"
            Text(
                text = "Coming Soon",
                style = TextStyle(
                    fontSize = if (isSmallScreen) 14.sp else 16.sp,
                    fontFamily = FontFamily(Font(R.font.urbanist_bold)),
                    color = Color.White
                ),
                modifier = Modifier.padding(start = if (isSmallScreen) 4.dp else 6.dp, top = if (isSmallScreen) 2.dp else 4.dp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun InfluencerRow() {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // More conservative responsive detection
    val isSmallScreen = screenWidth < 380.dp || screenHeight < 700.dp

    val influencers = listOf(
        InfluencerItem("Coming Soon", "", R.drawable.ic_influencer_banner, "125K"),
        InfluencerItem("Coming Soon", "", R.drawable.ic_influencer_banner, "89K"),
        InfluencerItem("Coming Soon", "", R.drawable.ic_influencer_banner, "234K"),
        InfluencerItem("Coming Soon", "", R.drawable.ic_influencer_banner, "156K")
    )

    LazyRow(
        contentPadding = PaddingValues(horizontal = if (isSmallScreen) 12.dp else 16.dp),
        horizontalArrangement = Arrangement.spacedBy(if (isSmallScreen) 12.dp else 16.dp)
    ) {
        items(influencers) { influencer ->
            InfluencerCard(
                name = influencer.name,
                profession = influencer.profession,
                imageRes = influencer.imageRes
            )
        }
    }
}

@Composable
fun InfluencerCard(name: String, profession: String, imageRes: Int) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp

    // More conservative responsive detection
    val isSmallScreen = screenWidth < 380.dp || screenHeight < 700.dp

    Box(
        modifier = Modifier
            .shadow(elevation = 4.dp, spotColor = Color(0xFF000000), ambientColor = Color(0xFF000000))
            .shadow(elevation = 27.dp, spotColor = Color(0x40FFFFFF), ambientColor = Color(0x40FFFFFF))
            .width(if (isSmallScreen) 115.dp else 131.dp)
            .height(if (isSmallScreen) 140.dp else 158.dp)
            .background(color = Color(0x1AFFFFFF), shape = RoundedCornerShape(size = if (isSmallScreen) 12.dp else 15.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Top,
            modifier = Modifier
                .padding(horizontal = if (isSmallScreen) 6.dp else 8.dp)
        ) {
            // Influencer Image
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = "Influencer Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(if (isSmallScreen) 103.dp else 120.dp)
                    .height(if (isSmallScreen) 90.dp else 103.dp)
                    .background(
                        color = Color(0xFFD9D9D9),
                        shape = RoundedCornerShape(topStart = if (isSmallScreen) 16.dp else 20.dp, bottomEnd = if (isSmallScreen) 16.dp else 20.dp)
                    )
            )

            Spacer(modifier = Modifier.height(if (isSmallScreen) 6.dp else 8.dp))

            // Influencer Name
            Text(
                text = name,
                style = TextStyle(
                    fontSize = if (isSmallScreen) 14.sp else 16.sp,
                    fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                    fontWeight = FontWeight(400),
                    color = Color.White
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (profession.isNotEmpty()) {
                Spacer(modifier = Modifier.height(if (isSmallScreen) 2.dp else 4.dp))

                // Influencer Profession
                Text(
                    text = profession,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = if (isSmallScreen) 2.dp else 4.dp),
                    style = TextStyle(
                        fontSize = if (isSmallScreen) 12.sp else 14.sp,
                        fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                        fontWeight = FontWeight(400),
                        color = Color(0xFFFFFFFF),
                        textAlign = TextAlign.Start
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
