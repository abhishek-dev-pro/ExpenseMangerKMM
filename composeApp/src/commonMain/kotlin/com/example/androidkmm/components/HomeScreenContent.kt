package com.example.androidkmm.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.androidkmm.design.DesignSystem
import com.example.androidkmm.data.SampleData

/**
 * Home screen content component
 */
@Composable
fun HomeScreenContent() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = DesignSystem.Spacing.safeAreaPadding)
    ) {
        Spacer(Modifier.height(DesignSystem.Spacing.lg))
        GreetingSection()
        Spacer(Modifier.height(DesignSystem.Spacing.lg))
        BalanceCard()
        Spacer(Modifier.height(DesignSystem.Spacing.lg))
        QuickActions()
        Spacer(Modifier.height(DesignSystem.Spacing.lg))
        ProgressCard()
        Spacer(Modifier.height(DesignSystem.Spacing.sectionSpacing))
        UpcomingBills()
        Spacer(Modifier.height(DesignSystem.Spacing.sectionSpacing))
        GroupHighlights()
        Spacer(Modifier.height(DesignSystem.Spacing.bottomNavHeight)) // bottom padding for navigation
    }
}

/**
 * Upcoming bills section
 */
@Composable
private fun UpcomingBills() {
    SectionHeader("Upcoming Bills")
    Spacer(Modifier.height(DesignSystem.Spacing.sm))
    SampleData.bills.forEach { bill ->
        BillItem(
            title = bill.title,
            subtitle = bill.subtitle,
            amount = bill.amount,
            color = bill.color
        )
        Spacer(Modifier.height(DesignSystem.Spacing.sm))
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
