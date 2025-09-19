package com.example.androidkmm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.database.rememberSQLiteTransactionDatabase
import com.example.androidkmm.database.rememberSQLiteCategoryDatabase
import com.example.androidkmm.database.rememberSQLiteAccountDatabase
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
    onViewAllClick: () -> Unit
) {
    val settingsDatabaseManager = rememberSQLiteSettingsDatabase()
    val appSettings by rememberSQLiteSettingsDatabase().getAppSettings().collectAsState(initial = AppSettings())
    val currencySymbol = appSettings.currencySymbol
    
    val transactionDatabaseManager = rememberSQLiteTransactionDatabase()
    var recentTransactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    
    // Fetch recent transactions with safety checks
    LaunchedEffect(Unit) {
        try {
            // Add a longer delay to ensure database is fully initialized
            kotlinx.coroutines.delay(500)
            
            // Check if database is ready
            transactionDatabaseManager.getAllTransactions().collect { allTransactions ->
                try {
                    // Sort by date and time (most recent first) and take first 3
                    recentTransactions = allTransactions
                        .filter { transaction ->
                            // Filter out any invalid transactions
                            transaction.id.isNotEmpty() && 
                            transaction.title.isNotEmpty() &&
                            transaction.date.isNotEmpty() &&
                            transaction.time.isNotEmpty()
                        }
                        .sortedWith(compareByDescending<Transaction> { it.date }.thenByDescending { it.time })
                        .take(3)
                    isLoading = false
                } catch (e: Exception) {
                    println("Error processing transactions: ${e.message}")
                    recentTransactions = emptyList()
                    isLoading = false
                }
            }
        } catch (e: Exception) {
            println("Error fetching recent transactions: ${e.message}")
            e.printStackTrace()
            recentTransactions = emptyList()
            isLoading = false
            hasError = true
        }
    }
    
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
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
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
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "View all",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (isLoading) {
            // Loading state
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Loading transactions...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (hasError) {
            // Error state
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Unable to load transactions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Please try again later",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else if (recentTransactions.isEmpty()) {
            // Empty state
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Receipt,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No recent transactions",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Your recent transactions will appear here",
                        fontSize = 14.sp,
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
                    RecentTransactionItem(transaction = transaction, currencySymbol = currencySymbol)
                }
            }
        }
    }
}

@Composable
private fun RecentTransactionItem(
transaction: Transaction,
currencySymbol: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon - more prominent like restaurant example
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(transaction.categoryColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = transaction.categoryIcon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Transaction details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = transaction.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Paid by ${transaction.account} • ${formatTransactionDate(transaction.date)}",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Amount and time
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = formatAmount(transaction.amount, transaction.type, currencySymbol),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when (transaction.type) {
                        TransactionType.INCOME -> Color(0xFF00A63E)
                        TransactionType.EXPENSE -> Color(0xFFEF4444)
                        TransactionType.TRANSFER -> Color(0xFF3B82F6)
                    }
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = transaction.time,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatAmount(amount: Double, type: TransactionType, currencySymbol: String): String {
    val formattedAmount = "%.2f".format(amount)
    return when (type) {
        TransactionType.INCOME -> "$currencySymbol$formattedAmount"
        TransactionType.EXPENSE -> "$currencySymbol$formattedAmount"
        TransactionType.TRANSFER -> "$currencySymbol$formattedAmount"
    }
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
