package com.example.talkeys_new.screens.events

import android.content.Context
import android.content.Intent
import androidx.navigation.NavController
import com.talkeys.shared.presentation.events.EventPlatformRequest
import com.talkeys.shared.presentation.events.EventsRoute

fun handleEventPlatformRequest(
    context: Context,
    navController: NavController,
    request: EventPlatformRequest
) {
    when (request) {
        is EventPlatformRequest.ShareEvent -> {
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, request.subject)
                putExtra(Intent.EXTRA_TEXT, request.text)
            }
            context.startActivity(Intent.createChooser(sendIntent, "Share Event"))
        }

        is EventPlatformRequest.NavigateToEventDetail -> {
            navController.navigate("eventDetail/${request.eventId}")
        }

        is EventPlatformRequest.NavigateToRoute -> {
            val route = when (request.route) {
                EventsRoute.EventList -> "events"
                EventsRoute.EventCreation -> "create_event_1"
            }
            navController.navigate(route)
        }
    }
}
