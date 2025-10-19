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
import com.example.androidkmm.database.SQLiteAccountDatabase
import com.example.androidkmm.database.rememberSQLiteAccountDatabase
import com.example.androidkmm.database.SQLiteGoalsDatabase
import com.example.androidkmm.database.rememberSQLiteGoalsDatabase
import com.example.androidkmm.design.AppStyleDesignSystem
import com.example.androidkmm.models.Account
import com.example.androidkmm.utils.DateFormatUtils
import com.example.androidkmm.screens.goals.SimpleDatePicker
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class, kotlin.time.ExperimentalTime::class)
@Composable
fun AddMoneyToGoalBottomSheet(
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
    
    // Date picker
    if (showDatePicker) {
        SimpleDatePicker(
            selectedDate = selectedDate,
            onDateSelected = { selectedDateValue ->
                selectedDate = selectedDateValue
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
    
    // Account selection
    if (showAccountSelection) {
        AccountSelectionBottomSheet(
            accounts = accounts,
            selectedAccount = selectedAccount,
            onAccountSelected = { account ->
                selectedAccount = account
                showAccountSelection = false
            },
            onDismiss = { showAccountSelection = false }
        )
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
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
            
            // Amount Input
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
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Account Selection
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
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Date Selection
            Text(
                text = "Date",
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
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Buttons
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
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountSelectionBottomSheet(
    accounts: List<Account>,
    selectedAccount: Account?,
    onAccountSelected: (Account) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1A1A1A),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
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
                            .clickable { onAccountSelected(account) }
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
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
