@file:OptIn(ExperimentalMaterial3Api::class)

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.androidkmm.database.rememberSQLiteSettingsDatabase
import com.example.androidkmm.models.Account
import com.example.androidkmm.models.AppSettings
import com.example.androidkmm.components.AddAccountBottomSheet
import com.example.androidkmm.components.AccountDeletionDialog
import com.example.androidkmm.design.AppStyleDesignSystem

// Color definitions for AccountsScreen - now using MaterialTheme
internal val AccountsGreenSuccess = Color(0xFF4CAF50)
internal val AccountsRedError = Color(0xFFF44336)
internal val AccountsBluePrimary = Color(0xFF2196F3)

@Composable
fun AccountsScreen(
    onBackClick: () -> Unit
) {
    // Get currency symbol from settings
    val settingsDatabaseManager = rememberSQLiteSettingsDatabase()
    val appSettings = settingsDatabaseManager.getAppSettings().collectAsState(initial = AppSettings())
    val currencySymbol = appSettings.value.currencySymbol
    
    var showAddAccountSheet by remember { mutableStateOf(false) }
    var showEditAccountSheet by remember { mutableStateOf(false) }
    var selectedAccount by remember { mutableStateOf<Account?>(null) }
    var selectedTab by remember { mutableStateOf(0) }
    
    // Dialog state for account deletion warning
    var showDeletionDialog by remember { mutableStateOf(false) }
    var accountToDelete by remember { mutableStateOf<Account?>(null) }
    
    // Database manager
    val accountDatabaseManager = rememberSQLiteAccountDatabase()
    val scope = rememberCoroutineScope()
    
    // Flow for active accounts from database
    val activeAccountsState = accountDatabaseManager.getActiveAccounts().collectAsState(initial = emptyList<Account>())
    val activeAccounts = activeAccountsState.value
    
    // Flow for archived accounts from database
    val archivedAccountsState = accountDatabaseManager.getArchivedAccounts().collectAsState(initial = emptyList<Account>())
    val archivedAccounts = archivedAccountsState.value

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppStyleDesignSystem.Padding.SCREEN_HORIZONTAL, vertical = AppStyleDesignSystem.Padding.SCREEN_VERTICAL),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Manage Accounts",
                        style = AppStyleDesignSystem.Typography.MAIN_PAGE_HEADING_TITLE,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Add, edit, or manage your financial accounts",
                        style = AppStyleDesignSystem.Typography.CALL_OUT,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(AppStyleDesignSystem.Sizes.AVATAR_MEDIUM)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_LARGE)
                    )
                }
            }

            // Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = AccountsBluePrimary
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Accounts", color = MaterialTheme.colorScheme.onBackground) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Overview", color = MaterialTheme.colorScheme.onBackground) }
                )
            }

            // Content based on selected tab
            when (selectedTab) {
                0 -> {
                    // Accounts List
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = AppStyleDesignSystem.Padding.SCREEN_HORIZONTAL, vertical = AppStyleDesignSystem.Padding.SCREEN_VERTICAL),
                        contentPadding = PaddingValues(bottom = AppStyleDesignSystem.Padding.SCREEN_VERTICAL),
                        verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.MEDIUM)
                    ) {
                        if (activeAccounts.isEmpty()) {
                            item {
                                EmptyAccountsState(
                                    onAddAccount = { showAddAccountSheet = true }
                                )
                            }
                        } else {
                            items(activeAccounts) { account ->
                                NewAccountCard(
                                    account = account,
                                    onEditClick = {
                                        selectedAccount = account
                                        showEditAccountSheet = true
                                    },
                                    onDeleteClick = if (account.name.equals("Cash", ignoreCase = true)) {
                                        { /* Cash account cannot be deleted */ }
                                    } else {
                                        {
                                            // Check if account has transactions before deletion
                                            scope.launch {
                                                val hasTransactions = accountDatabaseManager.hasAccountTransactions(account.name)
                                                if (hasTransactions) {
                                                    // Show dialog warning
                                                    accountToDelete = account
                                                    showDeletionDialog = true
                                                } else {
                                                    // Safe to delete
                                                    accountDatabaseManager.deleteAccount(account)
                                                }
                                            }
                                        }
                                    },
                                    showDeleteButton = !account.name.equals("Cash", ignoreCase = true),
                                    currencySymbol = currencySymbol
                                )
                            }
                        }
                        
                        // Archived Accounts Section
                        if (archivedAccounts.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Archived Accounts",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                            
                            items(archivedAccounts) { account ->
                                ArchivedAccountCard(
                                    account = account,
                                    onUnarchiveClick = {
                                        scope.launch {
                                            accountDatabaseManager.unarchiveAccount(account.id)
                                        }
                                    },
                                    currencySymbol = currencySymbol
                                )
                            }
                        }
                        
                        // Add New Account Button
                        item {
                            AddNewAccountButton(
                                onClick = { showAddAccountSheet = true }
                            )
                        }
                    }
                }
                1 -> {
                    // Overview Tab
                    OverviewContent(accounts = activeAccounts, currencySymbol = currencySymbol)
                }
            }
        }

        // Add Account Bottom Sheet
        if (showAddAccountSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddAccountSheet = false },
                containerColor = MaterialTheme.colorScheme.surface,
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
                containerColor = MaterialTheme.colorScheme.surface,
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
        
        // Account Deletion Warning Dialog
        AccountDeletionDialog(
            isVisible = showDeletionDialog,
            accountName = accountToDelete?.name ?: "",
            onDismiss = {
                showDeletionDialog = false
                accountToDelete = null
            },
            onCancel = {
                showDeletionDialog = false
                accountToDelete = null
            },
            onArchive = {
                // Archive the account
                accountToDelete?.let { account ->
                    scope.launch {
                        accountDatabaseManager.archiveAccount(account.id)
                    }
                }
                showDeletionDialog = false
                accountToDelete = null
            }
        )
    }
}

