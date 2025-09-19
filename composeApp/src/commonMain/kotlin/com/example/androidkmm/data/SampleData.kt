package com.example.androidkmm.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Group
import androidx.compose.ui.graphics.Color

/**
 * Sample data for the finance app
 */
object SampleData {

    val bills = listOf(
        BillData("Netflix", "Today", "-$15.99", Color.Red),
        BillData("Spotify", "Tomorrow", "-$9.99", Color.Green),
        BillData("Internet", "Sep 8", "-$79.99", Color.Blue),
        BillData("Electricity", "Sep 10", "-$45.50", Color.Magenta),
        BillData("Gym Membership", "Sep 12", "-$25.00", Color.Red),
        BillData("Amazon Prime", "Sep 15", "-$12.99", Color.Green),
    )


    val quickActions = listOf(
        QuickActionData("+ Ledger", Color(0xFF3B82F6), Icons.Default.ShowChart),
        QuickActionData("+ Transaction", Color(0xFF10B981), Icons.Default.Add),
    )

    val navigationItems = listOf(
        NavigationItem("Pal", Icons.Default.Home),
        NavigationItem("Transactions", Icons.Default.CurrencyExchange),
        NavigationItem("Ledger", Icons.Default.AccountBalance),
        NavigationItem("Insights", Icons.Default.Analytics),
        NavigationItem("Profile", Icons.Default.Person)
    )
}
