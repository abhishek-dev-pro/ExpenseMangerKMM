package com.example.androidkmm.screens.ledger

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.window.DialogProperties
import com.example.androidkmm.utils.DateTimeUtils
import com.example.androidkmm.utils.FormValidation
import kotlinx.datetime.*
import kotlin.time.ExperimentalTime
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.androidkmm.design.AppStyleDesignSystem
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import com.example.androidkmm.database.rememberSQLiteLedgerDatabase
import com.example.androidkmm.database.rememberSQLiteTransactionDatabase
import com.example.androidkmm.database.rememberSQLiteAccountDatabase
import com.example.androidkmm.database.rememberSQLiteSettingsDatabase
import com.example.androidkmm.models.AppSettings
import com.example.androidkmm.components.AddAccountBottomSheet
import com.example.androidkmm.components.BeautifulDateSelector
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// AddLedgerEntryBottomSheet.kt
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun AddLedgerEntryBottomSheet(
    onDismiss: () -> Unit,
    person: LedgerPerson? = null,
    transactionType: TransactionType = TransactionType.SENT
) {
    val ledgerDatabaseManager = rememberSQLiteLedgerDatabase()
    val accountDatabaseManager = rememberSQLiteAccountDatabase()
    val transactionDatabaseManager = rememberSQLiteTransactionDatabase()
    val settingsDatabaseManager = rememberSQLiteSettingsDatabase()
    val coroutineScope = rememberCoroutineScope()
    
    // Get currency symbol from settings
    val appSettings = settingsDatabaseManager.getAppSettings().collectAsState(initial = AppSettings())
    val currencySymbol = appSettings.value.currencySymbol
    var transactionCounter by remember { mutableStateOf(0) }
    var personCounter by remember { mutableStateOf(0) }
    
    // Get all existing people for suggestions
    val allPeopleState = ledgerDatabaseManager.getAllLedgerPersons().collectAsState(initial = emptyList<LedgerPerson>())
    val allPeople = allPeopleState.value
    
    // Removed bottom sheet state for full screen display
    var personName by remember { mutableStateOf(person?.name ?: "") }
    var showSuggestions by remember { mutableStateOf(false) }
    var currentTransactionType by remember { mutableStateOf(transactionType) }
    
    // Filter suggestions based on person name input
    val suggestions = remember(personName, allPeople) {
        if (personName.isBlank()) {
            emptyList()
        } else {
            allPeople.filter { it.name.contains(personName, ignoreCase = true) }
        }
    }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    
    // Get current date and time
    val currentDate = remember {
        val now = DateTimeUtils.getCurrentDateTime()
        DateTimeUtils.formatDate(now.date)
    }
    val currentTime = remember {
        val now = DateTimeUtils.getCurrentDateTime()
        val time = now.time
        val hour = time.hour
        val minute = time.minute
        val amPm = if (hour < 12) "AM" else "PM"
        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
        "${displayHour.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} $amPm"
    }
    
    var selectedDate by remember { mutableStateOf(currentDate) }
    var selectedTime by remember { mutableStateOf(currentTime) }
    var selectedAccount by remember { mutableStateOf<com.example.androidkmm.models.Account?>(null) }
    var showAccountSelection by remember { mutableStateOf(false) }
    var showAddAccountSheet by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    // Validation state
    var validationErrors by remember { mutableStateOf(mapOf<String, String>()) }
    
    // Validation function
    fun validateForm(): Boolean {
        val errors = mutableMapOf<String, String>()
        
        // Validate person name using the new validation function
        val nameValidation = FormValidation.validatePersonName(personName)
        if (!nameValidation.isValid) {
            errors["personName"] = nameValidation.errors["name"] ?: "Please enter a valid name"
        }
        
        val amountValue = amount.toDoubleOrNull()
        if (amount.isBlank() || amountValue == null || amountValue <= 0) {
            errors["amount"] = "Please enter a valid amount"
        }
        
        // Validate description length (optional but if provided, must be <= 100 characters)
        if (description.isNotBlank() && description.length > 100) {
            errors["description"] = "Description must be 100 characters or less"
        }
        
        if (selectedAccount == null) {
            errors["account"] = "Please select an account"
        }
        
        validationErrors = errors
        return errors.isEmpty()
    }
    
    // Check if form is valid for button state
    val isFormValid = FormValidation.validatePersonName(personName).isValid && 
                     amount.isNotBlank() && 
                     amount.toDoubleOrNull() != null && 
                     amount.toDoubleOrNull()!! > 0 &&
                     selectedAccount != null

    val commonExamples = listOf(
        "Dinner Split" to "Restaurant bill",
        "Ride Share" to "Travel expense",
        "Emergency Loan" to "Urgent help",
        "Groceries" to "Shared shopping"
    )

    // Get focus manager for keyboard dismissal
    val focusManager = LocalFocusManager.current
    
    // Full screen layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LedgerTheme.backgroundColor())
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                focusManager.clearFocus()
            }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .imePadding(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
                item {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when {
                                person != null && currentTransactionType == TransactionType.SENT -> "You Sent Money"
                                person != null && currentTransactionType == TransactionType.RECEIVED -> "You Received Money"
                                else -> "Add Ledger Entry"
                            },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = LedgerTheme.textPrimary()
                        )

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .background(
                                    Color(0xFF2C2C2E),
                                    CircleShape
                                )
                                .size(AppStyleDesignSystem.Sizes.AVATAR_MEDIUM)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_MEDIUM)
                            )
                        }
                    }
                }

                if (person != null) {
                    item {
                        // Recording message
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (currentTransactionType == TransactionType.SENT) Color(0xFF2A1919) else Color(0xFF0F2419),
                                    RoundedCornerShape(AppStyleDesignSystem.CornerRadius.XL)
                                )
                                .padding(AppStyleDesignSystem.Padding.MEDIUM_LARGE)
                        ) {
                            Text(
                                text = if (currentTransactionType == TransactionType.SENT) {
                                    "Recording money you sent to ${person.name}"
                                } else {
                                    "Recording money you received from ${person.name}"
                                },
                                fontSize = AppStyleDesignSystem.Typography.CALL_OUT.fontSize,
                                color = if (currentTransactionType == TransactionType.SENT) LedgerTheme.redAmount else LedgerTheme.greenAmount
                            )
                        }
                    }
                } else {
                    item {
                        // Person's Name
                        Text(
                            text = "Person's Name",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LedgerTheme.textPrimary()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = LedgerTheme.textSecondary(),
                                modifier = Modifier.padding(end = 12.dp)
                            )
                            val interactionSource = remember { MutableInteractionSource() }
                            val isFocused by interactionSource.collectIsFocusedAsState()
                            
                            BasicTextField(
                                value = personName,
                                onValueChange = { newValue ->
                                    // Limit to 22 characters
                                    if (newValue.length <= 22) {
                                        personName = newValue
                                        showSuggestions = newValue.isNotBlank() && suggestions.isNotEmpty()
                                    }
                                },
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 16.sp
                                ),
                                singleLine = true,
                                interactionSource = interactionSource,
                                cursorBrush = SolidColor(Color.White),
                                modifier = Modifier
                                    .weight(1f)
                                    .background(
                                        Color.Transparent,
                                        RoundedCornerShape(AppStyleDesignSystem.Padding.ARRANGEMENT_XL)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = Color.White.copy(alpha = 0.3f),
                                        shape = RoundedCornerShape(AppStyleDesignSystem.Padding.ARRANGEMENT_XL)
                                    )
                                    .padding(horizontal = 16.dp, vertical = 16.dp)
                            ) { innerTextField ->
                                if (personName.isEmpty() && !isFocused) {
                                    Text(
                                        text = "Enter name",
                                        color = LedgerTheme.textSecondary()
                                    )
                                }
                                innerTextField()
                            }
                        }
                            
                            // Show suggestions dropdown
                            if (showSuggestions && suggestions.isNotEmpty()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = AppStyleDesignSystem.Padding.ARRANGEMENT_TINY)
                                        .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM))
                                        .border(
                                            width = AppStyleDesignSystem.Sizes.BORDER_THIN, // very thin border
                                            color = Color.White.copy(alpha = 0.2f), // subtle white
                                            shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
                                        ),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                    ),
                                    shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
                                ) {
                                    Column {
                                        suggestions.take(5).forEach { suggestion ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        personName = suggestion.name
                                                        showSuggestions = false
                                                    }
                                                    .padding(AppStyleDesignSystem.Padding.ARRANGEMENT_MEDIUM),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = LedgerTheme.textSecondary(),
                                                    modifier = Modifier.size(AppStyleDesignSystem.Padding.ARRANGEMENT_XL)
                                                )
                                                Spacer(modifier = Modifier.width(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL))
                                                Text(
                                                    text = suggestion.name,
                                                    color = LedgerTheme.textPrimary(),
                                                    fontSize = AppStyleDesignSystem.Typography.CALL_OUT.fontSize
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        // Show error message for person name
                        validationErrors["personName"]?.let { error ->
                            Text(
                                text = error,
                                color = LedgerTheme.redAmount,
                                fontSize = AppStyleDesignSystem.Typography.FOOTNOTE.fontSize,
                                modifier = Modifier.padding(top = AppStyleDesignSystem.Padding.ARRANGEMENT_TINY, start = AppStyleDesignSystem.Padding.MEDIUM_LARGE)
                            )
                        }
                    }

                    item {
                        // Transaction Type
                        Text(
                            text = "Transaction Type",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LedgerTheme.textPrimary()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // You Sent Button
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .wrapContentHeight()
                                    .clickable { currentTransactionType = TransactionType.SENT }
                                    .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM))
                                    .border(
                                        width = AppStyleDesignSystem.Sizes.BORDER_THIN, // very thin border
                                        color = Color.White.copy(alpha = 0.2f), // subtle white
                                        shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (currentTransactionType == TransactionType.SENT) Color(0xFF0F2419) else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                border = if (currentTransactionType == TransactionType.SENT) BorderStroke(AppStyleDesignSystem.Sizes.BORDER_THICK, LedgerTheme.greenAmount) else null,
                                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(AppStyleDesignSystem.Padding.MEDIUM_LARGE),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(AppStyleDesignSystem.Sizes.ICON_SIZE_XL)
                                            .background(
                                                if (currentTransactionType == TransactionType.SENT) LedgerTheme.greenAmount else MaterialTheme.colorScheme.onSurfaceVariant,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowUpward,
                                            contentDescription = null,
                                            tint = if (currentTransactionType == TransactionType.SENT) Color.White else Color.Black,
                                            modifier = Modifier.size(AppStyleDesignSystem.Padding.MEDIUM_LARGE)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(AppStyleDesignSystem.Padding.ARRANGEMENT_TINY))

                                    Text(
                                        text = "You Sent",
                                        fontSize = AppStyleDesignSystem.Typography.FOOTNOTE.fontSize,
                                        fontWeight = FontWeight.Medium,
                                        color = if (currentTransactionType == TransactionType.SENT) LedgerTheme.greenAmount else LedgerTheme.textSecondary()
                                    )
                                    Text(
                                        text = "Money you sent",
                                        fontSize = AppStyleDesignSystem.Typography.CAPTION_2.fontSize,
                                        color = LedgerTheme.textSecondary()
                                    )
                                }
                            }

                            // You Received Button
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .wrapContentHeight()
                                    .clickable { currentTransactionType = TransactionType.RECEIVED }
                                    .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM))
                                    .border(
                                        width = AppStyleDesignSystem.Sizes.BORDER_THIN, // very thin border
                                        color = Color.White.copy(alpha = 0.2f), // subtle white
                                        shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (currentTransactionType == TransactionType.RECEIVED) Color(0xFF2A1919) else MaterialTheme.colorScheme.surfaceVariant
                                ),
                                border = if (currentTransactionType == TransactionType.RECEIVED) BorderStroke(AppStyleDesignSystem.Sizes.BORDER_THICK, LedgerTheme.redAmount) else null,
                                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(AppStyleDesignSystem.Padding.MEDIUM_LARGE),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(AppStyleDesignSystem.Sizes.ICON_SIZE_XL)
                                            .background(
                                                if (currentTransactionType == TransactionType.RECEIVED) LedgerTheme.redAmount else MaterialTheme.colorScheme.onSurfaceVariant,
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = null,
                                            tint = if (currentTransactionType == TransactionType.RECEIVED) Color.White else Color.Black,
                                            modifier = Modifier.size(AppStyleDesignSystem.Padding.MEDIUM_LARGE)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(AppStyleDesignSystem.Padding.ARRANGEMENT_TINY))

                                    Text(
                                        text = "You Received",
                                        fontSize = AppStyleDesignSystem.Typography.FOOTNOTE.fontSize,
                                        fontWeight = FontWeight.Medium,
                                        color = if (currentTransactionType == TransactionType.RECEIVED) LedgerTheme.redAmount else LedgerTheme.textSecondary()
                                    )
                                    Text(
                                        text = "Money you received",
                                        fontSize = AppStyleDesignSystem.Typography.CAPTION_2.fontSize,
                                        color = LedgerTheme.textSecondary()
                                    )
                                }
                            }
                        }
                    }
                }


                item {
                    // Amount
                    Text(
                        text = "Amount",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LedgerTheme.textPrimary()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column {
                        val amountInteractionSource = remember { MutableInteractionSource() }
                        val isAmountFocused by amountInteractionSource.collectIsFocusedAsState()
                        
                        BasicTextField(
                            value = amount,
                            onValueChange = { newValue ->
                                // Allow only numbers and one decimal point, max 2 decimal places
                                val filtered = newValue.filter { char ->
                                    char.isDigit() || char == '.'
                                }
                                
                                // Limit to maximum 8 digits (excluding decimal point)
                                val digitsOnly = filtered.filter { char -> char.isDigit() }
                                val decimalCount = filtered.count { char -> char == '.' }
                                
                                // Check if it's a valid decimal format and within digit limit
                                if (filtered.matches(Regex("^\\d*\\.?\\d{0,2}$")) && 
                                    digitsOnly.length <= 8 && 
                                    decimalCount <= 1) {
                                    amount = filtered
                                }
                            },
                            textStyle = TextStyle(
                                color = LedgerTheme.textPrimary(),
                                fontSize = 16.sp
                            ),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            interactionSource = amountInteractionSource,
                            cursorBrush = SolidColor(Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    Color(0xFF1F1F1F),
                                    RoundedCornerShape(AppStyleDesignSystem.Padding.ARRANGEMENT_XL)
                                )
                                .border(
                                    width = AppStyleDesignSystem.Sizes.BORDER_NORMAL,
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(AppStyleDesignSystem.Padding.ARRANGEMENT_XL)
                                )
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        ) { innerTextField ->
                            if (amount.isEmpty() && !isAmountFocused) {
                                Text(
                                    text = "Enter amount",
                                    color = LedgerTheme.textSecondary()
                                )
                            }
                            innerTextField()
                        }
                        
                        // Show error message
                        validationErrors["amount"]?.let { error ->
                            Text(
                                text = error,
                                color = LedgerTheme.redAmount,
                                fontSize = AppStyleDesignSystem.Typography.FOOTNOTE.fontSize,
                                modifier = Modifier.padding(top = AppStyleDesignSystem.Padding.ARRANGEMENT_TINY, start = AppStyleDesignSystem.Padding.MEDIUM_LARGE)
                            )
                        }
                    }
                }

                item {
                    // Description
                    Text(
                        text = "Description (Optional)",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LedgerTheme.textPrimary()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column {
                        val descriptionInteractionSource = remember { MutableInteractionSource() }
                        val isDescriptionFocused by descriptionInteractionSource.collectIsFocusedAsState()
                        
                        BasicTextField(
                            value = description,
                            onValueChange = { newValue ->
                                // Limit to 100 characters
                                if (newValue.length <= 100) {
                                    description = newValue
                                }
                            },
                            textStyle = TextStyle(
                                color = LedgerTheme.textPrimary(),
                                fontSize = 16.sp
                            ),
                            interactionSource = descriptionInteractionSource,
                            cursorBrush = SolidColor(Color.White),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = AppStyleDesignSystem.Sizes.INPUT_HEIGHT)
                                .background(
                                    Color(0xFF1F1F1F),
                                    RoundedCornerShape(AppStyleDesignSystem.Padding.ARRANGEMENT_XL)
                                )
                                .border(
                                    width = AppStyleDesignSystem.Sizes.BORDER_NORMAL,
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(AppStyleDesignSystem.Padding.ARRANGEMENT_XL)
                                )
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            maxLines = 3
                        ) { innerTextField ->
                            if (description.isEmpty() && !isDescriptionFocused) {
                                Text(
                                    text = "e.g., Dinner split, Uber ride share (optional)",
                                    color = LedgerTheme.textSecondary()
                                )
                            }
                            innerTextField()
                        }
                        
                        // Show error message
                        validationErrors["description"]?.let { error ->
                            Text(
                                text = error,
                                color = LedgerTheme.redAmount,
                                fontSize = AppStyleDesignSystem.Typography.FOOTNOTE.fontSize,
                                modifier = Modifier.padding(top = AppStyleDesignSystem.Padding.ARRANGEMENT_TINY, start = AppStyleDesignSystem.Padding.MEDIUM_LARGE)
                            )
                        }
                    }
                }

                item {
                    // Date & Time
                    Text(
                        text = "Date & Time",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LedgerTheme.textPrimary()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Date Picker
                        Button(
                            onClick = { showDatePicker = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2D2D2D)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 2.dp
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = LedgerTheme.textPrimary(),
                                modifier = Modifier.size(AppStyleDesignSystem.Padding.MEDIUM_LARGE)
                            )
                            Spacer(modifier = Modifier.width(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL))
                            Text(
                                text = selectedDate,
                                color = LedgerTheme.textPrimary(),
                                fontSize = AppStyleDesignSystem.Typography.CALL_OUT.fontSize
                            )
                        }

                        // Time Picker
                        Button(
                            onClick = { showTimePicker = true },
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2D2D2D)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 2.dp
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = LedgerTheme.textPrimary(),
                                modifier = Modifier.size(AppStyleDesignSystem.Padding.MEDIUM_LARGE)
                            )
                            Spacer(modifier = Modifier.width(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL))
                            Text(
                                text = selectedTime,
                                color = LedgerTheme.textPrimary(),
                                fontSize = AppStyleDesignSystem.Typography.CALL_OUT.fontSize
                            )
                            Spacer(modifier = Modifier.width(AppStyleDesignSystem.Padding.ARRANGEMENT_TINY))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = LedgerTheme.textSecondary(),
                                modifier = Modifier.size(AppStyleDesignSystem.Padding.ARRANGEMENT_MEDIUM)
                            )
                        }
                    }
                }

                item {
                    // Account
                    Text(
                        text = "Account",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = LedgerTheme.textPrimary()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Column {
                        Button(
                            onClick = { showAccountSelection = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2D2D2D)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 2.dp
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wallet,
                                    contentDescription = null,
                                    tint = LedgerTheme.textPrimary(),
                                    modifier = Modifier.size(AppStyleDesignSystem.Padding.MEDIUM_LARGE)
                                )
                                Spacer(modifier = Modifier.width(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL))
                                Box(
                                    modifier = Modifier
                                        .size(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL)
                                        .background(LedgerTheme.greenAmount, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL))
                                Text(
                                    text = selectedAccount?.name ?: "Select account",
                                    color = LedgerTheme.textPrimary(),
                                    fontSize = AppStyleDesignSystem.Typography.CALL_OUT.fontSize,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Start
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = LedgerTheme.textSecondary(),
                                    modifier = Modifier.size(AppStyleDesignSystem.Padding.MEDIUM_LARGE)
                                )
                            }
                        }
                        
                        // Show error message
                        validationErrors["account"]?.let { error ->
                            Text(
                                text = error,
                                color = LedgerTheme.redAmount,
                                fontSize = AppStyleDesignSystem.Typography.FOOTNOTE.fontSize,
                                modifier = Modifier.padding(top = AppStyleDesignSystem.Padding.ARRANGEMENT_TINY, start = AppStyleDesignSystem.Padding.MEDIUM_LARGE)
                            )
                        }
                    }

                    Text(
                        text = if (currentTransactionType == TransactionType.SENT) {
                            "Account from which you sent the money"
                        } else {
                            "Account in which you receive money"
                        },
                        fontSize = AppStyleDesignSystem.Typography.FOOTNOTE.fontSize,
                        color = LedgerTheme.textSecondary(),
                        modifier = Modifier.padding(top = AppStyleDesignSystem.Padding.ARRANGEMENT_TINY)
                    )
                }

                if (person == null) {
                    item {
                        // Common Examples
//                        Text(
//                            text = "Common Examples",
//                            fontSize = AppStyleDesignSystem.Typography.HEADLINE.fontSize,
//                            fontWeight = FontWeight.Medium,
//                            color = LedgerTheme.textPrimary()
//                        )
//
//                        Spacer(modifier = Modifier.height(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL))

//                        LazyVerticalGrid(
//                            columns = GridCells.Fixed(2),
//                            horizontalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL),
//                            verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL),
//                            modifier = Modifier.height(16AppStyleDesignSystem.Padding.XXS)
//                        ) {
//                            items(commonExamples) { (title, subtitle) ->
//                                Button(
//                                    onClick = {
//                                        description = title
//                                    },
//                                    modifier = Modifier
//                                        .fillMaxWidth()
//                                        .wrapContentHeight(),
//                                    colors = ButtonDefaults.buttonColors(
//                                        containerColor = Color(0xFF1F1F1F)
//                                    ),
//                                    shape = RoundedCornerShape(AppStyleDesignSystem.Padding.MEDIUM_LARGE),
//                                    contentPadding = PaddingValues(AppStyleDesignSystem.Padding.ARRANGEMENT_MEDIUM)
//                                ) {
//                                    Column(
//                                        horizontalAlignment = Alignment.CenterHorizontally
//                                    ) {
//                                        Text(
//                                            text = title,
//                                            color = LedgerTheme.textPrimary(),
//                                            fontSize = AppStyleDesignSystem.Typography.FOOTNOTE.fontSize,
//                                            fontWeight = FontWeight.Medium,
//                                            textAlign = TextAlign.Center
//                                        )
//                                        Text(
//                                            text = subtitle,
//                                            color = LedgerTheme.textSecondary(),
//                                            fontSize = AppStyleDesignSystem.Typography.CAPTION_2.fontSize,
//                                            textAlign = TextAlign.Center
//                                        )
//                                    }
//                                }
//                            }
//                        }

                    }
                }

                item {
                    // Add Entry Button
                    Button(
                        onClick = { 
                            if (validateForm()) {
                                coroutineScope.launch {
                                    try {
                                        // Format the person name (capitalize first letter of each word)
                                        val formattedName = FormValidation.capitalizeName(personName.trim())
                                        
                                        // Check if a person with this name already exists
                                        val existingPerson = ledgerDatabaseManager.getLedgerPersonByName(formattedName)
                                        
                                        if (existingPerson != null) {
                                            // Add transaction to existing person
                                            val transaction = LedgerTransaction(
                                                id = "transaction_${Clock.System.now().toEpochMilliseconds()}_${++transactionCounter}",
                                                personId = existingPerson.id,
                                                amount = amount.toDoubleOrNull() ?: 0.0,
                                                title = "",
                                                description = description,
                                                date = selectedDate,
                                                time = selectedTime,
                                                type = currentTransactionType,
                                                account = selectedAccount?.name
                                                // balanceAtTime will be set in addLedgerTransactionAndUpdatePerson
                                            )
                                            
                                            ledgerDatabaseManager.addLedgerTransactionAndUpdatePerson(transaction, existingPerson.id, transactionDatabaseManager, accountDatabaseManager)
                                        } else {
                                            // Create new person and add transaction
                                            val newPerson = LedgerPerson(
                                                id = "person_${Clock.System.now().toEpochMilliseconds()}_${++personCounter}",
                                                name = formattedName,
                                                avatarColor = LedgerTheme.avatarBlue,
                                                balance = 0.0,
                                                transactionCount = 0,
                                                lastTransactionDate = ""
                                            )
                                            
                                            ledgerDatabaseManager.insertLedgerPerson(newPerson)
                                            
                                            val transaction = LedgerTransaction(
                                                id = "transaction_${Clock.System.now().toEpochMilliseconds()}_${++transactionCounter}",
                                                personId = newPerson.id,
                                                amount = amount.toDoubleOrNull() ?: 0.0,
                                                title = "",
                                                description = description,
                                                date = selectedDate,
                                                time = selectedTime,
                                                type = currentTransactionType,
                                                account = selectedAccount?.name
                                                // balanceAtTime will be set in addLedgerTransactionAndUpdatePerson
                                            )
                                            
                                            ledgerDatabaseManager.addLedgerTransactionAndUpdatePerson(transaction, newPerson.id, transactionDatabaseManager, accountDatabaseManager)
                                        }
                                    onDismiss()
                                } catch (e: Exception) {
                                    // Handle error
                                    println("Error saving ledger transaction: ${e.message}")
                                }
                            }
                        }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        enabled = isFormValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                !isFormValid -> Color(0xFF404040)
                                person != null && currentTransactionType == TransactionType.SENT -> LedgerTheme.greenAmount
                                person != null && currentTransactionType == TransactionType.RECEIVED -> LedgerTheme.redAmount
                                else -> LedgerTheme.avatarBlue
                            }
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = if (isFormValid) 6.dp else 2.dp,
                            pressedElevation = if (isFormValid) 4.dp else 1.dp
                        ),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = if (isFormValid) Color.White else LedgerTheme.textSecondary(),
                            modifier = Modifier.size(AppStyleDesignSystem.Padding.MEDIUM_LARGE)
                        )
                        Spacer(modifier = Modifier.width(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL))
                        Text(
                            text = when {
                                person != null && currentTransactionType == TransactionType.SENT -> "Record Sent"
                                person != null && currentTransactionType == TransactionType.RECEIVED -> "Record Received"
                                else -> "Add Entry"
                            },
                            color = if (isFormValid) Color.White else LedgerTheme.textSecondary(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(100.dp))

                }
            }
        }
        
        // Show account selection bottom sheet
    if (showAccountSelection) {
        AccountSelectionBottomSheet(
            onDismiss = { showAccountSelection = false },
            title = "Select Account",
            subtitle = "Choose an account for your transaction",
            onAccountSelected = { account ->
                selectedAccount = account
                showAccountSelection = false
            },
            accountDatabaseManager = accountDatabaseManager,
            onAddAccount = { showAddAccountSheet = true }
        )
    }
    
    // Show date picker dialog
    if (showDatePicker) {
        SimpleDatePickerDialog(
            onDismiss = { showDatePicker = false },
            onDateSelected = { date ->
                selectedDate = date
                showDatePicker = false
            },
            initialDate = selectedDate
        )
    }
    
    // Show time picker dialog
    if (showTimePicker) {
        SimpleTimePickerDialog(
            onDismiss = { showTimePicker = false },
            onTimeSelected = { time ->
                selectedTime = time
                showTimePicker = false
            },
            initialTime = selectedTime
        )
    }
    
    // Add Account Bottom Sheet
    if (showAddAccountSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddAccountSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = null
        ) {
            AddAccountBottomSheet(
                onDismiss = { showAddAccountSheet = false },
                onAccountAdded = { account ->
                    accountDatabaseManager.addAccount(
                        account = account,
                        onSuccess = {
                            selectedAccount = account
                            showAddAccountSheet = false
                        },
                        onError = { error ->
                            println("Error adding account: ${error.message}")
                        }
                    )
                },
                accountDatabaseManager = accountDatabaseManager
            )
        }
    }
}

