package com.example.talkeys_new.screens.events.createEvent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.talkeys_new.R
import com.example.talkeys_new.screens.common.HomeTopBar
import com.talkeys.shared.presentation.events.EventCreationStep
import com.talkeys.shared.presentation.events.EventCreationViewModel
import com.talkeys.shared.presentation.events.EventReviewDraft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEvent6Screen(
    navController: NavController,
    eventCreationViewModel: EventCreationViewModel
) {
    LaunchedEffect(eventCreationViewModel) {
        eventCreationViewModel.moveToStep(EventCreationStep.Review)
    }

    // State variables for checkboxes
    var isInfoAccurate by remember { mutableStateOf(false) }
    var agreeToTerms by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Check if both checkboxes are checked
    val canSubmit = isInfoAccurate && agreeToTerms

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A0B2E),  // Deep purple at top
                        Color.Black         // Black at bottom
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar
            HomeTopBar(navController = navController)

            // Main Content Card
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.6f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Header
                        Text(
                            text = "Create Your Event",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily(Font(R.font.urbanist_bold)),
                            color = Color(0xFF7A2EC0), // Pink color matching the image
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Step 6 of 6",
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.urbanist_medium)),
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Section Title
                        Text(
                            text = "6. Agreement & Submission",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily(Font(R.font.urbanist_bold)),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // First Checkbox - Information is accurate
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isInfoAccurate = !isInfoAccurate }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Custom Checkbox
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (isInfoAccurate) Color(0xFF7A2EC0)
                                        else Color.Transparent
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!isInfoAccurate) {
                                    // Border when unchecked
                                    androidx.compose.foundation.Canvas(
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        drawRoundRect(
                                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                                        )
                                    }
                                } else {
                                    // Check mark when checked
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Checked",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Text(
                                text = "I confirm all information is accurate",
                                fontSize = 16.sp,
                                fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                color = Color.White,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Second Checkbox - Terms and privacy policy
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { agreeToTerms = !agreeToTerms }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Custom Checkbox
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        if (agreeToTerms) Color(0xFF7A2EC0)
                                        else Color.Transparent
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!agreeToTerms) {
                                    // Border when unchecked
                                    androidx.compose.foundation.Canvas(
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        drawRoundRect(
                                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.5f),
                                            style = androidx.compose.ui.graphics.drawscope.Stroke(width = 2.dp.toPx()),
                                            cornerRadius = androidx.compose.ui.geometry.CornerRadius(4.dp.toPx())
                                        )
                                    }
                                } else {
                                    // Check mark when checked
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Checked",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Terms text with underlined link
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "I agree to abide by ",
                                    fontSize = 16.sp,
                                    fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                    color = Color.White
                                )
                                Text(
                                    text = "terms and privacy policy",
                                    fontSize = 16.sp,
                                    fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                    color = Color(0xFF7A2EC0),
                                    textDecoration = TextDecoration.Underline,
                                    modifier = Modifier.clickable {
                                        // TODO: Navigate to terms and privacy policy
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(40.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                eventCreationViewModel.updateReview(
                                    EventReviewDraft(
                                        isInfoAccurate = isInfoAccurate,
                                        agreeToTerms = agreeToTerms
                                    )
                                )
                                if (eventCreationViewModel.requestSubmit()) {
                                    // TODO: Handle event submission logic here (API call, etc.)
                                    eventCreationViewModel.markSubmissionHandled()
                                    eventCreationViewModel.resetDraft()

                                    // Navigate to registration success screen
                                    navController.navigate("registration_success") {
                                        popUpTo("create_event_1") { inclusive = true }
                                    }
                                }
                            },
                            enabled = canSubmit,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (canSubmit)
                                    Color(0xFF7A2EC0) else Color.Gray.copy(alpha = 0.3f),
                                contentColor = Color.White,
                                disabledContainerColor = Color.Gray.copy(alpha = 0.2f),
                                disabledContentColor = Color.White.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Text(
                                text = "Submit ↗",
                                fontSize = 18.sp,
                                fontFamily = FontFamily(Font(R.font.urbanist_bold)),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Previous Button
                        OutlinedButton(
                            onClick = {
                                eventCreationViewModel.goPrevious()
                                navController.navigate("create_event_5")
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color.White.copy(alpha = 0.5f)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowBack,
                                contentDescription = "Previous",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Previous",
                                fontSize = 16.sp,
                                fontFamily = FontFamily(Font(R.font.urbanist_medium)),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}
