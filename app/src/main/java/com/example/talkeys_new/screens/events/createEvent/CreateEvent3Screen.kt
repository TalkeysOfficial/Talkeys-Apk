package com.example.talkeys_new.screens.events.createEvent

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.talkeys_new.R
import com.example.talkeys_new.screens.common.HomeTopBar
import com.talkeys.shared.presentation.events.EventCreationStep
import com.talkeys.shared.presentation.events.EventCreationViewModel
import com.talkeys.shared.presentation.events.EventScheduleDraft
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEvent3Screen(
    navController: NavController,
    eventCreationViewModel: EventCreationViewModel
) {
    LaunchedEffect(eventCreationViewModel) {
        eventCreationViewModel.moveToStep(EventCreationStep.Schedule)
    }

    // State variables for form fields
    var eventDates by remember { mutableStateOf("") }
    var startTime by remember { mutableStateOf("") }
    var endTime by remember { mutableStateOf("") }
    var registrationDeadline by remember { mutableStateOf("") }
    var maxAttendees by remember { mutableStateOf("") }
    var platformUsed by remember { mutableStateOf("") }
    var willBeRecorded by remember { mutableStateOf("") }
    var recordingDropdownExpanded by remember { mutableStateOf(false) }

    // Error states
    var eventDateError by remember { mutableStateOf("") }
    var startTimeError by remember { mutableStateOf("") }
    var endTimeError by remember { mutableStateOf("") }
    var registrationDeadlineError by remember { mutableStateOf("") }
    var maxAttendeesError by remember { mutableStateOf("") }

    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Recording options
    val recordingOptions = listOf("Yes", "No")

    // Date picker for event dates
    val eventDatePicker = DatePickerDialog(
        context,
        { _, year, month, day ->
            eventDates = String.format("%04d-%02d-%02d", year, month + 1, day)
            eventDateError = ""
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Date picker for registration deadline
    val registrationDatePicker = DatePickerDialog(
        context,
        { _, year, month, day ->
            registrationDeadline = String.format("%04d-%02d-%02d", year, month + 1, day)
            registrationDeadlineError = ""
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    // Time picker for start time
    val startTimePicker = TimePickerDialog(
        context,
        { _, hour, minute ->
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            }
            startTime = timeFormat.format(cal.time)
            startTimeError = ""
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )

    // Time picker for end time
    val endTimePicker = TimePickerDialog(
        context,
        { _, hour, minute ->
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val cal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            }
            endTime = timeFormat.format(cal.time)
            endTimeError = ""
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )

    // Validation functions
    fun validateMaxAttendees(value: String): Boolean {
        return try {
            val num = value.toInt()
            num > 0
        } catch (e: NumberFormatException) {
            false
        }
    }

    fun validateForm(): Boolean {
        var isValid = true

        if (eventDates.isBlank()) {
            eventDateError = "Event date is required"
            isValid = false
        }

        if (startTime.isBlank()) {
            startTimeError = "Start time is required"
            isValid = false
        }

        if (endTime.isBlank()) {
            endTimeError = "End time is required"
            isValid = false
        }

        if (registrationDeadline.isBlank()) {
            registrationDeadlineError = "Registration deadline is required"
            isValid = false
        }

        if (maxAttendees.isBlank()) {
            maxAttendeesError = "Maximum attendees is required"
            isValid = false
        } else if (!validateMaxAttendees(maxAttendees)) {
            maxAttendeesError = "Please enter a valid number greater than 0"
            isValid = false
        }

        return isValid
    }

    // Check if all fields are filled and valid
    val areAllFieldsFilled = eventDates.isNotBlank() &&
            startTime.isNotBlank() &&
            endTime.isNotBlank() &&
            registrationDeadline.isNotBlank() &&
            maxAttendees.isNotBlank() &&
            validateMaxAttendees(maxAttendees) &&
            platformUsed.isNotBlank() &&
            willBeRecorded.isNotBlank()

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
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                            color = Color(0xFF7A2EC0),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Step 3 of 6",
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Section Title
                        Text(
                            text = "3. Event Timing & Logistics",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Event Date(s) Field with Date Picker
                        Text(
                            text = "📅 Event Date(s)",
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = eventDates,
                            onValueChange = { },
                            readOnly = true,
                            placeholder = {
                                Text(
                                    "Select event date",
                                    fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Select Date",
                                    tint = Color(0xFF7A2EC0),
                                    modifier = Modifier.clickable {
                                        eventDatePicker.show()
                                    }
                                )
                            },
                            isError = eventDateError.isNotEmpty(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF7A2EC0),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                errorBorderColor = Color.Red,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF7A2EC0),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily(Font(R.font.urbanist_regular))
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    eventDatePicker.show()
                                }
                        )

                        if (eventDateError.isNotEmpty()) {
                            Text(
                                text = eventDateError,
                                color = Color.Red,
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Start Time Field with Time Picker
                        Text(
                            text = "🕒 Start Time",
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { },
                            readOnly = true,
                            placeholder = {
                                Text(
                                    "Select start time",
                                    fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Select Time",
                                    tint = Color(0xFF7A2EC0),
                                    modifier = Modifier.clickable {
                                        startTimePicker.show()
                                    }
                                )
                            },
                            isError = startTimeError.isNotEmpty(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF7A2EC0),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                errorBorderColor = Color.Red,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF7A2EC0),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily(Font(R.font.urbanist_regular))
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    startTimePicker.show()
                                }
                        )

                        if (startTimeError.isNotEmpty()) {
                            Text(
                                text = startTimeError,
                                color = Color.Red,
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // End Time Field with Time Picker
                        Text(
                            text = "🕐 End Time",
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = endTime,
                            onValueChange = { },
                            readOnly = true,
                            placeholder = {
                                Text(
                                    "Select end time",
                                    fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Schedule,
                                    contentDescription = "Select Time",
                                    tint = Color(0xFF7A2EC0),
                                    modifier = Modifier.clickable {
                                        endTimePicker.show()
                                    }
                                )
                            },
                            isError = endTimeError.isNotEmpty(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF7A2EC0),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                errorBorderColor = Color.Red,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF7A2EC0),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily(Font(R.font.urbanist_regular))
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    endTimePicker.show()
                                }
                        )

                        if (endTimeError.isNotEmpty()) {
                            Text(
                                text = endTimeError,
                                color = Color.Red,
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Registration Deadline Field with Date Picker
                        Text(
                            text = "Registration Deadline",
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = registrationDeadline,
                            onValueChange = { },
                            readOnly = true,
                            placeholder = {
                                Text(
                                    "Select registration deadline",
                                    fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = "Select Date",
                                    tint = Color(0xFF7A2EC0),
                                    modifier = Modifier.clickable {
                                        registrationDatePicker.show()
                                    }
                                )
                            },
                            isError = registrationDeadlineError.isNotEmpty(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF7A2EC0),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                errorBorderColor = Color.Red,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF7A2EC0),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily(Font(R.font.urbanist_regular))
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    registrationDatePicker.show()
                                }
                        )

                        if (registrationDeadlineError.isNotEmpty()) {
                            Text(
                                text = registrationDeadlineError,
                                color = Color.Red,
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Max No. of Attendees Field with Number Keyboard
                        Text(
                            text = "Max No. of Attendees",
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = maxAttendees,
                            onValueChange = { value ->
                                maxAttendees = value.filter { it.isDigit() }
                                if (maxAttendees.isNotEmpty() && validateMaxAttendees(maxAttendees)) {
                                    maxAttendeesError = ""
                                }
                            },
                            placeholder = {
                                Text(
                                    "100",
                                    fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = maxAttendeesError.isNotEmpty(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF7A2EC0),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                errorBorderColor = Color.Red,
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF7A2EC0),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily(Font(R.font.urbanist_regular))
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (maxAttendeesError.isNotEmpty()) {
                            Text(
                                text = maxAttendeesError,
                                color = Color.Red,
                                fontSize = 14.sp,
                                fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Online Details Section
                        Text(
                            text = "Online Details",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Platform Used Field
                        Text(
                            text = "Platform Used",
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        OutlinedTextField(
                            value = platformUsed,
                            onValueChange = { platformUsed = it },
                            placeholder = {
                                Text(
                                    "e.g Zoom, Google Meet, Teams",
                                    fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                    color = Color.White.copy(alpha = 0.5f)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF7A2EC0),
                                unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White,
                                cursorColor = Color(0xFF7A2EC0),
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily(Font(R.font.urbanist_regular))
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Will this be recorded? Dropdown
                        Text(
                            text = "Will this be recorded?",
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        ExposedDropdownMenuBox(
                            expanded = recordingDropdownExpanded,
                            onExpandedChange = { recordingDropdownExpanded = !recordingDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = willBeRecorded.ifEmpty { "-- Select an option --" },
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
                                expanded = recordingDropdownExpanded,
                                onDismissRequest = { recordingDropdownExpanded = false },
                                modifier = Modifier.background(Color(0xFF2A2A2A))
                            ) {
                                recordingOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option,
                                                fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                                color = Color.White
                                            )
                                        },
                                        onClick = {
                                            willBeRecorded = option
                                            recordingDropdownExpanded = false
                                        }
                                    )
                                }
                            }
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
                                    navController.navigate("create_event_2")
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
                                    fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                    fontWeight = FontWeight.Normal
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Next Button
                            Button(
                                onClick = {
                                    eventCreationViewModel.updateSchedule(
                                        EventScheduleDraft(
                                            eventDates = eventDates,
                                            startTime = startTime,
                                            endTime = endTime,
                                            registrationDeadline = registrationDeadline,
                                            maxAttendees = maxAttendees,
                                            platformUsed = platformUsed,
                                            willBeRecorded = willBeRecorded
                                        )
                                    )
                                    if (eventCreationViewModel.goNext()) {
                                        navController.navigate("create_event_4")
                                    } else {
                                        val errors = eventCreationViewModel.uiState.value.validationErrors
                                        eventDateError = errors["eventDates"].orEmpty()
                                        startTimeError = errors["startTime"].orEmpty()
                                        endTimeError = errors["endTime"].orEmpty()
                                        registrationDeadlineError = errors["registrationDeadline"].orEmpty()
                                        maxAttendeesError = errors["maxAttendees"].orEmpty()
                                    }
                                },
                                enabled = areAllFieldsFilled,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (areAllFieldsFilled)
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
                                    fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                    fontWeight = FontWeight.Normal
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
