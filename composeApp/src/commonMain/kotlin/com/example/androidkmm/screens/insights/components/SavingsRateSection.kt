package com.example.androidkmm.screens.insights.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.database.SQLiteTransactionDatabase
import com.example.androidkmm.design.iOSStyleDesignSystem
import com.example.androidkmm.models.TransactionType
import kotlinx.datetime.LocalDate

/**
 * Savings rate section for insights overview
 * 
 * Displays the user's savings rate for the selected month to help
 * track financial health and savings goals.
 * 
 * @param transactionDatabaseManager Database manager for transaction data
 * @param currentMonth Currently selected month (1-12)
 * @param currentYear Currently selected year
 */
@Composable
fun SavingsRateSection(
    transactionDatabaseManager: SQLiteTransactionDatabase?,
    currentMonth: Int,
    currentYear: Int
) {
    val allTransactions = if (transactionDatabaseManager != null) {
        transactionDatabaseManager.getAllTransactions().collectAsState(initial = emptyList()).value
    } else {
        emptyList()
    }
    
    val savingsData = remember(allTransactions, currentMonth, currentYear) {
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
        
        val savings = income - expenses
        val savingsRate = if (income > 0) (savings / income) * 100 else 0.0
        
        Triple(income, expenses, savingsRate)
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(iOSStyleDesignSystem.CornerRadius.MEDIUM)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(iOSStyleDesignSystem.Padding.LARGE)
        ) {
            Text(
                text = "Savings Rate",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(iOSStyleDesignSystem.Padding.MEDIUM))
            
            // Savings rate display
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${String.format("%.1f", savingsData.third)}%",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (savingsData.third >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Savings Rate",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
