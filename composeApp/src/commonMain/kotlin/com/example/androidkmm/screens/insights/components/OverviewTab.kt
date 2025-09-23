package com.example.androidkmm.screens.insights.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.androidkmm.database.SQLiteTransactionDatabase
import com.example.androidkmm.database.rememberSQLiteSettingsDatabase
import com.example.androidkmm.models.AppSettings
import com.example.androidkmm.design.AppStyleDesignSystem
import com.example.androidkmm.models.TransactionType
import com.example.androidkmm.utils.Logger
import kotlinx.datetime.LocalDate

/**
 * Overview tab for the insights screen
 * 
 * Displays a comprehensive overview of financial data including:
 * - Monthly income and expense summaries
 * - Recent large expenses
 * - Financial trends and charts
 * - Spending breakdown by category
 * - Savings rate analysis
 * - Smart insights and recommendations
 * 
 * The tab provides interactive month/year navigation and allows users to
 * drill down into specific categories for detailed analysis.
 * 
 * @param transactionDatabaseManager Database manager for transaction data access
 * @param onCategoryClick Callback when a category is clicked for detailed view
 */
@Composable
fun OverviewTab(
    transactionDatabaseManager: SQLiteTransactionDatabase? = null,
    currencySymbol: String = "$",
    onCategoryClick: (String, Int, Int) -> Unit = { _, _, _ -> }
) {
    val settingsDatabaseManager = rememberSQLiteSettingsDatabase()
    val appSettings by settingsDatabaseManager.getAppSettings().collectAsState(initial = AppSettings())
    val actualCurrencySymbol = if (currencySymbol == "$") appSettings.currencySymbol else currencySymbol
    
    var currentMonth by remember { mutableStateOf(9) } // September = 9
    var currentYear by remember { mutableStateOf(2025) }
    
    // Log overview tab initialization
    Logger.debug("OverviewTab initialized for month $currentMonth, year $currentYear", "OverviewTab")
    
    // Calculate monthly income for the current month
    val allTransactions = if (transactionDatabaseManager != null) {
        transactionDatabaseManager.getAllTransactions().collectAsState(initial = emptyList()).value
    } else {
        emptyList()
    }
    
    val monthlyIncome = remember(allTransactions, currentMonth, currentYear) {
        val monthTransactions = allTransactions.filter { transaction ->
            val transactionDate = LocalDate.parse(transaction.date)
            transactionDate.monthNumber == currentMonth && 
            transactionDate.year == currentYear
        }
        
        monthTransactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppStyleDesignSystem.Padding.SCREEN_HORIZONTAL)
    ) {
        item { Spacer(Modifier.height(AppStyleDesignSystem.Padding.SCREEN_VERTICAL)) }
        
        // Month Selector
        item {
            MonthSelectorCard(
                currentMonth = currentMonth,
                currentYear = currentYear,
                onPreviousMonth = {
                    if (currentMonth > 1) {
                        currentMonth--
                    } else {
                        currentMonth = 12
                        currentYear--
                    }
                },
                onNextMonth = {
                    if (currentMonth < 12) {
                        currentMonth++
                    } else {
                        currentMonth = 1
                        currentYear++
                    }
                }
            )
        }
        
        item { Spacer(Modifier.height(AppStyleDesignSystem.Padding.CONTENT_SPACING)) }
        
        // Monthly Summary
        item {
            MonthlySummaryCard(
                currentMonth = currentMonth,
                currentYear = currentYear,
                transactionDatabaseManager = transactionDatabaseManager
            )
        }
        
        item { Spacer(Modifier.height(AppStyleDesignSystem.Padding.CONTENT_SPACING)) }
        
        // Recent Large Expenses
        item {
            RecentLargeExpensesSection(
                transactionDatabaseManager = transactionDatabaseManager,
                currentMonth = currentMonth,
                currentYear = currentYear,
                monthlyIncome = monthlyIncome
            )
        }
        
        item { Spacer(Modifier.height(AppStyleDesignSystem.Padding.CONTENT_SPACING)) }
        
        // Financial Trends Chart
        item {
            FinancialTrendsSection(
                transactionDatabaseManager = transactionDatabaseManager,
                currentMonth = currentMonth,
                currentYear = currentYear
            )
        }
        
        item { Spacer(Modifier.height(AppStyleDesignSystem.Padding.CONTENT_SPACING)) }
        
        // Spending by Category
        item {
            SpendingByCategorySection(
                transactionDatabaseManager = transactionDatabaseManager,
                currentMonth = currentMonth,
                currentYear = currentYear,
                onCategoryClick = { category -> onCategoryClick(category, currentMonth, currentYear) }
            )
        }
        
        item { Spacer(Modifier.height(AppStyleDesignSystem.Padding.CONTENT_SPACING)) }
        
        // Savings Rate
        item {
            SavingsRateSection(
                transactionDatabaseManager = transactionDatabaseManager,
                currentMonth = currentMonth,
                currentYear = currentYear
            )
        }
        
        item { Spacer(Modifier.height(AppStyleDesignSystem.Padding.CONTENT_SPACING)) }
        
        // Smart Insights
        item {
            SmartInsightsSection(
                transactionDatabaseManager = transactionDatabaseManager,
                currentMonth = currentMonth,
                currentYear = currentYear
            )
        }
        
        item { Spacer(Modifier.height(AppStyleDesignSystem.Padding.SECTION_SPACING)) }
    }
}
