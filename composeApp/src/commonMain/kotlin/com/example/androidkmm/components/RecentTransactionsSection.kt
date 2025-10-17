package com.example.androidkmm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.androidkmm.design.AppStyleDesignSystem
import com.example.androidkmm.database.rememberSQLiteTransactionDatabase
import com.example.androidkmm.database.rememberSQLiteSettingsDatabase
import com.example.androidkmm.models.AppSettings
import com.example.androidkmm.models.Transaction
import com.example.androidkmm.models.TransactionType
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun RecentTransactionsSection(
    onViewAllClick: () -> Unit,
    onRetry: () -> Unit = {},
    refreshTrigger: Int = 0,
    onTransactionClick: (Transaction) -> Unit = {}
) {
    val settingsDatabaseManager = rememberSQLiteSettingsDatabase()
    val appSettings by rememberSQLiteSettingsDatabase().getAppSettings().collectAsState(initial = AppSettings())
    val currencySymbol = appSettings.currencySymbol
    
    val transactionDatabaseManager = rememberSQLiteTransactionDatabase()
    
    // Use collectAsState with the refresh trigger to force refresh
    val allTransactions by transactionDatabaseManager.getAllTransactions().collectAsState(initial = emptyList())
    
    // Debug logging
    LaunchedEffect(allTransactions, refreshTrigger) {
        println("DEBUG: RecentTransactionsSection - Received ${allTransactions.size} transactions (refresh trigger: $refreshTrigger)")
        allTransactions.take(5).forEach { transaction ->
            println("DEBUG: RecentTransactionsSection - Transaction: ${transaction.title} at ${transaction.time}")
        }
    }
    
    // Only show loading if we're actively fetching and have no data yet
    // For a fresh app start, empty list should show empty state, not loading
    val isLoading = false
    
    // Process transactions reactively
    val recentTransactions = remember(allTransactions) {
        val filtered = allTransactions
            .filter { transaction ->
                // Filter out any invalid transactions
                transaction.id.isNotEmpty() && 
                transaction.title.isNotEmpty() &&
                transaction.date.isNotEmpty() &&
                transaction.time.isNotEmpty() &&
                // Filter out account operation transactions
                transaction.category != "Account Operation"
            }
            .sortedWith(compareByDescending<Transaction> { it.date }.thenByDescending { it.time })
            .take(3)
        
        println("DEBUG: RecentTransactionsSection - Filtered to ${filtered.size} recent transactions")
        filtered
    }
    
    val hasError = false // Remove error state since we're using reactive state
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Header with "View all" button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Transactions",
                style = AppStyleDesignSystem.Typography.MAIN_PAGE_SUBHEADING_TITLE,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Row(
                modifier = Modifier.clickable { 
                    println("RecentTransactionsSection: View all clicked")
                    onViewAllClick() 
                },
                verticalAlignment = Alignment.CenterVertically
            ) {
                    Text(
                        text = "View all",
                        style = AppStyleDesignSystem.Typography.CALL_OUT.copy(
                            fontWeight = AppStyleDesignSystem.iOSFontWeights.medium
                        ),
                        color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View all",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_SMALL)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (isLoading) {
            // Loading state
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppStyleDesignSystem.Padding.XXL),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_LARGE),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Loading transactions...",
                            style = AppStyleDesignSystem.Typography.CALL_OUT,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (recentTransactions.isEmpty()) {
            // Empty state
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(AppStyleDesignSystem.Padding.XXL),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_BUTTON_SIZE)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No recent transactions",
                            style = AppStyleDesignSystem.Typography.BODY.copy(
                                fontWeight = AppStyleDesignSystem.iOSFontWeights.medium
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                    )
                        Text(
                            text = "Your recent transactions will appear here",
                            style = AppStyleDesignSystem.Typography.CALL_OUT,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            // Transaction list - use Column instead of LazyColumn to avoid nested scrollable components
            Column(
                verticalArrangement = Arrangement.spacedBy(0.8.dp)
            ) {
                recentTransactions.forEach { transaction ->
                    RecentTransactionItem(
                        transaction = transaction, 
                        currencySymbol = currencySymbol,
                        onClick = { onTransactionClick(transaction) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentTransactionItem(
    transaction: Transaction,
    currencySymbol: String,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppStyleDesignSystem.Padding.SMALL),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon - more prominent like restaurant example
            Box(
                modifier = Modifier
                    .size(AppStyleDesignSystem.Sizes.ICON_BUTTON_SIZE)
                    .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE))
                    .background(transaction.categoryColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = transaction.categoryIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_LARGE)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Transaction details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.title,
                    style = AppStyleDesignSystem.Typography.BODY.copy(
                        fontWeight = AppStyleDesignSystem.iOSFontWeights.medium
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = when (transaction.type) {
                        TransactionType.INCOME -> "Added to ${transaction.account} • ${formatTransactionDate(transaction.date)}"
                        TransactionType.EXPENSE -> "Paid by ${transaction.account} • ${formatTransactionDate(transaction.date)}"
                        TransactionType.TRANSFER -> "Transferred to ${transaction.account} • ${formatTransactionDate(transaction.date)}"
                    },
                    style = AppStyleDesignSystem.Typography.CALL_OUT,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Amount and time
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatAmount(transaction.amount, transaction.type, currencySymbol),
                    style = AppStyleDesignSystem.Typography.BODY.copy(
                        fontWeight = AppStyleDesignSystem.iOSFontWeights.semibold
                    ),
                    color = when (transaction.type) {
                        TransactionType.INCOME -> Color(0xFF00A63E)
                        TransactionType.EXPENSE -> Color(0xFFEF4444)
                        TransactionType.TRANSFER -> Color(0xFF3B82F6)
                    }
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = transaction.time,
                    style = AppStyleDesignSystem.Typography.CAPTION_1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatAmount(amount: Double, type: TransactionType, currencySymbol: String): String {
    val formattedAmount = formatDouble2Decimals(kotlin.math.abs(amount))
    return if (amount < 0) "-$currencySymbol$formattedAmount" else "$currencySymbol$formattedAmount"
}

private fun formatDouble2Decimals(value: Double): String {
    return String.format("%.2f", value)
}

@OptIn(ExperimentalTime::class)
private fun formatTransactionDate(date: String): String {
    return try {
        // Parse date and format it nicely
        val parts = date.split("-")
        if (parts.size == 3) {
            val day = parts[2].toInt()
            val month = parts[1].toInt()
            val year = parts[0].toInt()
            
            // Get current date for comparison
            val currentDate = kotlin.time.Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val currentDay = currentDate.dayOfMonth
            val currentMonth = currentDate.monthNumber
            val currentYear = currentDate.year
            
            when {
                day == currentDay && month == currentMonth && year == currentYear -> "Today"
                day == currentDay - 1 && month == currentMonth && year == currentYear -> "Yesterday"
                else -> {
                    val monthNames = arrayOf(
                        "", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                        "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
                    )
                    "${monthNames[month]} $day"
                }
            }
        } else {
            date
        }
    } catch (e: Exception) {
        date
    }
}
