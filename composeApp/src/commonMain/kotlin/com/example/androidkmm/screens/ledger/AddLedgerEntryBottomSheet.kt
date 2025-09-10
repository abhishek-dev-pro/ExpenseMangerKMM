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

// AddLedgerEntryBottomSheet.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLedgerEntryBottomSheet(
    onDismiss: () -> Unit,
    person: LedgerPerson? = null,
    transactionType: TransactionType = TransactionType.SENT
) {
    val bottomSheetState = rememberModalBottomSheetState()
    var personName by remember { mutableStateOf(person?.name ?: "") }
    var currentTransactionType by remember { mutableStateOf(transactionType) }
    var amount by remember { mutableStateOf("0") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf("Sep 10, 2025") }
    var selectedTime by remember { mutableStateOf("01:43 PM") }
    var selectedAccount by remember { mutableStateOf<Account?>(null) }
    var showAccountSelection by remember { mutableStateOf(false) }

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

                        TextField(
                            value = personName,
                            onValueChange = { personName = it },
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
                        onClick = { },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when {
                                person != null && currentTransactionType == TransactionType.SENT -> LedgerTheme.greenAmount
                                person != null && currentTransactionType == TransactionType.RECEIVED -> LedgerTheme.redAmount
                                else -> Color(0xFF404040)
                            }
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = if (person != null) Color.White else LedgerTheme.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when {
                                person != null && currentTransactionType == TransactionType.SENT -> "Record Sent"
                                person != null && currentTransactionType == TransactionType.RECEIVED -> "Record Received"
                                else -> "Add Entry"
                            },
                            color = if (person != null) Color.White else LedgerTheme.textSecondary,
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