@Composable
private fun AccountCard(
    account: Account,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    currencySymbol: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppStyleDesignSystem.Padding.CARD_PADDING),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Account Icon
                Box(
                    modifier = Modifier
                        .size(AppStyleDesignSystem.Sizes.ICON_BUTTON_SIZE)
                        .clip(CircleShape)
                        .background(getAccountTypeColor(account.type)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getAccountTypeIcon(account.type),
                        contentDescription = account.type,
                        tint = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_LARGE)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Account Details
                Column {
                    Text(
                        text = account.name,
                        style = AppStyleDesignSystem.Typography.HEADLINE,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = account.type,
                        style = AppStyleDesignSystem.Typography.CALL_OUT,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        text = "$currencySymbol${String.format("%.2f", account.balance.toDoubleOrNull() ?: 0.0)}",
                        style = AppStyleDesignSystem.Typography.HEADLINE,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Balance",
                        style = AppStyleDesignSystem.Typography.FOOTNOTE,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_XL)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = AccountsRedError,
                        modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_SMALL)
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
            .padding(vertical = AppStyleDesignSystem.Padding.XXL),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppStyleDesignSystem.Padding.XXL),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.AccountBalance,
                contentDescription = "No Accounts",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_HUGE)
            )

            Spacer(modifier = Modifier.height(AppStyleDesignSystem.Padding.MEDIUM))

            Text(
                text = "No Accounts Yet",
                style = AppStyleDesignSystem.Typography.TITLE_2,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(AppStyleDesignSystem.Padding.SMALL))

            Text(
                text = "Add your first account to start tracking your finances",
                style = AppStyleDesignSystem.Typography.CALL_OUT,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(AppStyleDesignSystem.Padding.LARGE))

            Button(
                onClick = onAddAccount,
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccountsBluePrimary
                ),
                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
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
private fun EditAccountBottomSheet(
    account: Account,
    onDismiss: () -> Unit,
    onAccountUpdated: (Account) -> Unit,
    accountDatabaseManager: com.example.androidkmm.database.SQLiteAccountDatabase
) {
    var accountName by remember { mutableStateOf(account.name) }
    var selectedAccountType by remember { mutableStateOf(account.type) }
    var initialBalance by remember { mutableStateOf(account.balance) }
    
    // Check if this is a Cash account
    val isCashAccount = account.name.equals("Cash", ignoreCase = true)
    
    // Get all existing accounts for duplicate validation
    val allAccounts = accountDatabaseManager.getAllAccounts().collectAsState(initial = emptyList<Account>()).value
    
    // Check for duplicate account (same name and type, excluding current account)
    val isDuplicateAccount = allAccounts.any { existingAccount ->
        existingAccount.id != account.id && // Exclude current account
        existingAccount.name.equals(accountName, ignoreCase = true) && 
        existingAccount.type == selectedAccountType
    }
    
    // Track if any changes have been made
    val hasChanges = remember(accountName, selectedAccountType, initialBalance) {
        accountName != account.name ||
        selectedAccountType != account.type ||
        initialBalance != account.balance
    }
    
    // Form is valid if there are changes and no duplicate
    val isFormValid = hasChanges && !isDuplicateAccount


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
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        BasicTextField(
            value = accountName,
            onValueChange = if (isCashAccount) { { /* Disabled for Cash account */ } } else { { accountName = it.take(24) } },
            textStyle = TextStyle(
                color = if (isCashAccount) Color.Gray else Color.White, 
                fontSize = 16.sp
            ),
            readOnly = isCashAccount,
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (isCashAccount) Color(0xFF2A2A2A) else Color.Black, 
                    RoundedCornerShape(12.dp)
                )
                .border(0.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(AppStyleDesignSystem.Padding.CARD_PADDING),
            decorationBox = { innerTextField ->
                if (accountName.isEmpty()) {
                    Text(
                        text = if (isCashAccount) "Cash (Default Account)" else "e.g. HDFC Savings, Cash Wallet",
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
            color = if (isCashAccount) Color.Gray else MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_MEDIUM),
            verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_MEDIUM)
        ) {
            item {
                AccountsAccountTypeCard(
                    title = "Bank Account",
                    icon = Icons.Default.AccountBalance,
                    iconColor = if (isCashAccount) Color.Gray else Color(0xFF4285F4),
                    isSelected = selectedAccountType == "Bank Account",
                    onClick = if (isCashAccount) { { /* Disabled for Cash account */ } } else { { selectedAccountType = "Bank Account" } }
                )
            }
            item {
                AccountsAccountTypeCard(
                    title = "Credit/Debit Card",
                    icon = Icons.Default.CreditCard,
                    iconColor = if (isCashAccount) Color.Gray else Color(0xFF34A853),
                    isSelected = selectedAccountType == "Credit/Debit Card",
                    onClick = if (isCashAccount) { { /* Disabled for Cash account */ } } else { { selectedAccountType = "Credit/Debit Card" } }
                )
            }
            item {
                AccountsAccountTypeCard(
                    title = "Cash",
                    icon = Icons.Default.Money,
                    iconColor = if (isCashAccount) Color.Gray else Color(0xFFFF6D01),
                    isSelected = selectedAccountType == "Cash",
                    onClick = if (isCashAccount) { { /* Disabled for Cash account */ } } else { { selectedAccountType = "Cash" } }
                )
            }
            item {
                AccountsAccountTypeCard(
                    title = "Digital Wallet",
                    icon = Icons.Default.Wallet,
                    iconColor = if (isCashAccount) Color.Gray else Color(0xFF9C27B0),
                    isSelected = selectedAccountType == "Digital Wallet",
                    onClick = if (isCashAccount) { { /* Disabled for Cash account */ } } else { { selectedAccountType = "Digital Wallet" } }
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
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black, RoundedCornerShape(12.dp))
                .border(0.5.dp, Color.White.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .padding(AppStyleDesignSystem.Padding.CARD_PADDING),
            decorationBox = { innerTextField ->
                if (initialBalance.isEmpty()) {
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

        // Update Account Button
        Button(
            onClick = {
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
                
                val updatedAccount = account.copy(
                    name = capitalizedAccountName.ifEmpty { selectedAccountType },
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
            enabled = isFormValid,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFormValid) Color(0xFF4285F4) else Color(0xFF6C6C6C)
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
            containerColor = if (isSelected) AccountsBluePrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(2.dp, AccountsBluePrimary) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppStyleDesignSystem.Padding.CARD_PADDING),
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
                color = MaterialTheme.colorScheme.onSurface,
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
            containerColor = if (isSelected) AccountsBluePrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) BorderStroke(2.dp, AccountsBluePrimary) else null
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppStyleDesignSystem.Padding.CARD_PADDING),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = bankName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
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
        "Cash" -> Icons.Default.Money
        "Digital Wallet" -> Icons.Default.PhoneAndroid
        else -> Icons.Default.AccountBalance
    }
}

private fun getAccountTypeColor(type: String): Color {
    return when (type) {
        "Bank Account" -> Color(0xFF2196F3) // Blue
        "Credit/Debit Card" -> Color(0xFF4CAF50) // Green
        "Cash" -> Color(0xFFFF9800) // Orange
        "Digital Wallet" -> Color(0xFF9C27B0) // Purple
        else -> Color(0xFF2196F3)
    }
}

@Composable
private fun NewAccountCard(
    account: Account,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    showDeleteButton: Boolean = true,
    currencySymbol: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEditClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppStyleDesignSystem.Padding.CARD_PADDING),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Account Icon
                Icon(
                    imageVector = getAccountTypeIcon(account.type),
                    contentDescription = account.type,
                    tint = getAccountTypeColor(account.type),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Account Details
                Column {
                    Text(
                        text = account.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "$currencySymbol${String.format("%.2f", account.balance.toDoubleOrNull() ?: 0.0)}",
                        fontSize = 14.sp,
                        color = AccountsGreenSuccess,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // Action Buttons
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onEditClick,
                    modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_XL)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_SMALL)
                    )
                }

                if (showDeleteButton) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_XL)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = AccountsRedError,
                            modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_SMALL)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ArchivedAccountCard(
    account: Account,
    onUnarchiveClick: () -> Unit,
    currencySymbol: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppStyleDesignSystem.Padding.CARD_PADDING),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Account Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        account.color.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = account.icon,
                    contentDescription = account.name,
                    tint = account.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Account Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = account.name,
                    style = AppStyleDesignSystem.Typography.HEADLINE,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = account.type,
                    style = AppStyleDesignSystem.Typography.CALL_OUT,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
                Text(
                    text = "Archived",
                    style = AppStyleDesignSystem.Typography.FOOTNOTE,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            }

            // Balance and Actions
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "$currencySymbol${String.format("%.2f", account.balance.toDoubleOrNull() ?: 0.0)}",
                        style = AppStyleDesignSystem.Typography.HEADLINE,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onUnarchiveClick,
                    modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_XL)
                ) {
                    Icon(
                        imageVector = Icons.Default.Restore,
                        contentDescription = "Unarchive",
                        tint = AccountsBluePrimary,
                        modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_SMALL)
                    )
                }
            }
        }
    }
}

