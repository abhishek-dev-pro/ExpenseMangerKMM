package com.example.androidkmm.screens.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.utils.formatDouble
import com.example.androidkmm.design.DesignSystem

@Composable
fun TransactionItem(
    transaction: LedgerTransaction,
    balanceAtTransaction: Double,
    onDelete: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(DesignSystem.CornerRadius.md))
            .border(
                width = 0.5.dp, // very thin border
                color = Color.White.copy(alpha = 0.2f), // subtle white
                shape = RoundedCornerShape(DesignSystem.CornerRadius.md)
            ),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1A1A)
        ),
        shape = RoundedCornerShape(DesignSystem.CornerRadius.md)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Transaction Type Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (transaction.type == TransactionType.SENT) Color(0xFF2A1919) else Color(0xFF0F2419),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (transaction.type == TransactionType.SENT) {
                        Icons.Default.ArrowUpward
                    } else {
                        Icons.Default.ArrowDownward
                    },
                    contentDescription = null,
                    tint = if (transaction.type == TransactionType.SENT) LedgerTheme.redAmount else LedgerTheme.greenAmount,
                    modifier = Modifier.size(16.dp)
                )

            }

            Spacer(modifier = Modifier.width(16.dp))

            // Transaction Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = if (transaction.type == TransactionType.SENT) "You sent" else "You received",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = LedgerTheme.textPrimary
                )

                if (transaction.description.isNotEmpty()) {
                    Text(
                        text = transaction.description,
                        fontSize = 14.sp,
                        color = LedgerTheme.textSecondary
                    )
                }

                transaction.account?.let { account ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wallet,
                            contentDescription = null,
                            tint = LedgerTheme.textSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = account,
                            fontSize = 12.sp,
                            color = LedgerTheme.textSecondary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = LedgerTheme.textSecondary,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${transaction.date}, ${transaction.time}",
                        fontSize = 12.sp,
                        color = LedgerTheme.textSecondary
                    )
                }
            }

            // Amount and Edit Icon
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${if (transaction.type == TransactionType.SENT) "-" else "+"}$${formatDouble(transaction.amount)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (transaction.type == TransactionType.SENT) LedgerTheme.redAmount else LedgerTheme.greenAmount
                )
                Text(
                    text = "Balance: $${formatDouble(kotlin.math.abs(balanceAtTransaction))}",
                    fontSize = 12.sp,
                    color = LedgerTheme.textSecondary
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Action Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = { },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = LedgerTheme.textSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = LedgerTheme.redAmount,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
