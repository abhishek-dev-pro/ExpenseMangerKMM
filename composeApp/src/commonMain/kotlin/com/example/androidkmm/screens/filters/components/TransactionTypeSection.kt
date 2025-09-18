package com.example.androidkmm.screens.filters.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.models.TransactionType
import com.example.androidkmm.screens.filters.FilterColors

/**
 * Transaction type filter section
 * 
 * Allows users to filter transactions by type (All, Expenses, Income, Transfers).
 * Provides a clean interface for selecting transaction type filters.
 * 
 * @param selectedType Currently selected transaction type (null for all)
 * @param onTypeSelected Callback when a transaction type is selected
 */
@Composable
fun TransactionTypeSection(
    selectedType: TransactionType?,
    onTypeSelected: (TransactionType?) -> Unit
) {
    Column {
        Text(
            text = "Transaction Type",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // All Transactions
            FilterOptionCard(
                icon = Icons.Default.AttachMoney,
                iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                title = "All Transactions",
                subtitle = "Show all transactions",
                isSelected = selectedType == null,
                onClick = { onTypeSelected(null) }
            )
            
            // Expenses Only
            FilterOptionCard(
                icon = Icons.Default.TrendingDown,
                iconColor = FilterColors.expense,
                title = "Expenses Only",
                subtitle = "Show only expenses",
                isSelected = selectedType == TransactionType.EXPENSE,
                onClick = { onTypeSelected(TransactionType.EXPENSE) }
            )
            
            // Income Only
            FilterOptionCard(
                icon = Icons.Default.TrendingUp,
                iconColor = FilterColors.income,
                title = "Income Only",
                subtitle = "Show only income",
                isSelected = selectedType == TransactionType.INCOME,
                onClick = { onTypeSelected(TransactionType.INCOME) }
            )
            
            // Transfers Only
            FilterOptionCard(
                icon = Icons.Default.SwapHoriz,
                iconColor = FilterColors.transfer,
                title = "Transfers Only",
                subtitle = "Show only transfers",
                isSelected = selectedType == TransactionType.TRANSFER,
                onClick = { onTypeSelected(TransactionType.TRANSFER) }
            )
        }
    }
}
