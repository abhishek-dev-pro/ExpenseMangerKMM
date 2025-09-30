package com.example.androidkmm.screens.transactions.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.androidkmm.models.Transaction
import com.example.androidkmm.models.DayGroup
import com.example.androidkmm.design.AppStyleDesignSystem
import com.example.androidkmm.utils.TextUtils

/**
 * Main content component for transaction list
 * 
 * Features:
 * - Grouped transaction display
 * - Empty state handling
 * - Loading state
 * - Error state
 */
@Composable
fun TransactionListContent(
    dayGroups: List<DayGroup>,
    isLoading: Boolean = false,
    error: String? = null,
    onTransactionClick: (Transaction) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        isLoading -> {
            LoadingState(modifier = modifier)
        }
        error != null -> {
            ErrorState(
                error = error,
                onRetry = onRetry,
                modifier = modifier
            )
        }
        dayGroups.isEmpty() -> {
            EmptyState(modifier = modifier)
        }
        else -> {
            TransactionList(
                dayGroups = dayGroups,
                onTransactionClick = onTransactionClick,
                modifier = modifier
            )
        }
    }
}

/**
 * Transaction list with grouped items
 */
@Composable
private fun TransactionList(
    dayGroups: List<DayGroup>,
    onTransactionClick: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(AppStyleDesignSystem.Padding.SCREEN_HORIZONTAL),
        verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.SMALL)
    ) {
        dayGroups.forEach { dayGroup ->
            item {
                DayGroupHeader(
                    date = dayGroup.date,
                    totalAmount = dayGroup.income + dayGroup.expense
                )
            }
            
            items(dayGroup.transactions) { transaction ->
                TransactionItem(
                    transaction = transaction,
                    onClick = onTransactionClick,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * Day group header component
 */
@Composable
private fun DayGroupHeader(
    date: String,
    totalAmount: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = AppStyleDesignSystem.Padding.SMALL),
        colors = CardDefaults.cardColors(
            containerColor = Color.Gray
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppStyleDesignSystem.Padding.CARD_PADDING),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextUtils.StandardText(
                text = date,
                fontSize = AppStyleDesignSystem.Typography.BODY.fontSize,
                fontWeight = AppStyleDesignSystem.iOSFontWeights.semibold,
                color = Color.White
            )
            
            TextUtils.StandardText(
                text = "$${formatDouble2Decimals(totalAmount)}",
                fontSize = AppStyleDesignSystem.Typography.BODY.fontSize,
                fontWeight = AppStyleDesignSystem.iOSFontWeights.semibold,
                color = Color.Gray
            )
        }
    }
}

private fun formatDouble2Decimals(value: Double): String {
    val rounded = (value * 100.0).toLong()
    val integerPart = rounded / 100
    val decimalPart = (rounded % 100).toInt()
    return "$integerPart.${decimalPart.toString().padStart(2, '0')}"
}

/**
 * Loading state component
 */
@Composable
private fun LoadingState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = Color.Blue
            )
            Spacer(modifier = Modifier.height(AppStyleDesignSystem.Padding.MEDIUM))
            TextUtils.StandardText(
                text = "Loading transactions...",
                fontSize = AppStyleDesignSystem.Typography.BODY.fontSize,
                color = Color.Gray
            )
        }
    }
}

/**
 * Error state component
 */
@Composable
private fun ErrorState(
    error: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextUtils.StandardText(
                text = "Error loading transactions",
                fontSize = AppStyleDesignSystem.Typography.HEADLINE.fontSize,
                fontWeight = AppStyleDesignSystem.iOSFontWeights.semibold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(AppStyleDesignSystem.Padding.SMALL))
            TextUtils.StandardText(
                text = error,
                fontSize = AppStyleDesignSystem.Typography.BODY.fontSize,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(AppStyleDesignSystem.Padding.MEDIUM))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Blue
                )
            ) {
                TextUtils.StandardText(
                    text = "Retry",
                    fontSize = AppStyleDesignSystem.Typography.BODY.fontSize,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Empty state component
 */
@Composable
private fun EmptyState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextUtils.StandardText(
                text = "No transactions found",
                fontSize = AppStyleDesignSystem.Typography.HEADLINE.fontSize,
                fontWeight = AppStyleDesignSystem.iOSFontWeights.semibold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(AppStyleDesignSystem.Padding.SMALL))
            TextUtils.StandardText(
                text = "Start by adding your first transaction",
                fontSize = AppStyleDesignSystem.Typography.BODY.fontSize,
                color = Color.Gray
            )
        }
    }
}
