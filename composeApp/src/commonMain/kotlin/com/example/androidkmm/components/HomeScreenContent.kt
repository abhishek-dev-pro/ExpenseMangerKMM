package com.example.androidkmm.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.androidkmm.design.DesignSystem
import com.example.androidkmm.data.SampleData
import kotlinx.coroutines.delay

/**
 * Home screen content component
 */
@Composable
fun HomeScreenContent() {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = DesignSystem.Spacing.safeAreaPadding),
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.lg)
    ) {
        item { Spacer(Modifier.height(DesignSystem.Spacing.lg)) }

        item { GreetingSection() }
        item { BalanceCard() }
        item { QuickActions() }
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
