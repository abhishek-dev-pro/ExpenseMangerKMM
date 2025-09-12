package com.example.androidkmm.screens.ledger

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.example.androidkmm.database.rememberSQLiteAccountDatabase
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.Clock as DateTimeClock
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
    val coroutineScope = rememberCoroutineScope()
    var transactionCounter by remember { mutableStateOf(0) }
    var personCounter by remember { mutableStateOf(0) }
    
    // Get all existing people for suggestions
    val allPeopleState = ledgerDatabaseManager.getAllLedgerPersons().collectAsState(initial = emptyList<LedgerPerson>())
    val allPeople = allPeopleState.value
    
    val bottomSheetState = rememberModalBottomSheetState()
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
    var amount by remember { mutableStateOf("0") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("Sep 10, 2025") }
    var selectedTime by remember { mutableStateOf("01:43 PM") }
    var selectedAccount by remember { mutableStateOf<com.example.androidkmm.models.Account?>(null) }
    var showAccountSelection by remember { mutableStateOf(false) }
    
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
        
        if (description.isBlank()) {
            errors["description"] = "Description is required"
        }
        
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
                     description.isNotBlank() && 
                     selectedAccount != null

    val commonExamples = listOf(
        "Dinner Split" to "Restaurant bill",
        "Ride Share" to "Travel expense",
        "Emergency Loan" to "Urgent help",
        "Groceries" to "Shared shopping"
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        content = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LedgerTheme.backgroundColor)
                    .navigationBarsPadding(),
                contentPadding = PaddingValues(24.dp),
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
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LedgerTheme.textPrimary
                        )

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = LedgerTheme.textPrimary
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
                                    if (currentTransactionType == TransactionType.SENT) Color(0xFF0F2419) else Color(0xFF2A1919),
                                    RoundedCornerShape(16.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Text(
                                text = if (currentTransactionType == TransactionType.SENT) {
                                    "Recording money you sent to ${person.name}"
                                } else {
                                    "Recording money you received from ${person.name}"
                                },
                                fontSize = 14.sp,
                                color = if (currentTransactionType == TransactionType.SENT) LedgerTheme.greenAmount else LedgerTheme.redAmount
                            )
                        }
                    }
                } else {
                    item {
                        // Person's Name
                        Text(
                            text = "Person's Name",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = LedgerTheme.textPrimary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Column {
                            TextField(
                                value = personName,
                                onValueChange = { 
                                    personName = it
                                    showSuggestions = it.isNotBlank() && suggestions.isNotEmpty()
                                },
                                placeholder = {
                                    Text(
                                        text = "Enter name",
                                        color = LedgerTheme.textSecondary
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = LedgerTheme.textSecondary
                                    )
                                },
                                colors = TextFieldDefaults.colors(
                                    unfocusedContainerColor = Color(0xFF1F1F1F),
                                    focusedContainerColor = Color(0xFF1F1F1F),
                                    unfocusedTextColor = LedgerTheme.textPrimary,
                                    focusedTextColor = LedgerTheme.textPrimary,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(20.dp),
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            // Show suggestions dropdown
                            if (showSuggestions && suggestions.isNotEmpty()) {
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFF2A2A2A)
                                    ),
                                    shape = RoundedCornerShape(12.dp)
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
                                                    .padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Person,
                                                    contentDescription = null,
                                                    tint = LedgerTheme.textSecondary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = suggestion.name,
                                                    color = LedgerTheme.textPrimary,
                                                    fontSize = 14.sp
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
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp, start = 16.dp)
                            )
                        }
                    }

                    item {
                        // Transaction Type
                        Text(
                            text = "Transaction Type",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = LedgerTheme.textPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // You Sent Button
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .wrapContentHeight()
                                    .clickable { currentTransactionType = TransactionType.SENT },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (currentTransactionType == TransactionType.SENT) Color(0xFF0F2419) else Color(0xFF1A1A1A)
                                ),
                                border = if (currentTransactionType == TransactionType.SENT) BorderStroke(2.dp, LedgerTheme.greenAmount) else null,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(
                                                if (currentTransactionType == TransactionType.SENT) LedgerTheme.greenAmount else Color(0xFF404040),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowUpward,
                                            contentDescription = null,
                                            tint = if (currentTransactionType == TransactionType.SENT) Color.White else LedgerTheme.textSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "You Sent",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (currentTransactionType == TransactionType.SENT) LedgerTheme.greenAmount else LedgerTheme.textSecondary
                                    )
                                    Text(
                                        text = "Money you sent",
                                        fontSize = 10.sp,
                                        color = LedgerTheme.textSecondary
                                    )
                                }
                            }

                            // You Received Button
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .wrapContentHeight()
                                    .clickable { currentTransactionType = TransactionType.RECEIVED },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (currentTransactionType == TransactionType.RECEIVED) Color(0xFF2A1919) else Color(0xFF1A1A1A)
                                ),
                                border = if (currentTransactionType == TransactionType.RECEIVED) BorderStroke(2.dp, LedgerTheme.redAmount) else null,
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(
                                                if (currentTransactionType == TransactionType.RECEIVED) LedgerTheme.redAmount else Color(0xFF404040),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowDownward,
                                            contentDescription = null,
                                            tint = if (currentTransactionType == TransactionType.RECEIVED) Color.White else LedgerTheme.textSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "You Received",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (currentTransactionType == TransactionType.RECEIVED) LedgerTheme.redAmount else LedgerTheme.textSecondary
                                    )
                                    Text(
                                        text = "Money you received",
                                        fontSize = 10.sp,
                                        color = LedgerTheme.textSecondary
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
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = LedgerTheme.textPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column {
                        TextField(
                            value = amount,
                            onValueChange = { amount = it },
                            placeholder = {
                                Text(
                                    text = "0",
                                    color = LedgerTheme.textSecondary
                                )
                            },
                            prefix = {
                                Text(
                                    text = "$",
                                    color = LedgerTheme.textPrimary,
                                    fontSize = 18.sp
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0xFF1F1F1F),
                                focusedContainerColor = Color(0xFF1F1F1F),
                                unfocusedTextColor = LedgerTheme.textPrimary,
                                focusedTextColor = LedgerTheme.textPrimary,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        
                        // Show error message
                        validationErrors["amount"]?.let { error ->
                            Text(
                                text = error,
                                color = LedgerTheme.redAmount,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp, start = 16.dp)
                            )
                        }
                    }
                }

                item {
                    // Description
                    Text(
                        text = "Description (Optional)",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = LedgerTheme.textPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column {
                        TextField(
                            value = description,
                            onValueChange = { description = it },
                            placeholder = {
                                Text(
                                    text = "e.g., Dinner split, Uber ride share (optional)",
                                    color = LedgerTheme.textSecondary
                                )
                            },
                            colors = TextFieldDefaults.colors(
                                unfocusedContainerColor = Color(0xFF1F1F1F),
                                focusedContainerColor = Color(0xFF1F1F1F),
                                unfocusedTextColor = LedgerTheme.textPrimary,
                                focusedTextColor = LedgerTheme.textPrimary,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent
                            ),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp),
                            maxLines = 3
                        )
                        
                        // Show error message
                        validationErrors["description"]?.let { error ->
                            Text(
                                text = error,
                                color = LedgerTheme.redAmount,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp, start = 16.dp)
                            )
                        }
                    }
                }

                item {
                    // Date & Time
                    Text(
                        text = "Date & Time",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = LedgerTheme.textPrimary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Date Picker
                        Button(
                            onClick = { },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1F1F1F)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = LedgerTheme.textPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedDate,
                                color = LedgerTheme.textPrimary,
                                fontSize = 14.sp
                            )
                        }

                        // Time Picker
                        Button(
                            onClick = { },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1F1F1F)
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = LedgerTheme.textPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedTime,
                                color = LedgerTheme.textPrimary,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                                tint = LedgerTheme.textSecondary,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                item {
                    // Account
                    Text(
                        text = "Account",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = LedgerTheme.textPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column {
                        Button(
                            onClick = { showAccountSelection = true },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1F1F1F)
                            ),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wallet,
                                    contentDescription = null,
                                    tint = LedgerTheme.textPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(LedgerTheme.greenAmount, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = selectedAccount?.name ?: "Select account (optional)",
                                    color = LedgerTheme.textPrimary,
                                    fontSize = 14.sp,
                                    modifier = Modifier.weight(1f),
                                    textAlign = TextAlign.Start
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = LedgerTheme.textSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        
                        // Show error message
                        validationErrors["account"]?.let { error ->
                            Text(
                                text = error,
                                color = LedgerTheme.redAmount,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(top = 4.dp, start = 16.dp)
                            )
                        }
                    }

                    Text(
                        text = "Account from which you sent the money",
                        fontSize = 12.sp,
                        color = LedgerTheme.textSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                if (person == null) {
                    item {
                        // Common Examples
                        Text(
                            text = "Common Examples",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = LedgerTheme.textPrimary
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.height(160.dp)
                        ) {
                            items(commonExamples) { (title, subtitle) ->
                                Button(
                                    onClick = {
                                        description = title
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Color(0xFF1F1F1F)
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = PaddingValues(12.dp)
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = title,
                                            color = LedgerTheme.textPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            textAlign = TextAlign.Center
                                        )
                                        Text(
                                            text = subtitle,
                                            color = LedgerTheme.textSecondary,
                                            fontSize = 10.sp,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                    }
                }

                item {
                    // Add Entry Button
                    Button(
                        onClick = { 
                            if (validateForm()) {
                                coroutineScope.launch {
                                    try {
                                    if (person != null) {
                                        // Add transaction to existing person
                                        val transaction = LedgerTransaction(
                                            id = "transaction_${System.currentTimeMillis()}_${++transactionCounter}",
                                            personId = person.id,
                                            amount = amount.toDoubleOrNull() ?: 0.0,
                                            description = description,
                                            date = selectedDate,
                                            time = "12:00",
                                            type = currentTransactionType,
                                            account = selectedAccount?.name
                                        )
                                        
                                        ledgerDatabaseManager.addLedgerTransactionAndUpdatePerson(transaction, person.id)
                                    } else {
                                        // Create new person and add transaction
                                        val newPerson = LedgerPerson(
                                            id = "person_${System.currentTimeMillis()}_${++personCounter}",
                                            name = personName,
                                            avatarColor = LedgerTheme.avatarBlue,
                                            balance = 0.0,
                                            transactionCount = 0,
                                            lastTransactionDate = ""
                                        )
                                        
                                        ledgerDatabaseManager.insertLedgerPerson(newPerson)
                                        
                                        val transaction = LedgerTransaction(
                                            id = "transaction_${System.currentTimeMillis()}_${++transactionCounter}",
                                            personId = newPerson.id,
                                            amount = amount.toDoubleOrNull() ?: 0.0,
                                            description = description,
                                            date = selectedDate,
                                            time = "12:00",
                                            type = currentTransactionType,
                                            account = selectedAccount?.name
                                        )
                                        
                                        ledgerDatabaseManager.addLedgerTransactionAndUpdatePerson(transaction, newPerson.id)
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
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = if (isFormValid) Color.White else LedgerTheme.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when {
                                person != null && currentTransactionType == TransactionType.SENT -> "Record Sent"
                                person != null && currentTransactionType == TransactionType.RECEIVED -> "Record Received"
                                else -> "Add Entry"
                            },
                            color = if (isFormValid) Color.White else LedgerTheme.textSecondary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    )
    
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
}
