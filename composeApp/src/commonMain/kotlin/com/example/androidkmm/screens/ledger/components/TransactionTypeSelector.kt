package com.example.androidkmm.screens.ledger.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.androidkmm.design.AppStyleDesignSystem
import com.example.androidkmm.screens.ledger.TransactionType
import com.example.androidkmm.screens.ledger.LedgerTheme

@Composable
fun TransactionTypeSelector(
    currentTransactionType: TransactionType,
    onTransactionTypeChanged: (TransactionType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_MEDIUM)
    ) {
        // You Sent Button
        Card(
            modifier = Modifier
                .weight(1f)
                .wrapContentHeight()
                .clickable { onTransactionTypeChanged(TransactionType.SENT) }
                .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM))
                .border(
                    width = AppStyleDesignSystem.Sizes.BORDER_THIN, // very thin border
                    color = Color.White.copy(alpha = 0.2f), // subtle white
                    shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (currentTransactionType == TransactionType.SENT) Color(0xFF0F2419) else MaterialTheme.colorScheme.surfaceVariant
            ),
            border = if (currentTransactionType == TransactionType.SENT) BorderStroke(AppStyleDesignSystem.Sizes.BORDER_THICK, LedgerTheme.greenAmount) else null,
            shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppStyleDesignSystem.Padding.MEDIUM_LARGE),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(AppStyleDesignSystem.Sizes.ICON_SIZE_XL)
                        .background(
                            if (currentTransactionType == TransactionType.SENT) LedgerTheme.greenAmount else MaterialTheme.colorScheme.onSurfaceVariant,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (currentTransactionType == TransactionType.SENT) Color.White else Color.Black,
                        modifier = Modifier.size(AppStyleDesignSystem.Padding.MEDIUM_LARGE)
                    )
                }

                Spacer(modifier = Modifier.height(AppStyleDesignSystem.Padding.ARRANGEMENT_TINY))

                Text(
                    text = "You Sent",
                    fontSize = AppStyleDesignSystem.Typography.FOOTNOTE.fontSize,
                    fontWeight = FontWeight.Medium,
                    color = if (currentTransactionType == TransactionType.SENT) LedgerTheme.greenAmount else LedgerTheme.textSecondary()
                )
                Text(
                    text = "Money you sent",
                    fontSize = AppStyleDesignSystem.Typography.CAPTION_2.fontSize,
                    color = LedgerTheme.textSecondary()
                )
            }
        }

        // You Received Button
        Card(
            modifier = Modifier
                .weight(1f)
                .wrapContentHeight()
                .clickable { onTransactionTypeChanged(TransactionType.RECEIVED) }
                .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM))
                .border(
                    width = AppStyleDesignSystem.Sizes.BORDER_THIN, // very thin border
                    color = Color.White.copy(alpha = 0.2f), // subtle white
                    shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (currentTransactionType == TransactionType.RECEIVED) Color(0xFF2A1919) else MaterialTheme.colorScheme.surfaceVariant
            ),
            border = if (currentTransactionType == TransactionType.RECEIVED) BorderStroke(AppStyleDesignSystem.Sizes.BORDER_THICK, LedgerTheme.redAmount) else null,
            shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(AppStyleDesignSystem.Padding.MEDIUM_LARGE),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(AppStyleDesignSystem.Sizes.ICON_SIZE_XL)
                        .background(
                            if (currentTransactionType == TransactionType.RECEIVED) LedgerTheme.redAmount else MaterialTheme.colorScheme.onSurfaceVariant,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (currentTransactionType == TransactionType.RECEIVED) Color.White else Color.Black,
                        modifier = Modifier.size(AppStyleDesignSystem.Padding.MEDIUM_LARGE)
                    )
                }

                Spacer(modifier = Modifier.height(AppStyleDesignSystem.Padding.ARRANGEMENT_TINY))

                Text(
                    text = "You Received",
                    fontSize = AppStyleDesignSystem.Typography.FOOTNOTE.fontSize,
                    fontWeight = FontWeight.Medium,
                    color = if (currentTransactionType == TransactionType.RECEIVED) LedgerTheme.redAmount else LedgerTheme.textSecondary()
                )
                Text(
                    text = "Money you received",
                    fontSize = AppStyleDesignSystem.Typography.CAPTION_2.fontSize,
                    color = LedgerTheme.textSecondary()
                )
            }
        }
    }
}
