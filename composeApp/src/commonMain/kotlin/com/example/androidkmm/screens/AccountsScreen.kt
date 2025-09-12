@file:OptIn(ExperimentalMaterial3Api::class)

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.database.rememberSQLiteAccountDatabase
import com.example.androidkmm.models.Account

// Color definitions for AccountsScreen
internal val AccountsDarkBackground = Color(0xFF1A1A1A)
internal val AccountsDarkSurface = Color(0xFF2A2A2A)
internal val AccountsDarkSurfaceVariant = Color(0xFF3A3A3A)
internal val AccountsWhiteText = Color(0xFFFFFFFF)
internal val AccountsGrayText = Color(0xFFB0B0B0)
internal val AccountsGreenSuccess = Color(0xFF4CAF50)
internal val AccountsRedError = Color(0xFFF44336)
internal val AccountsBluePrimary = Color(0xFF2196F3)

@Composable
fun AccountsScreen(
    onBackClick: () -> Unit
) {
    var showAddAccountSheet by remember { mutableStateOf(false) }
    var showEditAccountSheet by remember { mutableStateOf(false) }
    var selectedAccount by remember { mutableStateOf<Account?>(null) }
    
    // Database manager
    val accountDatabaseManager = rememberSQLiteAccountDatabase()
    val scope = rememberCoroutineScope()
    
    // Flow for accounts from database
    val accountsState = accountDatabaseManager.getAllAccounts().collectAsState(initial = emptyList<Account>())
    val accounts = accountsState.value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AccountsDarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = AccountsWhiteText,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    Column {
                        Text(
                            text = "Accounts",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccountsWhiteText
                        )
                        Text(
                            text = "Manage your financial accounts",
                            fontSize = 14.sp,
                            color = AccountsGrayText
                        )
                    }
                }

                IconButton(
                    onClick = { showAddAccountSheet = true },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            AccountsBluePrimary,
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Account",
                        tint = AccountsWhiteText,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Accounts List
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (accounts.isEmpty()) {
                    item {
                        EmptyAccountsState(
                            onAddAccount = { showAddAccountSheet = true }
                        )
                    }
                } else {
                    items(accounts) { account ->
                        AccountCard(
                            account = account,
                            onEditClick = {
                                selectedAccount = account
                                showEditAccountSheet = true
                            },
                            onDeleteClick = {
                                scope.launch {
                                    accountDatabaseManager.deleteAccount(account)
                                }
                            }
                        )
                    }
                }
            }
        }

        // Add Account Bottom Sheet
        if (showAddAccountSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddAccountSheet = false },
                containerColor = AccountsDarkSurface,
                dragHandle = null
            ) {
                AddAccountBottomSheet(
                    onDismiss = { showAddAccountSheet = false },
                    onAccountAdded = { account ->
                        scope.launch {
                            accountDatabaseManager.addAccount(account)
                        }
                        showAddAccountSheet = false
                    },
                    accountDatabaseManager = accountDatabaseManager
                )
            }
        }

        // Edit Account Bottom Sheet
        if (showEditAccountSheet && selectedAccount != null) {
            ModalBottomSheet(
                onDismissRequest = { 
                    showEditAccountSheet = false
                    selectedAccount = null
                },
                containerColor = AccountsDarkSurface,
                dragHandle = null
            ) {
                EditAccountBottomSheet(
                    account = selectedAccount!!,
                    onDismiss = { 
                        showEditAccountSheet = false
                        selectedAccount = null
                    },
                    onAccountUpdated = { updatedAccount ->
                        scope.launch {
                            accountDatabaseManager.updateAccount(updatedAccount)
                        }
                        showEditAccountSheet = false
                        selectedAccount = null
                    },
                    accountDatabaseManager = accountDatabaseManager
                )
            }
        }
    }
}

