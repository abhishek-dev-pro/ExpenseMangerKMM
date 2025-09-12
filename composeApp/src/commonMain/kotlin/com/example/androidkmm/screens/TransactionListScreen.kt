@file:OptIn(ExperimentalMaterial3Api::class)

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import com.example.androidkmm.models.Account
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.utils.formatDouble
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import com.example.androidkmm.database.rememberSQLiteTransactionDatabase
import com.example.androidkmm.database.rememberSQLiteCategoryDatabase
import com.example.androidkmm.database.rememberSQLiteAccountDatabase
import com.example.androidkmm.database.rememberSQLiteLedgerDatabase

// Color definitions matching the iOS design
object TransactionColors {
    val background = Color(0xFF000000)
    val surface = Color(0xFF1A1A1A)
    val primaryText = Color(0xFFFFFFFF)
    val secondaryText = Color(0xFF8E8E93)
    val income = Color(0xFF10B981)
    val expense = Color(0xFFEF4444)
    val transfer = Color(0xFF3B82F6)
    val cardBackground = Color(0xFF1C1C1E)
    val searchBackground = Color(0xFF1C1C1E)
    val divider = Color(0xFF38383A)
}

// Data classes
data class Transaction(
    val id: String,
    val title: String,
    val amount: Double,
    val category: String,
    val categoryIcon: ImageVector,
    val categoryColor: Color,
    val account: String,
    val transferTo: String? = null,
    val time: String,
    val type: TransactionType,
    val description: String = "",
    val date: String = "",
    // Added missing properties referenced in TransactionDetails
    val accountIcon: ImageVector = Icons.Default.CreditCard,
    val accountColor: Color = Color.Blue
)

enum class TransactionType {
    INCOME, EXPENSE, TRANSFER
}

data class DayGroup(
    val date: String,
    val displayDate: String,
    val transactions: List<com.example.androidkmm.models.Transaction>,
    val income: Double,
    val expense: Double
)

// Data classes for the form
data class TransactionFormData(
    val amount: String = "",
    val title: String = "",
    val category: TransactionCategory? = null,
    val account: Account? = null,
    val toAccount: Account? = null,
    val date: String = "",
    val time: String = "",
    val description: String = "",
    val type: TransactionType = TransactionType.EXPENSE
)

data class TransactionCategory(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val color: Color
)



