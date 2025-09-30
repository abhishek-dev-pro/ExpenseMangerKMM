package com.example.androidkmm.screens.ledger

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.androidkmm.utils.DateTimeUtils
import kotlinx.datetime.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
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
import com.example.androidkmm.utils.CurrencyUtils.removeCurrencySymbols
import com.example.androidkmm.models.AppSettings
import com.example.androidkmm.design.AppStyleDesignSystem
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlin.time.ExperimentalTime

// EditLedgerEntryBottomSheet.kt
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun EditLedgerEntryBottomSheet(
    onDismiss: () -> Unit,
    transaction: LedgerTransaction,
    person: LedgerPerson
) {
    val ledgerDatabaseManager = rememberSQLiteLedgerDatabase()
    val accountDatabaseManager = rememberSQLiteAccountDatabase()
    val transactionDatabaseManager = rememberSQLiteTransactionDatabase()
    val settingsDatabaseManager = rememberSQLiteSettingsDatabase()
    val coroutineScope = rememberCoroutineScope()
    
    // Get currency symbol from settings
    val appSettings = settingsDatabaseManager.getAppSettings().collectAsState(initial = AppSettings())
    val currencySymbol = appSettings.value.currencySymbol
    
    // Get all accounts for selection
    val allAccountsState = accountDatabaseManager.getActiveAccounts().collectAsState(initial = emptyList<com.example.androidkmm.models.Account>())
    val allAccounts = allAccountsState.value
    
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    
    // Initialize form fields with transaction data
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var description by remember { mutableStateOf(transaction.description) }
    var selectedDate by remember { mutableStateOf(transaction.date) }
    var selectedTime by remember { mutableStateOf(transaction.time) }
    var selectedAccount by remember { mutableStateOf<com.example.androidkmm.models.Account?>(null) }
    var showAccountSelection by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    // Find the account from the transaction
    LaunchedEffect(transaction.account, allAccounts) {
        println("DEBUG: EditLedgerEntryBottomSheet - Looking for account: '${transaction.account}'")
        println("DEBUG: EditLedgerEntryBottomSheet - Available accounts: ${allAccounts.map { it.name }}")
        
        if (transaction.account != null && transaction.account.isNotBlank()) {
            // Try exact match first
            var foundAccount = allAccounts.find { it.name == transaction.account }
            
            // If not found, try case-insensitive match
            if (foundAccount == null) {
                foundAccount = allAccounts.find { it.name.equals(transaction.account, ignoreCase = true) }
            }
            
            // If still not found, try partial match
            if (foundAccount == null) {
                foundAccount = allAccounts.find { 
                    it.name.contains(transaction.account, ignoreCase = true) || 
                    transaction.account.contains(it.name, ignoreCase = true)
                }
            }
            
            println("DEBUG: EditLedgerEntryBottomSheet - Found account: $foundAccount")
            
            if (foundAccount != null) {
                selectedAccount = foundAccount
            } else {
                // If account is not found in database, show error and prevent editing
                println("ERROR: EditLedgerEntryBottomSheet - Account '${transaction.account}' not found in database")
                // Don't create temporary account - this can cause data integrity issues
                // Instead, show error state or disable editing
                selectedAccount = null
            }
        } else {
            // If no account is specified, try to find "Cash" as default
            val cashAccount = allAccounts.find { it.name.equals("Cash", ignoreCase = true) }
            println("DEBUG: EditLedgerEntryBottomSheet - No account specified, using Cash: $cashAccount")
            selectedAccount = cashAccount
        }
    }
    
    // Validation state
    var validationErrors by remember { mutableStateOf(mapOf<String, String>()) }
    
    // Validation function
    fun validateForm(): Boolean {
        val errors = mutableMapOf<String, String>()
        
        val amountValue = amount.toDoubleOrNull()
        if (amount.isBlank() || amountValue == null || amountValue <= 0) {
            errors["amount"] = "Please enter a valid amount"
        }
        
        if (selectedAccount == null) {
            errors["account"] = "Please select an account"
        }
        
        validationErrors = errors
        return errors.isEmpty()
    }
    
    // Check if form is valid for button state
    val isFormValid = amount.isNotBlank() && 
                     amount.toDoubleOrNull() != null && 
                     amount.toDoubleOrNull()!! > 0 &&
                     selectedAccount != null

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        content = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LedgerTheme.backgroundColor())
                    .navigationBarsPadding()
                    .imePadding(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_XXL)
            ) {
                // Show error if account is not found
                if (selectedAccount == null && !transaction.account.isNullOrBlank()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFD32F2F),
                                    modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_BUTTON_SIZE)
                                )
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = "Account Not Found",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color(0xFFD32F2F),
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "The account '${transaction.account}' is no longer available. This transaction cannot be edited.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color(0xFFD32F2F),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
                item {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Edit Transaction",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = LedgerTheme.textPrimary()
                        )
                        
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = LedgerTheme.textSecondary(),
                                modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_LARGE)
                            )
                        }
                    }
                }
                
                item {
                    // Amount Field
                    Column {
                        Text(
                            text = "Amount",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = LedgerTheme.textPrimary(),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        TextField(
                            value = amount,
                            onValueChange = { newValue ->
                                // Filter to allow only numbers and decimal point
                                val filtered = newValue.filter { it.isDigit() || it == '.' }
                                // Allow only one decimal point
                                val decimalCount = filtered.count { it == '.' }
                                
                                // Limit to maximum 8 digits (excluding decimal point)
                                val digitsOnly = filtered.filter { char -> char.isDigit() }
                                
                                // Check if it's a valid decimal format and within digit limit
                                if (filtered.matches(Regex("^\\d*\\.?\\d{0,2}$")) && 
                                    digitsOnly.length <= 8 && 
                                    decimalCount <= 1) {
                                    amount = filtered
                                }
                            },
                            placeholder = { Text("0.00", color = LedgerTheme.textSecondary()) },
                            leadingIcon = {
                                Text(
                                    text = currencySymbol,
                                    color = LedgerTheme.textSecondary(),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF1A1A1A),
                                unfocusedContainerColor = Color(0xFF1A1A1A),
                                focusedTextColor = LedgerTheme.textPrimary(),
                                unfocusedTextColor = LedgerTheme.textPrimary(),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .border(
                                    width = AppStyleDesignSystem.Sizes.BORDER_NORMAL,
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(20.dp)
                                ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true
                        )
                        if (validationErrors.containsKey("amount")) {
                            Text(
                                text = validationErrors["amount"]!!,
                                color = LedgerTheme.redAmount,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
                
                item {
                    // Description Field
                    Column {
                        Text(
                            text = "Description (Optional)",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = LedgerTheme.textPrimary(),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        TextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = { Text("Add a note", color = LedgerTheme.textSecondary()) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFF1A1A1A),
                                unfocusedContainerColor = Color(0xFF1A1A1A),
                                focusedTextColor = LedgerTheme.textPrimary(),
                                unfocusedTextColor = LedgerTheme.textPrimary(),
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .border(
                                    width = AppStyleDesignSystem.Sizes.BORDER_NORMAL,
                                    color = Color.White.copy(alpha = 0.2f),
                                    shape = RoundedCornerShape(20.dp)
                                ),
                            singleLine = true
                        )
                    }
                }
                
                item {
                    // Date and Time Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_MEDIUM)
                    ) {
                        // Date Field
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Date",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = LedgerTheme.textPrimary(),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Button(
                                onClick = { showDatePicker = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1A1A1A)
                                ),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = LedgerTheme.textSecondary(),
                                        modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_SMALL)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = selectedDate,
                                        color = LedgerTheme.textPrimary(),
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                        
                        // Time Field
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Time",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = LedgerTheme.textPrimary(),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Button(
                                onClick = { showTimePicker = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF1A1A1A)
                                ),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = LedgerTheme.textSecondary(),
                                        modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_SMALL)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = selectedTime,
                                        color = LedgerTheme.textPrimary(),
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }
                
                item {
                    // Account Selection
                    Column {
                        Text(
                            text = "Account",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = LedgerTheme.textPrimary(),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Button(
                            onClick = { showAccountSelection = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1A1A1A)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Wallet,
                                        contentDescription = null,
                                        tint = LedgerTheme.textSecondary(),
                                        modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_SMALL)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = selectedAccount?.name ?: "Select Account",
                                        color = if (selectedAccount != null) LedgerTheme.textPrimary() else LedgerTheme.textSecondary(),
                                        fontSize = 14.sp
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = LedgerTheme.textSecondary(),
                                    modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_MEDIUM)
                                )
                            }
                        }
                        if (validationErrors.containsKey("account")) {
                            Text(
                                text = validationErrors["account"]!!,
                                color = LedgerTheme.redAmount,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
                
                item {
                    // Update Button
                    Button(
                        onClick = {
                            if (validateForm()) {
                                coroutineScope.launch {
                                    try {
                                        // Get current account balance for balanceAtTime
                                        val currentAccountBalance = selectedAccount?.let { account ->
                                            removeCurrencySymbols(account.balance).toDoubleOrNull() ?: 0.0
                                        } ?: 0.0
                                        
                                        // Update the ledger transaction
                                        val updatedTransaction = transaction.copy(
                                            amount = amount.toDouble(),
                                            description = description,
                                            date = selectedDate,
                                            time = selectedTime,
                                            account = selectedAccount?.name,
                                            balanceAtTime = currentAccountBalance
                                        )
                                        
                                        // Update the ledger transaction
                                        ledgerDatabaseManager.updateLedgerTransaction(updatedTransaction)
                                        
                                        // Recalculate person's balance after updating transaction
                                        // Get current person to start with their current balance
                                        val currentPerson = ledgerDatabaseManager.getLedgerPersonById(person.id)
                                        if (currentPerson != null) {
                                            // Get all transactions and recalculate balance from scratch
                                            val allTransactions = ledgerDatabaseManager.getLedgerTransactionsByPerson(person.id).first()
                                            var newBalance = 0.0
                                            allTransactions.forEach { tx ->
                                                when (tx.type) {
                                                    com.example.androidkmm.screens.ledger.TransactionType.SENT -> newBalance -= tx.amount
                                                    com.example.androidkmm.screens.ledger.TransactionType.RECEIVED -> newBalance += tx.amount
                                                }
                                            }
                                            println("DEBUG: EditLedgerEntryBottomSheet - Recalculated balance: $newBalance")
                                            ledgerDatabaseManager.updateLedgerPersonBalance(person.id, newBalance)
                                        }
                                        
                                        onDismiss()
                                    } catch (e: Exception) {
                                        // Handle error
                                        println("Error updating transaction: ${e.message}")
                                    }
                                }
                            }
                        },
                        enabled = isFormValid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFormValid) Color(0xFF4CAF50) else Color(0xFF6C6C6C),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = "Update Transaction",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    )
    
    // Date Picker Dialog
    if (showDatePicker) {
        Dialog(
            onDismissRequest = { showDatePicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Select Date",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = LedgerTheme.textPrimary(),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Simple date picker implementation
                    val currentDate = selectedDate.split("-")
                    val year = currentDate[0].toInt()
                    val month = currentDate[1].toInt()
                    val day = currentDate[2].toInt()
                    
                    val today = DateTimeUtils.getCurrentDate()
                    val todayMillis = DateTimeUtils.getStartOfDay(today).toEpochMilliseconds()
                    val tomorrowMillis = DateTimeUtils.getStartOfDay(today.plus(DatePeriod(days = 1))).toEpochMilliseconds()
                    
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = DateTimeUtils.createDate(year, month, day)?.let { date ->
                            DateTimeUtils.getStartOfDay(date).toEpochMilliseconds()
                        },
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
                    
                    DatePicker(
                        state = datePickerState,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val date = DateTimeUtils.instantToLocalDate(
                                    Instant.fromEpochMilliseconds(millis)
                                )
                                val today = DateTimeUtils.getCurrentDate()
                                
                                // Check if selected date is in the future
                                if (DateTimeUtils.isDateAfter(date, today)) {
                                    // Don't allow future dates - just close dialog without selecting
                                    showDatePicker = false
                                    return@Button
                                }
                                
                                selectedDate = DateTimeUtils.formatDate(date)
                            }
                            showDatePicker = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Select Date", color = Color.White)
                    }
                }
            }
        }
    }
    
    // Time Picker Dialog
    if (showTimePicker) {
        Dialog(
            onDismissRequest = { showTimePicker = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Select Time",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = LedgerTheme.textPrimary(),
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    // Simple time picker implementation
                    val timeParts = selectedTime.split(":")
                    val hour = timeParts[0].toInt()
                    val minute = timeParts[1].split(" ")[0].toInt()
                    
                    val timePickerState = rememberTimePickerState(
                        initialHour = hour,
                        initialMinute = minute,
                        is24Hour = false
                    )
                    
                    TimePicker(
                        state = timePickerState,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Button(
                        onClick = {
                            val hour12 = if (timePickerState.hour == 0) 12 else if (timePickerState.hour > 12) timePickerState.hour - 12 else timePickerState.hour
                            val amPm = if (timePickerState.hour < 12) "AM" else "PM"
                            selectedTime = "${hour12.toString().padStart(2, '0')}:${timePickerState.minute.toString().padStart(2, '0')} $amPm"
                            showTimePicker = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Select Time", color = Color.White)
                    }
                }
            }
        }
    }
    
    // Account Selection Bottom Sheet
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
            onAddAccount = { /* TODO: Add account functionality for ledger */ }
        )
    }
}
