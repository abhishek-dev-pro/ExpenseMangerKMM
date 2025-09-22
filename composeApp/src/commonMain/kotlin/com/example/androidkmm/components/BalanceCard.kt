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
import com.example.androidkmm.database.rememberSQLiteSettingsDatabase
import com.example.androidkmm.models.AppSettings
import com.example.androidkmm.design.DesignSystem
import com.example.androidkmm.design.iOSStyleDesignSystem
import com.example.androidkmm.utils.TextUtils
import com.example.androidkmm.utils.removeCurrencySymbols
import com.example.androidkmm.utils.Logger
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.unit.dp

/**
 * Balance card component showing total balance and monthly change
 */
@Composable
fun BalanceCard(
    totalBalance: String? = null,
    monthlyChange: String? = null,
    isVisible: Boolean = true
) {
    val settingsDatabaseManager = rememberSQLiteSettingsDatabase()
    val appSettings by settingsDatabaseManager.getAppSettings().collectAsState(initial = AppSettings())
    val currencySymbol = appSettings.currencySymbol
    
    val accountDatabaseManager = rememberSQLiteAccountDatabase()
    val ledgerDatabaseManager = rememberSQLiteLedgerDatabase()
    val accountsState = accountDatabaseManager.getAllAccounts().collectAsState(initial = emptyList())
    val ledgerPersonsState = ledgerDatabaseManager.getAllLedgerPersons().collectAsState(initial = emptyList())
    val accounts = accountsState.value
    val ledgerPersons = ledgerPersonsState.value
    
    // Loading state - show loading when data is being fetched
    val isLoading = remember { mutableStateOf(true) }
    
    // Track if data has been loaded at least once
    var hasLoadedData by remember { mutableStateOf(false) }
    
    // Update loading state based on data availability
    LaunchedEffect(accounts, ledgerPersons) {
        if (accounts.isNotEmpty() || ledgerPersons.isNotEmpty()) {
            hasLoadedData = true
            isLoading.value = false
        } else if (hasLoadedData) {
            // If we had data before but now it's empty, it might be a real empty state
            isLoading.value = false
        }
        // If we haven't loaded data yet and both are empty, keep loading
    }
    
    // Add timeout to prevent infinite loading
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(5000) // 5 second timeout
        if (isLoading.value && !hasLoadedData) {
            println("DEBUG: BalanceCard loading timeout - showing empty state")
            isLoading.value = false
        }
    }
    
    // Toggle visibility state
    var isBalanceVisible by remember { mutableStateOf(isVisible) }
    
    // Calculate total balance from all accounts with improved data integrity
    val calculatedTotalBalance = remember(accounts, currencySymbol) {
        if (accounts.isNotEmpty()) {
            val total = accounts.sumOf { account ->
                try {
                    // Use the utility function for consistent currency symbol removal
                    val cleanBalance = removeCurrencySymbols(account.balance)
                    val parsedBalance = cleanBalance.toDoubleOrNull()
                    
                    if (parsedBalance == null) {
                        println("WARNING: Failed to parse account balance: '${account.balance}' for account: '${account.name}'")
                        Logger.warning("Failed to parse account balance: '${account.balance}'", "BalanceCard")
                        0.0
                    } else {
                        // Validate balance is reasonable (not NaN or infinite)
                        if (parsedBalance.isNaN() || parsedBalance.isInfinite()) {
                            println("WARNING: Invalid balance value: $parsedBalance for account: '${account.name}'")
                            Logger.warning("Invalid balance value: $parsedBalance", "BalanceCard")
                            0.0
                        } else {
                            parsedBalance
                        }
                    }
                } catch (e: Exception) {
                    println("ERROR: Exception parsing account balance: '${account.balance}' for account: '${account.name}': ${e.message}")
                    Logger.error("Exception parsing account balance", "BalanceCard", e)
                    0.0
                }
            }
            "$currencySymbol${String.format("%.2f", total)}"
        } else {
            "$currencySymbol.00"
        }
    }
    
    // Calculate net ledger amount with improved data integrity
    val netLedgerAmount = remember(ledgerPersons, currencySymbol) {
        try {
            // Validate ledger persons data
            val validPersons = ledgerPersons.filter { person ->
                !person.balance.isNaN() && !person.balance.isInfinite()
            }
            
            if (validPersons.isEmpty()) {
                "$currencySymbol.0"
            } else {
                // Calculate amounts with proper validation
                val toReceiveAmount = validPersons
                    .filter { it.balance > 0 }
                    .sumOf { it.balance }
                
                val toSendAmount = validPersons
                    .filter { it.balance < 0 }
                    .sumOf { kotlin.math.abs(it.balance) }
                
                // Apply formula: to receive - to send
                val netAmount = toReceiveAmount - toSendAmount
                
                // Validate the result
                val finalAmount = if (netAmount.isNaN() || netAmount.isInfinite()) {
                    println("WARNING: Invalid ledger net amount calculated: $netAmount")
                    Logger.warning("Invalid ledger net amount calculated: $netAmount", "BalanceCard")
                    0.0
                } else {
                    netAmount
                }
                
                val sign = if (finalAmount >= 0) "+" else ""
                "${sign}$currencySymbol${String.format("%.1f", finalAmount)}"
            }
        } catch (e: Exception) {
            println("ERROR: Exception calculating ledger balance: ${e.message}")
            Logger.error("Exception calculating ledger balance", "BalanceCard", e)
            "$currencySymbol.0"
        }
    }
    
    val displayBalance = totalBalance ?: calculatedTotalBalance
    val displayMonthlyChange = monthlyChange ?: if (netLedgerAmount != "${currencySymbol}0.0") "$netLedgerAmount in ledger" else "+${currencySymbol}0.0 this month"
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .background(
                brush = Brush.linearGradient(
                    listOf(Color(0xFF4C2EFF), Color(0xFF9F3DFF))
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp) // iOS rounded corners
            )
            .padding(iOSStyleDesignSystem.Padding.CARD_PADDING)
    ) {
        Column {
            TextUtils.StandardText(
                text = "Total Balance",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = iOSStyleDesignSystem.Typography.FOOTNOTE.fontSize,
                fontWeight = iOSStyleDesignSystem.iOSFontWeights.regular
            )
            Spacer(Modifier.height(iOSStyleDesignSystem.Padding.MEDIUM))
            if (isLoading.value) {
                // Show loading indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    androidx.compose.material3.CircularProgressIndicator(
                        modifier = Modifier.size(iOSStyleDesignSystem.Sizes.ICON_SIZE_MEDIUM),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    TextUtils.StandardText(
                        text = "Loading...",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = iOSStyleDesignSystem.Typography.TITLE_2.fontSize,
                        fontWeight = iOSStyleDesignSystem.iOSFontWeights.light
                    )
                }
            } else {
                TextUtils.StandardText(
                    text = if (isBalanceVisible) displayBalance else "••••••",
                    color = Color.White,
                    fontSize = iOSStyleDesignSystem.Typography.TITLE_2.fontSize,
                    fontWeight = iOSStyleDesignSystem.iOSFontWeights.light
                )
                Spacer(Modifier.height(iOSStyleDesignSystem.Padding.XS))
                TextUtils.StandardText(
                    text = if (isBalanceVisible) displayMonthlyChange else "•••••• in ledger",
                    color = Color(0xFF9FFFA5),
                    fontSize = iOSStyleDesignSystem.Typography.FOOTNOTE.fontSize
                )
            }
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
