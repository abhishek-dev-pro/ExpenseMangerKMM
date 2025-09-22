package com.example.androidkmm.screens.transactions

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background
import com.example.androidkmm.models.Transaction
import com.example.androidkmm.models.DayGroup
import com.example.androidkmm.screens.transactions.components.*
import com.example.androidkmm.viewmodel.TransactionViewModel
import com.example.androidkmm.design.DesignSystem

/**
 * Refactored Transaction List Screen
 * 
 * Features:
 * - Clean separation of concerns
 * - Reusable components
 * - Proper state management
 * - Error handling
 * - Loading states
 */
@Composable
fun TransactionListScreenRefactored(
    viewModel: TransactionViewModel,
    onNavigateToLedger: (String, String) -> Unit = { _, _ -> }
) {
    val uiState by viewModel.uiState.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    
    // Local state
    var searchQuery by remember { mutableStateOf("") }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showDateRangeDialog by remember { mutableStateOf(false) }
    
    // Group transactions by date
    val dayGroups = remember(transactions) {
        groupTransactionsByDate(transactions)
    }
    
    // Handle search
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotEmpty()) {
            viewModel.searchTransactions(searchQuery)
        } else {
            viewModel.refreshTransactions()
        }
    }
    
    // Handle transaction click
    val onTransactionClick = { transaction: Transaction ->
        // Navigate to ledger if it's a ledger transaction
        if (transaction.type == com.example.androidkmm.models.TransactionType.TRANSFER) {
            onNavigateToLedger(transaction.account, transaction.id)
        }
    }
    
    // Handle retry
    val onRetry = {
        viewModel.refreshTransactions()
    }
    
    // Handle add transaction
    val onAddTransaction = {
        // Navigate to add transaction screen
        // This would be handled by the parent component
    }
    
    // Handle filter
    val onFilterClick = {
        showFilterDialog = true
    }
    
    // Handle date range
    val onDateRangeClick = {
        showDateRangeDialog = true
    }
    
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header with search and filters
            TransactionListHeader(
                searchQuery = searchQuery,
                onSearchQueryChange = { searchQuery = it },
                onFilterClick = onFilterClick,
                onAddTransactionClick = onAddTransaction,
                onDateRangeClick = onDateRangeClick
            )
            
            // Main content
            TransactionListContent(
                dayGroups = dayGroups,
                isLoading = isLoading,
                error = error,
                onTransactionClick = onTransactionClick,
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        // Filter dialog
        if (showFilterDialog) {
            FilterDialog(
                onDismiss = { showFilterDialog = false },
                onApply = { /* Apply filters */ }
            )
        }
        
        // Date range dialog
        if (showDateRangeDialog) {
            DateRangeDialog(
                onDismiss = { showDateRangeDialog = false },
                onApply = { /* Apply date range */ }
            )
        }
    }
}

/**
 * Group transactions by date
 */
private fun groupTransactionsByDate(transactions: List<Transaction>): List<DayGroup> {
    return transactions
        .groupBy { it.date }
        .map { (date, transactionList) ->
            DayGroup(
                date = date,
                transactions = transactionList,
                displayDate = date,
                income = transactionList.filter { it.type == com.example.androidkmm.models.TransactionType.INCOME }.sumOf { it.amount },
                expense = transactionList.filter { it.type == com.example.androidkmm.models.TransactionType.EXPENSE }.sumOf { it.amount }
            )
        }
        .sortedByDescending { it.date }
}

/**
 * Filter dialog component
 */
@Composable
private fun FilterDialog(
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Filter Transactions")
        },
        text = {
            Text("Filter options will be implemented here")
        },
        confirmButton = {
            TextButton(onClick = onApply) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Date range dialog component
 */
@Composable
private fun DateRangeDialog(
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Select Date Range")
        },
        text = {
            Text("Date range selection will be implemented here")
        },
        confirmButton = {
            TextButton(onClick = onApply) {
                Text("Apply")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
