package com.example.androidkmm.screens.transactions.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.androidkmm.models.Transaction
import com.example.androidkmm.models.TransactionType
import com.example.androidkmm.design.AppStyleDesignSystem
import com.example.androidkmm.utils.TextUtils

/**
 * Transaction item component for displaying individual transactions
 * 
 * Features:
 * - Transaction details display
 * - Type-specific styling
 * - Click handling
 * - Icon and color coding
 */
@Composable
fun TransactionItem(
    transaction: Transaction,
    onClick: (Transaction) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick(transaction) },
        colors = CardDefaults.cardColors(
            containerColor = Color.Gray
        ),
        shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppStyleDesignSystem.Padding.MEDIUM_LARGE),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Icon and details
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Category icon
                CategoryIcon(
                    icon = transaction.categoryIcon,
                    color = transaction.categoryColor,
                    type = transaction.type
                )
                
                Spacer(modifier = Modifier.width(AppStyleDesignSystem.Padding.MEDIUM))
                
                // Transaction details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    TextUtils.StandardText(
                        text = transaction.title,
                        fontSize = AppStyleDesignSystem.Typography.BODY.fontSize,
                        fontWeight = AppStyleDesignSystem.iOSFontWeights.medium,
                        color = Color.White
                    )
                    
                    TextUtils.StandardText(
                        text = transaction.category,
                        fontSize = AppStyleDesignSystem.Typography.CAPTION_1.fontSize,
                        color = Color.Gray
                    )
                    
                    if (transaction.description.isNotEmpty()) {
                        TextUtils.StandardText(
                            text = transaction.description,
                            fontSize = AppStyleDesignSystem.Typography.CAPTION_2.fontSize,
                            color = Color.Gray
                        )
                    }
                }
            }
            
            // Right side - Amount and type
            Column(
                horizontalAlignment = Alignment.End
            ) {
                AmountDisplay(
                    amount = transaction.amount,
                    type = transaction.type
                )
                
                TextUtils.StandardText(
                    text = transaction.date,
                    fontSize = AppStyleDesignSystem.Typography.CAPTION_2.fontSize,
                    color = Color.Gray
                )
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

/**
 * Category icon component
 */
@Composable
private fun CategoryIcon(
    icon: ImageVector,
    color: Color,
    type: TransactionType,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(AppStyleDesignSystem.Sizes.AVATAR_MEDIUM)
            .background(
                color = color.copy(alpha = 0.2f),
                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "Category",
            tint = color,
            modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_MEDIUM)
        )
    }
}

/**
 * Amount display component
 */
@Composable
private fun AmountDisplay(
    amount: Double,
    type: TransactionType,
    modifier: Modifier = Modifier
) {
    val (color, prefix) = when (type) {
        TransactionType.INCOME -> Color(0xFF00A63E) to "+"
        TransactionType.EXPENSE -> Color(0xFFEF4444) to "-"
        TransactionType.TRANSFER -> Color(0xFF3B82F6) to ""
    }
    
        TextUtils.StandardText(
            text = "$prefix$${formatDouble2Decimals(amount)}",
            fontSize = AppStyleDesignSystem.Typography.BODY.fontSize,
            fontWeight = AppStyleDesignSystem.iOSFontWeights.bold,
            color = color,
            modifier = modifier
        )
}

/**
 * Transaction type indicator
 */
@Composable
fun TransactionTypeIndicator(
    type: TransactionType,
    modifier: Modifier = Modifier
) {
    val (text, color) = when (type) {
        TransactionType.INCOME -> "Income" to Color(0xFF00A63E)
        TransactionType.EXPENSE -> "Expense" to Color(0xFFEF4444)
        TransactionType.TRANSFER -> "Transfer" to Color(0xFF3B82F6)
    }
    
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.2f)
        ),
        shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.SMALL)
    ) {
        TextUtils.StandardText(
            text = text,
            fontSize = AppStyleDesignSystem.Typography.CAPTION_2.fontSize,
            fontWeight = AppStyleDesignSystem.iOSFontWeights.medium,
            color = color,
            modifier = Modifier.padding(horizontal = AppStyleDesignSystem.Padding.SMALL, vertical = AppStyleDesignSystem.Padding.XS)
        )
    }
}
