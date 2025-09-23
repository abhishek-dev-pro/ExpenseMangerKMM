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
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.utils.CurrencyUtils.formatDouble
import com.example.androidkmm.design.iOSStyleDesignSystem
import com.example.androidkmm.database.rememberSQLiteSettingsDatabase
import com.example.androidkmm.models.AppSettings

@Composable
fun TransactionItem(
    transaction: LedgerTransaction,
    balanceAtTransaction: Double,
    onDelete: () -> Unit = {},
    onEdit: () -> Unit = {},
    isHighlighted: Boolean = false
) {
    // Get currency symbol from settings
    val settingsDatabaseManager = rememberSQLiteSettingsDatabase()
    val appSettings = settingsDatabaseManager.getAppSettings().collectAsState(initial = AppSettings())
    val currencySymbol = appSettings.value.currencySymbol
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(iOSStyleDesignSystem.CornerRadius.MEDIUM))
            .border(
                width = if (isHighlighted) 2.dp else 0.5.dp, // thicker border when highlighted
                color = if (isHighlighted) Color(0xFFE0E0E0) else Color.White.copy(alpha = 0.2f), // dark white border when highlighted
                shape = RoundedCornerShape(iOSStyleDesignSystem.CornerRadius.MEDIUM)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isHighlighted) Color(0xFF000000) else Color(0xFF1A1A1A)
        ),
        shape = RoundedCornerShape(iOSStyleDesignSystem.CornerRadius.MEDIUM)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Row 1: Money sent on left, -200 on right (big size)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
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
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (transaction.type == TransactionType.SENT) "Money sent" else "Money received",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = LedgerTheme.textPrimary()
                        )
                    }
                    
                    // Title directly below without spacing
                    if (transaction.description.isNotEmpty()) {
                        Text(
                            text = transaction.description,
                            fontSize = 14.sp,
                            color = LedgerTheme.textSecondary(),
                            modifier = Modifier.padding(start = 22.dp) // Align with text, not icon
                        )
                    }
                }
                
                Text(
                    text = "${if (transaction.type == TransactionType.SENT) "-" else "+"}$currencySymbol${formatDouble(transaction.amount)}",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (transaction.type == TransactionType.SENT) LedgerTheme.redAmount else LedgerTheme.greenAmount
                )
            }

            // Row 3: Date Time on left, balance on right (with gap)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Date section
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = LedgerTheme.textSecondary(),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = transaction.date,
                            fontSize = 12.sp,
                            color = LedgerTheme.textSecondary()
                        )
                    }
                    
                    // Time section
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = LedgerTheme.textSecondary(),
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = transaction.time,
                            fontSize = 12.sp,
                            color = LedgerTheme.textSecondary()
                        )
                    }
                }
                
                Text(
                    text = "Balance: $currencySymbol${formatDouble(kotlin.math.abs(balanceAtTransaction))}",
                    fontSize = 12.sp,
                    color = LedgerTheme.textSecondary()
                )
            }

            // Row 4: Account on left, edit and delete buttons on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Wallet,
                        contentDescription = null,
                        tint = LedgerTheme.textSecondary(),
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = transaction.account ?: "Cash",
                        fontSize = 12.sp,
                        color = LedgerTheme.textSecondary()
                    )
                }
                
                // Edit and Delete buttons on the right
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Edit button
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = Color(0xFF2196F3),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    
                    // Delete button
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
}
