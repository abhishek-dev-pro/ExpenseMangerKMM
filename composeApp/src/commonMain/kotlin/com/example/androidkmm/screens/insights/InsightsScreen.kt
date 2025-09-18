package com.example.androidkmm.screens.insights

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.androidkmm.database.SQLiteTransactionDatabase
import com.example.androidkmm.design.DesignSystem
import com.example.androidkmm.screens.insights.components.OverviewTab
import com.example.androidkmm.utils.Logger

/**
 * Insights screen for financial analysis
 * 
 * Provides comprehensive financial insights and analytics including:
 * - Monthly overview with income/expense summaries
 * - Spending patterns and trends
 * - Category-wise breakdowns
 * - Savings rate analysis
 * - Smart recommendations
 * 
 * @param transactionDatabaseManager Database manager for transaction data access
 */
@Composable
fun InsightsScreen(transactionDatabaseManager: SQLiteTransactionDatabase) {
    Logger.debug("InsightsScreen initialized", "InsightsScreen")
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = DesignSystem.Spacing.safeAreaPadding)
    ) {
        item { Spacer(Modifier.height(DesignSystem.Spacing.lg)) }
        
        item {
            OverviewTab(
                transactionDatabaseManager = transactionDatabaseManager,
                onCategoryClick = { category, month, year ->
                    Logger.debug("Category clicked: $category for $month/$year", "InsightsScreen")
                    // TODO: Navigate to category detail screen
                }
            )
        }
        
        item { Spacer(Modifier.height(DesignSystem.Spacing.lg)) }
    }
}
