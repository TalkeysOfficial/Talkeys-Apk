package com.example.talkeys_new.screens.events.createEvent

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.talkeys_new.R
import com.example.talkeys_new.screens.common.HomeTopBar
import com.talkeys.shared.presentation.events.EventAudienceDraft
import com.talkeys.shared.presentation.events.EventCreationStep
import com.talkeys.shared.presentation.events.EventCreationViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEvent5Screen(
    navController: NavController,
    eventCreationViewModel: EventCreationViewModel
) {
    LaunchedEffect(eventCreationViewModel) {
        eventCreationViewModel.moveToStep(EventCreationStep.Audience)
    }

    // State variables for form fields
    var communityChat by remember { mutableStateOf("") }
    var communityChatDropdownExpanded by remember { mutableStateOf(false) }
    var sponsors by remember { mutableStateOf("") }
    var sponsorsDropdownExpanded by remember { mutableStateOf(false) }
    var audienceType by remember { mutableStateOf("") }
    var selectedFileName by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }

    val context = LocalContext.current
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedFileUri = uri
        selectedFileName = uri?.lastPathSegment
            ?.split("/")
            ?.lastOrNull()
            ?.split("?")
            ?.firstOrNull()
            ?: ""
    }

    // Dropdown options
    val communityChatOptions = listOf("Yes", "No")
    val sponsorsOptions = listOf("Yes", "No")

    // Check if required fields are filled
    val areRequiredFieldsFilled = communityChat.isNotBlank() &&
            sponsors.isNotBlank() &&
            audienceType.isNotBlank()

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
                            text = "Step 5 of 6",
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.urbanist_medium)),
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Section Title
                        Text(
                            text = "5. Additional Features",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = FontFamily(Font(R.font.urbanist_bold)),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Enable Community Chat? Dropdown
                        Text(
                            text = "Enable Community Chat?",
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.urbanist_medium)),
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        ExposedDropdownMenuBox(
                            expanded = communityChatDropdownExpanded,
                            onExpandedChange = { communityChatDropdownExpanded = !communityChatDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = communityChat.ifEmpty { "-- Select an option --" },
                                onValueChange = { },
                                readOnly = true,
                                placeholder = {
                                    Text(
                                        "-- Select an option --",
                                        fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Dropdown",
                                        tint = Color.White.copy(alpha = 0.7f)
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF7A2EC0),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = FontFamily(Font(R.font.urbanist_regular))
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = communityChatDropdownExpanded,
                                onDismissRequest = { communityChatDropdownExpanded = false },
                                modifier = Modifier.background(Color(0xFF2A2A2A))
                            ) {
                                communityChatOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option,
                                                fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                                color = Color.White
                                            )
                                        },
                                        onClick = {
                                            communityChat = option
                                            communityChatDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Any sponsors or partners? Dropdown
                        Text(
                            text = "Any sponsors or partners?",
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.urbanist_medium)),
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        ExposedDropdownMenuBox(
                            expanded = sponsorsDropdownExpanded,
                            onExpandedChange = { sponsorsDropdownExpanded = !sponsorsDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = sponsors.ifEmpty { "-- Select an option --" },
                                onValueChange = { },
                                readOnly = true,
                                placeholder = {
                                    Text(
                                        "-- Select an option --",
                                        fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Dropdown",
                                        tint = Color.White.copy(alpha = 0.7f)
                                    )
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Color(0xFF7A2EC0),
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = FontFamily(Font(R.font.urbanist_regular))
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = sponsorsDropdownExpanded,
                                onDismissRequest = { sponsorsDropdownExpanded = false },
                                modifier = Modifier.background(Color(0xFF2A2A2A))
                            ) {
                                sponsorsOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option,
                                                fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                                color = Color.White
                                            )
                                        },
                                        onClick = {
                                            sponsors = option
                                            sponsorsDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Expected Audience Type Text Field
                        Text(
                            text = "Expected Audience Type",
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.urbanist_medium)),
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        OutlinedTextField(
                            value = audienceType,
                            onValueChange = { audienceType = it },
                            placeholder = {
                                Text(
                                    "e.g. College Students",
                                    fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF7A2EC0),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                cursorColor = Color(0xFF7A2EC0)
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily(Font(R.font.urbanist_regular))
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Upload Event Deck / Proposal Section
                        Text(
                            text = "📤 Upload Event Deck / Proposal",
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.urbanist_medium)),
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // File Upload Button
                        Button(
                            onClick = {
                                filePickerLauncher.launch("*/*")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF7A2EC0),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = "Upload",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Choose File",
                                fontSize = 16.sp,
                                fontFamily = FontFamily(Font(R.font.urbanist_medium)),
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // File selection status
                        if (selectedFileName.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = selectedFileName,
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                color = Color(0xFF7A2EC0),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No file selected",
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                color = Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // Navigation Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Previous Button
                            OutlinedButton(
                                onClick = {
                                    eventCreationViewModel.goPrevious()
                                    navController.navigate("create_event_4")
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
                                    .weight(0.4f)
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

                            Spacer(modifier = Modifier.width(16.dp))

                            // Next Button
                            Button(
                                onClick = {
                                    eventCreationViewModel.updateAudience(
                                        EventAudienceDraft(
                                            communityChat = communityChat,
                                            sponsors = sponsors,
                                            audienceType = audienceType,
                                            sponsorDeck = selectedFileUri.toSharedImage(context, selectedFileName)
                                        )
                                    )
                                    if (eventCreationViewModel.goNext()) {
                                        navController.navigate("create_event_6")
                                    }
                                },
                                enabled = areRequiredFieldsFilled,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (areRequiredFieldsFilled)
                                        Color(0xFF7A2EC0) else Color.Gray.copy(alpha = 0.3f),
                                    contentColor = Color.White,
                                    disabledContainerColor = Color.Gray.copy(alpha = 0.2f),
                                    disabledContentColor = Color.White.copy(alpha = 0.4f)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(0.4f)
                                    .height(56.dp)
                            ) {
                                Text(
                                    "Next ›",
                                    fontSize = 18.sp,
                                    fontFamily = FontFamily(Font(R.font.urbanist_bold)),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}