@OptIn(ExperimentalTime::class)
@Composable
fun TransactionsScreen() {
    val transactionDatabaseManager = rememberSQLiteTransactionDatabase()
    val categoryDatabaseManager = rememberSQLiteCategoryDatabase()
    val accountDatabaseManager = rememberSQLiteAccountDatabase()
    val ledgerDatabaseManager = rememberSQLiteLedgerDatabase()
    
    val transactionsState = transactionDatabaseManager.getAllTransactions().collectAsState(initial = emptyList<com.example.androidkmm.models.Transaction>())
    val ledgerTransactionsState = ledgerDatabaseManager.getAllLedgerTransactions().collectAsState(initial = emptyList<com.example.androidkmm.screens.ledger.LedgerTransaction>())
    
    val allTransactions = remember(transactionsState.value, ledgerTransactionsState.value) {
        val regularTransactions = transactionsState.value
        val ledgerTransactions = ledgerTransactionsState.value.map { ledgerTransaction ->
            // Convert ledger transaction to regular transaction format
            com.example.androidkmm.models.Transaction(
                id = ledgerTransaction.id,
                title = ledgerTransaction.description.ifEmpty { 
                    if (ledgerTransaction.type == com.example.androidkmm.screens.ledger.TransactionType.SENT) "Sent to ${ledgerTransaction.personId}" 
                    else "Received from ${ledgerTransaction.personId}" 
                },
                amount = if (ledgerTransaction.type == com.example.androidkmm.screens.ledger.TransactionType.SENT) -ledgerTransaction.amount else ledgerTransaction.amount,
                category = if (ledgerTransaction.type == com.example.androidkmm.screens.ledger.TransactionType.SENT) "Transfer" else "Transfer",
                categoryIcon = Icons.Default.SwapHoriz,
                categoryColor = Color(0xFF3B82F6),
                account = ledgerTransaction.account ?: "Cash",
                accountIcon = Icons.Default.AttachMoney,
                accountColor = Color(0xFF4CAF50),
                transferTo = null,
                time = ledgerTransaction.time,
                type = if (ledgerTransaction.type == com.example.androidkmm.screens.ledger.TransactionType.SENT) com.example.androidkmm.models.TransactionType.EXPENSE else com.example.androidkmm.models.TransactionType.INCOME,
                description = ledgerTransaction.description,
                date = ledgerTransaction.date
            )
        }
        (regularTransactions + ledgerTransactions).sortedByDescending { it.date }
    }
    
    // Current month state management
    val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    var selectedMonth by remember { mutableStateOf(currentDate.monthNumber) }
    var selectedYear by remember { mutableStateOf(currentDate.year) }
    
    // Filter transactions for the selected month (for summary card only)
    val transactionsForSummary = remember(allTransactions, selectedMonth, selectedYear) {
        allTransactions.filter { transaction ->
            try {
                // Parse transaction date (assuming format like "2025-09-10")
                val parts = transaction.date.split("-")
                if (parts.size >= 2) {
                    val year = parts[0].toInt()
                    val month = parts[1].toInt()
                    year == selectedYear && month == selectedMonth
                } else {
                    false
                }
            } catch (e: Exception) {
                false
            }
        }
    }
    
    // Use all transactions for the transaction list (no filtering)
    val dayGroups = remember(allTransactions) { groupTransactionsByDay(allTransactions) }

    var showAddSheet by remember { mutableStateOf(false) }
    var showSearchScreen by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TransactionColors.background)
            .statusBarsPadding()
    ) {
        // Header Section
        TransactionHeader(onAddClick = { showAddSheet = true })

        // Month Navigation
        MonthNavigation(
            selectedMonth = selectedMonth,
            selectedYear = selectedYear,
            onPreviousMonth = {
                if (selectedMonth > 1) {
                    selectedMonth--
                } else {
                    selectedMonth = 12
                    selectedYear--
                }
            },
            onNextMonth = {
                if (selectedMonth < 12) {
                    selectedMonth++
                } else {
                    selectedMonth = 1
                    selectedYear++
                }
            }
        )

        // Summary Card
        SummaryCard(transactions = transactionsForSummary)

        // Search and Filter
        SearchAndFilter(
            onSearchClick = { showSearchScreen = true },
            onFilterClick = { showSearchScreen = true }
        )

        // Transaction List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(dayGroups) { dayGroup ->
                DayGroupSection(
                    dayGroup = dayGroup,
                    transactionDatabaseManager = transactionDatabaseManager,
                    categoryDatabaseManager = categoryDatabaseManager,
                    accountDatabaseManager = accountDatabaseManager
                )
            }
        }
    }

    // Show bottom sheet
    if (showAddSheet) {
        AddTransactionBottomSheet(
            onDismiss = { showAddSheet = false },
            onSave = { transactionFormData ->
                // Convert TransactionFormData to Transaction and save to database
                val transaction = com.example.androidkmm.models.Transaction(
                    id = "${Clock.System.now().epochSeconds}",
                    title = transactionFormData.title,
                    amount = transactionFormData.amount.toDoubleOrNull() ?: 0.0,
                    category = if (transactionFormData.type == TransactionType.TRANSFER) "Transfer" else (transactionFormData.category?.name ?: ""),
                    categoryIcon = if (transactionFormData.type == TransactionType.TRANSFER) Icons.Default.SwapHoriz else (transactionFormData.category?.icon ?: Icons.Default.Category),
                    categoryColor = if (transactionFormData.type == TransactionType.TRANSFER) Color(0xFF3B82F6) else (transactionFormData.category?.color ?: Color.Gray),
                    account = transactionFormData.account?.name ?: "",
                    transferTo = transactionFormData.toAccount?.name,
                    time = transactionFormData.time,
                    type = com.example.androidkmm.models.TransactionType.valueOf(transactionFormData.type.name),
                    description = transactionFormData.description,
                    date = transactionFormData.date,
                    accountIcon = transactionFormData.account?.icon ?: Icons.Default.AccountBalance,
                    accountColor = transactionFormData.account?.color ?: Color.Blue
                )
                
                transactionDatabaseManager.addTransactionWithBalanceUpdate(
                    transaction = transaction,
                    accountDatabaseManager = accountDatabaseManager
                )
                showAddSheet = false
            },
            categoryDatabaseManager = categoryDatabaseManager,
            accountDatabaseManager = accountDatabaseManager
        )
    }
    
    // Show Search Screen
    if (showSearchScreen) {
        SearchTransactionsScreen(
            onBackClick = { showSearchScreen = false },
            onCloseClick = { showSearchScreen = false },
            onTransactionClick = { /* Handled directly in SearchTransactionsScreen */ }
        )
    }
}

// Image Picker Dialog
@Composable
private fun ImagePickerDialog(
    onDismiss: () -> Unit,
    onImageSelected: (String) -> Unit
) {
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Select Image Source",
                    color = TransactionColors.primaryText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Camera option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // For now, simulate camera selection
                                onImageSelected("camera_image_path")
                                showDialog = false
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Camera",
                            modifier = Modifier.size(24.dp),
                            tint = TransactionColors.primaryText
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Take Photo",
                            color = TransactionColors.primaryText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Divider(color = TransactionColors.divider)
                    
                    // Gallery option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // For now, simulate gallery selection
                                onImageSelected("gallery_image_path")
                                showDialog = false
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Gallery",
                            modifier = Modifier.size(24.dp),
                            tint = TransactionColors.primaryText
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Choose from Gallery",
                            color = TransactionColors.primaryText,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Cancel",
                        color = TransactionColors.secondaryText
                    )
                }
            },
            containerColor = TransactionColors.background
        )
    }
}

