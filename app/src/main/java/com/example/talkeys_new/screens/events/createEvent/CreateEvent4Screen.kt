package com.example.talkeys_new.screens.events.createEvent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
import com.talkeys.shared.presentation.events.EventPricingDraft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEvent4Screen(
    navController: NavController,
    eventCreationViewModel: EventCreationViewModel
) {
    LaunchedEffect(eventCreationViewModel) {
        eventCreationViewModel.moveToStep(EventCreationStep.Pricing)
    }

    // State variables for form fields
    var eventType by remember { mutableStateOf("") }
    var eventTypeDropdownExpanded by remember { mutableStateOf(false) }
    var ticketPrice by remember { mutableStateOf("") }
    var discounts by remember { mutableStateOf("") }
    var discountsDropdownExpanded by remember { mutableStateOf(false) }
    var discountPercentage by remember { mutableStateOf("") }
    var qrCheckIn by remember { mutableStateOf("") }
    var qrCheckInDropdownExpanded by remember { mutableStateOf(false) }
    var refundPolicy by remember { mutableStateOf("") }
    var refundPolicyDropdownExpanded by remember { mutableStateOf(false) }

    // Error states
    var ticketPriceError by remember { mutableStateOf("") }
    var discountPercentageError by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Dropdown options
    val eventTypeOptions = listOf("Free", "Paid")
    val discountOptions = listOf("Yes", "No")
    val qrCheckInOptions = listOf("Yes", "No")
    val refundPolicyOptions = listOf(
        "No Refunds",
        "Full Refund (24 hours before)",
        "Partial Refund (48 hours before)",
        "Custom Policy"
    )

    // Validation functions
    fun validateTicketPrice(value: String): Boolean {
        return try {
            val price = value.toDouble()
            price >= 0
        } catch (e: NumberFormatException) {
            false
        }
    }

    fun validateDiscountPercentage(value: String): Boolean {
        return try {
            val percentage = value.toInt()
            percentage in 1..100
        } catch (e: NumberFormatException) {
            false
        }
    }

    fun validateForm(): Boolean {
        var isValid = true

        // Validate ticket price if event is paid
        if (eventType == "Paid") {
            if (ticketPrice.isBlank()) {
                ticketPriceError = "Ticket price is required for paid events"
                isValid = false
            } else if (!validateTicketPrice(ticketPrice)) {
                ticketPriceError = "Please enter a valid price (0 or greater)"
                isValid = false
            }
        }

        // Validate discount percentage if discounts are enabled
        if (discounts == "Yes") {
            if (discountPercentage.isBlank()) {
                discountPercentageError = "Discount percentage is required"
                isValid = false
            } else if (!validateDiscountPercentage(discountPercentage)) {
                discountPercentageError = "Please enter a valid percentage (1-100)"
                isValid = false
            }
        }

        return isValid
    }

    // Check if all required fields are filled and valid
    val areAllFieldsFilled = eventType.isNotBlank() &&
            discounts.isNotBlank() &&
            qrCheckIn.isNotBlank() &&
            refundPolicy.isNotBlank() &&
            (eventType != "Paid" || (ticketPrice.isNotBlank() && validateTicketPrice(ticketPrice))) &&
            (discounts != "Yes" || (discountPercentage.isNotBlank() && validateDiscountPercentage(discountPercentage)))

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
                            text = "Step 4 of 6",
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Section Title
                        Text(
                            text = "4. Ticketing & Access",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                            color = Color.White,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Is the Event Free or Paid? Dropdown
                        Text(
                            text = "Is the Event Free or Paid?",
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        ExposedDropdownMenuBox(
                            expanded = eventTypeDropdownExpanded,
                            onExpandedChange = {
                                eventTypeDropdownExpanded = !eventTypeDropdownExpanded
                            }
                        ) {
                            OutlinedTextField(
                                value = eventType.ifEmpty { "-- Select an option --" },
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
                                expanded = eventTypeDropdownExpanded,
                                onDismissRequest = { eventTypeDropdownExpanded = false },
                                modifier = Modifier.background(Color(0xFF2A2A2A))
                            ) {
                                eventTypeOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option,
                                                fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                                color = Color.White
                                            )
                                        },
                                        onClick = {
                                            eventType = option
                                            eventTypeDropdownExpanded = false
                                            // Clear ticket price if switching to Free
                                            if (option == "Free") {
                                                ticketPrice = ""
                                                ticketPriceError = ""
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Ticket Price Field (only show if event is paid)
                        if (eventType == "Paid") {
                            Text(
                                text = "Ticket Price (₹)",
                                fontSize = 16.sp,
                                fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                color = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = ticketPrice,
                                onValueChange = { value ->
                                    // Allow only numbers and decimal point
                                    val filtered = value.filter { it.isDigit() || it == '.' }
                                    if (filtered.count { it == '.' } <= 1) {
                                        ticketPrice = filtered
                                        if (ticketPrice.isNotEmpty() && validateTicketPrice(ticketPrice)) {
                                            ticketPriceError = ""
                                        }
                                    }
                                },
                                placeholder = {
                                    Text(
                                        "500.00",
                                        fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                isError = ticketPriceError.isNotEmpty(),
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

                            if (ticketPriceError.isNotEmpty()) {
                                Text(
                                    text = ticketPriceError,
                                    color = Color.Red,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        // Any Discounts? Dropdown
                        Text(
                            text = "Any Discounts?",
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        ExposedDropdownMenuBox(
                            expanded = discountsDropdownExpanded,
                            onExpandedChange = { discountsDropdownExpanded = !discountsDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = discounts.ifEmpty { "-- Select an option --" },
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
                                expanded = discountsDropdownExpanded,
                                onDismissRequest = { discountsDropdownExpanded = false },
                                modifier = Modifier.background(Color(0xFF2A2A2A))
                            ) {
                                discountOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option,
                                                fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                                color = Color.White
                                            )
                                        },
                                        onClick = {
                                            discounts = option
                                            discountsDropdownExpanded = false
                                            // Clear discount percentage if switching to No
                                            if (option == "No") {
                                                discountPercentage = ""
                                                discountPercentageError = ""
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Discount Percentage Field (only show if discounts are enabled)
                        if (discounts == "Yes") {
                            Text(
                                text = "Discount Percentage (%)",
                                fontSize = 16.sp,
                                fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                color = Color.White.copy(alpha = 0.9f),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            OutlinedTextField(
                                value = discountPercentage,
                                onValueChange = { value ->
                                    val filtered = value.filter { it.isDigit() }
                                    if (filtered.length <= 3) { // Max 3 digits (100%)
                                        discountPercentage = filtered
                                        if (discountPercentage.isNotEmpty() && validateDiscountPercentage(discountPercentage)) {
                                            discountPercentageError = ""
                                        }
                                    }
                                },
                                placeholder = {
                                    Text(
                                        "10",
                                        fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                        color = Color.White.copy(alpha = 0.5f)
                                    )
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                isError = discountPercentageError.isNotEmpty(),
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

                            if (discountPercentageError.isNotEmpty()) {
                                Text(
                                    text = discountPercentageError,
                                    color = Color.Red,
                                    fontSize = 14.sp,
                                    fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                    modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                        }

                        // Enable QR-based check-in? Dropdown
                        Text(
                            text = "Enable QR-based check-in?",
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        ExposedDropdownMenuBox(
                            expanded = qrCheckInDropdownExpanded,
                            onExpandedChange = { qrCheckInDropdownExpanded = !qrCheckInDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = qrCheckIn.ifEmpty { "-- Select an option --" },
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
                                expanded = qrCheckInDropdownExpanded,
                                onDismissRequest = { qrCheckInDropdownExpanded = false },
                                modifier = Modifier.background(Color(0xFF2A2A2A))
                            ) {
                                qrCheckInOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option,
                                                fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                                color = Color.White
                                            )
                                        },
                                        onClick = {
                                            qrCheckIn = option
                                            qrCheckInDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Refund Policy Dropdown
                        Text(
                            text = "Refund Policy",
                            fontSize = 16.sp,
                            fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                            color = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        ExposedDropdownMenuBox(
                            expanded = refundPolicyDropdownExpanded,
                            onExpandedChange = { refundPolicyDropdownExpanded = !refundPolicyDropdownExpanded }
                        ) {
                            OutlinedTextField(
                                value = refundPolicy.ifEmpty { "-- Select an option --" },
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
                                expanded = refundPolicyDropdownExpanded,
                                onDismissRequest = { refundPolicyDropdownExpanded = false },
                                modifier = Modifier.background(Color(0xFF2A2A2A))
                            ) {
                                refundPolicyOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = option,
                                                fontFamily = FontFamily(Font(R.font.urbanist_regular)),
                                                color = Color.White
                                            )
                                        },
                                        onClick = {
                                            refundPolicy = option
                                            refundPolicyDropdownExpanded = false
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
                                    navController.navigate("create_event_3")
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
                                    eventCreationViewModel.updatePricing(
                                        EventPricingDraft(
                                            eventType = eventType,
                                            ticketPrice = ticketPrice,
                                            discounts = discounts,
                                            discountPercentage = discountPercentage,
                                            qrCheckIn = qrCheckIn,
                                            refundPolicy = refundPolicy
                                        )
                                    )
                                    if (eventCreationViewModel.goNext()) {
                                        navController.navigate("create_event_5")
                                    } else {
                                        val errors = eventCreationViewModel.uiState.value.validationErrors
                                        ticketPriceError = errors["ticketPrice"].orEmpty()
                                        discountPercentageError = errors["discountPercentage"].orEmpty()
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
