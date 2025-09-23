@file:OptIn(ExperimentalMaterial3Api::class)

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.database.rememberSQLiteTransactionDatabase
import com.example.androidkmm.database.rememberSQLiteCategoryDatabase
import com.example.androidkmm.database.rememberSQLiteAccountDatabase
import com.example.androidkmm.database.rememberSQLiteSettingsDatabase
import com.example.androidkmm.models.AppSettings
import com.example.androidkmm.design.AppStyleDesignSystem
import com.example.androidkmm.models.Transaction
import com.example.androidkmm.utils.CurrencyUtils.formatDouble

// Import FilterOptions from FilterTransactionsBottomSheet

// Color definitions matching the search screen design
object SearchColors {
    val income = Color(0xFF10B981)
    val expense = Color(0xFFEF4444)
    val transfer = Color(0xFF3B82F6)
}

@Composable
fun SearchTransactionsScreen(
    onBackClick: () -> Unit,
    onCloseClick: () -> Unit,
    onTransactionClick: (Transaction) -> Unit,
    onNavigateToLedger: (String, String) -> Unit = { _, _ -> },
    initialFilters: FilterOptions = FilterOptions()
) {
    val transactionDatabaseManager = rememberSQLiteTransactionDatabase()
    val categoryDatabaseManager = rememberSQLiteCategoryDatabase()
    val accountDatabaseManager = rememberSQLiteAccountDatabase()
    val settingsDatabaseManager = rememberSQLiteSettingsDatabase()
    
    // Get currency symbol from settings
    val appSettings = settingsDatabaseManager.getAppSettings().collectAsState(initial = AppSettings())
    val currencySymbol = appSettings.value.currencySymbol
    
    val allTransactionsState = transactionDatabaseManager.getAllTransactions().collectAsState(initial = emptyList<Transaction>())
    val allTransactions = allTransactionsState.value
    
    var searchQuery by remember { mutableStateOf("") }
    var filterOptions by remember { mutableStateOf(initialFilters) }
    var showFilterBottomSheet by remember { mutableStateOf(false) }
    var filteredTransactions by remember { mutableStateOf(allTransactions) }
    var selectedTransactionForDetails by remember { mutableStateOf<Transaction?>(null) }
    
    // Update filtered transactions when search query, filter options, or all transactions change
    LaunchedEffect(searchQuery, filterOptions, allTransactions) {
        filteredTransactions = allTransactions.filter { transaction ->
            // Search filter
            val matchesSearch = searchQuery.isEmpty() || 
                transaction.title.contains(searchQuery, ignoreCase = true) ||
                transaction.description.contains(searchQuery, ignoreCase = true) ||
                transaction.category.contains(searchQuery, ignoreCase = true) ||
                transaction.account.contains(searchQuery, ignoreCase = true)
            
            // Transaction type filter
            val matchesType = filterOptions.transactionType == null || 
                transaction.type == filterOptions.transactionType
            
            // Category filter
            val matchesCategory = filterOptions.selectedCategories.isEmpty() || 
                filterOptions.selectedCategories.contains(transaction.category)
            
            // Account filter
            val matchesAccount = filterOptions.selectedAccounts.isEmpty() || 
                filterOptions.selectedAccounts.contains(transaction.account)
            
            // Date range filter
            val matchesDate = filterOptions.dateRange?.let { dateRange ->
                try {
                    when (dateRange.predefined) {
                        PredefinedDateRange.TODAY -> {
                            val today = java.time.LocalDate.now().toString()
                            transaction.date == today
                        }
                        PredefinedDateRange.THIS_WEEK -> {
                            val today = java.time.LocalDate.now()
                            val weekStart = today.minusDays(today.dayOfWeek.value.toLong() - 1)
                            val weekEnd = weekStart.plusDays(6)
                            val transactionDate = parseTransactionDate(transaction.date)
                            (transactionDate.isAfter(weekStart) || transactionDate.isEqual(weekStart)) && 
                            (transactionDate.isBefore(weekEnd) || transactionDate.isEqual(weekEnd))
                        }
                        PredefinedDateRange.THIS_MONTH -> {
                            val today = java.time.LocalDate.now()
                            val monthStart = today.withDayOfMonth(1)
                            val monthEnd = today.withDayOfMonth(today.lengthOfMonth())
                            val transactionDate = parseTransactionDate(transaction.date)
                            
                            // Use inclusive comparison: transaction date should be >= monthStart AND <= monthEnd
                            (transactionDate.isAfter(monthStart) || transactionDate.isEqual(monthStart)) && 
                            (transactionDate.isBefore(monthEnd) || transactionDate.isEqual(monthEnd))
                        }
                        PredefinedDateRange.LAST_3_MONTHS -> {
                            val today = java.time.LocalDate.now()
                            val threeMonthsAgo = today.minusMonths(3)
                            val transactionDate = parseTransactionDate(transaction.date)
                            (transactionDate.isAfter(threeMonthsAgo) || transactionDate.isEqual(threeMonthsAgo)) && 
                            (transactionDate.isBefore(today) || transactionDate.isEqual(today))
                        }
                        null -> {
                            // Custom date range
                            if (dateRange.from.isNotEmpty() && dateRange.to.isNotEmpty()) {
                                try {
                                    val fromDate = parseCustomDate(dateRange.from)
                                    val toDate = parseCustomDate(dateRange.to)
                                    val transactionDate = parseTransactionDate(transaction.date)
                                    (transactionDate.isAfter(fromDate) || transactionDate.isEqual(fromDate)) && 
                                    (transactionDate.isBefore(toDate) || transactionDate.isEqual(toDate))
                                } catch (e: Exception) {
                                    true // If date parsing fails, include the transaction
                                }
                            } else {
                                true // No custom date range specified
                            }
                        }
                    }
                } catch (e: Exception) {
                    true // If any date parsing fails, include the transaction
                }
            } ?: true // No date filter specified
            
            // Amount range filter
            val matchesAmount = filterOptions.amountRange?.let { amountRange ->
                when (amountRange.predefined) {
                    PredefinedAmountRange.UNDER_25 -> transaction.amount < 25.0
                    PredefinedAmountRange.BETWEEN_25_100 -> transaction.amount >= 25.0 && transaction.amount <= 100.0
                    PredefinedAmountRange.BETWEEN_100_500 -> transaction.amount >= 100.0 && transaction.amount <= 500.0
                    PredefinedAmountRange.OVER_500 -> transaction.amount > 500.0
                    null -> {
                        // Custom amount range
                        val minAmount = amountRange.min ?: Double.MIN_VALUE
                        val maxAmount = amountRange.max ?: Double.MAX_VALUE
                        transaction.amount >= minAmount && transaction.amount <= maxAmount
                    }
                }
            } ?: true // No amount filter specified
            
            matchesSearch && matchesType && matchesCategory && matchesAccount && matchesDate && matchesAmount
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // Header Section
        SearchHeader(
            onBackClick = onBackClick,
            onCloseClick = onCloseClick,
            resultCount = filteredTransactions.size
        )
        
        // Search and Filter Bar
        SearchAndFilterBar(
            searchQuery = searchQuery,
            onSearchQueryChange = { searchQuery = it },
            onFilterClick = { showFilterBottomSheet = true }
        )
        
        // Filter Summary
        FilterSummary(
            filterOptions = filterOptions,
            onClearFilters = { 
                filterOptions = FilterOptions()
            }
        )
        
        // Transaction List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredTransactions) { transaction ->
                SearchTransactionItem(
                    transaction = transaction,
                    currencySymbol = currencySymbol,
                    onClick = { selectedTransactionForDetails = transaction }
                )
            }
        }
    }
    
    // Filter Bottom Sheet
    FilterTransactionsBottomSheet(
        isVisible = showFilterBottomSheet,
        onDismiss = { showFilterBottomSheet = false },
        onApplyFilters = { newFilterOptions ->
            filterOptions = newFilterOptions
            showFilterBottomSheet = false
        },
        initialFilters = filterOptions
    )
    
    // Transaction Details Bottom Sheet
    selectedTransactionForDetails?.let { transaction ->
        TransactionDetailsBottomSheet(
            transaction = transaction,
            isVisible = true,
            onDismiss = { selectedTransactionForDetails = null },
            onEdit = { editedTransaction ->
                // Update the transaction in the database
                transactionDatabaseManager.updateTransaction(editedTransaction)
                selectedTransactionForDetails = null
            },
            onDelete = {
                // Delete the transaction from the database
                transactionDatabaseManager.deleteTransaction(transaction)
                selectedTransactionForDetails = null
            },
            onNavigateToLedger = { personName, transactionId -> onNavigateToLedger(personName, transactionId) },
            categoryDatabaseManager = categoryDatabaseManager,
            accountDatabaseManager = accountDatabaseManager
        )
    }
}

