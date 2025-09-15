package com.example.androidkmm.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.androidkmm.components.BalanceCard
import com.example.androidkmm.components.BillItem
import com.example.androidkmm.components.GreetingSection
import com.example.androidkmm.components.GroupItem
import com.example.androidkmm.components.ProgressCard
import com.example.androidkmm.components.QuickActions
import com.example.androidkmm.components.RecentTransactionsSection
import com.example.androidkmm.components.SectionHeader
import com.example.androidkmm.design.DesignSystem
import com.example.androidkmm.data.SampleData
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
                        "+ Ledger" -> onNavigateToLedger()
                        "+ Transaction" -> onNavigateToAddTransaction()
                    }
                }
            )
        }
        item { RecentTransactionsSection(onViewAllClick = onNavigateToTransactions) }
        item { ProgressCard() }
        item { UpcomingBills() }
        item { GroupHighlights() }

        item { Spacer(Modifier.height(DesignSystem.Spacing.bottomNavHeight)) }
    }
}




/**
 * Upcoming bills section
 */
@Composable
private fun UpcomingBills() {
    SectionHeader("Upcoming Bills")
    Spacer(Modifier.height(DesignSystem.Spacing.sm))

    SampleData.bills.forEachIndexed { index, bill ->
        var visible by remember { mutableStateOf(false) }

        // Staggered animation
        LaunchedEffect(Unit) {
            delay(index * 100L) // each item appears after a delay
            visible = true
        }

        AnimatedVisibility(
            visible = visible,
            enter = slideInVertically(
                initialOffsetY = { it } // slide in from bottom
            ) + fadeIn()
        ) {
            Column {
                BillItem(
                    title = bill.title,
                    subtitle = bill.subtitle,
                    amount = bill.amount,
                    color = bill.color
                )
                Spacer(Modifier.height(DesignSystem.Spacing.sm))
            }
        }
    }
}


/**
 * Group highlights section
 */
@Composable
private fun GroupHighlights() {
    SectionHeader("Group Highlights")
    Spacer(Modifier.height(DesignSystem.Spacing.sm))
    SampleData.groups.forEach { group ->
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
