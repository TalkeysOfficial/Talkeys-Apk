package com.example.talkeys_new.screens.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.talkeys_new.R


private val UrbanistFontFamily = FontFamily(
    Font(R.font.urbanist_regular, FontWeight.Normal),
    Font(R.font.urbanist_regular, FontWeight.Medium),
    Font(R.font.urbanist_regular, FontWeight.SemiBold)
)

// Define colors
private object AppColors {
    val Background = Color(0xFF000000)
    val White = Color(0xFFFFFFFF)
    val Purple = Color(0xFF8A44CB)
    val TextPrimary = Color(0xFFB794F6) // Purple tint for subtitle

    // Purple gradient for subtitle text
    val PurpleGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF8A44CB), // Main purple
            Color(0xFFB794F6), // Light purple
            Color(0xFF6A4C93)  // Dark purple
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrganizerDashboardScreen(
    onBackClick: () -> Unit = {},
    onHostEventClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColors.Background)
            .systemBarsPadding()
    ) {
        // Top Bar
        TopAppBar(
            title = {
                Text(
                    text = "Organizer Dashboard",
                    style = TextStyle(
                        fontSize = 27.sp,
                        fontFamily = UrbanistFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.White
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = AppColors.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = Color.Transparent
            )
        )

        // Main Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(32.dp))

            // Subtitle Text with Purple Gradient
            Text(
                text = "All the events that you're organizing or helping organize will appear here",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontFamily = UrbanistFontFamily,
                    fontWeight = FontWeight.Medium,
                    brush = AppColors.PurpleGradient,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Sticker and Text Layout
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Sticker/Illustration - Increased size
                Image(
                    painter = painterResource(id = R.drawable.noevents_registered_sticker),
                    contentDescription = "No events illustration",
                    modifier = Modifier
                        .size(280.dp), // Increased from 200.dp to 280.dp
                    contentScale = ContentScale.Fit
                )

                // Text Column - Positioned lower
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 16.dp, top = 60.dp), // Added top padding to move text down
                    horizontalAlignment = Alignment.Start
                ) {
                    // Main Message
                    Text(
                        text = "You have not organized any event till now",
                        style = TextStyle(
                            fontSize = 18.sp,
                            fontFamily = UrbanistFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = AppColors.White,
                            textAlign = TextAlign.Start
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(50.dp))

            // Call to Action Text
            Text(
                text = "Organize your first event now!!!",
                style = TextStyle(
                    fontSize = 22.sp,
                    fontFamily = UrbanistFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    color = AppColors.White,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Host New Event Button
            Button(
                onClick = onHostEventClick,
                modifier = Modifier
                    .width(151.dp)
                    .height(39.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColors.Purple
                ),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(
                    start = 20.dp,
                    top = 10.dp,
                    end = 20.dp,
                    bottom = 10.dp
                )
            ) {
                Text(
                    text = "Host New Event",
                    style = TextStyle(
                        fontSize = 16.sp,
                        fontFamily = UrbanistFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.White,
                        textAlign = TextAlign.Center
                    )
                )
            }
            Spacer(modifier = Modifier.weight(0.4f))
        }
    }
}
