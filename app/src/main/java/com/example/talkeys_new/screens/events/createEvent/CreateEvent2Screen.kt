package com.example.talkeys_new.screens.events.createEvent

// Enhanced Create Event Screen 2 with comprehensive validation
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.talkeys_new.screens.common.HomeTopBar
import com.talkeys.shared.presentation.events.EventCreationStep
import com.talkeys.shared.presentation.events.EventCreationViewModel
import com.talkeys.shared.presentation.events.EventDetailsDraft
import java.util.regex.Pattern

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEvent2Screen(
    navController: NavController,
    eventCreationViewModel: EventCreationViewModel
) {
    LaunchedEffect(eventCreationViewModel) {
        eventCreationViewModel.moveToStep(EventCreationStep.Details)
    }

    // State variables for form fields
    var eventName by remember { mutableStateOf("") }
    var eventType by remember { mutableStateOf("") }
    var eventCategory by remember { mutableStateOf("") }
    var eventDescription by remember { mutableStateOf("") }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("No file selected") }
    var expanded by remember { mutableStateOf(false) }

    // Error state variables
    var eventNameError by remember { mutableStateOf("") }
    var eventTypeError by remember { mutableStateOf("") }
    var eventCategoryError by remember { mutableStateOf("") }
    var eventDescriptionError by remember { mutableStateOf("") }
    var fileError by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Event type options
    val eventTypeOptions = listOf(
        "Conference",
        "Workshop",
        "Seminar",
        "Concert",
        "Gaming Event",
        "Sports Event",
        "Exhibition",
        "Networking Event",
        "Competition",
        "Hackathon",
        "Meetup",
        "Training Session",
        "Other"
    )

    // Validation functions
    fun isValidEventName(name: String): Boolean {
        // Allow letters, numbers, spaces, and common punctuation
        val namePattern = Pattern.compile("^[a-zA-Z0-9\\s&.,'-:!?()]{3,100}$")
        return name.isNotBlank() && namePattern.matcher(name.trim()).matches()
    }

    fun isValidEventCategory(category: String): Boolean {
        val categoryPattern = Pattern.compile("^[a-zA-Z\\s,&-]{2,50}$")
        return category.isNotBlank() && categoryPattern.matcher(category.trim()).matches()
    }

    fun isValidDescription(description: String): Boolean {
        return description.isNotBlank() &&
                description.trim().length >= 10 &&
                description.trim().length <= 1000
    }

    // Enhanced validation function
    fun validateFields(): Boolean {
        var isValid = true

        // Validate event name
        if (eventName.isBlank()) {
            eventNameError = "Event name is required"
            isValid = false
        } else if (!isValidEventName(eventName)) {
            eventNameError = "Please enter a valid event name (3-100 characters)"
            isValid = false
        } else {
            eventNameError = ""
        }

        // Validate event type
        if (eventType.isBlank()) {
            eventTypeError = "Event type is required"
            isValid = false
        } else {
            eventTypeError = ""
        }

        // Validate event category
        if (eventCategory.isBlank()) {
            eventCategoryError = "Event category is required"
            isValid = false
        } else if (!isValidEventCategory(eventCategory)) {
            eventCategoryError = "Please enter a valid category (2-50 characters)"
            isValid = false
        } else {
            eventCategoryError = ""
        }

        // Validate event description
        if (eventDescription.isBlank()) {
            eventDescriptionError = "Event description is required"
            isValid = false
        } else if (!isValidDescription(eventDescription)) {
            eventDescriptionError = "Description must be between 10-1000 characters"
            isValid = false
        } else {
            eventDescriptionError = ""
        }

        // File is optional for this step, but validate if provided
        if (selectedFileUri != null) {
            fileError = ""
        }

        return isValid
    }

    // File picker launcher - restrict to images for banner/poster
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedFileUri = uri
        fileName = if (uri != null) {
            // Extract file name from URI (simplified)
            uri.lastPathSegment?.let { segment ->
                segment.split("/").lastOrNull()?.split("?")?.firstOrNull() ?: "Image selected"
            } ?: "Image selected"
        } else {
            "No file selected"
        }
        // Clear error when file is selected
        if (uri != null && fileError.isNotEmpty()) {
            fileError = ""
        }
    }

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
                            color = Color(0xFF7A2EC0),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Step 2 of 6",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Section Title
                        Text(
                            text = "2. Event Information",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Event Name Field
                        Text(
                            text = "Event Name *",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = eventName,
                            onValueChange = {
                                eventName = it
                                if (eventNameError.isNotEmpty()) eventNameError = ""
                            },
                            placeholder = {
                                Text(
                                    "Enter event name",
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            isError = eventNameError.isNotEmpty(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (eventNameError.isNotEmpty()) Color.Red else Color(0xFF7A2EC0),
                                unfocusedBorderColor = if (eventNameError.isNotEmpty()) Color.Red else Color.White.copy(alpha = 0.3f),
                                errorBorderColor = Color.Red,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF7A2EC0),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (eventNameError.isNotEmpty()) {
                            Text(
                                text = eventNameError,
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Event Type Dropdown
                        Text(
                            text = "Event Type *",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = {
                                expanded = !expanded
                                if (eventTypeError.isNotEmpty()) eventTypeError = ""
                            }
                        ) {
                            OutlinedTextField(
                                value = eventType.ifEmpty { "-- Select an option --" },
                                onValueChange = { },
                                readOnly = true,
                                placeholder = {
                                    Text(
                                        "-- Select an option --",
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
                                isError = eventTypeError.isNotEmpty(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = if (eventTypeError.isNotEmpty()) Color.Red else Color(0xFF7A2EC0),
                                    unfocusedBorderColor = if (eventTypeError.isNotEmpty()) Color.Red else Color.White.copy(alpha = 0.3f),
                                    errorBorderColor = Color.Red,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.background(Color(0xFF2A2A2A))
                            ) {
                                eventTypeOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option,
                                                color = Color.White
                                            )
                                        },
                                        onClick = {
                                            eventType = option
                                            expanded = false
                                            if (eventTypeError.isNotEmpty()) eventTypeError = ""
                                        }
                                    )
                                }
                            }
                        }
                        if (eventTypeError.isNotEmpty()) {
                            Text(
                                text = eventTypeError,
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Event Category Field
                        Text(
                            text = "Event Category *",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = eventCategory,
                            onValueChange = {
                                eventCategory = it
                                if (eventCategoryError.isNotEmpty()) eventCategoryError = ""
                            },
                            placeholder = {
                                Text(
                                    "Concert, Gaming, Workshop etc",
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            isError = eventCategoryError.isNotEmpty(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (eventCategoryError.isNotEmpty()) Color.Red else Color(0xFF7A2EC0),
                                unfocusedBorderColor = if (eventCategoryError.isNotEmpty()) Color.Red else Color.White.copy(alpha = 0.3f),
                                errorBorderColor = Color.Red,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF7A2EC0),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (eventCategoryError.isNotEmpty()) {
                            Text(
                                text = eventCategoryError,
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Event Description Field
                        Text(
                            text = "Event Description *",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = eventDescription,
                            onValueChange = {
                                eventDescription = it
                                if (eventDescriptionError.isNotEmpty()) eventDescriptionError = ""
                            },
                            placeholder = {
                                Text(
                                    "Short and clear explanation (10-1000 characters)",
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            isError = eventDescriptionError.isNotEmpty(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (eventDescriptionError.isNotEmpty()) Color.Red else Color(0xFF7A2EC0),
                                unfocusedBorderColor = if (eventDescriptionError.isNotEmpty()) Color.Red else Color.White.copy(alpha = 0.3f),
                                errorBorderColor = Color.Red,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF7A2EC0),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(8.dp),
                            minLines = 4,
                            maxLines = 6,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (eventDescriptionError.isNotEmpty()) {
                            Text(
                                text = eventDescriptionError,
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )
                        }

                        // Character count
                        Text(
                            text = "${eventDescription.length}/1000 characters",
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // File Upload Section
                        Text(
                            text = "Event Banner / Poster (Optional)",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // File Picker Button
                        OutlinedButton(
                            onClick = {
                                filePickerLauncher.launch("image/*")
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = Color.Transparent,
                                contentColor = Color.White
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color.White.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachFile,
                                contentDescription = "Choose Image",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Choose Image", fontSize = 16.sp)
                        }

                        // File Status
                        Text(
                            text = fileName,
                            fontSize = 14.sp,
                            color = if (selectedFileUri != null) Color.Green else Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        if (fileError.isNotEmpty()) {
                            Text(
                                text = fileError,
                                color = Color.Red,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
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
                                    navController.navigate("create_event_1")
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
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Next Button
                            Button(
                                onClick = {
                                    eventCreationViewModel.updateDetails(
                                        EventDetailsDraft(
                                            eventName = eventName,
                                            eventType = eventType,
                                            eventCategory = eventCategory,
                                            eventDescription = eventDescription,
                                            eventBanner = selectedFileUri.toSharedImage(context, fileName)
                                        )
                                    )
                                    if (eventCreationViewModel.goNext()) {
                                        navController.navigate("create_event_3")
                                    } else {
                                        val errors = eventCreationViewModel.uiState.value.validationErrors
                                        eventNameError = errors["eventName"].orEmpty()
                                        eventTypeError = errors["eventType"].orEmpty()
                                        eventCategoryError = errors["eventCategory"].orEmpty()
                                        eventDescriptionError = errors["eventDescription"].orEmpty()
                                        fileError = errors["eventBanner"].orEmpty()
                                    }
                                },
                                enabled = true, // Always enabled, validation happens on click
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF7A2EC0),
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
