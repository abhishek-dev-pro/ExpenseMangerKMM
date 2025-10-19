package com.example.androidkmm.screens.goals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.example.androidkmm.database.SQLiteAccountDatabase
import com.example.androidkmm.database.rememberSQLiteAccountDatabase
import com.example.androidkmm.database.SQLiteGoalsDatabase
import com.example.androidkmm.database.rememberSQLiteGoalsDatabase
import com.example.androidkmm.design.AppStyleDesignSystem
import com.example.androidkmm.models.Account
import com.example.androidkmm.utils.DateFormatUtils
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import com.example.androidkmm.screens.goals.SimpleDatePicker

@Composable
fun AddMoneyFloatingBar(
    goalId: Long,
    goalTitle: String,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit = {}
) {
    val accountDatabaseManager = rememberSQLiteAccountDatabase()
    val goalsDatabaseManager = rememberSQLiteGoalsDatabase()
    val scope = rememberCoroutineScope()
    
    var amount by remember { mutableStateOf("") }
    var selectedAccount by remember { mutableStateOf<Account?>(null) }
    var selectedDate by remember { mutableStateOf(LocalDate(2024, 1, 1)) }
    var showAccountSelection by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var accounts by remember { mutableStateOf<List<Account>>(emptyList()) }
    
    // Load accounts
    LaunchedEffect(Unit) {
        accountDatabaseManager.getAllAccounts().collect { accountList ->
            accounts = accountList
        }
    }
    
    // Date picker popup
    if (showDatePicker) {
        Popup(
            onDismissRequest = { showDatePicker = false },
            properties = PopupProperties(focusable = true)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showDatePicker = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .wrapContentHeight(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    SimpleDatePicker(
                        selectedDate = selectedDate,
                        onDateSelected = { selectedDateValue ->
                            selectedDate = selectedDateValue
                            showDatePicker = false
                        },
                        onDismiss = { showDatePicker = false }
                    )
                }
            }
        }
    }
    
    // Account selection popup
    if (showAccountSelection) {
        Popup(
            onDismissRequest = { showAccountSelection = false },
            properties = PopupProperties(focusable = true)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showAccountSelection = false },
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .wrapContentHeight(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Select Account",
                            style = AppStyleDesignSystem.Typography.HEADLINE,
                            color = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(accounts.size) { index ->
                                val account = accounts[index]
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (selectedAccount?.id == account.id) Color(0xFF6A4C93) else Color(0xFF2A2A2A),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { 
                                            selectedAccount = account
                                            showAccountSelection = false
                                        }
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = account.name,
                                        style = AppStyleDesignSystem.Typography.BODY,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Floating bar popup
    Popup(
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.3f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .wrapContentHeight(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Add Money to Goal",
                            style = AppStyleDesignSystem.Typography.HEADLINE,
                            color = Color.White
                        )
                        
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    Color(0xFF2A2A2A),
                                    CircleShape
                                )
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = goalTitle,
                        style = AppStyleDesignSystem.Typography.CALL_OUT,
                        color = Color.Gray
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Form fields in vertical columns
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Amount Input
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Amount",
                                style = AppStyleDesignSystem.Typography.BODY,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            BasicTextField(
                                value = amount,
                                onValueChange = { newValue ->
                                    val filtered = newValue.filter { it.isDigit() || it == '.' }
                                    val parts = filtered.split('.')
                                    if (parts.size <= 2) {
                                        val decimalPart = if (parts.size == 2) parts[1].take(2) else ""
                                        amount = if (parts.size == 2) "${parts[0]}.$decimalPart" else parts[0]
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Color(0xFF2A2A2A),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        1.dp,
                                        Color(0xFF3A3A3A),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .padding(16.dp),
                                textStyle = AppStyleDesignSystem.Typography.BODY.copy(
                                    color = Color.White,
                                    textAlign = TextAlign.Start
                                ),
                                cursorBrush = Brush.linearGradient(colors = listOf(Color.White, Color.White)),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                decorationBox = { innerTextField ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "₹",
                                            style = AppStyleDesignSystem.Typography.BODY,
                                            color = Color.Gray
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(modifier = Modifier.weight(1f)) {
                                            if (amount.isEmpty()) {
                                                Text(
                                                    text = "Enter amount",
                                                    style = AppStyleDesignSystem.Typography.BODY,
                                                    color = Color.Gray
                                                )
                                            }
                                            innerTextField()
                                        }
                                    }
                                }
                            )
                        }
                        
                        // Account Selection
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Account",
                                style = AppStyleDesignSystem.Typography.BODY,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Color(0xFF2A2A2A),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        1.dp,
                                        Color(0xFF3A3A3A),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { showAccountSelection = true }
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedAccount?.name ?: "Select Account",
                                        style = AppStyleDesignSystem.Typography.BODY,
                                        color = if (selectedAccount != null) Color.White else Color.Gray
                                    )
                                    Text(
                                        text = "▼",
                                        style = AppStyleDesignSystem.Typography.BODY,
                                        color = Color.Gray
                                    )
                                }
                            }
                        }
                        
                        // Date Selection
                        Column(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Date & Time",
                                style = AppStyleDesignSystem.Typography.BODY,
                                color = Color.White,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Color(0xFF2A2A2A),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .border(
                                        1.dp,
                                        Color(0xFF3A3A3A),
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { showDatePicker = true }
                                    .padding(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = DateFormatUtils.formatDate(selectedDate, "dd MMM yyyy"),
                                        style = AppStyleDesignSystem.Typography.BODY,
                                        color = Color.White
                                    )
                                    Icon(
                                        Icons.Default.DateRange,
                                        contentDescription = "Select Date",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Action buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Cancel Button
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2A2A2A)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Cancel",
                                style = AppStyleDesignSystem.Typography.BODY,
                                color = Color.White
                            )
                        }
                        
                        // Save Button
                        Button(
                            onClick = {
                                if (amount.isNotEmpty() && selectedAccount != null) {
                                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                                    scope.launch {
                                        goalsDatabaseManager.updateGoalProgress(
                                            goalId = goalId,
                                            currentAmount = amountValue,
                                            onSuccess = {
                                                onSuccess()
                                                onDismiss()
                                            }
                                        )
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF6A4C93)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            enabled = amount.isNotEmpty() && selectedAccount != null
                        ) {
                            Text(
                                text = "Save",
                                style = AppStyleDesignSystem.Typography.BODY,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
