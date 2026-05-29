package com.example.talkeys_new.screens.events.eventDetailScreen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.talkeys_new.R
import com.example.talkeys_new.dataModels.EventResponse
import com.example.talkeys_new.screens.common.BottomBar
import com.example.talkeys_new.screens.common.Footer
import com.example.talkeys_new.screens.common.HomeTopBar
import com.example.talkeys_new.screens.events.sharedEventDetailViewModel
import com.example.talkeys_new.screens.events.toAndroidEventResponse
import com.example.talkeys_new.utils.LikedEventsManager
import com.talkeys.shared.presentation.events.EventDetailUiState as SharedEventDetailUiState
import java.text.SimpleDateFormat
import java.util.*

// Constants
private object EventDetailConstants {
     val CARD_WIDTH = 418.dp
     val CARD_HEIGHT = 266.66501.dp
     val EVENT_CARD_WIDTH = 150.dp
     val INFO_CARD_WIDTH = 370.dp
     val INFO_CARD_HEIGHT = 241.dp
     val CONTENT_CARD_WIDTH = 340.dp
     val REGISTER_BUTTON_WIDTH = 133.dp
    val REGISTER_BUTTON_HEIGHT = 39.dp
     val ANIMATION_DURATION = 500
     val HIGHLIGHT_DURATION = 2000L
     val INITIAL_LIKE_COUNT = 183
     val DEFAULT_ICON_SIZE = 24.dp
     val SMALL_ICON_SIZE = 16.dp
     val MEDIUM_ICON_SIZE = 20.dp
}

// UI State
private data class EventDetailUiState(
    val selectedItem: String = "Details",
    val isLiked: Boolean = false,
    val likeCount: Int = EventDetailConstants.INITIAL_LIKE_COUNT
)

