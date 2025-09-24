package com.example.androidkmm.screens.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
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
import com.example.androidkmm.screens.ledger.components.*
import com.example.androidkmm.screens.ledger.TransactionType
import com.example.androidkmm.screens.ledger.LedgerPerson
import com.example.androidkmm.screens.ledger.LedgerTheme
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun AddLedgerEntryBottomSheetRefactored(
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
    
    // State variables
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
    
    // Validation
    var validationErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    
    fun validateForm(): Map<String, String> {
        val errors = mutableMapOf<String, String>()
        
        if (personName.isBlank()) {
            errors["personName"] = "Person name is required"
        }
        
        if (amount.isBlank()) {
            errors["amount"] = "Amount is required"
        } else {
            val amountValue = amount.toDoubleOrNull()
            if (amountValue == null || amountValue <= 0) {
                errors["amount"] = "Please enter a valid amount"
            }
        }
        
        if (selectedAccount == null) {
            errors["account"] = "Please select an account"
        }
        
        return errors
    }
    
    val isFormValid = personName.isNotBlank() && 
                     amount.isNotBlank() && 
                     amount.toDoubleOrNull() != null && 
                     amount.toDoubleOrNull()!! > 0 && 
                     selectedAccount != null
    
    // Full screen layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LedgerTheme.backgroundColor())
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(AppStyleDesignSystem.Padding.XXS),
            verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_MEDIUM)
        ) {
            item {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(
                            text = "Cancel",
                            color = LedgerTheme.textSecondary()
                        )
                    }
                    
                    Text(
                        text = when {
                            person != null && currentTransactionType == TransactionType.SENT -> "You Sent Money"
                            person != null && currentTransactionType == TransactionType.RECEIVED -> "You Received Money"
                            else -> "Add Ledger Entry"
                        },
                        fontSize = AppStyleDesignSystem.Typography.HEADLINE.fontSize,
                        fontWeight = FontWeight.Medium,
                        color = LedgerTheme.textPrimary()
                    )
                    
                    Button(
                        onClick = {
                            validationErrors = validateForm()
                            if (validationErrors.isEmpty() && isFormValid) {
                                // Handle form submission
                                coroutineScope.launch {
                                    try {
                                        val formattedName = personName.trim().split(" ").joinToString(" ") { word ->
                                            word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                                        }
                                        
                                        val existingPerson = ledgerDatabaseManager.getLedgerPersonByName(formattedName)
                                        val personToUse = existingPerson ?: run {
                                        val newPerson = LedgerPerson(
                                            id = "person_${++personCounter}",
                                            name = formattedName,
                                            balance = 0.0,
                                            avatarColor = Color(0xFFFF6B6B),
                                            transactionCount = 0,
                                            lastTransactionDate = ""
                                        )
                                            ledgerDatabaseManager.insertLedgerPerson(newPerson)
                                            newPerson
                                        }
                                        
                                        val transaction = com.example.androidkmm.models.Transaction(
                                            id = "transaction_${++transactionCounter}",
                                            amount = amount.toDouble(),
                                            description = description,
                                            date = selectedDate,
                                            time = selectedTime,
                                            type = if (currentTransactionType == TransactionType.SENT) com.example.androidkmm.models.TransactionType.EXPENSE else com.example.androidkmm.models.TransactionType.INCOME,
                                            title = "Ledger Transaction",
                                            category = "Ledger",
                                            categoryIcon = Icons.Default.Person,
                                            categoryColor = Color(0xFFFF6B6B),
                                            account = selectedAccount?.name ?: ""
                                        )
                                        
                                        transactionDatabaseManager.addTransaction(
                                            transaction = transaction,
                                            onSuccess = {
                                                onDismiss()
                                            },
                                            onError = { error ->
                                                println("Error adding transaction: ${error.message}")
                                            }
                                        )
                                    } catch (e: Exception) {
                                        println("Error: ${e.message}")
                                    }
                                }
                            }
                        },
                        enabled = isFormValid,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (isFormValid) Color.White else LedgerTheme.textSecondary(),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when {
                                    person != null && currentTransactionType == TransactionType.SENT -> "Record Sent"
                                    person != null && currentTransactionType == TransactionType.RECEIVED -> "Record Received"
                                    else -> "Add Entry"
                                },
                                color = if (isFormValid) Color.White else LedgerTheme.textSecondary(),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL))
            }
            
            item {
                // Transaction Type Selector
                TransactionTypeSelector(
                    currentTransactionType = currentTransactionType,
                    onTransactionTypeChanged = { currentTransactionType = it }
                )
            }
            
            item {
                // Person Input Section
                PersonInputSection(
                    personName = personName,
                    onPersonNameChanged = { personName = it },
                    showSuggestions = showSuggestions,
                    onShowSuggestionsChanged = { showSuggestions = it },
                    suggestions = suggestions,
                    validationErrors = validationErrors
                )
            }
            
            item {
                // Amount Input Section
                AmountInputSection(
                    amount = amount,
                    onAmountChanged = { newAmount -> 
                        amount = newAmount
                        newAmount
                    },
                    validationErrors = validationErrors
                )
            }
            
            item {
                // Description Input Section
                DescriptionInputSection(
                    description = description,
                    onDescriptionChanged = { description = it },
                    validationErrors = validationErrors
                )
            }
            
            item {
                // Account Selection Section
                AccountSelectionSection(
                    selectedAccount = selectedAccount,
                    onAccountSelectionClick = { showAccountSelection = true },
                    validationErrors = validationErrors
                )
            }
            
            item {
                // Date and Time Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_MEDIUM)
                ) {
                    // Date Selection
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = LedgerTheme.textPrimary(),
                                modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_MEDIUM)
                            )
                            Spacer(modifier = Modifier.width(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL))
                            Text(
                                text = selectedDate,
                                fontSize = AppStyleDesignSystem.Typography.CALL_OUT.fontSize,
                                color = LedgerTheme.textPrimary()
                            )
                        }
                    }
                    
                    // Time Selection
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = LedgerTheme.textPrimary(),
                                modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_MEDIUM)
                            )
                            Spacer(modifier = Modifier.width(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL))
                            Text(
                                text = selectedTime,
                                fontSize = AppStyleDesignSystem.Typography.CALL_OUT.fontSize,
                                color = LedgerTheme.textPrimary()
                            )
                        }
                    }
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(AppStyleDesignSystem.Padding.ARRANGEMENT_LARGE))
            }
        }
        
        // Dialogs
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
}