private fun formatTime(hour: Int, minute: Int): String {
    val hourStr = if (hour < 10) "0$hour" else hour.toString()
    val minuteStr = if (minute < 10) "0$minute" else minute.toString()
    return "$hourStr:$minuteStr"
}

@OptIn(ExperimentalTime::class)
@Composable
fun SimpleDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit,
    initialDate: String
) {
    val today = DateTimeUtils.getCurrentDate()
    
    // Parse initial date or use today
    val initialParsedDate = if (initialDate.isNotEmpty()) {
        DateTimeUtils.parseDate(initialDate) ?: today
    } else {
        today
    }
    
    var selectedDate by remember { mutableStateOf(initialParsedDate) }
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            title = {
                Text(
                    text = "Select Date",
                    color = LedgerTheme.textPrimary(),
                    fontSize = AppStyleDesignSystem.Typography.MAIN_PAGE_SUBHEADING_TITLE.fontSize,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                BeautifulDateSelector(
                    selectedDate = selectedDate,
                    onDateSelected = { selectedDate = it },
                    maxDate = today
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Check if selected date is in the future
                        if (DateTimeUtils.isDateAfter(selectedDate, today)) {
                            // Don't allow future dates - just close dialog without selecting
                            showDialog = false
                            return@TextButton
                        }
                        
                        val dateString = DateTimeUtils.formatDate(selectedDate)
                        onDateSelected(dateString)
                        showDialog = false
                    }
                ) {
                    Text(
                        text = "OK",
                        color = LedgerTheme.textPrimary(),
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Cancel",
                        color = LedgerTheme.textSecondary()
                    )
                }
            },
            containerColor = Color(0xFF1F1F1F)
        )
    }
}

