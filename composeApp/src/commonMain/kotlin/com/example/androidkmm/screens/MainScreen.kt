package com.example.androidkmm.screens

import ProfileMainScreen
import TransactionsScreen
import AddTransactionScreen
// CreateGroupScreen is now in GroupScreen.kt
import com.example.androidkmm.screens.ledger.LedgerMainScreen
import com.example.androidkmm.screens.ledger.AddLedgerEntryBottomSheet
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.BackHandler
import com.example.androidkmm.components.*
import com.example.androidkmm.database.InitializeDatabase
import com.example.androidkmm.database.rememberSQLiteCategoryDatabase
import com.example.androidkmm.database.rememberSQLiteAccountDatabase
import com.example.androidkmm.database.rememberSQLiteTransactionDatabase
import com.example.androidkmm.database.rememberSQLiteGroupDatabase
import com.example.androidkmm.theme.AppTheme
import kotlin.time.ExperimentalTime

/**
 * Navigation state for tracking navigation history
 */
data class NavigationState(
    val screen: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Main screen with bottom navigation and content switching
 */
@OptIn(ExperimentalTime::class)
@Composable
fun MainScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    var navigateToLedgerPerson by remember { mutableStateOf<String?>(null) }
    var navigateToLedgerTransaction by remember { mutableStateOf<String?>(null) }
    var showAddTransactionSheet by remember { mutableStateOf(false) }
    var defaultTransactionType by remember { mutableStateOf<com.example.androidkmm.models.TransactionType?>(null) }
    var showCreateGroupScreen by remember { mutableStateOf(false) }
    var showAddExpenseScreen by remember { mutableStateOf(false) }
    var showAddLedgerEntrySheet by remember { mutableStateOf(false) }
    
    // Insufficient balance dialog state
    var showInsufficientBalanceDialog by remember { mutableStateOf(false) }
    var insufficientBalanceInfo by remember { mutableStateOf<com.example.androidkmm.models.InsufficientBalanceInfo?>(null) }
    
    // Refresh trigger for home screen components
    var homeScreenRefreshTrigger by remember { mutableStateOf(0) }
    
    // Navigation stack for proper back navigation
    var navigationStack by remember { mutableStateOf(listOf<NavigationState>()) }
    
    // Handle back navigation
    fun handleBackNavigation(): Boolean {
        return when {
            showAddLedgerEntrySheet -> {
                showAddLedgerEntrySheet = false
                true
            }
            showAddExpenseScreen -> {
                showAddExpenseScreen = false
                true
            }
            showCreateGroupScreen -> {
                showCreateGroupScreen = false
                true
            }
            showAddTransactionSheet -> {
                showAddTransactionSheet = false
                defaultTransactionType = null
                true
            }
            navigateToLedgerPerson != null -> {
                navigateToLedgerPerson = null
                navigateToLedgerTransaction = null
                true
            }
            else -> false
        }
    }
    
    // Database managers
    val categoryDatabaseManager = rememberSQLiteCategoryDatabase()
    val accountDatabaseManager = rememberSQLiteAccountDatabase()
    val transactionDatabaseManager = rememberSQLiteTransactionDatabase()
    val groupDatabaseManager = rememberSQLiteGroupDatabase()
    
    // Initialize database
    InitializeDatabase()
    
    // Handle back navigation
    BackHandler {
        handleBackNavigation()
    }

    // Handle tab selection with dialog/sheet management
    fun handleTabSelection(newTab: Int) {
        if (newTab != selectedTab) {
            // Close all dialogs/sheets when switching to a different tab
            showAddLedgerEntrySheet = false
            showAddExpenseScreen = false
            showCreateGroupScreen = false
            showAddTransactionSheet = false
            defaultTransactionType = null
            navigateToLedgerPerson = null
            navigateToLedgerTransaction = null
            selectedTab = newTab
        }
        // If clicking the same tab, do nothing (don't close dialogs/sheets)
    }

    AppTheme {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    selected = selectedTab,
                    onSelect = { handleTabSelection(it) }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                when (selectedTab) {
                    0 -> HomeScreenContent(
                        onNavigateToTransactions = { 
                            println("MainScreen: Navigating to transactions tab")
                            selectedTab = 1 
                        },
                        onNavigateToAddExpense = {
                            println("MainScreen: Navigating to add expense")
                            showAddExpenseScreen = true
                        },
                        onNavigateToAddIncome = {
                            println("MainScreen: Navigating to add income")
                            defaultTransactionType = com.example.androidkmm.models.TransactionType.INCOME
                            showAddTransactionSheet = true
                        },
                        onNavigateToGroups = {
                            println("MainScreen: Navigating to groups in profile")
                            selectedTab = 4 // Navigate to Profile tab
                        },
                        onNavigateToLedger = {
                            println("MainScreen: Opening Add Ledger Entry bottom sheet")
                            showAddLedgerEntrySheet = true
                        },
                        onNavigateToAddTransaction = {
                            println("MainScreen: Navigating to add transaction")
                            showAddTransactionSheet = true
                        },
                        refreshTrigger = homeScreenRefreshTrigger
                    )
                    1 -> TransactionsScreen(
                        onNavigateToLedger = { personName, transactionId ->
                            println("MainScreen - Received navigation: personName='$personName', transactionId='$transactionId'")
                            navigateToLedgerPerson = personName
                            navigateToLedgerTransaction = transactionId
                            selectedTab = 2 // Switch to ledger tab
                        }
                    )
                    2 -> LedgerMainScreen(
                        navigateToPerson = navigateToLedgerPerson,
                        navigateToTransaction = navigateToLedgerTransaction,
                        onPersonNavigated = { 
                            navigateToLedgerPerson = null
                            navigateToLedgerTransaction = null
                        }
                    )
                    3 -> InsightsScreen(transactionDatabaseManager = transactionDatabaseManager)
                    4 -> ProfileMainScreen()
                }
                
                // Show Create Group Screen
                if (showCreateGroupScreen) {
                    CreateGroupScreen(
                        onBack = {
                            showCreateGroupScreen = false
                        },
                        groupDatabaseManager = groupDatabaseManager
                    )
                }
                
                // Show Add Expense Screen
                if (showAddExpenseScreen) {
                    AddExpenseScreen(
                        onBack = {
                            showAddExpenseScreen = false
                        },
                        categoryDatabaseManager = categoryDatabaseManager,
                        accountDatabaseManager = accountDatabaseManager,
                        transactionDatabaseManager = transactionDatabaseManager
                    )
                }
                
                // Show Add Transaction Screen
                if (showAddTransactionSheet) {
                    AddTransactionScreen(
                        onDismiss = { 
                            showAddTransactionSheet = false
                            defaultTransactionType = null
                            // Trigger refresh when returning to home screen
                            homeScreenRefreshTrigger++
                            println("DEBUG: MainScreen - Triggering home screen refresh (trigger: $homeScreenRefreshTrigger)")
                        },
                        onSave = { transactionFormData: com.example.androidkmm.models.TransactionFormData ->
                            // Convert TransactionFormData to Transaction and save to database
                            val transaction = com.example.androidkmm.models.Transaction(
                                id = "${kotlin.time.Clock.System.now().epochSeconds}",
                                title = if (transactionFormData.type == com.example.androidkmm.models.TransactionType.TRANSFER && transactionFormData.title.isBlank()) {
                                    "Transfer from ${transactionFormData.account?.name ?: ""} to ${transactionFormData.toAccount?.name ?: ""}"
                                } else {
                                    transactionFormData.title
                                },
                                amount = transactionFormData.amount.toDoubleOrNull() ?: 0.0,
                                category = if (transactionFormData.type == com.example.androidkmm.models.TransactionType.TRANSFER) "Transfer" else (transactionFormData.category?.name ?: ""),
                                categoryIcon = if (transactionFormData.type == com.example.androidkmm.models.TransactionType.TRANSFER) Icons.Default.SwapHoriz else (transactionFormData.category?.icon ?: Icons.Default.Category),
                                categoryColor = if (transactionFormData.type == com.example.androidkmm.models.TransactionType.TRANSFER) Color(0xFF3B82F6) else (transactionFormData.category?.color ?: Color.Gray),
                                account = transactionFormData.account?.name ?: "",
                                transferTo = transactionFormData.toAccount?.name,
                                time = transactionFormData.time,
                                type = com.example.androidkmm.models.TransactionType.valueOf(transactionFormData.type.name),
                                description = transactionFormData.description,
                                date = transactionFormData.date,
                                accountIcon = transactionFormData.account?.icon ?: Icons.Default.AccountBalance,
                                accountColor = transactionFormData.account?.color ?: Color.Blue
                            )
                            
                            transactionDatabaseManager.addTransactionWithBalanceUpdate(
                                transaction = transaction,
                                accountDatabaseManager = accountDatabaseManager,
                                onSuccess = {
                                    showAddTransactionSheet = false
                                    defaultTransactionType = null
                                    // Trigger refresh when transaction is saved
                                    homeScreenRefreshTrigger++
                                    println("DEBUG: MainScreen - Transaction saved, triggering home screen refresh (trigger: $homeScreenRefreshTrigger)")
                                },
                                onError = { error ->
                                    println("DEBUG: MainScreen - Transaction failed: ${error.message}")
                                    // Check if it's an insufficient balance error
                                    if (error.message?.startsWith("Insufficient balance in source account") == true) {
                                        println("DEBUG: MainScreen - Insufficient balance detected, showing dialog")
                                        val parts = error.message!!.split("Available: ", ", Required: ")
                                        if (parts.size >= 3) {
                                            val accountName = error.message!!.substringAfter("source account '").substringBefore("'")
                                            val currentBalance = parts[1].toDoubleOrNull() ?: 0.0
                                            val requiredAmount = parts[2].toDoubleOrNull() ?: 0.0

                                            // Show insufficient balance dialog
                                            println("DEBUG: MainScreen - Setting showInsufficientBalanceDialog = true")
                                            showInsufficientBalanceDialog = true
                                            insufficientBalanceInfo = com.example.androidkmm.models.InsufficientBalanceInfo(
                                                accountName = accountName,
                                                currentBalance = currentBalance,
                                                requiredAmount = requiredAmount
                                            )
                                            println("DEBUG: MainScreen - Dialog state set - showInsufficientBalanceDialog: $showInsufficientBalanceDialog, insufficientBalanceInfo: $insufficientBalanceInfo")
                                        }
                                    }
                                }
                            )
                        },
                        categoryDatabaseManager = categoryDatabaseManager,
                        accountDatabaseManager = accountDatabaseManager,
                        defaultTransactionType = defaultTransactionType
                    )
                }
                
                // Show Add Ledger Entry Full Screen
                if (showAddLedgerEntrySheet) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        AddLedgerEntryBottomSheet(
                            onDismiss = { 
                                showAddLedgerEntrySheet = false
                                // Trigger refresh when returning to home screen
                                homeScreenRefreshTrigger++
                                println("DEBUG: MainScreen - Ledger entry dismissed, triggering home screen refresh (trigger: $homeScreenRefreshTrigger)")
                            }
                        )
                    }
                }
            }
            
            // Insufficient Balance Dialog
            if (showInsufficientBalanceDialog && insufficientBalanceInfo != null) {
                println("DEBUG: MainScreen - Rendering insufficient balance dialog")
                println("DEBUG: MainScreen - Dialog state - showInsufficientBalanceDialog: $showInsufficientBalanceDialog")
                println("DEBUG: MainScreen - Dialog state - insufficientBalanceInfo: $insufficientBalanceInfo")
                AlertDialog(
                    onDismissRequest = {
                        showInsufficientBalanceDialog = false
                        insufficientBalanceInfo = null
                    },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "Cannot Complete Transfer",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    text = {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Text(
                                text = "The source account '${insufficientBalanceInfo!!.accountName}' has a negative balance and cannot be used for transfers.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 24.sp
                            )

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                ),
                                shape = RoundedCornerShape(16.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Text(
                                        text = "To fix this:",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = "1.",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Add money to ${insufficientBalanceInfo!!.accountName} first",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 22.sp
                                        )
                                    }

                                    Row(
                                        verticalAlignment = Alignment.Top,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(
                                            text = "2.",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "Transfer money TO ${insufficientBalanceInfo!!.accountName} from another account",
                                            style = MaterialTheme.typography.bodyLarge,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            lineHeight = 22.sp
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showInsufficientBalanceDialog = false
                                insufficientBalanceInfo = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "Got it",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                )
            }
        }
    }
}