@Composable
private fun ReadOnlyAccountCard(account: Account, currencySymbol: String = "₹") {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, Color(0xFF333333)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Account Icon with different styling
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2A2A2A)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getAccountTypeIcon(account.type),
                        contentDescription = account.type,
                        tint = getAccountTypeColor(account.type),
                        modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_SMALL)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Account Details
                Column {
                    Text(
                        text = account.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = account.type,
                        fontSize = 12.sp,
                        color = Color(0xFF888888)
                    )
                }
            }

            // Balance with different styling
            Text(
                text = if (account.balance.startsWith("+") || account.balance.startsWith(currencySymbol)) {
                    account.balance // Already has currency symbol
                } else {
                    "$currencySymbol${account.balance}"
                },
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = if (account.balance.startsWith("+") || account.balance.startsWith(currencySymbol)) {
                    Color(0xFF4CAF50)
                } else {
                    Color(0xFFF44336)
                }
            )
        }
    }
}

@Composable
private fun AddNewAccountButton(
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppStyleDesignSystem.Padding.CARD_PADDING),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_MEDIUM)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Add New Account",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun OverviewContent(accounts: List<Account>, currencySymbol: String) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_MEDIUM)
    ) {
        item {
            // Financial Overview Section
            Column {
                Text(
                    text = "Financial Overview",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        // Total Assets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Assets",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "$currencySymbol${String.format("%.1f", accounts.sumOf { it.balance.toDoubleOrNull() ?: 0.0 })}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccountsGreenSuccess
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Divider
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            thickness = 1.dp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Total Liabilities
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Liabilities",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "$currencySymbol${0}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccountsRedError
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Divider
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                            thickness = 1.dp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Net Worth
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Net Worth",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "+$currencySymbol${String.format("%.1f", accounts.sumOf { it.balance.toDoubleOrNull() ?: 0.0 })}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccountsGreenSuccess
                            )
                        }
                    }
                }
            }
        }

        // Account List in Overview
        items(accounts) { account ->
            ReadOnlyAccountCard(account = account, currencySymbol = currencySymbol)
        }
    }
}
