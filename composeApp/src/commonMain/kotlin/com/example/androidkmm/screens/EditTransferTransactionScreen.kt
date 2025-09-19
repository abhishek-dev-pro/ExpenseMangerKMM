@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.androidkmm.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.database.SQLiteAccountDatabase
import com.example.androidkmm.models.Transaction

@Composable
fun EditTransferTransactionScreen(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit,
    accountDatabaseManager: SQLiteAccountDatabase
) {
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var title by remember { mutableStateOf(transaction.title) }
    var selectedDate by remember { mutableStateOf(transaction.date) }
    var selectedTime by remember { mutableStateOf(transaction.time) }
    var selectedFromAccount by remember { mutableStateOf(transaction.account) }
    var selectedToAccount by remember { mutableStateOf(transaction.transferTo ?: "") }
    
    var showFromAccountSheet by remember { mutableStateOf(false) }
    var showToAccountSheet by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    val accounts by accountDatabaseManager.getAllAccounts().collectAsState(initial = emptyList())
    
    // Date and Time picker states
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )
    val timePickerState = rememberTimePickerState(
        initialHour = 12,
        initialMinute = 0
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black) // Complete black background
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Edit Transaction",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
            
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        // Amount Input - Single editable field without $ sign
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Single editable input field
            BasicTextField(
                value = amount,
                onValueChange = { newValue: String ->
                    val filteredValue = newValue.filter { char -> char.isDigit() || char == '.' }
                    val decimalCount = filteredValue.count { char -> char == '.' }
                    if (decimalCount <= 1) {
                        amount = filteredValue
                    }
                },
                textStyle = TextStyle(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Transparent)
                    .padding(vertical = 8.dp)
            )
            Text(
                text = "Enter amount",
                color = Color(0xFF757575), // Gray text like in your design
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        // From Account
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "From Account",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showFromAccountSheet = true }
                    .background(Color.Black) // Black background
                    .border(
                        width = 1.dp,
                        color = Color(0xFF404040), // Subtle gray border
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = "From Account",
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFF4CAF50) // Green color like in your design
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = selectedFromAccount,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // To Account
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "To Account",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { showToAccountSheet = true }
                    .background(Color.Black) // Black background
                    .border(
                        width = 1.dp,
                        color = Color(0xFF404040), // Subtle gray border
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = "To Account",
                    modifier = Modifier.size(24.dp),
                    tint = Color(0xFF757575) // Gray color like in your design
                )
                Spacer(modifier = Modifier.width(12.dp))
                if (selectedToAccount.isNotEmpty()) {
                    Text(
                        text = selectedToAccount,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Text(
                        text = "Select",
                        color = Color(0xFF757575),
                        fontSize = 16.sp
                    )
                }
            }
        }
        
        // Date & Time
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Date & Time",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date - White background like in your design
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showDatePicker = true }
                            .background(Color.Black) // Black background
                            .border(
                                width = 1.dp,
                                color = Color(0xFF404040), // Subtle gray border
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Date",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF757575)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedDate,
                            color = Color(0xFF757575), // Light gray text like in your design
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                
                // Time - Dark background like in your design
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { showTimePicker = true }
                            .background(Color.Black) // Black background
                            .border(
                                width = 1.dp,
                                color = Color(0xFF404040), // Subtle gray border
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Time",
                            modifier = Modifier.size(16.dp),
                            tint = Color(0xFF757575)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedTime,
                            color = Color(0xFF757575),
                            fontSize = 14.sp,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        
        // Title
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Title",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Monthly salary") },
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Black, // Black background
                    unfocusedContainerColor = Color.Black,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }
        
        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cancel Button - Black background with white border
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onDismiss() }
                    .background(Color.Black) // Black background
                    .border(
                        width = 1.dp,
                        color = Color(0xFF404040), // Subtle gray border
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Cancel",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            
            // Save Changes Button - Black background with white border
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        val updatedTransaction = transaction.copy(
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            account = selectedFromAccount,
                            transferTo = selectedToAccount,
                            title = title,
                            date = selectedDate,
                            time = selectedTime
                        )
                        onSave(updatedTransaction)
                    }
                    .background(Color.Black) // Black background
                    .border(
                        width = 1.dp,
                        color = Color(0xFF404040), // Subtle gray border
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save",
                        modifier = Modifier.size(16.dp),
                        tint = Color.White // White icon
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Save Changes",
                        color = Color.White, // White text
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
    
    // From Account Selection Sheet
    if (showFromAccountSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFromAccountSheet = false },
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Black // Complete black background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Account",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showFromAccountSheet = false }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                // Subtitle
                Text(
                    text = "Choose an account for your transaction",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                
                // Account List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(accounts.size) { index ->
                        val account = accounts[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedFromAccount = account.name
                                    showFromAccountSheet = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Black // Black card background
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF404040))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AccountBalance,
                                    contentDescription = "Account",
                                    modifier = Modifier.size(24.dp),
                                    tint = Color(0xFF4CAF50) // Green color
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = account.name,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.TrendingUp,
                                            contentDescription = "Balance",
                                            modifier = Modifier.size(16.dp),
                                            tint = Color(0xFF4CAF50) // Green color
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${account.balance}",
                                            color = Color(0xFF4CAF50), // Green color
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // To Account Selection Sheet
    if (showToAccountSheet) {
        ModalBottomSheet(
            onDismissRequest = { showToAccountSheet = false },
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Black // Complete black background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Select Account",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = { showToAccountSheet = false }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                
                // Subtitle
                Text(
                    text = "Choose an account for your transaction",
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 20.dp)
                )
                
                // Account List
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(accounts.size) { index ->
                        val account = accounts[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedToAccount = account.name
                                    showToAccountSheet = false
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Black // Black card background
                            ),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF404040))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CreditCard,
                                    contentDescription = "Account",
                                    modifier = Modifier.size(24.dp),
                                    tint = Color(0xFF757575) // Gray color
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = account.name,
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.TrendingUp,
                                            contentDescription = "Balance",
                                            modifier = Modifier.size(16.dp),
                                            tint = Color(0xFF4CAF50) // Green color
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${account.balance}",
                                            color = Color(0xFF4CAF50), // Green color
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Date Picker
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                                .toString()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // Time Picker Dialog
    if (showTimePicker) {
        Dialog(onDismissRequest = { showTimePicker = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Select Time",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    
                    TimePicker(
                        state = timePickerState,
                        colors = TimePickerDefaults.colors(
                            clockDialColor = Color(0xFF2A2A2A),
                            clockDialSelectedContentColor = Color.White,
                            clockDialUnselectedContentColor = Color(0xFF757575),
                            selectorColor = Color(0xFF4CAF50),
                            containerColor = Color.Black
                        )
                    )
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { showTimePicker = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2A2A2A)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cancel", color = Color.White)
                        }
                        
                        Button(
                            onClick = {
                                selectedTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                                showTimePicker = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("OK", color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