@Composable
private fun SearchHeader(
    onBackClick: () -> Unit,
    onCloseClick: () -> Unit,
    resultCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Arrow
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(20.dp)
            )
        }
        
        // Title and Results Count
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Search Transactions",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "$resultCount results found",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }
        
        // Close Icon
        IconButton(
            onClick = onCloseClick,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SearchAndFilterBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onFilterClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Search Input Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(
                    text = "Search by title, description,",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.outline,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                cursorColor = MaterialTheme.colorScheme.onBackground
            ),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            singleLine = true
        )
        
        // Filter Button
        IconButton(
            onClick = onFilterClick,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SearchTransactionItem(
    transaction: Transaction,
    currencySymbol: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM))
            .border(
                width = 0.5.dp, // very thin border
                color = Color.White.copy(alpha = 0.2f), // subtle white
                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
            ),
        shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Transaction Type Indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when (transaction.type) {
                            com.example.androidkmm.models.TransactionType.INCOME -> SearchColors.income
                            com.example.androidkmm.models.TransactionType.EXPENSE -> SearchColors.expense
                            com.example.androidkmm.models.TransactionType.TRANSFER -> SearchColors.transfer
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (transaction.type) {
                        com.example.androidkmm.models.TransactionType.INCOME -> Icons.Default.TrendingUp
                        com.example.androidkmm.models.TransactionType.EXPENSE -> Icons.Default.TrendingDown
                        com.example.androidkmm.models.TransactionType.TRANSFER -> Icons.Default.SwapHoriz
                    },
                    contentDescription = transaction.type.name,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Transaction Details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Title
                Text(
                    text = if (transaction.type == com.example.androidkmm.models.TransactionType.TRANSFER && 
                               transaction.transferTo != null && transaction.transferTo.isNotEmpty()) {
                        "${transaction.account} → ${transaction.transferTo}"
                    } else {
                        transaction.title
                    },
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Date & Category
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    Text(
                        text = formatDateForDisplay(transaction.date),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    
                    Text(
                        text = " • ",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                    
                    Text(
                        text = transaction.category,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Amount & Account
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Amount
                val amountColor = when (transaction.type) {
                    com.example.androidkmm.models.TransactionType.INCOME -> SearchColors.income
                    com.example.androidkmm.models.TransactionType.EXPENSE -> SearchColors.expense
                    com.example.androidkmm.models.TransactionType.TRANSFER -> SearchColors.transfer
                }
                
                val amountText = when (transaction.type) {
                    com.example.androidkmm.models.TransactionType.INCOME -> "+$currencySymbol${formatDouble(transaction.amount, 2)}"
                    com.example.androidkmm.models.TransactionType.EXPENSE -> "-$currencySymbol${formatDouble(transaction.amount, 2)}"
                    com.example.androidkmm.models.TransactionType.TRANSFER -> "$currencySymbol${formatDouble(transaction.amount, 2)}"
                }
                
                Text(
                    text = amountText,
                    color = amountColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Account
                Text(
                    text = transaction.account,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun formatDateForDisplay(dateString: String): String {
    if (dateString.isEmpty()) return "Today"
    
    return try {
        // Parse YYYY-MM-DD format and convert to MMM DD for display
        val parts = dateString.split("-")
        if (parts.size == 3) {
            val year = parts[0]
            val month = parts[1].toInt()
            val day = parts[2]
            
            val monthNames = listOf(
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
            )
            
            "${monthNames[month - 1]} $day"
        } else {
            dateString
        }
    } catch (e: Exception) {
        dateString
    }
}

@Composable
private fun FilterSummary(
    filterOptions: FilterOptions,
    onClearFilters: () -> Unit
) {
    val hasActiveFilters = filterOptions.transactionType != null ||
        filterOptions.selectedCategories.isNotEmpty() ||
        filterOptions.selectedAccounts.isNotEmpty() ||
        filterOptions.dateRange?.predefined != null ||
        filterOptions.amountRange?.predefined != null ||
        (filterOptions.dateRange?.from?.isNotEmpty() == true && filterOptions.dateRange?.to?.isNotEmpty() == true) ||
        (filterOptions.amountRange?.min != null || filterOptions.amountRange?.max != null)
    
    if (hasActiveFilters) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM))
                .border(
                    width = 0.5.dp, // very thin border
                    color = Color.White.copy(alpha = 0.2f), // subtle white
                    shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
                ),
            shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Active Filters",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = buildString {
                            if (filterOptions.transactionType != null) {
                                append("Type: ${filterOptions.transactionType.name}")
                            }
                            if (filterOptions.selectedCategories.isNotEmpty()) {
                                if (isNotEmpty()) append(", ")
                                append("Categories: ${filterOptions.selectedCategories.size}")
                            }
                            if (filterOptions.selectedAccounts.isNotEmpty()) {
                                if (isNotEmpty()) append(", ")
                                append("Accounts: ${filterOptions.selectedAccounts.size}")
                            }
                            if (filterOptions.dateRange?.predefined != null) {
                                if (isNotEmpty()) append(", ")
                                append("Date: ${filterOptions.dateRange!!.predefined!!.name}")
                            }
                            if (filterOptions.amountRange?.predefined != null) {
                                if (isNotEmpty()) append(", ")
                                append("Amount: ${filterOptions.amountRange!!.predefined!!.name}")
                            }
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp
                    )
                }
                
                TextButton(
                    onClick = onClearFilters,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onBackground
                    )
                ) {
                    Text(
                        text = "Clear All",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// Helper functions for date parsing
private fun parseTransactionDate(dateString: String): java.time.LocalDate {
    return try {
        // Try YYYY-MM-DD format first (database format)
        java.time.LocalDate.parse(dateString)
    } catch (e: Exception) {
        try {
            // Try MM/DD/YYYY format
            java.time.LocalDate.parse(dateString, java.time.format.DateTimeFormatter.ofPattern("M/d/yyyy"))
        } catch (e2: Exception) {
            try {
                // Try MM/DD/YYYY format with leading zeros
                java.time.LocalDate.parse(dateString, java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy"))
            } catch (e3: Exception) {
                // If all parsing fails, return today's date as fallback
                java.time.LocalDate.now()
            }
        }
    }
}

private fun parseCustomDate(dateString: String): java.time.LocalDate {
    return try {
        // Try YYYY-MM-DD format first
        java.time.LocalDate.parse(dateString)
    } catch (e: Exception) {
        try {
            // Try MM/DD/YYYY format
            java.time.LocalDate.parse(dateString, java.time.format.DateTimeFormatter.ofPattern("M/d/yyyy"))
        } catch (e2: Exception) {
            try {
                // Try MM/DD/YYYY format with leading zeros
                java.time.LocalDate.parse(dateString, java.time.format.DateTimeFormatter.ofPattern("MM/dd/yyyy"))
            } catch (e3: Exception) {
                // If all parsing fails, return today's date as fallback
                java.time.LocalDate.now()
            }
        }
    }
}
