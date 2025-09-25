package com.example.androidkmm.data

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color

/**
 * Sample data for the finance app
 */
object SampleData {


    val quickActions = listOf(
        QuickActionData("Ledger", Color(0xFF3B82F6), Icons.Default.ShowChart),
        QuickActionData("Transaction", Color(0xFF10B981), Icons.Default.Add),
    )

    val navigationItems = listOf(
        NavigationItem("Home", Icons.Default.Home),
        NavigationItem("Transactions", Icons.Default.CurrencyExchange),
        NavigationItem("Ledger", Icons.Default.AccountBalance),
        NavigationItem("Insights", Icons.Default.Analytics),
        NavigationItem("Profile", Icons.Default.Person)
    )
}