@Composable
private fun CustomDatePicker(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    maxDate: LocalDate
) {
    val today = DateTimeUtils.getCurrentDate()
    
    // Generate date options
    val dateOptions = remember {
        val options = mutableListOf<DateOption>()
        
        // Today
        options.add(DateOption("Today", today, today == selectedDate))
        
        // Yesterday
        val yesterday = DateTimeUtils.addDays(today, -1)
        if (!DateTimeUtils.isDateAfter(yesterday, maxDate)) {
            options.add(DateOption("Yesterday", yesterday, yesterday == selectedDate))
        }
        
        // This week (last 7 days)
        for (i in 2..7) {
            val date = DateTimeUtils.addDays(today, -i)
            if (!DateTimeUtils.isDateAfter(date, maxDate)) {
                val dayName = when (i) {
                    2 -> "2 days ago"
                    3 -> "3 days ago"
                    4 -> "4 days ago"
                    5 -> "5 days ago"
                    6 -> "6 days ago"
                    7 -> "1 week ago"
                    else -> "${i} days ago"
                }
                options.add(DateOption(dayName, date, date == selectedDate))
            }
        }
        
        // This month (last 30 days)
        for (i in 8..30) {
            val date = DateTimeUtils.addDays(today, -i)
            if (!DateTimeUtils.isDateAfter(date, maxDate)) {
                val dayName = when (i) {
                    8, 9, 10, 11, 12, 13, 14 -> "${i} days ago"
                    15, 16, 17, 18, 19, 20, 21 -> "${i} days ago"
                    22, 23, 24, 25, 26, 27, 28 -> "${i} days ago"
                    29 -> "29 days ago"
                    30 -> "1 month ago"
                    else -> "${i} days ago"
                }
                options.add(DateOption(dayName, date, date == selectedDate))
            }
        }
        
        options
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(dateOptions.size) { index ->
            val option = dateOptions[index]
            DateOptionItem(
                option = option,
                onClick = { onDateSelected(option.date) }
            )
        }
    }
}

