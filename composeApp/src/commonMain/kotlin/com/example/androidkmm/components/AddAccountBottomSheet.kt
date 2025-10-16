@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.androidkmm.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.coroutines.delay
import com.example.androidkmm.utils.TimeUtils
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.windowInsetsPadding
import com.example.androidkmm.design.AppStyleDesignSystem
import com.example.androidkmm.models.Account

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountBottomSheet(
    onDismiss: () -> Unit,
    onAccountAdded: (Account) -> Unit,
    accountDatabaseManager: com.example.androidkmm.database.SQLiteAccountDatabase
) {
    var accountName by remember { mutableStateOf("") }
    var selectedAccountType by remember { mutableStateOf("Bank Account") }
    var initialBalance by remember { mutableStateOf("") }

    // Get all existing accounts for duplicate validation
    val allAccounts = accountDatabaseManager.getActiveAccounts().collectAsState(initial = emptyList<Account>()).value
    
    // Check for duplicate account (same name and type)
    val isDuplicateAccount = allAccounts.any { existingAccount ->
        existingAccount.name.equals(accountName, ignoreCase = true) && 
        existingAccount.type == selectedAccountType
    }
    
    // Validation logic - button should be enabled when account name is filled and not duplicate
    val isFormValid = accountName.isNotEmpty() && !isDuplicateAccount
    
    // Keyboard controller for better keyboard handling
    val keyboardController = LocalSoftwareKeyboardController.current
    
    // Name input focus state
    val nameInteractionSource = remember { MutableInteractionSource() }
    val isNameFocused = nameInteractionSource.collectIsFocusedAsState()
    
    // Amount input focus state
    val amountInteractionSource = remember { MutableInteractionSource() }
    val isAmountFocused = amountInteractionSource.collectIsFocusedAsState()
    
    // Handle keyboard visibility for both name and amount inputs
    LaunchedEffect(isNameFocused.value, isAmountFocused.value) {
        if (isNameFocused.value || isAmountFocused.value) {
            // Small delay to ensure keyboard is shown
            kotlinx.coroutines.delay(100)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .imePadding()
            .windowInsetsPadding(WindowInsets.ime)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
        // Subtle top border line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(0.5.dp)
                )
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Account Name Section
        Text(
            text = "Account Name",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Account Name Input with focus handling
        BasicTextField(
            value = accountName,
            onValueChange = { newValue ->
                // Limit to 24 characters only, don't capitalize while typing
                accountName = newValue.take(24)
            },
            textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
            cursorBrush = SolidColor(Color.White),
            interactionSource = nameInteractionSource,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isNameFocused.value) Color.Gray.copy(alpha = 0.3f) else Color.Black,
                    RoundedCornerShape(12.dp)
                )
                .border(0.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(AppStyleDesignSystem.Padding.CARD_PADDING),
            decorationBox = { innerTextField ->
                if (accountName.isEmpty() && !isNameFocused.value) {
                    Text(
                        text = "e.g. HDFC Savings, Cash Wallet",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                }
                innerTextField()
            }
        )

        // Show duplicate account warning
        if (isDuplicateAccount && accountName.isNotEmpty()) {
            Text(
                text = "An account with this name and type already exists",
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Account Type Section
        Text(
            text = "Account Type",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_MEDIUM),
            verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_MEDIUM)
        ) {
            item {
                SharedAccountTypeCard(
                    title = "Bank Account",
                    icon = Icons.Default.AccountBalance,
                    iconColor = Color(0xFF4285F4),
                    isSelected = selectedAccountType == "Bank Account",
                    onClick = { selectedAccountType = "Bank Account" }
                )
            }
            item {
                SharedAccountTypeCard(
                    title = "Credit/Debit Card",
                    icon = Icons.Default.CreditCard,
                    iconColor = Color(0xFF34A853),
                    isSelected = selectedAccountType == "Credit/Debit Card",
                    onClick = { selectedAccountType = "Credit/Debit Card" }
                )
            }
            item {
                SharedAccountTypeCard(
                    title = "Cash",
                    icon = Icons.Default.Money,
                    iconColor = Color(0xFFFF6D01),
                    isSelected = selectedAccountType == "Cash",
                    onClick = { selectedAccountType = "Cash" }
                )
            }
            item {
                SharedAccountTypeCard(
                    title = "Digital Wallet",
                    icon = Icons.Default.Wallet,
                    iconColor = Color(0xFF9C27B0),
                    isSelected = selectedAccountType == "Digital Wallet",
                    onClick = { selectedAccountType = "Digital Wallet" }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Initial Balance Section
        Text(
            text = "Initial Balance",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Amount Input with focus handling
        
        BasicTextField(
            value = initialBalance,
            onValueChange = { newValue ->
                // Limit to 10 digits (including decimal point)
                val filteredValue = newValue.filter { it.isDigit() || it == '.' }
                if (filteredValue.length <= 10) {
                    initialBalance = filteredValue
                }
            },
            textStyle = TextStyle(color = Color.White, fontSize = 16.sp),
            cursorBrush = SolidColor(Color.White),
            interactionSource = amountInteractionSource,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isAmountFocused.value) Color.Gray.copy(alpha = 0.3f) else Color.Black,
                    RoundedCornerShape(12.dp)
                )
                .border(0.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(AppStyleDesignSystem.Padding.CARD_PADDING),
            decorationBox = { innerTextField ->
                if (initialBalance.isEmpty() && !isAmountFocused.value) {
                    Text(
                        text = "Enter amount",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 16.sp
                    )
                }
                innerTextField()
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Add Account Button
        Button(
            onClick = {
                println("DEBUG: AddAccountBottomSheet - Button clicked")
                println("DEBUG: Account name: '$accountName', Type: '$selectedAccountType', Balance: '$initialBalance'")
                
                // Capitalize first letter of every word when saving
                val capitalizedAccountName = if (accountName.isNotEmpty()) {
                    accountName.split(" ").joinToString(" ") { word ->
                        if (word.isNotEmpty()) {
                            word.replaceFirstChar { it.uppercase() }
                        } else {
                            word
                        }
                    }
                } else {
                    accountName
                }
                
                val account = Account(
                    id = "${TimeUtils.currentTimeMillis()}_${kotlin.random.Random.nextInt(10000)}",
                    name = capitalizedAccountName,
                    balance = if (initialBalance.isEmpty()) "0.00" else initialBalance,
                    icon = when (selectedAccountType) {
                        "Bank Account" -> Icons.Default.AccountBalance
                        "Credit/Debit Card" -> Icons.Default.CreditCard
                        "Cash" -> Icons.Default.Money
                        "Digital Wallet" -> Icons.Default.Wallet
                        else -> Icons.Default.AccountBalance
                    },
                    color = getAccountTypeColor(selectedAccountType),
                    type = selectedAccountType
                )
                println("DEBUG: Created account object: ${account.name}, ID: ${account.id}")
                println("DEBUG: Account type: ${account.type}, Balance: ${account.balance}")
                println("DEBUG: Calling onAccountAdded callback")
                onAccountAdded(account)
            },
            enabled = isFormValid,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFormValid) Color(0xFF2196F3) else Color(0xFF6C6C6C)
            )
        ) {
            Text(
                text = "Add Account",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
        }
    }
}

@Composable
fun SharedAccountTypeCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) iconColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) iconColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }
    }
}

// Helper functions for account type colors
private fun getAccountTypeColor(type: String): Color {
    return when (type) {
        "Bank Account" -> Color(0xFF2196F3) // Blue
        "Credit/Debit Card" -> Color(0xFF4CAF50) // Green
        "Cash" -> Color(0xFFFF9800) // Orange
        "Digital Wallet" -> Color(0xFF9C27B0) // Purple
        else -> Color(0xFF2196F3)
    }
}
