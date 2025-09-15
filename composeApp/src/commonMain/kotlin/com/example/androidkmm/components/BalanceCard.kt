package com.example.androidkmm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.androidkmm.database.rememberSQLiteAccountDatabase
import com.example.androidkmm.database.rememberSQLiteLedgerDatabase
import com.example.androidkmm.design.DesignSystem
import com.example.androidkmm.utils.TextUtils
import androidx.compose.runtime.collectAsState

/**
 * Balance card component showing total balance and monthly change
 */
@Composable
fun BalanceCard(
    totalBalance: String? = null,
    monthlyChange: String? = null,
    isVisible: Boolean = true
) {
    val accountDatabaseManager = rememberSQLiteAccountDatabase()
    val ledgerDatabaseManager = rememberSQLiteLedgerDatabase()
    val accountsState = accountDatabaseManager.getAllAccounts().collectAsState(initial = emptyList())
    val ledgerPersonsState = ledgerDatabaseManager.getAllLedgerPersons().collectAsState(initial = emptyList())
    val accounts = accountsState.value
    val ledgerPersons = ledgerPersonsState.value
    
    // Toggle visibility state
    var isBalanceVisible by remember { mutableStateOf(isVisible) }
    
    // Calculate total balance from all accounts
    val calculatedTotalBalance = remember(accounts) {
        if (accounts.isNotEmpty()) {
            val total = accounts.sumOf { account ->
                try {
                    account.balance.replace("$", "").replace(",", "").toDoubleOrNull() ?: 0.0
                } catch (e: Exception) {
                    0.0
                }
            }
            "$${String.format("%.2f", total)}"
        } else {
            "$0.00"
        }
    }
    
    // Calculate net ledger amount (received - sent)
    val netLedgerAmount = remember(ledgerPersons) {
        val total = ledgerPersons.sumOf { person ->
            person.balance // This should be the net amount (received - sent)
        }
        if (total != 0.0) {
            val sign = if (total > 0) "+" else ""
            "${sign}$${String.format("%.1f", total)}"
        } else {
            "$0.0"
        }
    }
    
    val displayBalance = totalBalance ?: calculatedTotalBalance
    val displayMonthlyChange = monthlyChange ?: if (netLedgerAmount != "$0.0") "$netLedgerAmount in ledger" else "+$0.0 this month"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                brush = Brush.linearGradient(
                    listOf(Color(0xFF4C2EFF), Color(0xFF9F3DFF))
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(DesignSystem.CornerRadius.xl)
            )
            .padding(DesignSystem.Spacing.cardPadding)
    ) {
        Column {
            TextUtils.StandardText(
                text = "Total Balance",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = DesignSystem.Typography.balanceLabel,
                fontWeight = FontWeight.Normal
            )
            Spacer(Modifier.height(DesignSystem.Spacing.xl))
            TextUtils.StandardText(
                text = if (isBalanceVisible) displayBalance else "••••••",
                color = Color.White,
                fontSize = DesignSystem.Typography.balanceAmount,
                fontWeight = FontWeight.ExtraLight
            )
            Spacer(Modifier.height(DesignSystem.Spacing.xs))
            TextUtils.StandardText(
                text = if (isBalanceVisible) displayMonthlyChange else "•••••• in ledger",
                color = Color(0xFF9FFFA5),
                fontSize = DesignSystem.Typography.balanceSubtext
            )
        }
        Row(
            modifier = Modifier.align(Alignment.TopEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isBalanceVisible) Icons.Outlined.Visibility else Icons.Outlined.VisibilityOff,
                contentDescription = if (isBalanceVisible) "Hide balance" else "Show balance",
                tint = Color.White,
                modifier = Modifier
                    .size(DesignSystem.IconSize.md)
                    .clickable { isBalanceVisible = !isBalanceVisible }
            )
        }
    }
}