@Composable
fun EventDetailScreen(
    eventId: String,
    navController: NavController
) {
    // Input validation
    if (eventId.isBlank()) {
        Log.e("EventDetailScreen", "Invalid eventId: $eventId")
        ErrorScreen(
            message = "Invalid event ID",
            onRetry = { navController.popBackStack() }
        )
        return
    }

    Log.d("EventDetailScreen", "Screen opened with eventId: $eventId")

    val context = LocalContext.current
    val viewModel = sharedEventDetailViewModel()
    val likedEventsManager = remember { LikedEventsManager.getInstance(context) }

    // State management
    val eventState by viewModel.uiState.collectAsState()
    val likedEventIds by likedEventsManager.likedEventIds.collectAsState()
    val event = (eventState as? SharedEventDetailUiState.Content)
        ?.event
        ?.toAndroidEventResponse()
    val errorMessage = (eventState as? SharedEventDetailUiState.Error)?.message

    // Initialize UI state with liked status from storage
    var uiState by remember { mutableStateOf(EventDetailUiState()) }

    // Update liked state when event or liked IDs change
    LaunchedEffect(eventId, likedEventIds) {
        uiState = uiState.copy(isLiked = likedEventIds.contains(eventId))
    }

    // Fetch event details
    LaunchedEffect(eventId) {
        viewModel.loadEvent(eventId)
    }

    Scaffold(
        topBar = { HomeTopBar(navController = navController) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Background image
            BackgroundImage()

            // Content based on state
            when {
                eventState is SharedEventDetailUiState.Loading -> {
                    LoadingIndicator(modifier = Modifier.align(Alignment.Center))
                }

                errorMessage != null -> {
                    ErrorScreen(
                        message = errorMessage!!,
                        onRetry = { viewModel.retry() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                event != null -> {
                    EventContent(
                        event = event,
                        navController = navController,
                        uiState = uiState,
                        onUiStateChange = { uiState = it },
                        onLike = likedEventsManager::likeEvent,
                        onUnlike = likedEventsManager::unlikeEvent
                    )
                }

                else -> {
                    ErrorScreen(
                        message = "Event not found",
                        onRetry = { navController.popBackStack() },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            // Bottom navigation
            BottomBar(
                navController = navController,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                scrollState = rememberScrollState()
            )
        }
    }
}

@Composable
private fun BackgroundImage() {
    Image(
        painter = painterResource(id = R.drawable.background),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun LoadingIndicator(modifier: Modifier = Modifier) {
    EventDetailSkeleton(modifier = modifier)
}

@Composable
private fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = message,
            color = Color.White,
            fontSize = 16.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF8A44CB))
        ) {
            Text("Retry", color = Color.White)
        }
    }
}

@Composable
private fun EventContent(
    event: EventResponse,
    navController: NavController,
    uiState: EventDetailUiState,
    onUiStateChange: (EventDetailUiState) -> Unit,
    onLike: (String) -> Unit,
    onUnlike: (String) -> Unit
) {
    val tabItems = listOf("Details", "Dates & Deadlines", "Prizes", "Link to Community")
    val reorderedItems = remember(uiState.selectedItem) {
        listOf(uiState.selectedItem) + tabItems.filter { it != uiState.selectedItem }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = WindowInsets.navigationBars.asPaddingValues()
    ) {
        // Event header
        item {
            EventHeader(event = event)
        }

        item {
            Spacer(modifier = Modifier.height(5.dp))
        }

        // Event info box
        item {
            EventInfoBox(
                navController = navController,
                event = event,
                uiState = uiState,
                onUiStateChange = onUiStateChange,
                onLike = onLike,
                onUnlike = onUnlike
            )
        }

        // Tab selection
        item {
            Spacer(modifier = Modifier.height(20.dp))
            SelectionBar(
                items = tabItems,
                selectedItem = uiState.selectedItem
            ) { selectedItem ->
                onUiStateChange(uiState.copy(selectedItem = selectedItem))
            }
            Spacer(modifier = Modifier.height(33.dp))
        }

        // Tab content
        items(reorderedItems) { item ->
            TabContentItem(
                item = item,
                event = event,
                isSelected = item == uiState.selectedItem
            )
            Spacer(modifier = Modifier.height(30.dp))
        }

        // Footer
        item {
            Footer(navController = navController)
        }
    }
}

@Composable
private fun EventHeader(event: EventResponse) {
    Card(
        modifier = Modifier
            .width(EventDetailConstants.CARD_WIDTH)
            .height(EventDetailConstants.CARD_HEIGHT)
            .padding(top = 34.dp, start = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            // Event image
            EventCard(
                title = event.name.orEmpty(),
                location = event.location.orEmpty(),
                imageUrl = event.photographs?.firstOrNull()
            )

            // Event details
            EventDetailsColumn(event = event)
        }
    }
}

@Composable
private fun EventDetailsColumn(event: EventResponse) {
    Column {
        // Event name
        Text(
            text = event.name.orEmpty(),
            style = TextStyle(
                fontSize = 22.sp,
                fontFamily = FontFamily(Font(R.font.urbanist_bold)),
                fontWeight = FontWeight.Bold,
                color = Color.White
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Event details rows
        event.category?.let { category ->
            EventDetailRow(R.drawable.college, category)
            Spacer(modifier = Modifier.height(10.dp))
        }

        event.location?.let { location ->
            EventDetailRow(R.drawable.ic_location, location)
            Spacer(modifier = Modifier.height(10.dp))
        }

        EventDetailRow(R.drawable.event_date, formatEventDate(event.startDate))
        Spacer(modifier = Modifier.height(10.dp))

        event.organizerName?.takeIf { it.isNotEmpty() }?.let { organizer ->
            EventDetailRow(R.drawable.trophy, organizer)
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun TabContentItem(
    item: String,
    event: EventResponse,
    isSelected: Boolean
) {
    val highlightColor = Color(0xFF8A44CB)
    val normalColor = Color(0xFF171717)

    var showHighlight by remember { mutableStateOf(isSelected) }

    val backgroundColor by animateColorAsState(
        targetValue = if (showHighlight) highlightColor else normalColor,
        animationSpec = tween(durationMillis = EventDetailConstants.ANIMATION_DURATION),
        label = "highlight"
    )

    LaunchedEffect(isSelected) {
        if (isSelected) {
            showHighlight = true
            kotlinx.coroutines.delay(EventDetailConstants.HIGHLIGHT_DURATION)
            showHighlight = false
        }
    }

    InfoCard(
        title = item,
        content = getTabContent(item, event),
        backgroundColor = backgroundColor
    )
}

private fun getTabContent(item: String, event: EventResponse): String {
    return when (item) {
        "Details" -> event.eventDescription?.takeIf { it.isNotEmpty() }
            ?: "No details available."

        "Dates & Deadlines" -> buildString {
            append("Start Date: ${formatEventDate(event.startDate)}\n")
            append("Start Time: ${event.startTime?.takeIf { it.isNotEmpty() } ?: "Not specified"}\n")
            append("Duration: ${event.duration?.takeIf { it.isNotEmpty() } ?: "Not specified"}\n")
            append("Registration Ends: ${event.endRegistrationDate?.takeIf { it.isNotEmpty() } ?: "Not specified"}")
        }

        "Prizes" -> event.prizes?.takeIf { it.isNotEmpty() }
            ?: "No prizes available."

        "Link to Community" -> "No information available"

        else -> "No information available"
    }
}

@Composable
private fun SelectionBar(
    items: List<String>,
    selectedItem: String,
    onSelect: (String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(Color(0xFF2E2E2E), shape = RoundedCornerShape(20.dp))
            .padding(8.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(items) { item ->
                SelectionItem(
                    text = item,
                    isSelected = item == selectedItem,
                    onClick = { onSelect(item) }
                )
            }
        }
    }
}

@Composable
private fun SelectionItem(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = if (isSelected) Color(0xFF8A44CB) else Color(0xFF444444),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun InfoCard(
    title: String,
    content: String,
    backgroundColor: Color
) {
    Column(
        modifier = Modifier
            .width(EventDetailConstants.CONTENT_CARD_WIDTH)
            .wrapContentHeight()
            .padding(start = 32.dp)
            .background(Color(0xFF171717), shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            VerticalIndicator()
            Spacer(modifier = Modifier.width    (8.dp))

            Text(
                text = title,
                style = TextStyle(
                    fontSize = 22.sp,
                    fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                ),
                modifier = Modifier.fillMaxWidth(),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = content,
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                fontWeight = FontWeight.Light,
                color = Color.White
            ),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun EventDetailRow(icon: Int, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = icon),
            contentDescription = null,
            modifier = Modifier.size(EventDetailConstants.SMALL_ICON_SIZE)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = TextStyle(
                fontSize = 16.sp,
                fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                fontWeight = FontWeight.Normal,
                color = Color.White
            ),
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun EventCard(
    title: String,
    location: String,
    imageUrl: String?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .shadow(
                elevation = 4.dp,
                spotColor = Color(0x40000000),
                ambientColor = Color(0x40000000)
            )
            .width(EventDetailConstants.EVENT_CARD_WIDTH)
            .height(EventDetailConstants.CARD_HEIGHT)
    ) {
        if (!imageUrl.isNullOrEmpty()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = "Event image for $title",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                placeholder = painterResource(id = R.drawable.ic_location),
                error = painterResource(id = R.drawable.ic_location)
            )
        } else {
            // Fallback image
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF404040).copy(alpha = 0.6f))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_location),
                    contentDescription = "Default event image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    alpha = 0.5f
                )
            }
        }
    }
}

@Composable
private fun VerticalIndicator() {
    Box(
        modifier = Modifier
            .width(5.dp)
            .height(39.dp)
            .background(
                color = Color(0xFF8A44CB),
                shape = RoundedCornerShape(50.dp)
            )
    )
}

@Composable
private fun EventInfoBox(
    navController: NavController,
    event: EventResponse,
    uiState: EventDetailUiState,
    onUiStateChange: (EventDetailUiState) -> Unit,
    onLike: (String) -> Unit,
    onUnlike: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .width(EventDetailConstants.INFO_CARD_WIDTH)
            .height(EventDetailConstants.INFO_CARD_HEIGHT)
            .padding(start = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF171717), shape = RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Cost and actions row
                CostAndActionsRow(
                    event = event,
                    context = context,
                    uiState = uiState,
                    onUiStateChange = onUiStateChange,
                    onLike = onLike,
                    onUnlike = onUnlike
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Register button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    RegisterButton(navController = navController, event = event)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Divider
                Image(
                    painter = painterResource(id = R.drawable.divider_line),
                    contentDescription = "Divider",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .width(320.dp)
                        .height(2.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Event tags
                EventTags(event = event)
            }
        }
    }
}

@Composable
private fun CostAndActionsRow(
    event: EventResponse,
    context: Context,
    uiState: EventDetailUiState,
    onUiStateChange: (EventDetailUiState) -> Unit,
    onLike: (String) -> Unit,
    onUnlike: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cost information
        Column {
            Text(
                text = "Cost for Event",
                style = TextStyle(
                    fontSize = 18.sp,
                    fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                    fontWeight = FontWeight.Normal,
                    color = Color.White
                )
            )
            Text(
                text = formatEventPrice(event.ticketPrice),
                style = TextStyle(
                    fontSize = 22.sp,
                    fontFamily = FontFamily(Font(R.font.urbanist_bold)),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8A44CB)
                )
            )
        }

        // Action buttons
        ActionButtons(
            event = event,
            context = context,
            uiState = uiState,
            onUiStateChange = onUiStateChange,
            onLike = onLike,
            onUnlike = onUnlike
        )
    }
}

@Composable
private fun ActionButtons(
    event: EventResponse,
    context: Context,
    uiState: EventDetailUiState,
    onUiStateChange: (EventDetailUiState) -> Unit,
    onLike: (String) -> Unit,
    onUnlike: (String) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Like button - use filled heart when liked, outlined when not
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.clickable {
                // Toggle like state and persist to storage
                if (uiState.isLiked) {
                    onUnlike(event._id)
                } else {
                    onLike(event._id)
                }
                // Update UI state (will be synced by LaunchedEffect)
                onUiStateChange(uiState.copy(isLiked = !uiState.isLiked))
            }
        ) {
            Icon(
                imageVector = if (uiState.isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = "Like event",
                modifier = Modifier.size(EventDetailConstants.DEFAULT_ICON_SIZE),
                tint = if (uiState.isLiked) Color.Red else Color.White
            )
        }

        // Comment button
        ActionButton(
            iconRes = R.drawable.ic_comment,
            contentDescription = "Comment on event",
            onClick = {
                // Replace with actual comment system implementation
                // This would typically navigate to a comments screen or open a comment dialog
                Log.d("EventDetail", "Comment button clicked for event: ${event.name}")
            }
        )

        // Share button
        ActionButton(
            iconRes = R.drawable.ic_share,
            contentDescription = "Share event",
            onClick = { shareEvent(context, event) }
        )
    }
}

@Composable
private fun ActionButton(
    iconRes: Int,
    contentDescription: String,
    text: String? = null,
    tint: Color = Color.White,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(EventDetailConstants.DEFAULT_ICON_SIZE),
            colorFilter = ColorFilter.tint(tint)
        )
        text?.let {
            Text(
                text = it,
                color = Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun EventTags(event: EventResponse) {
    Column(modifier = Modifier.fillMaxWidth()) {
        // First row - Category and Mode
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            event.category?.let { category ->
                EventTag(category)
                Spacer(modifier = Modifier.width(12.dp))
            }
            event.mode?.let { mode ->
                EventTag(mode)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Second row - Price and Team status
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            EventTag(
                text = if (isEventFree(event.ticketPrice)) "Free Event" else "Paid Event",
                isBigger = true
            )
            Spacer(modifier = Modifier.width(12.dp))
            EventTag(if (event.isTeamEvent) "Team Event" else "Solo Event")
        }
    }
}

@Composable
private fun EventTag(
    text: String,
    isBigger: Boolean = false
) {
    val width = if (isBigger) 149.dp else 104.dp

    Box(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = Color(0xFFCCA1F4),
                shape = RoundedCornerShape(27.dp)
            )
            .width(width)
            .height(25.dp)
            .padding(horizontal = 12.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = TextStyle(
                fontSize = 14.sp,
                fontFamily = FontFamily(Font(R.font.urbanist_semibold)),
                fontWeight = FontWeight.Bold,
                color = Color(0xFFCCA1F4)
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun RegisterButton(
    navController: NavController,
    event: EventResponse
) {
    Box(
        modifier = Modifier
            .width(EventDetailConstants.REGISTER_BUTTON_WIDTH)
            .height(48.dp) // Increased height for better text visibility
            .background(Color(0xFF8A44CB), shape = RoundedCornerShape(8.dp))
            .clickable {
                // Safe navigation with null checks
                val eventId = event._id?.takeIf { it.isNotEmpty() } ?: return@clickable
                val eventName = event.name?.takeIf { it.isNotEmpty() } ?: "Unknown Event"
                val ticketPrice = event.ticketPrice ?: 0
                val encodedEventId = Uri.encode(eventId)
                val encodedEventName = Uri.encode(eventName)
                val encodedTicketPrice = Uri.encode(ticketPrice.toString())

                try {
                    navController.navigate("payment/$encodedEventId/$encodedEventName/$encodedTicketPrice")
                } catch (e: Exception) {
                    Log.e("RegisterButton", "Navigation failed", e)
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Register Now",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

// Utility functions
private fun formatEventDate(date: String?): String {
    if (date.isNullOrEmpty()) return "Not specified"

    return try {
        val dateOnly = if (date.contains("T")) {
            date.split("T")[0]
        } else {
            date
        }

        val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val parsedDate = parser.parse(dateOnly)

        parsedDate?.let { formatter.format(it) } ?: "Not specified"
    } catch (e: Exception) {
        Log.e("EventDetailScreen", "Date parsing failed for: $date", e)
        "Not specified"
    }
}

private fun formatEventPrice(price: Any?): String {
    return when {
        price == null -> "Free"
        price.toString().isEmpty() -> "Free"
        price.toString() == "0" -> "Free"
        price.toString() == "0.0" -> "Free"
        else -> {
            try {
                val numericPrice = price.toString().toDoubleOrNull()
                if (numericPrice != null && numericPrice > 0) {
                    "₹${numericPrice.toInt()}"
                } else {
                    "Free"
                }
            } catch (e: Exception) {
                "Free"
            }
        }
    }
}

private fun isEventFree(price: Any?): Boolean {
    return when {
        price == null -> true
        price.toString().isEmpty() -> true
        price.toString() == "0" -> true
        price.toString() == "0.0" -> true
        else -> {
            try {
                val numericPrice = price.toString().toDoubleOrNull()
                numericPrice == null || numericPrice <= 0
            } catch (e: Exception) {
                true
            }
        }
    }
}

private fun shareEvent(context: Context, event: EventResponse) {
    try {
        val shareText = buildString {
            append("Check out this event: ${event.name ?: "Unknown Event"}\n")
            event.location?.let { append("Location: $it\n") }
            append("Date: ${formatEventDate(event.startDate)}")
            event.startTime?.let { append(" at $it") }
            append("\nPrice: ${formatEventPrice(event.ticketPrice)}")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, "Share Event")
        context.startActivity(shareIntent)
    } catch (e: Exception) {
        Log.e("EventDetailScreen", "Share failed", e)
    }
}

// SKELETON LOADING ANIMATION FOR EVENT DETAIL SCREEN
@Composable
private fun EventDetailSkeleton(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "skeleton_transition")

    // Shimmer wave animation
    val shimmerTranslateAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_wave"
    )

    // Pulse animation for skeleton elements
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    // Breathing scale animation
    val breathingScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.01f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathing_scale"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                scaleX = breathingScale
                scaleY = breathingScale
            },
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Event header skeleton
        item {
            EventHeaderSkeleton(
                pulseAlpha = pulseAlpha,
                shimmerTranslateAnim = shimmerTranslateAnim
            )
        }

        item {
            Spacer(modifier = Modifier.height(5.dp))
        }

        // Event info box skeleton
        item {
            EventInfoBoxSkeleton(
                pulseAlpha = pulseAlpha,
                shimmerTranslateAnim = shimmerTranslateAnim
            )
        }

        // Tab selection skeleton
        item {
            Spacer(modifier = Modifier.height(20.dp))
            SelectionBarSkeleton(
                pulseAlpha = pulseAlpha,
                shimmerTranslateAnim = shimmerTranslateAnim
            )
            Spacer(modifier = Modifier.height(33.dp))
        }

        // Tab content skeleton
        items(4) { index ->
            TabContentSkeleton(
                pulseAlpha = pulseAlpha,
                shimmerTranslateAnim = shimmerTranslateAnim,
                index = index
            )
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun EventHeaderSkeleton(pulseAlpha: Float, shimmerTranslateAnim: Float) {
    Card(
        modifier = Modifier
            .width(EventDetailConstants.CARD_WIDTH)
            .height(EventDetailConstants.CARD_HEIGHT)
            .padding(top = 34.dp, start = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(16.dp)
        ) {
            // Event image skeleton
            SkeletonBox(
                modifier = Modifier
                    .width(EventDetailConstants.EVENT_CARD_WIDTH)
                    .height(EventDetailConstants.CARD_HEIGHT),
                alpha = pulseAlpha,
                shimmerTranslateAnim = shimmerTranslateAnim,
                shape = RoundedCornerShape(8.dp)
            )

            // Event details skeleton
            Column {
                // Event name skeleton - two lines
                SkeletonBox(
                    modifier = Modifier
                        .width(180.dp)
                        .height(22.sp.value.dp),
                    alpha = pulseAlpha,
                    shimmerTranslateAnim = shimmerTranslateAnim
                )
                Spacer(modifier = Modifier.height(8.dp))
                SkeletonBox(
                    modifier = Modifier
                        .width(140.dp)
                        .height(22.sp.value.dp),
                    alpha = pulseAlpha * 0.8f,
                    shimmerTranslateAnim = shimmerTranslateAnim
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Event detail rows skeleton
                repeat(4) { index ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SkeletonBox(
                            modifier = Modifier.size(EventDetailConstants.SMALL_ICON_SIZE),
                            alpha = pulseAlpha * 0.6f,
                            shimmerTranslateAnim = shimmerTranslateAnim,
                            shape = RoundedCornerShape(2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        SkeletonBox(
                            modifier = Modifier
                                .width((80 + index * 20).dp)
                                .height(16.sp.value.dp),
                            alpha = pulseAlpha * 0.7f,
                            shimmerTranslateAnim = shimmerTranslateAnim
                        )
                    }
                    if (index < 3) Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun EventInfoBoxSkeleton(pulseAlpha: Float, shimmerTranslateAnim: Float) {
    Card(
        modifier = Modifier
            .width(EventDetailConstants.INFO_CARD_WIDTH)
            .height(EventDetailConstants.INFO_CARD_HEIGHT)
            .padding(start = 20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF171717), shape = RoundedCornerShape(20.dp))
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Cost and actions row skeleton
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Cost information skeleton
                    Column {
                        SkeletonBox(
                            modifier = Modifier
                                .width(120.dp)
                                .height(18.sp.value.dp),
                            alpha = pulseAlpha,
                            shimmerTranslateAnim = shimmerTranslateAnim
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        SkeletonBox(
                            modifier = Modifier
                                .width(80.dp)
                                .height(22.sp.value.dp),
                            alpha = pulseAlpha * 0.9f,
                            shimmerTranslateAnim = shimmerTranslateAnim
                        )
                    }

                    // Action buttons skeleton
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(3) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                SkeletonBox(
                                    modifier = Modifier.size(EventDetailConstants.DEFAULT_ICON_SIZE),
                                    alpha = pulseAlpha * 0.6f,
                                    shimmerTranslateAnim = shimmerTranslateAnim,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                SkeletonBox(
                                    modifier = Modifier
                                        .width(30.dp)
                                        .height(12.sp.value.dp),
                                    alpha = pulseAlpha * 0.5f,
                                    shimmerTranslateAnim = shimmerTranslateAnim
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Register button skeleton
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    SkeletonBox(
                        modifier = Modifier
                            .width(EventDetailConstants.REGISTER_BUTTON_WIDTH)
                            .height(EventDetailConstants.REGISTER_BUTTON_HEIGHT),
                        alpha = pulseAlpha,
                        shimmerTranslateAnim = shimmerTranslateAnim,
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Divider skeleton
                SkeletonBox(
                    modifier = Modifier
                        .width(320.dp)
                        .height(2.dp),
                    alpha = pulseAlpha * 0.4f,
                    shimmerTranslateAnim = shimmerTranslateAnim
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Event tags skeleton
                Column(modifier = Modifier.fillMaxWidth()) {
                    // First row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SkeletonBox(
                            modifier = Modifier
                                .width(104.dp)
                                .height(25.dp),
                            alpha = pulseAlpha * 0.6f,
                            shimmerTranslateAnim = shimmerTranslateAnim,
                            shape = RoundedCornerShape(27.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        SkeletonBox(
                            modifier = Modifier
                                .width(104.dp)
                                .height(25.dp),
                            alpha = pulseAlpha * 0.6f,
                            shimmerTranslateAnim = shimmerTranslateAnim,
                            shape = RoundedCornerShape(27.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Second row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SkeletonBox(
                            modifier = Modifier
                                .width(149.dp)
                                .height(25.dp),
                            alpha = pulseAlpha * 0.6f,
                            shimmerTranslateAnim = shimmerTranslateAnim,
                            shape = RoundedCornerShape(27.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        SkeletonBox(
                            modifier = Modifier
                                .width(104.dp)
                                .height(25.dp),
                            alpha = pulseAlpha * 0.6f,
                            shimmerTranslateAnim = shimmerTranslateAnim,
                            shape = RoundedCornerShape(27.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectionBarSkeleton(pulseAlpha: Float, shimmerTranslateAnim: Float) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(Color(0xFF2E2E2E), shape = RoundedCornerShape(20.dp))
            .padding(8.dp)
    ) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(4) { index ->
                SkeletonBox(
                    modifier = Modifier
                        .width((80 + index * 20).dp)
                        .height(32.dp),
                    alpha = if (index == 0) pulseAlpha else pulseAlpha * 0.6f,
                    shimmerTranslateAnim = shimmerTranslateAnim,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }
    }
}

@Composable
private fun TabContentSkeleton(pulseAlpha: Float, shimmerTranslateAnim: Float, index: Int) {
    Column(
        modifier = Modifier
            .width(EventDetailConstants.CONTENT_CARD_WIDTH)
            .wrapContentHeight()
            .padding(start = 32.dp)
            .background(Color(0xFF171717), shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Vertical indicator skeleton
            SkeletonBox(
                modifier = Modifier
                    .width(5.dp)
                    .height(39.dp),
                alpha = pulseAlpha,
                shimmerTranslateAnim = shimmerTranslateAnim,
                shape = RoundedCornerShape(50.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))

            // Title skeleton
            SkeletonBox(
                modifier = Modifier
                    .width((120 + index * 30).dp)
                    .height(22.sp.value.dp),
                alpha = pulseAlpha,
                shimmerTranslateAnim = shimmerTranslateAnim
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Content skeleton - multiple lines
        repeat(3 + index) { lineIndex ->
            SkeletonBox(
                modifier = Modifier
                    .fillMaxWidth(if (lineIndex == 2 + index) 0.7f else 1f)
                    .height(16.sp.value.dp),
                alpha = pulseAlpha * (0.8f - lineIndex * 0.1f),
                shimmerTranslateAnim = shimmerTranslateAnim
            )
            if (lineIndex < 2 + index) Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

// Helper composable for skeleton elements
@Composable
private fun SkeletonBox(
    modifier: Modifier = Modifier,
    alpha: Float = 0.5f,
    shimmerTranslateAnim: Float = 0f,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(4.dp)
) {
    Box(
        modifier = modifier
            .background(
                color = Color(0xFF404040).copy(alpha = alpha),
                shape = shape
            )
            .drawWithCache {
                // Shimmer gradient effect
                val shimmerColors = listOf(
                    Color.Transparent,
                    Color(0xFF8A44CB).copy(alpha = 0.1f),
                    Color.White.copy(alpha = 0.2f),
                    Color(0xFF8A44CB).copy(alpha = 0.1f),
                    Color.Transparent
                )

                val brush = Brush.linearGradient(
                    colors = shimmerColors,
                    start = Offset(shimmerTranslateAnim - 200f, 0f),
                    end = Offset(shimmerTranslateAnim + 200f, size.height)
                )

                onDrawBehind {
                    drawRoundRect(
                        brush = brush,
                        size = size,
                        cornerRadius = CornerRadius(
                            when (shape) {
                                is RoundedCornerShape -> 4.dp.toPx()
                                else -> 0f
                            }
                        )
                    )
                }
            }
    )
}
