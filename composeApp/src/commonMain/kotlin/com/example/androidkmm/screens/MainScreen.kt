package com.example.androidkmm.screens

import ProfileMainScreen
import TransactionsScreen
import AddTransactionBottomSheet
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
import com.example.androidkmm.components.*
import com.example.androidkmm.database.InitializeDatabase
import com.example.androidkmm.database.rememberSQLiteCategoryDatabase
import com.example.androidkmm.database.rememberSQLiteAccountDatabase
import com.example.androidkmm.database.rememberSQLiteTransactionDatabase
import com.example.androidkmm.database.rememberSQLiteGroupDatabase
import com.example.androidkmm.theme.AppTheme
import kotlin.time.ExperimentalTime

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
    
    // Database managers
    val categoryDatabaseManager = rememberSQLiteCategoryDatabase()
    val accountDatabaseManager = rememberSQLiteAccountDatabase()
    val transactionDatabaseManager = rememberSQLiteTransactionDatabase()
    val groupDatabaseManager = rememberSQLiteGroupDatabase()
    
    // Initialize database
    InitializeDatabase()

    AppTheme {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    selected = selectedTab,
                    onSelect = { selectedTab = it }
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
            
            // Add Transaction Bottom Sheet
            if (showAddTransactionSheet) {
                AddTransactionBottomSheet(
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
            
            // Add Ledger Entry Bottom Sheet
            if (showAddLedgerEntrySheet) {
                AddLedgerEntryBottomSheet(
                    onDismiss = { 
                        showAddLedgerEntrySheet = false
                    }
                )
            }
        }
    }
}