@Composable
private fun TransactionHeader(onAddClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Transactions",
                color = TransactionColors.primaryText,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "6 transactions",
                color = TransactionColors.secondaryText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }

        FloatingActionButton(
            onClick = onAddClick,
            modifier = Modifier.size(56.dp),
            containerColor = TransactionColors.primaryText,
            contentColor = TransactionColors.background,
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Transaction",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun MonthNavigation(
    selectedMonth: Int,
    selectedYear: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val currentMonthYear = "${monthNames[selectedMonth - 1]} $selectedYear"
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPreviousMonth,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(TransactionColors.surface)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Previous Month",
                tint = TransactionColors.primaryText,
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = currentMonthYear,
            color = TransactionColors.primaryText,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        IconButton(
            onClick = onNextMonth,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(TransactionColors.surface)
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Next Month",
                tint = TransactionColors.primaryText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SummaryCard(transactions: List<com.example.androidkmm.models.Transaction>) {
    // Calculate totals from actual transaction data
    val totalIncome = transactions
        .filter { it.type == com.example.androidkmm.models.TransactionType.INCOME }
        .sumOf { it.amount }
    
    val totalExpense = transactions
        .filter { it.type == com.example.androidkmm.models.TransactionType.EXPENSE }
        .sumOf { it.amount }
    
    val total = totalIncome - totalExpense
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = TransactionColors.cardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryColumn(
                icon = Icons.Default.TrendingUp,
                iconColor = TransactionColors.income,
                amount = "$${formatDouble(totalIncome, 2)}",
                label = "Income",
                amountColor = TransactionColors.income
            )

            SummaryColumn(
                icon = Icons.Default.TrendingDown,
                iconColor = TransactionColors.expense,
                amount = "$${formatDouble(totalExpense, 2)}",
                label = "Expenses",
                amountColor = TransactionColors.expense
            )

            SummaryColumn(
                icon = Icons.Default.AttachMoney,
                iconColor = if (total >= 0) TransactionColors.income else TransactionColors.expense,
                amount = "${if (total >= 0) "+" else ""}$${formatDouble(total, 2)}",
                label = "Total",
                amountColor = if (total >= 0) TransactionColors.income else TransactionColors.expense
            )
        }
    }
}