@Composable
private fun AccountCard(
    account: Account,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() },
        colors = CardDefaults.cardColors(containerColor = AccountsDarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Account Icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(getAccountTypeColor(account.type)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getAccountTypeIcon(account.type),
                        contentDescription = account.type,
                        tint = AccountsWhiteText,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Account Details
                Column {
                    Text(
                        text = account.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AccountsWhiteText
                    )
                    Text(
                        text = account.type,
                        fontSize = 14.sp,
                        color = AccountsGrayText
                    )
                }
            }

            // Balance and Actions
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "$${String.format("%.2f", account.balance.toDoubleOrNull() ?: 0.0)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AccountsWhiteText
                    )
                    Text(
                        text = "Balance",
                        fontSize = 12.sp,
                        color = AccountsGrayText
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = AccountsRedError,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyAccountsState(
    onAddAccount: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        colors = CardDefaults.cardColors(containerColor = AccountsDarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalance,
                contentDescription = "No Accounts",
                tint = AccountsGrayText,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "No Accounts Yet",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = AccountsWhiteText
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Add your first account to start tracking your finances",
                fontSize = 14.sp,
                color = AccountsGrayText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAddAccount,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccountsBluePrimary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add Account",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAccountBottomSheet(
    onDismiss: () -> Unit,
    onAccountAdded: (Account) -> Unit,
    accountDatabaseManager: com.example.androidkmm.database.SQLiteAccountDatabase
) {
    var accountName by remember { mutableStateOf("") }
    var selectedAccountType by remember { mutableStateOf("Bank Account") }
    var selectedBank by remember { mutableStateOf("HDFC Bank") }
    var initialBalance by remember { mutableStateOf("0.00") }

    val bankOptions = listOf(
        "HDFC Bank", "State Bank of India (SBI)",
        "ICICI Bank", "Axis Bank",
        "Bank of Baroda", "Punjab National Bank"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        // Account Name Section
        Text(
            text = "Account Name",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = AccountsWhiteText
        )

        Spacer(modifier = Modifier.height(8.dp))

        BasicTextField(
            value = accountName,
            onValueChange = { accountName = it },
            textStyle = TextStyle(color = AccountsWhiteText, fontSize = 16.sp),
            modifier = Modifier
                .fillMaxWidth()
                .background(AccountsDarkSurfaceVariant, RoundedCornerShape(12.dp))
                .padding(16.dp),
            decorationBox = { innerTextField ->
                if (accountName.isEmpty()) {
                    Text(
                        text = "e.g. HDFC Savings, Cash Wallet",
                        color = AccountsGrayText,
                        fontSize = 16.sp
                    )
                }
                innerTextField()
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Account Type Section
        Text(
            text = "Account Type",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = AccountsWhiteText
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                AccountsAccountTypeCard(
                    title = "Bank Account",
                    icon = Icons.Default.AccountBalance,
                    iconColor = Color(0xFF4285F4),
                    isSelected = selectedAccountType == "Bank Account",
                    onClick = { selectedAccountType = "Bank Account" }
                )
            }
            item {
                AccountsAccountTypeCard(
                    title = "Credit/Debit Card",
                    icon = Icons.Default.CreditCard,
                    iconColor = Color(0xFF34A853),
                    isSelected = selectedAccountType == "Credit/Debit Card",
                    onClick = { selectedAccountType = "Credit/Debit Card" }
                )
            }
            item {
                AccountsAccountTypeCard(
                    title = "Cash",
                    icon = Icons.Default.AttachMoney,
                    iconColor = Color(0xFFFF6D01),
                    isSelected = selectedAccountType == "Cash",
                    onClick = { selectedAccountType = "Cash" }
                )
            }
            item {
                AccountsAccountTypeCard(
                    title = "Digital Wallet",
                    icon = Icons.Default.Wallet,
                    iconColor = Color(0xFF9C27B0),
                    isSelected = selectedAccountType == "Digital Wallet",
                    onClick = { selectedAccountType = "Digital Wallet" }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Bank Name Section (only show if Bank Account is selected)
        if (selectedAccountType == "Bank Account") {
            Text(
                text = "Bank Name",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = AccountsWhiteText
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bankOptions) { bank ->
                    BankSelectionCard(
                        bankName = bank,
                        isSelected = selectedBank == bank,
                        onClick = { selectedBank = bank }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Initial Balance Section
        Text(
            text = "Initial Balance",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = AccountsWhiteText
        )

        Spacer(modifier = Modifier.height(8.dp))

        BasicTextField(
            value = initialBalance,
            onValueChange = { initialBalance = it },
            textStyle = TextStyle(color = AccountsWhiteText, fontSize = 16.sp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
                .fillMaxWidth()
                .background(AccountsDarkSurfaceVariant, RoundedCornerShape(12.dp))
                .padding(16.dp),
            decorationBox = { innerTextField ->
                if (initialBalance.isEmpty()) {
                    Text(
                        text = "0.00",
                        color = AccountsGrayText,
                        fontSize = 16.sp
                    )
                }
                innerTextField()
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Add Account Button
        Button(
            onClick = {
                val account = Account(
                    id = System.currentTimeMillis().toString(),
                    name = accountName.ifEmpty {
                        if (selectedAccountType == "Bank Account") selectedBank else selectedAccountType
                    },
                    balance = initialBalance,
                    icon = when (selectedAccountType) {
                        "Bank Account" -> Icons.Default.AccountBalance
                        "Credit/Debit Card" -> Icons.Default.CreditCard
                        "Cash" -> Icons.Default.AttachMoney
                        "Digital Wallet" -> Icons.Default.Wallet
                        else -> Icons.Default.AccountBalance
                    },
                    color = getAccountTypeColor(selectedAccountType),
                    type = selectedAccountType
                )
                onAccountAdded(account)
            },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6C6C6C)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditAccountBottomSheet(
    account: Account,
    onDismiss: () -> Unit,
    onAccountUpdated: (Account) -> Unit,
    accountDatabaseManager: com.example.androidkmm.database.SQLiteAccountDatabase
) {
    var accountName by remember { mutableStateOf(account.name) }
    var selectedAccountType by remember { mutableStateOf(account.type) }
    var selectedBank by remember { mutableStateOf("HDFC Bank") }
    var initialBalance by remember { mutableStateOf(account.balance) }

    val bankOptions = listOf(
        "HDFC Bank", "State Bank of India (SBI)",
        "ICICI Bank", "Axis Bank",
        "Bank of Baroda", "Punjab National Bank"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        // Account Name Section
        Text(
            text = "Account Name",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = AccountsWhiteText
        )

        Spacer(modifier = Modifier.height(8.dp))

        BasicTextField(
            value = accountName,
            onValueChange = { accountName = it },
            textStyle = TextStyle(color = AccountsWhiteText, fontSize = 16.sp),
            modifier = Modifier
                .fillMaxWidth()
                .background(AccountsDarkSurfaceVariant, RoundedCornerShape(12.dp))
                .padding(16.dp),
            decorationBox = { innerTextField ->
                if (accountName.isEmpty()) {
                    Text(
                        text = "e.g. HDFC Savings, Cash Wallet",
                        color = AccountsGrayText,
                        fontSize = 16.sp
                    )
                }
                innerTextField()
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Account Type Section
        Text(
            text = "Account Type",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = AccountsWhiteText
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                AccountsAccountTypeCard(
                    title = "Bank Account",
                    icon = Icons.Default.AccountBalance,
                    iconColor = Color(0xFF4285F4),
                    isSelected = selectedAccountType == "Bank Account",
                    onClick = { selectedAccountType = "Bank Account" }
                )
            }
            item {
                AccountsAccountTypeCard(
                    title = "Credit/Debit Card",
                    icon = Icons.Default.CreditCard,
                    iconColor = Color(0xFF34A853),
                    isSelected = selectedAccountType == "Credit/Debit Card",
                    onClick = { selectedAccountType = "Credit/Debit Card" }
                )
            }
            item {
                AccountsAccountTypeCard(
                    title = "Cash",
                    icon = Icons.Default.AttachMoney,
                    iconColor = Color(0xFFFF6D01),
                    isSelected = selectedAccountType == "Cash",
                    onClick = { selectedAccountType = "Cash" }
                )
            }
            item {
                AccountsAccountTypeCard(
                    title = "Digital Wallet",
                    icon = Icons.Default.Wallet,
                    iconColor = Color(0xFF9C27B0),
                    isSelected = selectedAccountType == "Digital Wallet",
                    onClick = { selectedAccountType = "Digital Wallet" }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Bank Name Section (only show if Bank Account is selected)
        if (selectedAccountType == "Bank Account") {
            Text(
                text = "Bank Name",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = AccountsWhiteText
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bankOptions) { bank ->
                    BankSelectionCard(
                        bankName = bank,
                        isSelected = selectedBank == bank,
                        onClick = { selectedBank = bank }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Initial Balance Section
        Text(
            text = "Initial Balance",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = AccountsWhiteText
        )

        Spacer(modifier = Modifier.height(8.dp))

        BasicTextField(
            value = initialBalance,
            onValueChange = { initialBalance = it },
            textStyle = TextStyle(color = AccountsWhiteText, fontSize = 16.sp),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
                .fillMaxWidth()
                .background(AccountsDarkSurfaceVariant, RoundedCornerShape(12.dp))
                .padding(16.dp),
            decorationBox = { innerTextField ->
                if (initialBalance.isEmpty()) {
                    Text(
                        text = "0.00",
                        color = AccountsGrayText,
                        fontSize = 16.sp
                    )
                }
                innerTextField()
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Update Account Button
        Button(
            onClick = {
                val updatedAccount = account.copy(
                    name = accountName.ifEmpty {
                        if (selectedAccountType == "Bank Account") selectedBank else selectedAccountType
                    },
                    balance = initialBalance,
                    icon = when (selectedAccountType) {
                        "Bank Account" -> Icons.Default.AccountBalance
                        "Credit/Debit Card" -> Icons.Default.CreditCard
                        "Cash" -> Icons.Default.AttachMoney
                        "Digital Wallet" -> Icons.Default.Wallet
                        else -> Icons.Default.AccountBalance
                    },
                    color = getAccountTypeColor(selectedAccountType),
                    type = selectedAccountType
                )
                onAccountUpdated(updatedAccount)
            },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6C6C6C)
            )
        ) {
            Text(
                text = "Update Account",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

@Composable
private fun AccountsAccountTypeCard(
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
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AccountsBluePrimary.copy(alpha = 0.2f) else AccountsDarkSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(2.dp, AccountsBluePrimary) else null
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
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = AccountsWhiteText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BankSelectionCard(
    bankName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AccountsBluePrimary.copy(alpha = 0.2f) else AccountsDarkSurfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(2.dp, AccountsBluePrimary) else null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = bankName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = AccountsWhiteText,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// Helper functions
private fun getAccountTypeIcon(type: String): ImageVector {
    return when (type) {
        "Bank Account" -> Icons.Default.AccountBalance
        "Credit/Debit Card" -> Icons.Default.CreditCard
        "Cash" -> Icons.Default.AttachMoney
        "Digital Wallet" -> Icons.Default.Wallet
        else -> Icons.Default.AccountBalance
    }
}

private fun getAccountTypeColor(type: String): Color {
    return when (type) {
        "Bank Account" -> Color(0xFF4285F4)
        "Credit/Debit Card" -> Color(0xFF34A853)
        "Cash" -> Color(0xFFFF6D01)
        "Digital Wallet" -> Color(0xFF9C27B0)
        else -> Color(0xFF4285F4)
    }
}
