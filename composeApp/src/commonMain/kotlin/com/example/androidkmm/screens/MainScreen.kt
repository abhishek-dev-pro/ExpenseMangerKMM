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
                        }
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
            }
            
            // Add Transaction Screen
            if (showAddTransactionSheet) {
                AddTransactionScreen(
                    onDismiss = { 
                        showAddTransactionSheet = false
                        defaultTransactionType = null
                    },
                    onSave = { transactionFormData: com.example.androidkmm.models.TransactionFormData ->
                        // Convert TransactionFormData to Transaction and save to database
                        val transaction = com.example.androidkmm.models.Transaction(
                            id = "${kotlin.time.Clock.System.now().epochSeconds}",
                            title = transactionFormData.title,
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
                            accountDatabaseManager = accountDatabaseManager
                        )
                        showAddTransactionSheet = false
                        defaultTransactionType = null
                    },
                    categoryDatabaseManager = categoryDatabaseManager,
                    accountDatabaseManager = accountDatabaseManager,
                    defaultTransactionType = defaultTransactionType
                )
            }
            
            // Add Ledger Entry Full Screen
            if (showAddLedgerEntrySheet) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    AddLedgerEntryBottomSheet(
                        onDismiss = { 
                            showAddLedgerEntrySheet = false
                        }
                    )
                }
            }
        }
    }
}