@Composable
private fun SummaryColumn(
    icon: ImageVector,
    iconColor: Color,
    amount: String,
    label: String,
    amountColor: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(iconColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }

        Text(
            text = amount,
            color = amountColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = label,
            color = TransactionColors.secondaryText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SearchAndFilter(
    onSearchClick: () -> Unit,
    onFilterClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .clickable { onSearchClick() }
        ) {
            OutlinedTextField(
                value = "",
                onValueChange = { },
                placeholder = {
                    Text(
                        text = "Search transactions...",
                        color = TransactionColors.secondaryText
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = TransactionColors.secondaryText,
                        modifier = Modifier.size(20.dp)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = TransactionColors.searchBackground,
                    unfocusedContainerColor = TransactionColors.searchBackground,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = TransactionColors.primaryText,
                    unfocusedTextColor = TransactionColors.primaryText
                ),
                readOnly = true,
                enabled = false
            )
        }

        IconButton(
            onClick = onFilterClick,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(TransactionColors.searchBackground)
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter",
                tint = TransactionColors.primaryText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun DayGroupSection(
    dayGroup: DayGroup,
    transactionDatabaseManager: com.example.androidkmm.database.SQLiteTransactionDatabase,
    categoryDatabaseManager: com.example.androidkmm.database.SQLiteCategoryDatabase,
    accountDatabaseManager: com.example.androidkmm.database.SQLiteAccountDatabase
) {
    var selectedTransaction by remember { mutableStateOf<com.example.androidkmm.models.Transaction?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Day Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = dayGroup.displayDate,
                    color = TransactionColors.primaryText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "(${dayGroup.transactions.size})",
                    color = TransactionColors.primaryText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (dayGroup.income > 0) {
                    Text(
                        text = "+$${formatDouble(dayGroup.income, 2)}",
                        color = TransactionColors.income,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (dayGroup.expense > 0) {
                    Text(
                        text = "-$${formatDouble(dayGroup.expense, 2)}",
                        color = TransactionColors.expense,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Transactions
        dayGroup.transactions.forEach { transaction ->
            TransactionCard(transaction) { clickedTransaction ->
                selectedTransaction = clickedTransaction
                showBottomSheet = true
            }
        }
    }

    // Transaction Details Bottom Sheet
    selectedTransaction?.let { transaction ->
        TransactionDetailsBottomSheet(
            transaction = transaction,
            isVisible = showBottomSheet,
            onDismiss = {
                showBottomSheet = false
                selectedTransaction = null
            },
            onEdit = { editedTransaction ->
                // Update transaction in database with balance updates
                transactionDatabaseManager.updateTransactionWithBalanceUpdate(
                    oldTransaction = transaction,
                    newTransaction = editedTransaction,
                    accountDatabaseManager = accountDatabaseManager,
                    onSuccess = {
                        println("Transaction updated successfully: ${editedTransaction.title}")
                        showBottomSheet = false
                        selectedTransaction = null
                    },
                    onError = { error ->
                        println("Error updating transaction: ${error.message}")
                    }
                )
            },
            onDelete = {
                // Delete transaction from database with balance updates
                transactionDatabaseManager.deleteTransactionWithBalanceUpdate(
                    transaction = transaction,
                    accountDatabaseManager = accountDatabaseManager,
                    onSuccess = {
                        println("Transaction deleted successfully")
                        showBottomSheet = false
                        selectedTransaction = null
                    },
                    onError = { error ->
                        println("Error deleting transaction: ${error.message}")
                    }
                )
            },
            categoryDatabaseManager = categoryDatabaseManager,
            accountDatabaseManager = accountDatabaseManager
        )
    }
}

@Composable
fun TransactionCard(
    transaction: com.example.androidkmm.models.Transaction,
    onClick: (com.example.androidkmm.models.Transaction) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(transaction) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = TransactionColors.cardBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(transaction.categoryColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = transaction.categoryIcon,
                    contentDescription = transaction.category,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Transaction Details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = transaction.title,
                    color = TransactionColors.primaryText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = transaction.time,
                        color = TransactionColors.secondaryText,
                        fontSize = 14.sp
                    )

                    Text(
                        text = "•",
                        color = TransactionColors.secondaryText,
                        fontSize = 14.sp
                    )

                    Text(
                        text = transaction.category,
                        color = TransactionColors.secondaryText,
                        fontSize = 14.sp
                    )
                }
            }

            // Amount and Account
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                val amountColor = when (transaction.type) {
                    com.example.androidkmm.models.TransactionType.INCOME -> TransactionColors.income
                    com.example.androidkmm.models.TransactionType.EXPENSE -> TransactionColors.expense
                    com.example.androidkmm.models.TransactionType.TRANSFER -> TransactionColors.transfer
                }

                val amountText = when (transaction.type) {
                    com.example.androidkmm.models.TransactionType.INCOME -> "+${formatDouble(transaction.amount, 2)}"
                    com.example.androidkmm.models.TransactionType.EXPENSE -> "-${formatDouble(transaction.amount, 2)}"
                    com.example.androidkmm.models.TransactionType.TRANSFER -> "${formatDouble(transaction.amount, 2)}"
                }

                Text(
                    text = amountText,
                    color = amountColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                val accountText = if (transaction.type == com.example.androidkmm.models.TransactionType.TRANSFER && transaction.transferTo != null) {
                    "${transaction.account} → ${transaction.transferTo}"
                } else {
                    transaction.account
                }

                Text(
                    text = accountText,
                    color = TransactionColors.secondaryText,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun AddTransactionBottomSheet(
    onDismiss: () -> Unit,
    onSave: (TransactionFormData) -> Unit,
    categoryDatabaseManager: com.example.androidkmm.database.SQLiteCategoryDatabase,
    accountDatabaseManager: com.example.androidkmm.database.SQLiteAccountDatabase
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var formData by remember { mutableStateOf(TransactionFormData()) }
    var showCategorySheet by remember { mutableStateOf(false) }
    var showFromAccountSheet by remember { mutableStateOf(false) }
    var showToAccountSheet by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Set default date and time
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val currentDate = now.date
        val currentTime = now.time
        
        formData = formData.copy(
            date = "${currentDate.year}-${currentDate.monthNumber.toString().padStart(2, '0')}-${currentDate.dayOfMonth.toString().padStart(2, '0')}",
            time = "${currentTime.hour.toString().padStart(2, '0')}:${currentTime.minute.toString().padStart(2, '0')}"
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        containerColor = TransactionColors.background,
        contentColor = TransactionColors.primaryText,
        dragHandle = {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .background(
                        color = TransactionColors.secondaryText.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
        AddTransactionContent(
            formData = formData,
            onFormDataChange = { formData = it },
            onShowCategorySheet = { showCategorySheet = true },
            onShowFromAccountSheet = { showFromAccountSheet = true },
            onShowToAccountSheet = { showToAccountSheet = true },
            onShowDatePicker = { showDatePicker = true },
            onShowTimePicker = { showTimePicker = true },
            onSave = { onSave(formData) },
            onDismiss = onDismiss
        )
    }

    // Category Selection Sheet
    if (showCategorySheet) {
        CategorySelectionBottomSheet(
            onDismiss = { showCategorySheet = false },
            onCategorySelected = { category ->
                formData = formData.copy(category = category)
                showCategorySheet = false
            },
            categoryDatabaseManager = categoryDatabaseManager
        )
    }

    // From Account Selection Sheet
    if (showFromAccountSheet) {
        AccountSelectionBottomSheet(
            onDismiss = { showFromAccountSheet = false },
            title = "Select Account",
            subtitle = "Choose an account for your transaction",
            onAccountSelected = { account ->
                formData = formData.copy(account = account)
                showFromAccountSheet = false
            },
            accountDatabaseManager = accountDatabaseManager
        )
    }

    // To Account Selection Sheet
    if (showToAccountSheet) {
        AccountSelectionBottomSheet(
            onDismiss = { showToAccountSheet = false },
            title = "Select Account",
            subtitle = "Choose destination account",
            onAccountSelected = { account ->
                formData = formData.copy(toAccount = account)
                showToAccountSheet = false
            },
            accountDatabaseManager = accountDatabaseManager
        )
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismiss = { showDatePicker = false },
            onDateSelected = { dateString ->
                formData = formData.copy(date = dateString)
                showDatePicker = false
            }
        )
    }

    // Time Picker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onTimeSelected = { timeString ->
                formData = formData.copy(time = timeString)
                showTimePicker = false
            }
        )
    }
}

// Rest of the composables remain the same...
// (AddTransactionContent, TransactionTypeSelector, AmountInputSection, etc.)
// I'll include the key ones that were causing issues:

@Composable
private fun AddTransactionContent(
    formData: TransactionFormData,
    onFormDataChange: (TransactionFormData) -> Unit,
    onShowCategorySheet: () -> Unit,
    onShowFromAccountSheet: () -> Unit,
    onShowToAccountSheet: () -> Unit,
    onShowDatePicker: () -> Unit,
    onShowTimePicker: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    // Validation state
    var validationErrors by remember { mutableStateOf(emptyMap<String, String>()) }
    
    // Validation function
    fun validateForm(): Boolean {
        val errors = mutableMapOf<String, String>()
        
        // Validate title
        if (formData.title.isBlank()) {
            errors["title"] = "Title is required"
        }
        
        // Validate amount
        if (formData.amount.isBlank()) {
            errors["amount"] = "Amount is required"
        } else {
            val amountValue = formData.amount.toDoubleOrNull()
            if (amountValue == null || amountValue <= 0) {
                errors["amount"] = "Please enter a valid amount"
            }
        }
        
        // Validate category
        if (formData.category == null) {
            errors["category"] = "Category is required"
        }
        
        // Validate account
        if (formData.account == null) {
            errors["account"] = "Account is required"
        }
        
        // Validate transfer accounts for transfer type
        if (formData.type == TransactionType.TRANSFER) {
            if (formData.toAccount == null) {
                errors["transferTo"] = "Transfer to account is required"
            }
            if (formData.account?.name == formData.toAccount?.name) {
                errors["transferTo"] = "Transfer to account must be different from from account"
            }
        }
        
        validationErrors = errors
        return errors.isEmpty()
    }
    
    // Save function with validation
    fun handleSave() {
        if (validateForm()) {
            onSave()
        }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = TransactionColors.secondaryText,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Add Transaction",
                        color = TransactionColors.primaryText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    val subtitle = when (formData.type) {
                        TransactionType.EXPENSE -> "Track your expense"
                        TransactionType.INCOME -> "Track your income"
                        TransactionType.TRANSFER -> "Track your transfer"
                    }
                    Text(
                        text = subtitle,
                        color = TransactionColors.secondaryText,
                        fontSize = 14.sp
                    )
                }

                // Spacer instead of duplicate close button
                Spacer(modifier = Modifier.size(32.dp))
            }
        }

        item {
            // Transaction Type Selector
            TransactionTypeSelector(
                selectedType = formData.type,
                onTypeSelected = { type ->
                    onFormDataChange(formData.copy(type = type))
                }
            )
        }

        item {
            // Amount Input
            AmountInputSection(
                amount = formData.amount,
                onAmountChange = { amount ->
                    onFormDataChange(formData.copy(amount = amount))
                },
                errorMessage = validationErrors["amount"]
            )
        }

        item {
            // Account Selection - different for transfer vs others
            if (formData.type == TransactionType.TRANSFER) {
                // From Account and To Account for transfers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CategoryAccountSelector(
                        modifier = Modifier.weight(1f),
                        title = "From Account",
                        selectedText = formData.account?.name ?: "Select",
                        icon = formData.account?.icon ?: Icons.Default.CreditCard,
                        iconColor = formData.account?.color ?: TransactionColors.secondaryText,
                        onClick = onShowFromAccountSheet
                    )

                    CategoryAccountSelector(
                        modifier = Modifier.weight(1f),
                        title = "To Account",
                        selectedText = formData.toAccount?.name ?: "Select",
                        icon = formData.toAccount?.icon ?: Icons.Default.CreditCard,
                        iconColor = formData.toAccount?.color ?: TransactionColors.secondaryText,
                        onClick = onShowToAccountSheet
                    )
                }
            } else {
                // Category and Account for income/expense
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CategoryAccountSelector(
                        modifier = Modifier.weight(1f),
                        title = "Category",
                        selectedText = formData.category?.name ?: "Select",
                        icon = formData.category?.icon ?: Icons.Default.AttachMoney,
                        iconColor = formData.category?.color ?: TransactionColors.secondaryText,
                        onClick = onShowCategorySheet
                    )

                    CategoryAccountSelector(
                        modifier = Modifier.weight(1f),
                        title = "Account",
                        selectedText = formData.account?.name ?: "Select",
                        icon = formData.account?.icon ?: Icons.Default.CreditCard,
                        iconColor = formData.account?.color ?: TransactionColors.secondaryText,
                        onClick = onShowFromAccountSheet
                    )
                }
            }
        }

        item {
            // Title Input
            InputField(
                label = "Title",
                value = formData.title,
                onValueChange = { title ->
                    onFormDataChange(formData.copy(title = title))
                },
                placeholder = "e.g., Lunch at Subway",
                errorMessage = validationErrors["title"]
            )
        }

        item {
            // Date and Time
            Text(
                text = "Date & Time",
                color = TransactionColors.primaryText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DateTimeSelector(
                    modifier = Modifier.weight(1f),
                    value = if (formData.date.isEmpty()) "Today" else {
                        try {
                            val parts = formData.date.split("-")
                            if (parts.size == 3) {
                                val year = parts[0]
                                val month = parts[1]
                                val day = parts[2]
                                "$day/$month/$year"
                            } else {
                                formData.date
                            }
                        } catch (e: Exception) {
                            formData.date
                        }
                    },
                    icon = Icons.Default.DateRange,
                    isSelected = true,
                    onClick = onShowDatePicker
                )

                DateTimeSelector(
                    modifier = Modifier.weight(1f),
                    value = formData.time.ifEmpty { "01:31" },
                    icon = Icons.Default.Schedule,
                    isSelected = false,
                    onClick = onShowTimePicker
                )
            }
        }

        item {
            // Description Input
            InputField(
                label = "Description (Optional)",
                value = formData.description,
                onValueChange = { description ->
                    onFormDataChange(formData.copy(description = description))
                },
                placeholder = "e.g., we ate two each"
            )
        }

        item {
            // Receipt Upload
            ReceiptUploadSection()
        }

        item {
            // Save Button
            Button(
                onClick = { handleSave() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = TransactionColors.secondaryText.copy(alpha = 0.2f),
                    contentColor = TransactionColors.primaryText
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                val buttonText = when (formData.type) {
                    TransactionType.TRANSFER -> "Save Transfer"
                    else -> "Save Transaction"
                }
                Text(
                    text = buttonText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun TransactionTypeSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TransactionColors.surface)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        TransactionType.values().forEach { type ->
            val isSelected = selectedType == type
            val (icon, text) = when (type) {
                TransactionType.EXPENSE -> Icons.Default.TrendingDown to "Expense"
                TransactionType.INCOME -> Icons.Default.TrendingUp to "Income"
                TransactionType.TRANSFER -> Icons.Default.SwapHoriz to "Transfer"
            }

            Button(
                onClick = { onTypeSelected(type) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) TransactionColors.cardBackground
                    else Color.Transparent,
                    contentColor = if (isSelected) TransactionColors.primaryText
                    else TransactionColors.secondaryText
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun AmountInputSection(
    amount: String,
    onAmountChange: (String) -> Unit,
    errorMessage: String? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$",
                color = TransactionColors.primaryText,
                fontSize = 48.sp,
                fontWeight = FontWeight.Light
            )

            OutlinedTextField(
                value = amount,
                onValueChange = onAmountChange,
                textStyle = TextStyle(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.Center,
                    color = TransactionColors.primaryText
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = errorMessage != null,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = TransactionColors.primaryText,
                    focusedTextColor = TransactionColors.primaryText,
                    unfocusedTextColor = TransactionColors.primaryText,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    errorBorderColor = Color.Red,
                    errorContainerColor = Color.Transparent
                ),
                modifier = Modifier.width(200.dp)
            )
        }

        Text(
            text = "Enter amount",
            color = TransactionColors.secondaryText,
            fontSize = 17.sp
        )
        
        // Error message
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CategoryAccountSelector(
    modifier: Modifier = Modifier,
    title: String,
    selectedText: String,
    icon: ImageVector,
    iconColor: Color = TransactionColors.secondaryText,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            color = TransactionColors.primaryText,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = TransactionColors.secondaryText
            ),
            border = BorderStroke(
                width = 1.dp,
                color = TransactionColors.secondaryText.copy(alpha = 0.3f)
            ),
            contentPadding = PaddingValues(16.dp)
        ) {
            Row {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(20.dp),
                    tint = iconColor
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = selectedText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Start
                )
            }
        }
    }
}

@Composable
private fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    errorMessage: String? = null
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = label,
            color = TransactionColors.primaryText,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = {
                Text(
                    text = placeholder,
                    color = TransactionColors.secondaryText
                )
            },
            isError = errorMessage != null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = TransactionColors.cardBackground,
                unfocusedContainerColor = TransactionColors.cardBackground,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = TransactionColors.primaryText,
                unfocusedTextColor = TransactionColors.primaryText,
                errorBorderColor = Color.Red,
                errorContainerColor = TransactionColors.cardBackground
            )
        )
        
        // Error message
        if (errorMessage != null) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

@Composable
private fun DateTimeSelector(
    modifier: Modifier = Modifier,
    value: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) TransactionColors.primaryText
            else TransactionColors.cardBackground,
            contentColor = if (isSelected) TransactionColors.background
            else TransactionColors.primaryText
        ),
        contentPadding = PaddingValues(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ReceiptUploadSection(
    onReceiptSelected: (String) -> Unit = {}
) {
    var showImagePicker by remember { mutableStateOf(false) }
    var selectedImagePath by remember { mutableStateOf<String?>(null) }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Receipt (Optional)",
            color = TransactionColors.primaryText,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        if (selectedImagePath != null) {
            // Show selected image
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = TransactionColors.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Receipt Image",
                        modifier = Modifier.size(32.dp),
                        tint = TransactionColors.primaryText
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Receipt attached",
                            color = TransactionColors.primaryText,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Tap to change",
                            color = TransactionColors.secondaryText,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(
                        onClick = { selectedImagePath = null }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = TransactionColors.secondaryText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        } else {
            // Show upload button
        Button(
                onClick = { showImagePicker = true },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .border(
                    width = 2.dp,
                    brush = SolidColor(TransactionColors.secondaryText.copy(alpha = 0.3f)),
                    shape = RoundedCornerShape(12.dp)
                ),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = TransactionColors.secondaryText
            ),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Upload Receipt",
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Upload Receipt",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Images only",
                        fontSize = 12.sp,
                        color = TransactionColors.secondaryText
                    )
                }
            }
        }
    }

    // Image Picker Dialog
    if (showImagePicker) {
        ImagePickerDialog(
            onDismiss = { showImagePicker = false },
            onImageSelected = { imagePath ->
                selectedImagePath = imagePath
                onReceiptSelected(imagePath)
                showImagePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSelectionBottomSheet(
    onDismiss: () -> Unit,
    title: String = "Select Account",
    subtitle: String = "Choose an account for your transaction",
    onAccountSelected: (Account) -> Unit,
    accountDatabaseManager: com.example.androidkmm.database.SQLiteAccountDatabase
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val accountsState = accountDatabaseManager.getAllAccounts().collectAsState(initial = emptyList<Account>())
    val accounts = accountsState.value

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        containerColor = TransactionColors.background,
        contentColor = TransactionColors.primaryText,
        dragHandle = null
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(0.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                // Header with close button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.size(32.dp))

                    Text(
                        text = title,
                        color = TransactionColors.primaryText,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TransactionColors.primaryText,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            item {
                // Subtitle
                Text(
                    text = subtitle,
                    color = TransactionColors.secondaryText,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp),
                    textAlign = TextAlign.Center
                )
            }

            items(accounts) { account ->
                AccountCard(
                    account = account,
                    onClick = { onAccountSelected(account) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun AccountCard(
    account: Account,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = TransactionColors.cardBackground
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Dollar sign icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(TransactionColors.surface),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$",
                    color = TransactionColors.primaryText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Account details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = account.name,
                    color = TransactionColors.primaryText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Green arrow up icon
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = TransactionColors.income,
                        modifier = Modifier.size(16.dp)
                    )

                    Text(
                        text = account.balance,
                        color = TransactionColors.income,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategorySelectionBottomSheet(
    onDismiss: () -> Unit,
    onCategorySelected: (TransactionCategory) -> Unit,
    categoryDatabaseManager: com.example.androidkmm.database.SQLiteCategoryDatabase
) {
    val bottomSheetState = rememberModalBottomSheetState()
    val categoriesState = categoryDatabaseManager.getAllCategories().collectAsState(initial = emptyList<com.example.androidkmm.models.Category>())
    val categories = categoriesState.value.map { category ->
        TransactionCategory(
            id = category.id,
            name = category.name,
            icon = category.icon,
            color = category.color
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        containerColor = TransactionColors.background,
        contentColor = TransactionColors.primaryText,
        dragHandle = {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .background(
                        color = TransactionColors.secondaryText.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.size(32.dp))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Select Expense Category",
                            color = TransactionColors.primaryText,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Choose a category for your transaction",
                            color = TransactionColors.secondaryText,
                            fontSize = 14.sp
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TransactionColors.secondaryText,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            items(categories) { category ->
                CategoryCard(
                    modifier = Modifier.fillMaxWidth(),
                    category = category,
                    onClick = { onCategorySelected(category) }
                )
            }
        }
    }
}

@Composable
private fun CategoryCard(
    modifier: Modifier = Modifier,
    category: TransactionCategory,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .height(80.dp)
            .border(
                width = 1.dp,
                color = TransactionColors.secondaryText.copy(alpha = 0.3f),
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = TransactionColors.cardBackground
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(category.color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = category.name,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = category.name,
                color = TransactionColors.primaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

private fun getSampleTransactions(): List<Transaction> {
    return listOf(
        Transaction(
            id = "1",
            title = "Lunch at Subway",
            amount = 24.50,
            category = "Food & Dining",
            categoryIcon = Icons.Default.Restaurant,
            categoryColor = Color(0xFFFF8C00),
            account = "HDFC Debit Card",
            time = "10:55 PM",
            type = TransactionType.EXPENSE,
            description = "Delicious subway sandwich"
        ),
        Transaction(
            id = "2",
            title = "Monthly salary",
            amount = 2500.00,
            category = "Salary",
            categoryIcon = Icons.Default.AttachMoney,
            categoryColor = TransactionColors.income,
            account = "SBI Savings",
            time = "10:55 PM",
            type = TransactionType.INCOME,
            description = "Monthly salary payment"
        ),
        Transaction(
            id = "3",
            title = "New headphones",
            amount = 89.99,
            category = "Shopping",
            categoryIcon = Icons.Default.ShoppingCart,
            categoryColor = Color(0xFF9C27B0),
            account = "HDFC Debit Card",
            time = "10:55 PM",
            type = TransactionType.EXPENSE,
            description = "Wireless headphones"
        ),
        Transaction(
            id = "4",
            title = "Fund transfer",
            amount = 500.00,
            category = "Transfer",
            categoryIcon = Icons.Default.SwapHoriz,
            categoryColor = TransactionColors.transfer,
            account = "SBI Savings",
            transferTo = "HDFC Debit Card",
            time = "10:55 PM",
            type = TransactionType.TRANSFER,
            description = "Transfer between accounts"
        )
    )
}

private fun groupTransactionsByDay(transactions: List<com.example.androidkmm.models.Transaction>): List<DayGroup> {
    if (transactions.isEmpty()) {
        return emptyList()
    }
    
    // Group transactions by date
    val groupedByDate = transactions.groupBy { it.date }
    
    return groupedByDate.map { (date, dayTransactions) ->
        val displayDate = when (date) {
            "2025-09-10" -> "Today"
            "2025-09-09" -> "Yesterday"
            else -> date
        }
        
        DayGroup(
            date = date,
            displayDate = displayDate,
            transactions = dayTransactions,
            income = dayTransactions.filter { it.type == com.example.androidkmm.models.TransactionType.INCOME }.sumOf { it.amount },
            expense = dayTransactions.filter { it.type == com.example.androidkmm.models.TransactionType.EXPENSE }.sumOf { it.amount }
        )
    }.sortedByDescending { it.date }
}

private fun getSampleAccounts(): List<Account> {
    return listOf(
        Account(
            id = "1",
            name = "Personal Account",
            balance = "₹10,000",
            icon = Icons.Default.AccountBalance,
            color = BlueAccent,
            type = "Savings"
        ),
        Account(
            id = "2",
            name = "Business Account",
            balance = "₹50,000",
            icon = Icons.Default.Business,
            color = Color(0xFF4CAF50), // green
            type = "Current"
        ),
        Account(
            id = "3",
            name = "Travel Fund",
            balance = "₹5,000",
            icon = Icons.Default.Flight,
            color = Color(0xFFFF9800), // orange
            type = "Savings"
        ),
        Account(
            id = "4",
            name = "Emergency Fund",
            balance = "₹25,000",
            icon = Icons.Default.Savings,
            color = Color(0xFF2196F3), // blue
            type = "Savings"
        ),
        Account(
            id = "5",
            name = "Joint Account",
            balance = "₹15,000",
            icon = Icons.Default.Group,
            color = Color(0xFFE91E63), // pink
            type = "Shared"
        )
    )
}

private fun getSampleCategories(): List<TransactionCategory> {
    return listOf(
        TransactionCategory("1", "Food &\nDining", Icons.Default.Restaurant, Color(0xFFFF8C00)),
        TransactionCategory("2", "Shopping", Icons.Default.ShoppingCart, Color(0xFF9C27B0)),
        TransactionCategory("3", "Transportation", Icons.Default.DirectionsCar, Color(0xFF2196F3)),
        TransactionCategory("4", "Home &\nUtilities", Icons.Default.Home, Color(0xFF4CAF50)),
        TransactionCategory("5", "Entertainment", Icons.Default.Movie, Color(0xFFE91E63)),
        TransactionCategory("6", "Work &\nBusiness", Icons.Default.Work, Color(0xFF3F51B5)),
        TransactionCategory("7", "Health &\nFitness", Icons.Default.FitnessCenter, Color(0xFFF44336)),
        TransactionCategory("8", "Travel", Icons.Default.Flight, Color(0xFF00BCD4)),
        TransactionCategory("9", "Education", Icons.Default.School, Color(0xFFFF9800)),
        TransactionCategory("10", "Music &\nAudio", Icons.Default.MusicNote, Color(0xFF9C27B0)),
        TransactionCategory("11", "Cafes &\nCoffee", Icons.Default.LocalCafe, Color(0xFFFF8C00)),
        TransactionCategory("12", "Electronics", Icons.Default.PhoneAndroid, Color(0xFF607D8B)),
        TransactionCategory("13", "Clothing", Icons.Default.Checkroom, Color(0xFFE91E63)),
        TransactionCategory("14", "Medical", Icons.Default.LocalHospital, Color(0xFF4CAF50))
    )
}

// Date Picker Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val datePickerState = rememberDatePickerState()
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Select Date",
                    color = TransactionColors.primaryText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp)
                ) {
                    DatePicker(
                        state = datePickerState,
                        modifier = Modifier.fillMaxWidth(),
                        colors = DatePickerDefaults.colors(
                            containerColor = TransactionColors.background,
                            titleContentColor = TransactionColors.primaryText,
                            headlineContentColor = TransactionColors.primaryText,
                            weekdayContentColor = TransactionColors.secondaryText,
                            subheadContentColor = TransactionColors.secondaryText,
                            yearContentColor = TransactionColors.primaryText,
                            currentYearContentColor = TransactionColors.primaryText,
                            selectedYearContentColor = Color.White,
                            selectedYearContainerColor = Color(0xFF2196F3), // Blue accent for better visibility
                            dayContentColor = TransactionColors.primaryText,
                            disabledDayContentColor = TransactionColors.secondaryText,
                            selectedDayContentColor = Color.White,
                            disabledSelectedDayContentColor = Color.White,
                            selectedDayContainerColor = Color(0xFF2196F3), // Blue accent for better visibility
                            disabledSelectedDayContainerColor = TransactionColors.secondaryText,
                            todayContentColor = Color(0xFF2196F3), // Blue accent for today
                            todayDateBorderColor = Color(0xFF2196F3),
                            dayInSelectionRangeContentColor = TransactionColors.primaryText,
                            dayInSelectionRangeContainerColor = Color(0xFF2196F3).copy(alpha = 0.3f)
                        )
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            val dateString = "${date.year}-${date.monthValue.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}"
                            onDateSelected(dateString)
                        }
                        showDialog = false
                    }
                ) {
                    Text(
                        text = "OK",
                        color = TransactionColors.primaryText,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Cancel",
                        color = TransactionColors.secondaryText
                    )
                }
            },
            containerColor = TransactionColors.background
        )
    }
}

// Time Picker Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onTimeSelected: (String) -> Unit
) {
    val timePickerState = rememberTimePickerState(
        initialHour = 12,
        initialMinute = 0,
        is24Hour = false
    )
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = "Select Time",
                    color = TransactionColors.primaryText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = TransactionColors.cardBackground,
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = TransactionColors.primaryText,
                        selectorColor = Color(0xFF2196F3), // Blue accent for better visibility
                        periodSelectorBorderColor = TransactionColors.secondaryText,
                        periodSelectorSelectedContainerColor = Color(0xFF2196F3),
                        periodSelectorUnselectedContainerColor = TransactionColors.cardBackground,
                        periodSelectorSelectedContentColor = Color.White,
                        periodSelectorUnselectedContentColor = TransactionColors.primaryText,
                        timeSelectorSelectedContainerColor = Color(0xFF2196F3),
                        timeSelectorUnselectedContainerColor = TransactionColors.cardBackground,
                        timeSelectorSelectedContentColor = Color.White,
                        timeSelectorUnselectedContentColor = TransactionColors.primaryText
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val hour = timePickerState.hour
                        val minute = timePickerState.minute
                        val timeString = String.format("%02d:%02d", hour, minute)
                        onTimeSelected(timeString)
                        showDialog = false
                    }
                ) {
                    Text(
                        text = "OK",
                        color = TransactionColors.primaryText,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Cancel",
                        color = TransactionColors.secondaryText
                    )
                }
            },
            containerColor = TransactionColors.background
        )
    }
}