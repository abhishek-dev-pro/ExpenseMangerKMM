package com.example.androidkmm.screens.insights.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.androidkmm.database.SQLiteTransactionDatabase
import com.example.androidkmm.database.rememberSQLiteSettingsDatabase
import com.example.androidkmm.models.AppSettings
import com.example.androidkmm.design.AppStyleDesignSystem
import com.example.androidkmm.models.TransactionType
import kotlinx.datetime.LocalDate

/**
 * Monthly summary card for insights overview
 * 
 * Displays key financial metrics for the selected month including
 * total income, expenses, and net balance.
 * 
 * @param currentMonth Currently selected month (1-12)
 * @param currentYear Currently selected year
 * @param transactionDatabaseManager Database manager for transaction data
 */
@Composable
fun MonthlySummaryCard(
    currentMonth: Int,
    currentYear: Int,
    transactionDatabaseManager: SQLiteTransactionDatabase?
) {
    val settingsDatabaseManager = rememberSQLiteSettingsDatabase()
    val appSettings by settingsDatabaseManager.getAppSettings().collectAsState(initial = AppSettings())
    val currencySymbol = appSettings.currencySymbol
    
    val allTransactions = if (transactionDatabaseManager != null) {
        transactionDatabaseManager.getAllTransactions().collectAsState(initial = emptyList()).value
    } else {
        emptyList()
    }
    
    val monthlyData = remember(allTransactions, currentMonth, currentYear) {
        val monthTransactions = allTransactions.filter { transaction ->
            val transactionDate = LocalDate.parse(transaction.date)
            transactionDate.monthNumber == currentMonth && 
            transactionDate.year == currentYear
        }
        
        val income = monthTransactions
            .filter { it.type == TransactionType.INCOME }
            .sumOf { it.amount }
        
        val expenses = monthTransactions
            .filter { it.type == TransactionType.EXPENSE }
            .sumOf { it.amount }
        
        val netBalance = income - expenses
        
        Triple(income, expenses, netBalance)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE) // iOS rounded corners
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppStyleDesignSystem.Padding.CARD_PADDING)
        ) {
            Text(
                text = "Monthly Summary",
                style = AppStyleDesignSystem.Typography.HEADLINE,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(AppStyleDesignSystem.Padding.MEDIUM))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Income
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Income",
                        style = AppStyleDesignSystem.Typography.FOOTNOTE,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$currencySymbol${formatDouble2Decimals(monthlyData.first)}",
                        style = AppStyleDesignSystem.Typography.BODY,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Expenses
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Expenses",
                        style = AppStyleDesignSystem.Typography.FOOTNOTE,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$currencySymbol${formatDouble2Decimals(monthlyData.second)}",
                        style = AppStyleDesignSystem.Typography.BODY,
                        color = MaterialTheme.colorScheme.error
                    )
                }
                
                // Net Balance
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Net",
                        style = AppStyleDesignSystem.Typography.FOOTNOTE,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$currencySymbol${formatDouble2Decimals(monthlyData.third)}",
                        style = AppStyleDesignSystem.Typography.BODY,
                        color = if (monthlyData.third >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
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
