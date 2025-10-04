@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.androidkmm.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.androidkmm.components.BalanceCard
import com.example.androidkmm.components.GreetingSection
import com.example.androidkmm.components.GroupItem
import com.example.androidkmm.components.QuickActions
import com.example.androidkmm.components.RecentTransactionsSection
import com.example.androidkmm.design.AppStyleDesignSystem
import com.example.androidkmm.data.GroupData
import com.example.androidkmm.database.rememberSQLiteGroupDatabase
import com.example.androidkmm.database.rememberSQLiteSettingsDatabase
import com.example.androidkmm.database.rememberSQLiteTransactionDatabase
import com.example.androidkmm.database.rememberSQLiteCategoryDatabase
import com.example.androidkmm.database.rememberSQLiteAccountDatabase
import com.example.androidkmm.models.AppSettings
import com.example.androidkmm.models.Transaction

/**
 * Home screen content component
 */
@Composable
fun HomeScreenContent(
    onNavigateToTransactions: () -> Unit = {},
    onNavigateToAddExpense: () -> Unit = {},
    onNavigateToAddIncome: () -> Unit = {},
    onNavigateToGroups: () -> Unit = {},
    onNavigateToLedger: () -> Unit = {},
    onNavigateToAddTransaction: () -> Unit = {},
    refreshTrigger: Int = 0,
    onBottomSheetVisibilityChange: (Boolean) -> Unit = {}
) {
    // State for transaction details bottom sheet
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
    var showTransactionDetails by remember { mutableStateOf(false) }
    
    // Database managers for transaction details
    val transactionDatabaseManager = rememberSQLiteTransactionDatabase()
    val categoryDatabaseManager = rememberSQLiteCategoryDatabase()
    val accountDatabaseManager = rememberSQLiteAccountDatabase()
    
    // Notify parent when bottom sheet visibility changes
    LaunchedEffect(showTransactionDetails) {
        onBottomSheetVisibilityChange(showTransactionDetails)
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppStyleDesignSystem.Padding.SCREEN_HORIZONTAL)
            .padding(top = AppStyleDesignSystem.Padding.SCREEN_VERTICAL),
        verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.MEDIUM)
    ) {

        item { GreetingSection() }
        item { BalanceCard() }
        item { 
            QuickActions(
                onActionClick = { action ->
                    println("HomeScreen: Quick action clicked: $action")
                    when (action) {
                        "Ledger" -> {
                            println("HomeScreen: Navigating to Ledger")
                            onNavigateToLedger()
                        }
                        "Transaction" -> {
                            println("HomeScreen: Navigating to Add Expense")
                            onNavigateToAddTransaction()
                        }
                        else -> {
                            println("HomeScreen: Unknown action: $action")
                        }
                    }
                }
            )
        }
        item { 
            RecentTransactionsSection(
                onViewAllClick = onNavigateToTransactions, 
                refreshTrigger = refreshTrigger,
                onTransactionClick = { transaction ->
                    selectedTransaction = transaction
                    showTransactionDetails = true
                }
            ) 
        }
//        item { GroupHighlights(onViewAllClick = onNavigateToGroups) }
//        item { ProgressCard() }

        item { Spacer(Modifier.height(AppStyleDesignSystem.Padding.SECTION_SPACING)) }
    }
    
    // Simple Transaction Details Bottom Sheet
    if (showTransactionDetails && selectedTransaction != null) {
        val transaction = selectedTransaction!!
        val settingsDatabaseManager = rememberSQLiteSettingsDatabase()
        val appSettings by settingsDatabaseManager.getAppSettings().collectAsState(initial = AppSettings())
        val currencySymbol = appSettings.currencySymbol
        
        val bottomSheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        )
        
        ModalBottomSheet(
            onDismissRequest = {
                showTransactionDetails = false
                selectedTransaction = null
            },
            sheetState = bottomSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
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
                        text = "Transaction Details",
                        style = AppStyleDesignSystem.Typography.HEADLINE,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    IconButton(
                        onClick = {
                            showTransactionDetails = false
                            selectedTransaction = null
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Transaction details
                Text(
                    text = transaction.title,
                    style = AppStyleDesignSystem.Typography.BODY,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "${currencySymbol}${transaction.amount}",
                    style = AppStyleDesignSystem.Typography.BODY,
                    color = when (transaction.type) {
                        com.example.androidkmm.models.TransactionType.INCOME -> Color(0xFF4CAF50)
                        com.example.androidkmm.models.TransactionType.EXPENSE -> Color(0xFFE53935)
                        com.example.androidkmm.models.TransactionType.TRANSFER -> Color(0xFF3B82F6)
                    }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Account: ${transaction.account}",
                    style = AppStyleDesignSystem.Typography.CALL_OUT,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Date: ${transaction.date} ${transaction.time}",
                    style = AppStyleDesignSystem.Typography.CALL_OUT,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            // Edit functionality - for now just close
                            showTransactionDetails = false
                            selectedTransaction = null
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Edit")
                    }
                    
                    Button(
                        onClick = {
                            // Delete functionality
                            transactionDatabaseManager.deleteTransaction(transaction)
                            showTransactionDetails = false
                            selectedTransaction = null
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Red
                        )
                    ) {
                        Text("Delete")
                    }
                }
            }
        }
    }
}




