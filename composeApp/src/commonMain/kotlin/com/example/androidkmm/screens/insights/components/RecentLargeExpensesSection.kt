package com.example.androidkmm.screens.insights.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.database.SQLiteTransactionDatabase
import com.example.androidkmm.database.rememberSQLiteSettingsDatabase
import com.example.androidkmm.models.AppSettings
import com.example.androidkmm.design.AppStyleDesignSystem
import com.example.androidkmm.models.TransactionType
import kotlinx.datetime.LocalDate

/**
 * Recent large expenses section for insights overview
 * 
 * Displays the largest expense transactions for the selected month
 * to help users identify significant spending patterns.
 * 
 * @param transactionDatabaseManager Database manager for transaction data
 * @param currentMonth Currently selected month (1-12)
 * @param currentYear Currently selected year
 * @param monthlyIncome Total monthly income for context
 */
@Composable
fun RecentLargeExpensesSection(
    transactionDatabaseManager: SQLiteTransactionDatabase?,
    currentMonth: Int,
    currentYear: Int,
    monthlyIncome: Double
) {
    val settingsDatabaseManager = rememberSQLiteSettingsDatabase()
    val appSettings by settingsDatabaseManager.getAppSettings().collectAsState(initial = AppSettings())
    val currencySymbol = appSettings.currencySymbol
    
    val allTransactions = if (transactionDatabaseManager != null) {
        transactionDatabaseManager.getAllTransactions().collectAsState(initial = emptyList()).value
    } else {
        emptyList()
    }
    
    val largeExpenses = remember(allTransactions, currentMonth, currentYear) {
        val monthTransactions = allTransactions.filter { transaction ->
            val transactionDate = LocalDate.parse(transaction.date)
            transactionDate.monthNumber == currentMonth && 
            transactionDate.year == currentYear &&
            transaction.type == TransactionType.EXPENSE
        }
        
        monthTransactions
            .sortedByDescending { it.amount }
            .take(5) // Show top 5 largest expenses
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
                text = "Recent Large Expenses",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(AppStyleDesignSystem.Padding.MEDIUM))
            
            if (largeExpenses.isEmpty()) {
                Text(
                    text = "No expenses found for this month",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                largeExpenses.forEach { expense ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = expense.title,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "$currencySymbol${formatDouble2Decimals(expense.amount)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
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
