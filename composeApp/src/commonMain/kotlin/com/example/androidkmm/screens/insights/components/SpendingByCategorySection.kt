package com.example.androidkmm.screens.insights.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.androidkmm.database.SQLiteTransactionDatabase
import com.example.androidkmm.database.rememberSQLiteSettingsDatabase
import com.example.androidkmm.models.AppSettings
import com.example.androidkmm.design.AppStyleDesignSystem
import com.example.androidkmm.models.TransactionType
import kotlinx.datetime.LocalDate

/**
 * Spending by category section for insights overview
 * 
 * Displays spending breakdown by category to help users understand
 * where their money is going and identify spending patterns.
 * 
 * @param transactionDatabaseManager Database manager for transaction data
 * @param currentMonth Currently selected month (1-12)
 * @param currentYear Currently selected year
 * @param onCategoryClick Callback when a category is clicked for detailed view
 */
@Composable
fun SpendingByCategorySection(
    transactionDatabaseManager: SQLiteTransactionDatabase?,
    currentMonth: Int,
    currentYear: Int,
    onCategoryClick: (String) -> Unit
) {
    val settingsDatabaseManager = rememberSQLiteSettingsDatabase()
    val appSettings by settingsDatabaseManager.getAppSettings().collectAsState(initial = AppSettings())
    val currencySymbol = appSettings.currencySymbol
    
    val allTransactions = if (transactionDatabaseManager != null) {
        transactionDatabaseManager.getAllTransactions().collectAsState(initial = emptyList()).value
    } else {
        emptyList()
    }
    
    val categorySpending = remember(allTransactions, currentMonth, currentYear) {
        val monthTransactions = allTransactions.filter { transaction ->
            val transactionDate = LocalDate.parse(transaction.date)
            transactionDate.monthNumber == currentMonth && 
            transactionDate.year == currentYear &&
            transaction.type == TransactionType.EXPENSE
        }
        
        monthTransactions
            .groupBy { it.category }
            .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
            .toList()
            .sortedByDescending { it.second }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppStyleDesignSystem.Padding.LARGE)
        ) {
            Text(
                text = "Spending by Category",
                style = AppStyleDesignSystem.Typography.MAIN_PAGE_SUBHEADING_TITLE,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(AppStyleDesignSystem.Padding.MEDIUM))
            
            if (categorySpending.isEmpty()) {
                Text(
                    text = "No spending data for this month",
                    style = AppStyleDesignSystem.Typography.CALL_OUT,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                categorySpending.forEach { (category, amount) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = AppStyleDesignSystem.Padding.XS),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = category,
                            style = AppStyleDesignSystem.Typography.CALL_OUT,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$currencySymbol${String.format("%.2f", amount)}",
                            style = AppStyleDesignSystem.Typography.CALL_OUT.copy(
                                fontWeight = AppStyleDesignSystem.iOSFontWeights.medium
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