@Composable
private fun DateOptionItem(
    option: DateOption,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (option.isSelected) Color(0xFF2196F3) else Color(0xFF2A2A2A)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = option.label,
                    color = if (option.isSelected) Color.White else LedgerTheme.textPrimary(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = DateTimeUtils.formatDate(option.date),
                    color = if (option.isSelected) Color.White.copy(alpha = 0.8f) else LedgerTheme.textSecondary(),
                    fontSize = 14.sp
                )
            }
            
            if (option.isSelected) {
                Text(
                    text = "✓",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

private data class DateOption(
    val label: String,
    val date: LocalDate,
    val isSelected: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTimePickerDialog(
    onDismiss: () -> Unit,
    onTimeSelected: (String) -> Unit,
    initialTime: String
) {
    val timePickerState = rememberTimePickerState(
        initialHour = 12,
        initialMinute = 0,
        is24Hour = false
    )
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            title = {
                Text(
                    text = "Select Time",
                    color = LedgerTheme.textPrimary(),
                    fontSize = AppStyleDesignSystem.Typography.MAIN_PAGE_SUBHEADING_TITLE.fontSize,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = Color(0xFF2A2A2A),
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = LedgerTheme.textPrimary(),
                        selectorColor = Color(0xFF2196F3),
                        periodSelectorBorderColor = LedgerTheme.textSecondary(),
                        periodSelectorSelectedContainerColor = Color(0xFF2196F3),
                        periodSelectorUnselectedContainerColor = Color(0xFF2A2A2A),
                        periodSelectorSelectedContentColor = Color.White,
                        periodSelectorUnselectedContentColor = LedgerTheme.textPrimary(),
                        timeSelectorSelectedContainerColor = Color(0xFF2196F3),
                        timeSelectorUnselectedContainerColor = Color(0xFF2A2A2A),
                        timeSelectorSelectedContentColor = Color.White,
                        timeSelectorUnselectedContentColor = LedgerTheme.textPrimary()
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val hour = timePickerState.hour
                        val minute = timePickerState.minute
                        val timeString = formatTime(hour, minute)
                        onTimeSelected(timeString)
                        showDialog = false
                    }
                ) {
                    Text(
                        text = "OK",
                        color = LedgerTheme.textPrimary(),
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Cancel",
                        color = LedgerTheme.textSecondary()
                    )
                }
            },
            containerColor = Color(0xFF1F1F1F)
        )
    }
}
