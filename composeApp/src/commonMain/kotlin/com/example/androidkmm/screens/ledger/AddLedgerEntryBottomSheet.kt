package com.example.androidkmm.screens.ledger

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.window.DialogProperties
import java.time.Instant
import java.time.ZoneId
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import com.example.androidkmm.design.AppStyleDesignSystem
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

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
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        "${now.date.year}-${now.date.monthNumber.toString().padStart(2, '0')}-${now.date.dayOfMonth.toString().padStart(2, '0')}"
    }
    val currentTime = remember {
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
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
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    // Validation state
    var validationErrors by remember { mutableStateOf(mapOf<String, String>()) }
    
    // Validation function
    fun validateForm(): Boolean {
        val errors = mutableMapOf<String, String>()
        
        if (personName.isBlank()) {
            errors["personName"] = "Person name is required"
        }
        
        val amountValue = amount.toDoubleOrNull()
        if (amount.isBlank() || amountValue == null || amountValue <= 0) {
            errors["amount"] = "Please enter a valid amount"
        }
        
        // Description is optional, no validation needed
        
        if (selectedAccount == null) {
            errors["account"] = "Please select an account"
        }
        
        validationErrors = errors
        return errors.isEmpty()
    }
    
    // Check if form is valid for button state
    val isFormValid = personName.isNotBlank() && 
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

    // Full screen layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LedgerTheme.backgroundColor())
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding(),
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

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = LedgerTheme.textPrimary()
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
                            TextField(
                                value = personName,
                                onValueChange = { newValue ->
                                    // Limit to 20 characters
                                    if (newValue.length <= 20) {
                                        personName = newValue
                                        showSuggestions = newValue.isNotBlank() && suggestions.isNotEmpty()
                                    }
                                },
                                placeholder = {
                                    Text(
                                        text = "Enter name",
                                        color = LedgerTheme.textSecondary()
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = LedgerTheme.textSecondary()
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    unfocusedTextColor = LedgerTheme.textPrimary(),
                                    focusedTextColor = LedgerTheme.textPrimary(),
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(AppStyleDesignSystem.Padding.ARRANGEMENT_XL),
                                modifier = Modifier.fillMaxWidth()
                            )
                            
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
                        TextField(
                            value = amount,
                            onValueChange = { newValue ->
                                // Allow only numbers and one decimal point, max 2 decimal places
                                val filtered = newValue.filter { char ->
                                    char.isDigit() || char == '.'
                                }
                                
                                // Check if it's a valid decimal format
                                if (filtered.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
                                    amount = filtered
                                }
                            },
                            placeholder = {
                                Text(
                                    text = "Enter amount",
                                    color = LedgerTheme.textSecondary()
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedContainerColor = Color(0xFF1F1F1F),
                                unfocusedTextColor = LedgerTheme.textPrimary(),
                                focusedTextColor = LedgerTheme.textPrimary(),
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(AppStyleDesignSystem.Padding.ARRANGEMENT_XL),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    width = AppStyleDesignSystem.Sizes.BORDER_NORMAL,
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(AppStyleDesignSystem.Padding.ARRANGEMENT_XL)
                                ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        
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
                        TextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = {
                                Text(
                                    text = "e.g., Dinner split, Uber ride share (optional)",
                                    color = LedgerTheme.textSecondary()
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                                focusedContainerColor = Color(0xFF1F1F1F),
                                unfocusedTextColor = LedgerTheme.textPrimary(),
                                focusedTextColor = LedgerTheme.textPrimary(),
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(AppStyleDesignSystem.Padding.ARRANGEMENT_XL),
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = AppStyleDesignSystem.Sizes.INPUT_HEIGHT)
                                .border(
                                    width = AppStyleDesignSystem.Sizes.BORDER_NORMAL,
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(AppStyleDesignSystem.Padding.ARRANGEMENT_XL)
                                ),
                            maxLines = 3
                        )
                        
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
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1F1F1F)
                            ),
                            shape = RoundedCornerShape(AppStyleDesignSystem.Padding.ARRANGEMENT_XL)
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
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1F1F1F)
                            ),
                            shape = RoundedCornerShape(AppStyleDesignSystem.Padding.ARRANGEMENT_XL)
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
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1F1F1F)
                            ),
                            shape = RoundedCornerShape(AppStyleDesignSystem.Padding.ARRANGEMENT_XL),
                            contentPadding = PaddingValues(AppStyleDesignSystem.Padding.MEDIUM_LARGE)
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
                        text = "Account from which you sent the money",
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
                                        val formattedName = personName.trim().split(" ").joinToString(" ") { word ->
                                            if (word.isNotEmpty()) {
                                                word.first().uppercaseChar() + word.drop(1).lowercase()
                                            } else {
                                                word
                                            }
                                        }
                                        
                                        // Check if a person with this name already exists
                                        val existingPerson = ledgerDatabaseManager.getLedgerPersonByName(formattedName)
                                        
                                        if (existingPerson != null) {
                                            // Add transaction to existing person
                                            val transaction = LedgerTransaction(
                                                id = "transaction_${Clock.System.now().toEpochMilliseconds()}_${++transactionCounter}",
                                                personId = existingPerson.id,
                                                amount = amount.toDoubleOrNull() ?: 0.0,
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
                            .height(AppStyleDesignSystem.Sizes.INPUT_HEIGHT),
                        enabled = isFormValid,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                !isFormValid -> Color(0xFF404040)
                                person != null && currentTransactionType == TransactionType.SENT -> LedgerTheme.greenAmount
                                person != null && currentTransactionType == TransactionType.RECEIVED -> LedgerTheme.redAmount
                                else -> LedgerTheme.avatarBlue
                            }
                        ),
                        shape = RoundedCornerShape(AppStyleDesignSystem.Padding.ARRANGEMENT_XL)
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
            }
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit,
    initialDate: String
) {
    val today = java.time.LocalDate.now()
    val todayMillis = today.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    val tomorrowMillis = today.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    
    val datePickerState = rememberDatePickerState(
        selectableDates = object : androidx.compose.material3.SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                // Allow dates up to and including today
                return utcTimeMillis < tomorrowMillis
            }
            
            override fun isSelectableYear(year: Int): Boolean {
                // Only allow current year and previous years
                return year <= today.year
            }
        }
    )
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
                    fontSize = AppStyleDesignSystem.Typography.TITLE_3.fontSize,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                DatePicker(
                    state = datePickerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(horizontal = AppStyleDesignSystem.Padding.XXS),
                    colors = DatePickerDefaults.colors(
                            containerColor = Color(0xFF1F1F1F),
                            titleContentColor = LedgerTheme.textPrimary(),
                            headlineContentColor = LedgerTheme.textPrimary(),
                            weekdayContentColor = LedgerTheme.textSecondary(),
                            subheadContentColor = LedgerTheme.textSecondary(),
                            yearContentColor = LedgerTheme.textPrimary(),
                            currentYearContentColor = LedgerTheme.textPrimary(),
                            selectedYearContentColor = Color.White,
                            selectedYearContainerColor = Color(0xFF2196F3),
                            dayContentColor = LedgerTheme.textPrimary(),
                            disabledDayContentColor = LedgerTheme.textSecondary(),
                            selectedDayContentColor = Color.White,
                            disabledSelectedDayContentColor = Color.White,
                            selectedDayContainerColor = Color(0xFF2196F3),
                            disabledSelectedDayContainerColor = LedgerTheme.textSecondary(),
                            todayContentColor = Color(0xFF2196F3),
                            todayDateBorderColor = Color(0xFF2196F3),
                            dayInSelectionRangeContentColor = LedgerTheme.textPrimary(),
                            dayInSelectionRangeContainerColor = Color(0xFF2196F3).copy(alpha = 0.3f)
                        )
                    )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            val today = java.time.LocalDate.now()
                            
                            // Check if selected date is in the future
                            if (date.isAfter(today)) {
                                // Don't allow future dates - just close dialog without selecting
                                showDialog = false
                                return@TextButton
                            }
                            
                            val dateString = "${date.year}-${date.monthValue.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}"
                            onDateSelected(dateString)
                        }
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
                    fontSize = AppStyleDesignSystem.Typography.TITLE_3.fontSize,
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
                        val timeString = String.format("%02d:%02d", hour, minute)
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
