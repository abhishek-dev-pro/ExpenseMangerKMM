package com.example.androidkmm.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.components.BalanceCard
import com.example.androidkmm.components.GreetingSection
import com.example.androidkmm.components.GroupItem
import com.example.androidkmm.components.ProgressCard
import com.example.androidkmm.components.QuickActions
import com.example.androidkmm.components.RecentTransactionsSection
import com.example.androidkmm.components.SectionHeader
import com.example.androidkmm.design.DesignSystem
import com.example.androidkmm.data.GroupData
import com.example.androidkmm.utils.CardUtils
import com.example.androidkmm.database.rememberSQLiteGroupDatabase
import com.example.androidkmm.database.rememberSQLiteSettingsDatabase
import com.example.androidkmm.models.AppSettings
import com.example.androidkmm.models.Group
import com.example.androidkmm.models.GroupMember
import com.example.androidkmm.models.GroupExpense
import kotlinx.coroutines.delay

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
    onNavigateToAddTransaction: () -> Unit = {}
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = DesignSystem.Spacing.safeAreaPadding),
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.lg)
    ) {
        item { Spacer(Modifier.height(DesignSystem.Spacing.lg)) }

        item { GreetingSection() }
        item { BalanceCard() }
        item { 
            QuickActions(
                onActionClick = { action ->
                    when (action) {
                        "Ledger" -> onNavigateToLedger()
                        "Transaction" -> onNavigateToAddTransaction()
                    }
                }
            )
        }
        item { RecentTransactionsSection(onViewAllClick = onNavigateToTransactions) }
//        item { GroupHighlights(onViewAllClick = onNavigateToGroups) }
//        item { ProgressCard() }

        item { Spacer(Modifier.height(DesignSystem.Spacing.bottomNavHeight)) }
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
                "+$currencySymbol${String.format("%.2f", userBalance)}"
            } else {
                "$currencySymbol${String.format("%.2f", -userBalance)}"
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
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
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
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View all",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
    
    Spacer(modifier = Modifier.height(12.dp))
    
    if (groupHighlights.isEmpty()) {
        // Show empty state
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No groups yet",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Create your first group to start splitting expenses",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    } else {
        // Group list - use Column with consistent spacing
        Column(
            verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.xs)
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
