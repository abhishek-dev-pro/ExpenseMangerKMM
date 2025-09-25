package com.example.androidkmm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.database.rememberSQLiteAccountDatabase
import com.example.androidkmm.database.rememberSQLiteLedgerDatabase
import com.example.androidkmm.database.rememberSQLiteSettingsDatabase
import com.example.androidkmm.database.rememberSQLiteTransactionDatabase
import com.example.androidkmm.design.AppStyleDesignSystem
import com.example.androidkmm.models.AppSettings
import com.example.androidkmm.models.Transaction
import com.example.androidkmm.models.TransactionType
import com.example.androidkmm.utils.CurrencyUtils.removeCurrencySymbols
import com.example.androidkmm.utils.Logger
import com.example.androidkmm.utils.TextUtils
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.format.DateTimeFormatter

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
    val transactionDatabaseManager = rememberSQLiteTransactionDatabase()
    val accountsState = accountDatabaseManager.getAllAccounts().collectAsState(initial = emptyList())
    val ledgerPersonsState = ledgerDatabaseManager.getAllLedgerPersons().collectAsState(initial = emptyList())
    val accounts = accountsState.value
    val ledgerPersons = ledgerPersonsState.value
    
    // Get today's transactions for today's change calculation
    val today = LocalDate.now()
    val todayString = today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    
    // Use a more explicit state management for real-time updates
    var todaysTransactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    
    // Force refresh the data periodically and on database changes
    LaunchedEffect(todayString) {
        println("DEBUG: LaunchedEffect triggered for date: $todayString")
        
        // Initial load
        val initialTransactions = transactionDatabaseManager.getTransactionsByDateRange(todayString, todayString).first()
        println("DEBUG: Initial load - received ${initialTransactions.size} transactions for $todayString")
        todaysTransactions = initialTransactions
        
        // Then listen for changes
        transactionDatabaseManager.getTransactionsByDateRange(todayString, todayString).collect { transactions ->
            println("DEBUG: Database updated - received ${transactions.size} transactions for $todayString")
            todaysTransactions = transactions
            transactions.forEach { transaction ->
                println("DEBUG: Transaction: ${transaction.title}, Type: ${transaction.type}, Amount: ${transaction.amount}, Date: ${transaction.date}")
            }
        }
    }
    
    // Add a manual refresh mechanism as backup
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(2000) // Refresh every 2 seconds
            println("DEBUG: Manual refresh triggered")
            try {
                val refreshedTransactions = transactionDatabaseManager.getTransactionsByDateRange(todayString, todayString).first()
                println("DEBUG: Manual refresh - received ${refreshedTransactions.size} transactions")
                if (refreshedTransactions != todaysTransactions) {
                    println("DEBUG: Manual refresh - data changed, updating state")
                    todaysTransactions = refreshedTransactions
                }
            } catch (e: Exception) {
                println("DEBUG: Manual refresh failed: ${e.message}")
            }
        }
    }
    
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
                // Debug: Print all ledger person balances
                println("DEBUG: All ledger persons:")
                validPersons.forEach { person ->
                    println("DEBUG: Person: ${person.name}, Balance: ${person.balance}")
                }
                
                // Debug: Test scenario - simulate user's case
                println("DEBUG: === TEST SCENARIO ===")
                println("DEBUG: User says: To Receive = 500, To Send = 1000")
                println("DEBUG: Expected net: 500 - 1000 = -500")
                println("DEBUG: But app shows: +500")
                println("DEBUG: === END TEST SCENARIO ===")
                
                // Calculate amounts with proper validation
                val toReceiveAmount = validPersons
                    .filter { it.balance > 0 }
                    .sumOf { it.balance }
                
                val toSendAmount = validPersons
                    .filter { it.balance < 0 }
                    .sumOf { kotlin.math.abs(it.balance) }
                
                println("DEBUG: === CALCULATION BREAKDOWN ===")
                println("DEBUG: toReceiveAmount (positive balances): $toReceiveAmount")
                println("DEBUG: toSendAmount (abs of negative balances): $toSendAmount")
                println("DEBUG: Formula: toSendAmount - toReceiveAmount (FIXED)")
                println("DEBUG: Calculation: $toSendAmount - $toReceiveAmount")
                
                // Apply formula: to send - to receive (FIXED)
                // Positive means you owe money, negative means you're owed money
                val netAmount = toSendAmount - toReceiveAmount
                
                println("DEBUG: Ledger calculation - toReceive: $toReceiveAmount, toSend: $toSendAmount, net: $netAmount")
                
                // Validate the result
                val finalAmount = if (netAmount.isNaN() || netAmount.isInfinite()) {
                    println("WARNING: Invalid ledger net amount calculated: $netAmount")
                    Logger.warning("Invalid ledger net amount calculated: $netAmount", "BalanceCard")
                    0.0
                } else {
                    netAmount
                }
                
                println("DEBUG: netAmount: $netAmount, finalAmount: $finalAmount")
                println("DEBUG: finalAmount >= 0: ${finalAmount >= 0}")
                
                val sign = if (finalAmount >= 0) "+" else ""
                val formattedAmount = "${sign}$currencySymbol${String.format("%.1f", finalAmount)}"
                println("DEBUG: sign: '$sign', formattedAmount: $formattedAmount")
                formattedAmount
            }
        } catch (e: Exception) {
            println("ERROR: Exception calculating ledger balance: ${e.message}")
            Logger.error("Exception calculating ledger balance", "BalanceCard", e)
            "$currencySymbol.0"
        }
    }
    
    // Calculate today's change (incoming and outgoing)
    val todaysChange = remember(todaysTransactions, currencySymbol) {
        println("DEBUG: Today's date: $todayString")
        println("DEBUG: Found ${todaysTransactions.size} transactions for today")
        todaysTransactions.forEach { transaction ->
            println("DEBUG: Transaction: ${transaction.title}, Type: ${transaction.type}, Amount: ${transaction.amount}, Date: ${transaction.date}")
        }
        
        var incoming = 0.0
        var outgoing = 0.0
        
        todaysTransactions.forEach { transaction ->
            when (transaction.type) {
                TransactionType.INCOME -> {
                    incoming += transaction.amount
                    println("DEBUG: Added to incoming: ${transaction.amount}, total incoming: $incoming")
                }
                TransactionType.EXPENSE -> {
                    outgoing += transaction.amount
                    println("DEBUG: Added to outgoing: ${transaction.amount}, total outgoing: $outgoing")
                }
                TransactionType.TRANSFER -> {
                    // For transfers, we don't count them as incoming/outgoing
                    println("DEBUG: Transfer transaction ignored: ${transaction.title}")
                }
            }
        }
        
        println("DEBUG: Final calculation - Incoming: $incoming, Outgoing: $outgoing")
        Pair(incoming, outgoing)
    }
    
    val displayBalance = totalBalance ?: calculatedTotalBalance
    val displayMonthlyChange = monthlyChange ?: if (netLedgerAmount != "${currencySymbol}0.0") "$netLedgerAmount in ledger" else "+${currencySymbol}0.0 this month"
    Column {
        // Main balance card
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
                .padding(AppStyleDesignSystem.Padding.CARD_PADDING)
        ) {
            Column {
                TextUtils.StandardText(
                    text = "Total Balance",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = AppStyleDesignSystem.Typography.TITLE_2.fontSize,
                    fontWeight = AppStyleDesignSystem.iOSFontWeights.regular
                )
                Spacer(Modifier.height(AppStyleDesignSystem.Padding.MEDIUM))
                if (isLoading.value) {
                    // Show loading indicator
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        androidx.compose.material3.CircularProgressIndicator(
                            modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_MEDIUM),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        TextUtils.StandardText(
                            text = "Loading...",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = AppStyleDesignSystem.Typography.TITLE_2.fontSize,
                            fontWeight = AppStyleDesignSystem.iOSFontWeights.light
                        )
                    }
                } else {
                    TextUtils.StandardText(
                        text = if (isBalanceVisible) displayBalance else "••••••",
                        color = Color.White,
                        fontSize = AppStyleDesignSystem.Typography.TITLE_2.fontSize,
                        fontWeight = AppStyleDesignSystem.iOSFontWeights.light
                    )
                    Spacer(Modifier.height(AppStyleDesignSystem.Padding.XS))
                    TextUtils.StandardText(
                        text = if (isBalanceVisible) displayMonthlyChange else "•••••• in ledger",
                        color = Color(0xFF9FFFA5),
                        fontSize = AppStyleDesignSystem.Typography.FOOTNOTE.fontSize
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
                        .size(AppStyleDesignSystem.Sizes.ICON_SIZE_MEDIUM)
                        .clickable { isBalanceVisible = !isBalanceVisible }
                )
            }
        }
        
        // Today's Change section - protruding from the balance card
        if (!isLoading.value) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = Color(0xFF2D2D2D),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(bottomStart = 10.dp, bottomEnd = 10.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                // Check if amounts are very large (more than 6 digits) to determine layout
                val incomingAmount = todaysChange.first
                val outgoingAmount = todaysChange.second
                val isLargeAmount = incomingAmount >= 1000000 || outgoingAmount >= 1000000
                
                if (isLargeAmount) {
                    // Two-line layout for large amounts - keep label in center
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Incoming amount (green with arrow down)
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Incoming",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBalanceVisible) "$currencySymbol${String.format("%.2f", incomingAmount)}" else "••••",
                                color = Color(0xFF4CAF50),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        // Today's Overview label in the center (two lines)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Today's",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = "Overview",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        // Outgoing amount (red with arrow up)
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBalanceVisible) "$currencySymbol${String.format("%.2f", outgoingAmount)}" else "••••",
                                color = Color(0xFFF44336),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Outgoing",
                                tint = Color(0xFFF44336),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                } else {
                    // Single-line layout for normal amounts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Incoming amount (green with arrow down)
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Incoming",
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBalanceVisible) "$currencySymbol${String.format("%.2f", incomingAmount)}" else "••••",
                                color = Color(0xFF4CAF50),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        
                        // Today's Overview label in the center (single line)
                        Text(
                            text = "Today's Overview",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        
                        // Outgoing amount (red with arrow up)
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isBalanceVisible) "$currencySymbol${String.format("%.2f", outgoingAmount)}" else "••••",
                                color = Color(0xFFF44336),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowUp,
                                contentDescription = "Outgoing",
                                tint = Color(0xFFF44336),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