/**
 * Group highlights section
 */
@Composable
private fun GroupHighlights(
    onViewAllClick: () -> Unit = {}
) {
    val settingsDatabaseManager = rememberSQLiteSettingsDatabase()
    val appSettings by settingsDatabaseManager.getAppSettings().collectAsState(initial = AppSettings())
    val currencySymbol = appSettings.currencySymbol
    
    val groupDatabaseManager = rememberSQLiteGroupDatabase()
    val allGroups by groupDatabaseManager.getAllGroups().collectAsState(initial = emptyList())
    val allMembers by groupDatabaseManager.getAllGroupMembers().collectAsState(initial = emptyList())
    val allExpenses by groupDatabaseManager.getAllGroupExpenses().collectAsState(initial = emptyList())
    
    // Calculate user's balance for each group
    val groupHighlights = remember(allGroups, allMembers, allExpenses, currencySymbol) {
        allGroups.map { group ->
            val groupMembers = allMembers.filter { it.groupId == group.id }
            val groupExpenses = allExpenses.filter { it.groupId == group.id }
            
            // Find the current user (assuming first member is the current user for now)
            val currentUser = groupMembers.firstOrNull()
            val userBalance = currentUser?.balance ?: 0.0
            
            val isPositive = userBalance >= 0
            val amountText = if (isPositive) {
                "+$currencySymbol${formatDouble2Decimals(userBalance)}"
            } else {
                "$currencySymbol${formatDouble2Decimals(-userBalance)}"
            }
            
            val chipText = if (isPositive) "You get" else "You owe"
            val memberCount = groupMembers.size
            
            GroupData(
                title = group.name,
                amount = amountText,
                chip = chipText,
                color = group.color,
                positive = isPositive,
                members = "$memberCount members"
            )
        }.take(5) // Show only first 5 groups
    }
    
    // Header with "View all" button
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Group Highlights",
            style = AppStyleDesignSystem.Typography.HEADLINE,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Row(
            modifier = Modifier.clickable { 
                onViewAllClick() 
            },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "View all",
                style = AppStyleDesignSystem.Typography.CALL_OUT,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View all",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_SMALL)
            )
        }
    }
    
    Spacer(modifier = Modifier.height(AppStyleDesignSystem.Padding.MEDIUM))
    
    if (groupHighlights.isEmpty()) {
        // Show empty state
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppStyleDesignSystem.Padding.CARD_PADDING),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No groups yet",
                    style = AppStyleDesignSystem.Typography.HEADLINE,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(AppStyleDesignSystem.Padding.MEDIUM))
                Text(
                    text = "Create your first group to start splitting expenses",
                    style = AppStyleDesignSystem.Typography.CALL_OUT,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        // Group list - use Column with consistent spacing
        Column(
            verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.SMALL)
        ) {
            groupHighlights.forEach { group ->
                GroupItem(
                    title = group.title,
                    amount = group.amount,
                    chip = group.chip,
                    color = group.color,
                    positive = group.positive,
                    members = group.members
                )
            }
        }
    }
}

private fun formatDouble2Decimals(value: Double): String {
    val rounded = (value * 100.0).toLong()
    val integerPart = rounded / 100
    val decimalPart = (rounded % 100).toInt()
    return "$integerPart.${decimalPart.toString().padStart(2, '0')}"
}