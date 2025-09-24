@file:OptIn(ExperimentalMaterial3Api::class)

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.androidkmm.components.AddAccountBottomSheet
import com.example.androidkmm.components.SharedAccountSelectionBottomSheet
import com.example.androidkmm.database.rememberSQLiteAccountDatabase
import com.example.androidkmm.database.rememberSQLiteCategoryDatabase
import com.example.androidkmm.database.rememberSQLiteSettingsDatabase
import com.example.androidkmm.database.rememberSQLiteTransactionDatabase
import com.example.androidkmm.design.AppStyleDesignSystem
import com.example.androidkmm.models.Account
import com.example.androidkmm.models.AppSettings
import com.example.androidkmm.models.DayGroup
import com.example.androidkmm.models.Transaction
import com.example.androidkmm.models.TransactionCategory
import com.example.androidkmm.models.TransactionFormData
import com.example.androidkmm.models.TransactionType
import com.example.androidkmm.screens.EditTransferTransactionScreen
import com.example.androidkmm.theme.AppColors
import com.example.androidkmm.utils.CurrencyUtils.formatDouble
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// Color definitions matching the iOS design
object TransactionColors {
    val income = Color(0xFF00A63E)
    val incomeBackground = Color(0xFF0A1A11)
    val expense = Color(0xFFEF4444)
    val expenseBackground = Color(0xFF260D0D)
    val transfer = Color(0xFF3B82F6)
}

// Data classes for the form (using models from TransactionModels.kt)

// Data class for insufficient balance dialog

@OptIn(ExperimentalTime::class)
@Composable
fun TransactionsScreen(
    onNavigateToLedger: (String, String) -> Unit = { _, _ -> }
) {
    val transactionDatabaseManager = rememberSQLiteTransactionDatabase()
    val settingsDatabaseManager = rememberSQLiteSettingsDatabase()

    // Get currency symbol from settings
    val appSettings =
        settingsDatabaseManager.getAppSettings().collectAsState(initial = AppSettings())
    val currencySymbol = appSettings.value.currencySymbol
    val categoryDatabaseManager = rememberSQLiteCategoryDatabase()
    val accountDatabaseManager = rememberSQLiteAccountDatabase()

    val transactionsState = transactionDatabaseManager.getAllTransactions()
        .collectAsState(initial = emptyList<Transaction>())
    val allAccounts =
        accountDatabaseManager.getActiveAccounts().collectAsState(initial = emptyList<Account>())

    // Fix any existing transfer transactions that might not have proper transferTo field
    LaunchedEffect(Unit) {
        transactionDatabaseManager.fixTransferTransactions()
    }

    val allTransactions = remember(transactionsState.value) {
        // Only show regular transactions in the main transaction list
        // Ledger transactions should only appear in the dedicated ledger screen
        transactionsState.value.sortedByDescending { it.date }
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

    // Filter transactions for the selected month (for transaction list too)
    val filteredTransactions = remember(allTransactions, selectedMonth, selectedYear) {
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

    val dayGroups = remember(filteredTransactions) { groupTransactionsByDay(filteredTransactions) }

    // Calculate carry forward amount from previous months
    val carryForwardAmount = remember(
        allTransactions,
        selectedMonth,
        selectedYear,
        appSettings.value.carryForwardEnabled
    ) {
        println("TransactionListScreen - Carry Forward Enabled: ${appSettings.value.carryForwardEnabled}")
        if (appSettings.value.carryForwardEnabled) {
            val amount = calculateCarryForwardAmount(allTransactions, selectedMonth, selectedYear)
            println("TransactionListScreen - Calculated carry forward amount: $amount")
            amount
        } else {
            println("TransactionListScreen - Carry forward disabled, amount: 0.0")
            0.0
        }
    }

    // Calculate sum of all account balances
    val totalAccountBalance = remember(allAccounts.value) {
        allAccounts.value.sumOf { account ->
            try {
                account.balance.toDoubleOrNull() ?: 0.0
            } catch (e: Exception) {
                0.0
            }
        }
    }

    var showAddSheet by remember { mutableStateOf(false) }
    var showSearchScreen by remember { mutableStateOf(false) }

    // Insufficient balance dialog state
    var showInsufficientBalanceDialog by remember { mutableStateOf(false) }
    var insufficientBalanceInfo by remember {
        mutableStateOf<com.example.androidkmm.models.InsufficientBalanceInfo?>(
            null
        )
    }

    // Track scroll state for showing compact summary
    val listState = rememberLazyListState()
    var showCompactSummary by remember { mutableStateOf(false) }

    // Reset compact summary when month changes or when there are few transactions
    LaunchedEffect(selectedMonth, selectedYear, filteredTransactions.size) {
        showCompactSummary = false
    }

    // Monitor scroll state with better threshold to prevent glitching
    LaunchedEffect(listState, filteredTransactions.size) {
        // Only monitor scroll state if there are transactions to scroll through
        if (filteredTransactions.isNotEmpty()) {
            snapshotFlow {
                listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
            }
                .collect { (firstVisibleIndex, scrollOffset) ->
                    // Show compact summary when user is scrolling
                    val shouldShowCompact = firstVisibleIndex > 0 || scrollOffset > 100
                    if (showCompactSummary != shouldShowCompact) {
                        showCompactSummary = shouldShowCompact
                    }
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = AppStyleDesignSystem.Padding.SCREEN_HORIZONTAL)
            .padding(top = AppStyleDesignSystem.Padding.SCREEN_VERTICAL),
        verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.SCREEN_SECTION_SPACING),
    ) {
        // Fixed Header Section (sticky)
        TransactionHeader(
            transactionCount = filteredTransactions.size,
            onAddClick = { showAddSheet = true }
        )

        // Fixed Month Navigation (sticky)
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
                // Get current date
                val currentDate =
                    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                val currentMonth = currentDate.monthNumber
                val currentYear = currentDate.year

                // Check if we can go to next month
                val nextMonth = if (selectedMonth < 12) selectedMonth + 1 else 1
                val nextYear = if (selectedMonth < 12) selectedYear else selectedYear + 1

                // Only allow navigation if next month/year is not beyond current month/year
                if (nextYear < currentYear || (nextYear == currentYear && nextMonth <= currentMonth)) {
                    if (selectedMonth < 12) {
                        selectedMonth++
                    } else {
                        selectedMonth = 1
                        selectedYear++
                    }
                }
            }
        )

        // Animated Summary Card that shrinks smoothly
        AnimatedSummaryCard(
            transactions = transactionsForSummary,
            currencySymbol = currencySymbol,
            carryForwardAmount = carryForwardAmount,
            totalAccountBalance = totalAccountBalance,
            isCompact = showCompactSummary
        )

        // Fixed Search and Filter (sticky) - only show when not scrolling
        AnimatedVisibility(
            visible = !showCompactSummary,
            enter = fadeIn(animationSpec = tween(300, easing = FastOutSlowInEasing)),
            exit = fadeOut(animationSpec = tween(300, easing = FastOutSlowInEasing))
        ) {
            SearchAndFilter(
                onSearchClick = { showSearchScreen = true },
                onFilterClick = { showSearchScreen = true }
            )
        }

        // Scrollable Transaction List or Empty State
        if (filteredTransactions.isEmpty()) {
            // Show empty state when no transactions for selected month
            EmptyTransactionState(
                selectedMonth = selectedMonth,
                selectedYear = selectedYear,
                onAddClick = { showAddSheet = true }
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(
                    if (filteredTransactions.size > 10) AppStyleDesignSystem.Padding.SMALL else AppStyleDesignSystem.Padding.MEDIUM
                )
            ) {
                items(dayGroups) { dayGroup ->
                    DayGroupSection(
                        dayGroup = dayGroup,
                        currencySymbol = currencySymbol,
                        transactionDatabaseManager = transactionDatabaseManager,
                        categoryDatabaseManager = categoryDatabaseManager,
                        accountDatabaseManager = accountDatabaseManager,
                        onNavigateToLedger = onNavigateToLedger
                    )
                }
                item {
                    Spacer(Modifier.height(64.dp))
                }
            }

        }

    }

    // Show full screen add transaction
    if (showAddSheet) {
        AddTransactionScreen(
            onDismiss = { showAddSheet = false },
            onSave = { transactionFormData ->
                println("DEBUG: onSave called with transactionFormData: $transactionFormData")
                // Convert TransactionFormData to Transaction and save to database
                val transaction = Transaction(
                    id = "${Clock.System.now().epochSeconds}",
                    title = if (transactionFormData.type == TransactionType.TRANSFER && transactionFormData.title.isBlank()) {
                        "Transfer from ${transactionFormData.account?.name ?: ""} to ${transactionFormData.toAccount?.name ?: ""}"
                    } else {
                        transactionFormData.title
                    },
                    amount = transactionFormData.amount.toDoubleOrNull() ?: 0.0,
                    category = if (transactionFormData.type == TransactionType.TRANSFER) "Transfer" else (transactionFormData.category?.name
                        ?: ""),
                    categoryIcon = if (transactionFormData.type == TransactionType.TRANSFER) Icons.Default.SwapHoriz else (transactionFormData.category?.icon
                        ?: Icons.Default.Category),
                    categoryColor = if (transactionFormData.type == TransactionType.TRANSFER) Color(
                        0xFF3B82F6
                    ) else (transactionFormData.category?.color ?: Color.Gray),
                    account = transactionFormData.account?.name ?: "",
                    transferTo = transactionFormData.toAccount?.name,
                    time = transactionFormData.time,
                    type = TransactionType.valueOf(transactionFormData.type.name),
                    description = transactionFormData.description,
                    date = transactionFormData.date,
                    accountIcon = transactionFormData.account?.icon ?: Icons.Default.AccountBalance,
                    accountColor = transactionFormData.account?.color ?: Color.Blue
                )

                println("DEBUG: Calling addTransactionWithBalanceUpdate")
                transactionDatabaseManager.addTransactionWithBalanceUpdate(
                    transaction = transaction,
                    accountDatabaseManager = accountDatabaseManager,
                    onSuccess = {
                        println("DEBUG: Transaction added successfully")
                        showAddSheet = false
                    },
                    onError = { error ->
                        println("DEBUG: TransactionListScreen - onError callback called")
                        println("DEBUG: TransactionListScreen - Error message: ${error.message}")
                        println("DEBUG: TransactionListScreen - Error type: ${error.javaClass.simpleName}")
                        error.printStackTrace()

                        // Check if it's an insufficient balance error
                        if (error.message?.startsWith("Insufficient balance in source account") == true) {
                            println("DEBUG: TransactionListScreen - Insufficient balance detected, showing dialog")
                            val parts = error.message!!.split("Available: ", ", Required: ")
                            if (parts.size >= 3) {
                                val accountName = error.message!!.substringAfter("source account '")
                                    .substringBefore("'")
                                val currentBalance = parts[1].toDoubleOrNull() ?: 0.0
                                val requiredAmount = parts[2].toDoubleOrNull() ?: 0.0

                                // Show insufficient balance dialog
                                println("DEBUG: TransactionListScreen - Setting showInsufficientBalanceDialog = true")
                                showInsufficientBalanceDialog = true
                                insufficientBalanceInfo =
                                    com.example.androidkmm.models.InsufficientBalanceInfo(
                                        accountName = accountName,
                                        currentBalance = currentBalance,
                                        requiredAmount = requiredAmount
                                    )
                                println("DEBUG: TransactionListScreen - Dialog state set - showInsufficientBalanceDialog: $showInsufficientBalanceDialog, insufficientBalanceInfo: $insufficientBalanceInfo")
                            }
                        } else {
                            println("DEBUG: TransactionListScreen - Error is not insufficient balance error")
                        }
                    }
                )
            },
            categoryDatabaseManager = categoryDatabaseManager,
            accountDatabaseManager = accountDatabaseManager
        )
    }

    // Insufficient Balance Dialog
    if (showInsufficientBalanceDialog && insufficientBalanceInfo != null) {
        println("DEBUG: TransactionListScreen - Rendering insufficient balance dialog")
        println("DEBUG: TransactionListScreen - Dialog state - showInsufficientBalanceDialog: $showInsufficientBalanceDialog")
        println("DEBUG: TransactionListScreen - Dialog state - insufficientBalanceInfo: $insufficientBalanceInfo")
        AlertDialog(
            onDismissRequest = {
                showInsufficientBalanceDialog = false
                insufficientBalanceInfo = null
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Cannot Complete Transfer",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "The source account '${insufficientBalanceInfo!!.accountName}' has a negative balance and cannot be used for transfers.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 22.sp
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "To fix this:",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "1.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Add money to ${insufficientBalanceInfo!!.accountName} first",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "2.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Transfer money TO ${insufficientBalanceInfo!!.accountName} from another account",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showInsufficientBalanceDialog = false
                        insufficientBalanceInfo = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Got it",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        )
    }

    // Show Search Screen
    if (showSearchScreen) {
        SearchTransactionsScreen(
            onBackClick = { showSearchScreen = false },
            onCloseClick = { showSearchScreen = false },
            onTransactionClick = { /* Handled directly in SearchTransactionsScreen */ },
            initialFilters = FilterOptions()
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
                    color = MaterialTheme.colorScheme.onBackground,
                    style = AppStyleDesignSystem.Typography.MAIN_PAGE_SUBHEADING_TITLE
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.SCREEN_SECTION_SPACING)
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
                            .padding(vertical = AppStyleDesignSystem.Padding.SMALL),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Camera",
                            modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_LARGE),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Take Photo",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = AppStyleDesignSystem.Typography.BODY.copy(
                                fontWeight = AppStyleDesignSystem.iOSFontWeights.medium
                            )
                        )
                    }

                    Divider(color = MaterialTheme.colorScheme.outline)

                    // Gallery option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                // For now, simulate gallery selection
                                onImageSelected("gallery_image_path")
                                showDialog = false
                            }
                            .padding(vertical = AppStyleDesignSystem.Padding.SMALL),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = "Gallery",
                            modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_LARGE),
                            tint = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Choose from Gallery",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = AppStyleDesignSystem.Typography.BODY.copy(
                                fontWeight = AppStyleDesignSystem.iOSFontWeights.medium
                            )
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

@Composable
private fun TransactionHeader(
    transactionCount: Int,
    onAddClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Transactions",
                color = MaterialTheme.colorScheme.onBackground,
                style = AppStyleDesignSystem.Typography.MAIN_PAGE_HEADING_TITLE
            )
            Text(
                text = "$transactionCount transactions",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = AppStyleDesignSystem.Typography.MAIN_PAGE_SUBHEADING_TITLE
            )
        }

        IconButton(
            onClick = onAddClick,
            modifier = Modifier
                .size(AppStyleDesignSystem.Sizes.ICON_BUTTON_SIZE)
                .background(
                    Color.White,
                    CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Transaction",
                tint = Color.Black,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun MonthNavigation(
    selectedMonth: Int,
    selectedYear: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    // Check if next month button should be disabled
    val currentDate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    val currentMonth = currentDate.monthNumber
    val currentYear = currentDate.year

    val nextMonth = if (selectedMonth < 12) selectedMonth + 1 else 1
    val nextYear = if (selectedMonth < 12) selectedYear else selectedYear + 1
    val isNextMonthDisabled =
        nextYear > currentYear || (nextYear == currentYear && nextMonth > currentMonth)
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val currentMonthYear = "${monthNames[selectedMonth - 1]} $selectedYear"

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous Month
        Icon(
            imageVector = Icons.Default.KeyboardArrowLeft,
            contentDescription = "Previous Month",
            tint = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier
                .size(AppStyleDesignSystem.Sizes.MONTH_SLIDER_ICON_SIZE)
                .clip(CircleShape)
                .background(Color(0xFF121212)) // background #121212
                .border(
                    width = 0.5.dp,
                    color = Color.White.copy(alpha = 0.2f),
                    shape = CircleShape
                )
                .clickable(onClick = onPreviousMonth)
                .padding(6.dp) // optional padding so touch target feels better
        )

        // Current Month Text
        Text(
            text = currentMonthYear,
            color = MaterialTheme.colorScheme.onBackground,
            style = AppStyleDesignSystem.Typography.HEADLINE
        )

        // Next Month
        Icon(
            imageVector = Icons.Default.KeyboardArrowRight,
            contentDescription = "Next Month",
            tint = if (isNextMonthDisabled) {
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.onBackground
            },
            modifier = Modifier
                .size(AppStyleDesignSystem.Sizes.MONTH_SLIDER_ICON_SIZE)
                .clip(CircleShape)
                .background(Color(0xFF121212))
                .border(
                    width = 0.5.dp,
                    color = Color.White.copy(alpha = 0.2f),
                    shape = CircleShape
                )
                .clickable(enabled = !isNextMonthDisabled, onClick = onNextMonth)
                .padding(6.dp) // optional for touch target
        )
    }

}

@Composable
private fun SummaryCard(
    transactions: List<Transaction>,
    currencySymbol: String,
    carryForwardAmount: Double = 0.0,
    totalAccountBalance: Double = 0.0
) {
    // Calculate totals from actual transaction data
    val totalIncome = transactions
        .filter { it.type == TransactionType.INCOME }
        .sumOf { it.amount }

    val totalExpense = transactions
        .filter { it.type == TransactionType.EXPENSE }
        .sumOf { it.amount }

    // Total calculation: income - expenses + carry forward
    val monthlyTotal = totalIncome - totalExpense
    val total = monthlyTotal + carryForwardAmount

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = AppStyleDesignSystem.Padding.XXL,
                vertical = AppStyleDesignSystem.Padding.SMALL
            )
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
                .padding(AppStyleDesignSystem.Padding.MEDIUM_LARGE),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryColumn(
                icon = Icons.Default.TrendingUp,
                iconColor = TransactionColors.income,
                amount = "$currencySymbol${formatDouble(totalIncome, 2)}",
                label = "Income",
                amountColor = TransactionColors.income
            )

            SummaryColumn(
                icon = Icons.Default.TrendingDown,
                iconColor = TransactionColors.expense,
                amount = "$currencySymbol${formatDouble(totalExpense, 2)}",
                label = "Expenses",
                amountColor = TransactionColors.expense
            )

            SummaryColumn(
                icon = Icons.Default.AttachMoney,
                iconColor = if (total >= 0) TransactionColors.income else TransactionColors.expense,
                amount = "${if (total >= 0) "+" else ""}$currencySymbol${formatDouble(total, 2)}",
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
        verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL)
    ) {
        Box(
            modifier = Modifier
                .size(AppStyleDesignSystem.Sizes.ICON_BUTTON_SIZE)
                .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE))
                .background(iconColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = iconColor,
                modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_LARGE)
            )
        }

        Text(
            text = amount,
            color = amountColor,
            style = AppStyleDesignSystem.Typography.MAIN_PAGE_SUBHEADING_TITLE
        )

        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = AppStyleDesignSystem.Typography.CALL_OUT.copy(
                fontWeight = AppStyleDesignSystem.iOSFontWeights.medium
            )
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
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_MEDIUM),
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
                placeholder = { Text("Search transactions...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_MEDIUM)
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedTextColor = MaterialTheme.colorScheme.onBackground,
                    unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                ),
                readOnly = true,
                enabled = false,

                )
        }

        IconButton(
            onClick = onFilterClick,
            modifier = Modifier
                .size(AppStyleDesignSystem.Sizes.ICON_BUTTON_SIZE)
                .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(
                    width = 0.5.dp, // very thin border
                    color = Color.White.copy(alpha = 0.2f), // subtle white
                    shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
                )
        ) {
            Icon(
                imageVector = Icons.Default.FilterAlt,
                contentDescription = "Filter",
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_MEDIUM)
            )
        }
    }
}

@Composable
private fun DayGroupSection(
    dayGroup: DayGroup,
    currencySymbol: String,
    transactionDatabaseManager: com.example.androidkmm.database.SQLiteTransactionDatabase,
    categoryDatabaseManager: com.example.androidkmm.database.SQLiteCategoryDatabase,
    accountDatabaseManager: com.example.androidkmm.database.SQLiteAccountDatabase,
    onNavigateToLedger: (String, String) -> Unit = { _, _ -> }
) {
    var selectedTransaction by remember {
        mutableStateOf<Transaction?>(
            null
        )
    }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showEditScreen by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_TINY)
    ) {
        // Day Header
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${dayGroup.displayDate} (${dayGroup.transactions.size})",
                color = MaterialTheme.colorScheme.onBackground,
                style = AppStyleDesignSystem.Typography.TRANSACTION_DATE_HEADING.copy(
                    fontWeight = AppStyleDesignSystem.iOSFontWeights.medium
                ),
                fontStyle = FontStyle.Normal
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_MEDIUM)
            ) {
                // Always show income (0 if no income)
                Text(
                    text = "+$currencySymbol${formatDouble(dayGroup.income, 2)}",
                    color = TransactionColors.income,
                    style = AppStyleDesignSystem.Typography.MAIN_PAGE_SUBHEADING_TITLE
                )
                // Always show expense (0 if no expense)
                Text(
                    text = "-$currencySymbol${formatDouble(dayGroup.expense, 2)}",
                    color = TransactionColors.expense,
                    style = AppStyleDesignSystem.Typography.MAIN_PAGE_SUBHEADING_TITLE
                )
            }
        }

        // Transactions
        dayGroup.transactions.forEach { transaction ->
            TransactionCard(transaction, currencySymbol) { clickedTransaction ->
                selectedTransaction = clickedTransaction
                showBottomSheet = true
            }
        }
    }

    // Transaction Details Bottom Sheet
    if (selectedTransaction != null) {
        val transaction = selectedTransaction!!
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
            onNavigateToLedger = { personName, transactionId ->
                onNavigateToLedger(
                    personName,
                    transactionId
                )
            },
            categoryDatabaseManager = categoryDatabaseManager,
            accountDatabaseManager = accountDatabaseManager
        )
    }

    // Edit Transaction Screen
    if (showEditScreen && selectedTransaction != null) {
        if (selectedTransaction!!.type == TransactionType.TRANSFER) {
            EditTransferTransactionScreen(
                transaction = selectedTransaction!!,
                onDismiss = {
                    showEditScreen = false
                    selectedTransaction = null
                },
                onSave = { editedTransaction: Transaction ->
                    // Update transaction in database with balance updates
                    transactionDatabaseManager.updateTransactionWithBalanceUpdate(
                        oldTransaction = selectedTransaction!!,
                        newTransaction = editedTransaction,
                        accountDatabaseManager = accountDatabaseManager,
                        onSuccess = {
                            println("Transfer transaction updated successfully: ${editedTransaction.title}")
                            showEditScreen = false
                            selectedTransaction = null
                        },
                        onError = { error ->
                            println("Error updating transfer transaction: ${error.message}")
                        }
                    )
                },
                accountDatabaseManager = accountDatabaseManager
            )
        } else {
            EditTransactionScreen(
                transaction = selectedTransaction!!,
                onDismiss = {
                    showEditScreen = false
                    selectedTransaction = null
                },
                onSave = { editedTransaction: Transaction ->
                    // Update transaction in database with balance updates
                    transactionDatabaseManager.updateTransactionWithBalanceUpdate(
                        oldTransaction = selectedTransaction!!,
                        newTransaction = editedTransaction,
                        accountDatabaseManager = accountDatabaseManager,
                        onSuccess = {
                            println("Transaction updated successfully: ${editedTransaction.title}")
                            showEditScreen = false
                            selectedTransaction = null
                        },
                        onError = { error ->
                            println("Error updating transaction: ${error.message}")
                        }
                    )
                },
                categoryDatabaseManager = categoryDatabaseManager,
                accountDatabaseManager = accountDatabaseManager
            )
        }
    }
}

@Composable
fun TransactionCard(
    transaction: Transaction,
    currencySymbol: String,
    onClick: (Transaction) -> Unit = {}
) {
    // Debug: Print transaction details
    LaunchedEffect(transaction.id) {
        if (transaction.type == TransactionType.TRANSFER) {
            println("DEBUG: Transfer transaction - ID: ${transaction.id}, Title: ${transaction.title}, TransferTo: ${transaction.transferTo}, Account: ${transaction.account}")
        }
    }

    // Clean, integrated design without Card wrapper
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(transaction) }
            .padding(vertical = AppStyleDesignSystem.Padding.XS),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Category Icon - larger and more prominent
        Box(
            modifier = Modifier
                .size(AppStyleDesignSystem.Sizes.ICON_BUTTON_SIZE)
                .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE))
                .background(transaction.categoryColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = transaction.categoryIcon,
                contentDescription = transaction.category,
                tint = Color.White,
                modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_LARGE)
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Transaction Details
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_TINY)
        ) {
            // For transfers, show clear from/to information
            if (transaction.type == TransactionType.TRANSFER) {
                Text(
                    text = if (transaction.transferTo != null && transaction.transferTo.isNotEmpty()) {
                        "${transaction.account} → ${transaction.transferTo}"
                    } else {
                        "Transfer"
                    },
                    color = MaterialTheme.colorScheme.onBackground,
                    style = AppStyleDesignSystem.Typography.BODY.copy(
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${transaction.time} • Transfer",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = AppStyleDesignSystem.Typography.CALL_OUT.copy(
                        fontSize = 12.sp
                    ),
                    fontStyle = FontStyle.Normal
                )
            } else {
                Text(
                    text = transaction.title,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = AppStyleDesignSystem.Typography.BODY.copy(
                        fontSize = 16.sp,
                        fontWeight = AppStyleDesignSystem.iOSFontWeights.medium
                    ),
                    fontStyle = FontStyle.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.SMALL),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = transaction.time,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = AppStyleDesignSystem.Typography.CAPTION_1.copy(
                            fontSize = 12.sp
                        ),
                        fontStyle = FontStyle.Normal
                    )

                    Text(
                        text = "•",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = AppStyleDesignSystem.Typography.CAPTION_1.copy(
                            fontSize = 12.sp
                        ),
                        fontStyle = FontStyle.Normal
                    )

                    Text(
                        text = transaction.category,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = AppStyleDesignSystem.Typography.CAPTION_1.copy(
                            fontSize = 12.sp
                        ),
                        fontStyle = FontStyle.Normal
                    )
                }
            }
        }

        // Amount and Account
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_TINY)
        ) {
            val amountColor = when (transaction.type) {
                TransactionType.INCOME -> TransactionColors.income
                TransactionType.EXPENSE -> TransactionColors.expense
                TransactionType.TRANSFER -> TransactionColors.transfer
            }

            val amountText = when (transaction.type) {
                TransactionType.INCOME -> "$currencySymbol${
                    formatDouble(
                        transaction.amount,
                        2
                    )
                }"

                TransactionType.EXPENSE -> "$currencySymbol${
                    formatDouble(
                        transaction.amount,
                        2
                    )
                }"

                TransactionType.TRANSFER -> "$currencySymbol${
                    formatDouble(
                        transaction.amount,
                        2
                    )
                }"
            }

            Text(
                text = amountText,
                color = amountColor,
                style = AppStyleDesignSystem.Typography.BODY.copy(
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                ),
            )

            // For non-transfers, show time and category in the right column
            if (transaction.type != TransactionType.TRANSFER) {
                Text(
                    text = transaction.account,
                    style = AppStyleDesignSystem.Typography.CALL_OUT.copy(
                        fontSize = 12.sp
                    ),
                    fontStyle = FontStyle.Normal,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.End
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun AddTransactionScreen(
    onDismiss: () -> Unit,
    onSave: (TransactionFormData) -> Unit,
    categoryDatabaseManager: com.example.androidkmm.database.SQLiteCategoryDatabase,
    accountDatabaseManager: com.example.androidkmm.database.SQLiteAccountDatabase,
    defaultTransactionType: TransactionType? = null
) {
    var formData by remember { mutableStateOf(TransactionFormData()) }
    var showCategorySheet by remember { mutableStateOf(false) }
    var showFromAccountSheet by remember { mutableStateOf(false) }
    var showToAccountSheet by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showAddCategorySheet by remember { mutableStateOf(false) }
    var showAddAccountSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Set default date and time
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val currentDate = now.date
        val currentTime = now.time

        formData = formData.copy(
            date = "${currentDate.year}-${
                currentDate.monthNumber.toString().padStart(2, '0')
            }-${currentDate.dayOfMonth.toString().padStart(2, '0')}",
            time = "${currentTime.hour.toString().padStart(2, '0')}:${
                currentTime.minute.toString().padStart(2, '0')
            }",
            type = defaultTransactionType?.let {
                when (it) {
                    TransactionType.EXPENSE -> TransactionType.EXPENSE
                    TransactionType.INCOME -> TransactionType.INCOME
                    TransactionType.TRANSFER -> TransactionType.TRANSFER
                }
            } ?: TransactionType.EXPENSE
        )
    }

    // Full screen content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header with back button
        TopAppBar(
            title = {
                Text(
                    text = "Add Transaction",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            navigationIcon = {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
        )

        // Scrollable content - using LazyColumn like ledger for better scrolling
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = AppStyleDesignSystem.Padding.SCREEN_HORIZONTAL),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.MEDIUM)
        ) {
            item {
                AddTransactionContent(
                    formData = formData,
                    onFormDataChange = { formData = it },
                    onShowCategorySheet = { showCategorySheet = true },
                    onShowFromAccountSheet = { showFromAccountSheet = true },
                    onShowToAccountSheet = { showToAccountSheet = true },
                    onShowDatePicker = { showDatePicker = true },
                    onShowTimePicker = { showTimePicker = true },
                    onSave = { onSave(formData) },
                    onDismiss = onDismiss,
                    focusManager = LocalFocusManager.current
                )
            }
        }
    }

    // Category Selection Sheet
    if (showCategorySheet) {
        CategorySelectionBottomSheet(
            onDismiss = { showCategorySheet = false },
            onCategorySelected = { category: TransactionCategory ->
                formData = formData.copy(category = category)
                showCategorySheet = false
            },
            categoryDatabaseManager = categoryDatabaseManager,
            transactionType = when (formData.type) {
                TransactionType.EXPENSE -> TransactionType.EXPENSE
                TransactionType.INCOME -> TransactionType.INCOME
                TransactionType.TRANSFER -> TransactionType.TRANSFER
            },
            onAddCategory = { showAddCategorySheet = true }
        )
    }

    // From Account Selection Sheet
    if (showFromAccountSheet) {
        AccountSelectionBottomSheet(
            onDismiss = { showFromAccountSheet = false },
            title = "Select Account",
            subtitle = "Choose an account for your transaction",
            onAccountSelected = { account: Account ->
                formData = formData.copy(account = account)
                showFromAccountSheet = false
            },
            accountDatabaseManager = accountDatabaseManager,
            onAddAccount = { showAddAccountSheet = true }
        )
    }

    // To Account Selection Sheet
    if (showToAccountSheet) {
        AccountSelectionBottomSheet(
            onDismiss = { showToAccountSheet = false },
            title = "Select Account",
            subtitle = "Choose destination account",
            onAccountSelected = { account: Account ->
                formData = formData.copy(toAccount = account)
                showToAccountSheet = false
            },
            accountDatabaseManager = accountDatabaseManager,
            onAddAccount = { showAddAccountSheet = true }
        )
    }

    // Date Picker Dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismiss = { showDatePicker = false },
            onDateSelected = { dateString ->
                formData = formData.copy(date = dateString)
                showDatePicker = false
            },
            initialDate = formData.date
        )
    }

    // Time Picker Dialog
    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onTimeSelected = { timeString ->
                formData = formData.copy(time = timeString)
                showTimePicker = false
            },
            initialTime = formData.time
        )
    }

    // Add Category Bottom Sheet
    if (showAddCategorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddCategorySheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = null
        ) {
            AddCategoryBottomSheet(
                categoryType = when (formData.type) {
                    TransactionType.EXPENSE -> com.example.androidkmm.models.CategoryTab.EXPENSE
                    TransactionType.INCOME -> com.example.androidkmm.models.CategoryTab.INCOME
                    TransactionType.TRANSFER -> com.example.androidkmm.models.CategoryTab.EXPENSE // Default
                },
                onDismiss = { showAddCategorySheet = false },
                onCategoryAdded = { category, onSuccess, onError ->
                    categoryDatabaseManager.addCategory(
                        category = category,
                        onSuccess = {
                            onSuccess()
                            showAddCategorySheet = false
                        },
                        onError = { error ->
                            onError(error.message ?: "Unknown error occurred")
                        }
                    )
                }
            )
        }
    }

    // Add Account Bottom Sheet
    if (showAddAccountSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddAccountSheet = false },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = null
        ) {
            AddAccountBottomSheet(
                onDismiss = { showAddAccountSheet = false },
                onAccountAdded = { account ->
                    accountDatabaseManager.addAccount(
                        account = account,
                        onSuccess = {
                            showAddAccountSheet = false
                        },
                        onError = { error ->
                            println("Error adding account: ${error.message}")
                        }
                    )
                },
                accountDatabaseManager = accountDatabaseManager
            )
        }
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
    onDismiss: () -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    // Use iOS design system with reduced spacing for more compact form
    val spacing = AppStyleDesignSystem.Padding.MEDIUM
    val titleFontSize = AppStyleDesignSystem.Typography.TITLE_2.fontSize
    val labelFontSize = AppStyleDesignSystem.Typography.BODY.fontSize
    val inputHeight = AppStyleDesignSystem.Sizes.INPUT_HEIGHT
    AppStyleDesignSystem.Sizes.ICON_SIZE_MASSIVE
    val buttonHeight = AppStyleDesignSystem.Sizes.BUTTON_HEIGHT
    AppStyleDesignSystem.Padding.MEDIUM
    AppStyleDesignSystem.Typography.MAIN_PAGE_HEADING_TITLE.fontSize
    // Validation state
    var validationErrors by remember { mutableStateOf(emptyMap<String, String>()) }

    // Track if user has attempted to submit
    var hasAttemptedSubmit by remember { mutableStateOf(false) }

    // Focus manager to clear focus when transaction type changes
    val focusManager = LocalFocusManager.current

    // Clear focus and validation errors when transaction type changes
    LaunchedEffect(formData.type) {
        focusManager.clearFocus()
        validationErrors = emptyMap()
        hasAttemptedSubmit = false
    }

    // Validation function using standardized validation
    fun validateForm(): Boolean {
        val validationResult = when (formData.type) {
            TransactionType.TRANSFER -> {
                com.example.androidkmm.utils.FormValidation.validateTransferForm(
                    amount = formData.amount,
                    fromAccount = formData.account,
                    toAccount = formData.toAccount
                )
            }

            TransactionType.INCOME -> {
                com.example.androidkmm.utils.FormValidation.validateIncomeForm(
                    amount = formData.amount,
                    title = formData.title,
                    category = formData.category,
                    account = formData.account,
                    description = formData.description
                )
            }

            TransactionType.EXPENSE -> {
                com.example.androidkmm.utils.FormValidation.validateExpenseForm(
                    amount = formData.amount,
                    title = formData.title,
                    category = formData.category,
                    account = formData.account,
                    description = formData.description
                )
            }
        }

        // Only show validation errors after user has attempted to submit
        if (hasAttemptedSubmit) {
            validationErrors = validationResult.errors
        } else {
            validationErrors = emptyMap()
        }
        return validationResult.isValid
    }

    // Check if all mandatory fields are filled
    fun isFormReady(): Boolean {
        return when (formData.type) {
            TransactionType.TRANSFER -> {
                // For transfers, amount and both accounts are mandatory
                formData.amount.isNotBlank() &&
                        formData.amount.toDoubleOrNull() != null &&
                        formData.amount.toDoubleOrNull()!! > 0 &&
                        formData.account != null &&
                        formData.toAccount != null &&
                        // Also check that accounts are different
                        formData.account?.name != formData.toAccount?.name
            }

            else -> {
                // For income/expense, all fields are mandatory
                formData.title.isNotBlank() &&
                        formData.amount.isNotBlank() &&
                        formData.amount.toDoubleOrNull() != null &&
                        formData.amount.toDoubleOrNull()!! > 0 &&
                        formData.category != null &&
                        formData.account != null
            }
        }
    }

    // Save function with validation
    fun handleSave() {
        hasAttemptedSubmit = true
        if (validateForm()) {
            // Capitalize first letter of title and description before saving
            val capitalizedFormData = formData.copy(
                title = formData.title.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() },
                description = formData.description.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            )
            onFormDataChange(capitalizedFormData)
            onSave()
        }
    }

    // Simple Column layout without LazyColumn
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight() // Ensure content wraps properly
            .clickable {
                // Clear focus when tapping empty space
                focusManager.clearFocus()
            },
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        // Transaction Type Selector
        TransactionTypeSelector(
            selectedType = formData.type,
            onTypeSelected = { type ->
                // Clear category when transaction type changes to ensure proper filtering
                onFormDataChange(
                    formData.copy(
                        type = type,
                        category = null
                    )
                )
            }
        )

        Spacer(Modifier.height(48.dp))
        // Amount Input
        AmountInputSection(
            amount = formData.amount,
            onAmountChange = { amount ->
                onFormDataChange(formData.copy(amount = amount))
            },
            errorMessage = validationErrors["amount"]
        )
        Spacer(Modifier.height(32.dp))

        // Account Selection - different for transfer vs others
        if (formData.type == TransactionType.TRANSFER) {
            // From Account and To Account for transfers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_MEDIUM)
            ) {
                CategoryAccountSelector(
                    modifier = Modifier.weight(1f),
                    title = "From Account *",
                    selectedText = formData.account?.name ?: "Select",
                    icon = formData.account?.icon ?: Icons.Default.CreditCard,
                    iconColor = formData.account?.color
                        ?: MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onShowFromAccountSheet,
                    labelFontSize = labelFontSize,
                    inputHeight = inputHeight,
                    isError = validationErrors.containsKey("account"),
                    errorMessage = validationErrors["account"]
                )

                CategoryAccountSelector(
                    modifier = Modifier.weight(1f),
                    title = "To Account *",
                    selectedText = formData.toAccount?.name ?: "Select",
                    icon = formData.toAccount?.icon ?: Icons.Default.CreditCard,
                    iconColor = formData.toAccount?.color
                        ?: MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onShowToAccountSheet,
                    labelFontSize = labelFontSize,
                    inputHeight = inputHeight,
                    isError = validationErrors.containsKey("toAccount"),
                    errorMessage = validationErrors["toAccount"]
                )
            }
        } else {
            // Category and Account for income/expense
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_MEDIUM)
            ) {
                CategoryAccountSelector(
                    modifier = Modifier.weight(1f),
                    title = "Category *",
                    selectedText = formData.category?.name ?: "Select",
                    icon = formData.category?.icon ?: Icons.Default.Category,
                    iconColor = formData.category?.color
                        ?: MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onShowCategorySheet,
                    labelFontSize = labelFontSize,
                    inputHeight = inputHeight,
                    isError = validationErrors.containsKey("category"),
                    errorMessage = validationErrors["category"]
                )

                CategoryAccountSelector(
                    modifier = Modifier.weight(1f),
                    title = "Account *",
                    selectedText = formData.account?.name ?: "Select",
                    icon = formData.account?.icon ?: Icons.Default.CreditCard,
                    iconColor = formData.account?.color
                        ?: MaterialTheme.colorScheme.onSurfaceVariant,
                    onClick = onShowFromAccountSheet,
                    labelFontSize = labelFontSize,
                    inputHeight = inputHeight,
                    isError = validationErrors.containsKey("account"),
                    errorMessage = validationErrors["account"]
                )
            }
        }

        // Title Input
        InputField(
            label = if (formData.type == TransactionType.TRANSFER) "Title" else "Title *",
            value = formData.title,
            onValueChange = { title ->
                // Limit title to 30 characters
                val limitedTitle = if (title.length <= 30) title else title.take(30)
                onFormDataChange(formData.copy(title = limitedTitle))
            },
            placeholder = "Provide title here",
            errorMessage = validationErrors["title"],
            labelFontSize = labelFontSize,
            inputHeight = inputHeight
        )

        // Date and Time
        Text(
            text = "Date & Time",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = labelFontSize,
            fontWeight = FontWeight.Medium
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.SCREEN_SECTION_SPACING)
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
                icon = Icons.Default.CalendarMonth,
                isSelected = true,
                onClick = onShowDatePicker,
                inputHeight = inputHeight
            )

            DateTimeSelector(
                modifier = Modifier.weight(1f),
                value = formData.time.ifEmpty { "01:31" },
                icon = Icons.Default.AccessTime,
                isSelected = true,
                onClick = onShowTimePicker,
                inputHeight = inputHeight
            )
        }

        // Description Input
        InputField(
            label = "Description (Optional)",
            value = formData.description,
            onValueChange = { description ->
                // Limit description to 75 characters
                val limitedDescription =
                    if (description.length <= 75) description else description.take(75)
                onFormDataChange(formData.copy(description = limitedDescription))
            },
            placeholder = "Provide description here",
            errorMessage = validationErrors["description"],
            labelFontSize = labelFontSize,
            inputHeight = inputHeight
        )

//        // Receipt Upload
//        ReceiptUploadSection(
//            receiptHeight = receiptHeight,
//            labelFontSize = labelFontSize
//        )

        // Save Button - Always visible with prominent styling
        Button(
            onClick = { handleSave() },
            modifier = Modifier
                .fillMaxWidth()
                .height(buttonHeight),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isFormReady()) Color(0xFF2196F3) else Color.Gray// Bright blue color
            ),
            shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE)
        ) {
            Text(
                text = "Add Transaction",
                color = Color.White,
                fontSize = titleFontSize,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun TransactionTypeSelector(
    selectedType: TransactionType,
    onTypeSelected: (TransactionType) -> Unit,
    isSmallScreen: Boolean = false,
    isMediumScreen: Boolean = true
) {
    val toggleHeight = when {
        isSmallScreen -> 32.dp  // Much shorter
        isMediumScreen -> 36.dp
        else -> 40.dp
    }

    val toggleFontSize = when {
        isSmallScreen -> 12.sp
        isMediumScreen -> 14.sp
        else -> 16.sp
    }

    // Use a simpler approach with proper sliding
    Row(
        modifier = Modifier
            .height(toggleHeight)
            .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE))
            .background(MaterialTheme.colorScheme.surface)
            .padding(2.dp)
    ) {
        TransactionType.values().forEach { type ->
            val isSelected = selectedType == type
            val (icon, text) = when (type) {
                TransactionType.EXPENSE -> Icons.Default.TrendingDown to "Expense"
                TransactionType.INCOME -> Icons.Default.TrendingUp to "Income"
                TransactionType.TRANSFER -> Icons.Default.SwapHoriz to "Transfer"
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onTypeSelected(type) }
                    .background(
                        if (isSelected) Color(0xFF2196F3) else Color.Transparent,
                        RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = text,
                        modifier = Modifier.size(if (false) 14.dp else 16.dp),
                        tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(if (false) 4.dp else 6.dp))
                    Text(
                        text = text,
                        fontSize = toggleFontSize,
                        fontWeight = FontWeight.Medium,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AmountInputSection(
    amount: String,
    onAmountChange: (String) -> Unit,
    errorMessage: String? = null,
    isSmallScreen: Boolean = false,
    isMediumScreen: Boolean = true,
    titleFontSize: TextUnit = 18.sp,
    amountFontSize: TextUnit = 54.sp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BasicTextField(
            value = amount,
            onValueChange = { newValue: String ->
                // Only allow numbers and decimal point
                val filteredValue = newValue.filter { char: Char -> char.isDigit() || char == '.' }
                // Ensure only one decimal point
                val decimalCount = filteredValue.count { char: Char -> char == '.' }
                // Limit to maximum 10 digits (excluding decimal point)
                val digitsOnly = filteredValue.filter { char: Char -> char.isDigit() }
                if (decimalCount <= 1 && digitsOnly.length <= 10) {
                    onAmountChange(filteredValue)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                fontSize = amountFontSize,
                fontWeight = FontWeight.Light,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onBackground
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            interactionSource = interactionSource,
            cursorBrush = SolidColor(Color.White),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (amount.isEmpty() && !isFocused) {
                        Text(
                            text = "0.00",
                            fontSize = amountFontSize,
                            fontWeight = FontWeight.Light,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    innerTextField()
                }
            }
        )

        Text(
            text = "Enter amount",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
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
    iconColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    onClick: () -> Unit,
    isSmallScreen: Boolean = false,
    isMediumScreen: Boolean = true,
    labelFontSize: TextUnit = 14.sp,
    inputHeight: Dp = 56.dp,
    isError: Boolean = false,
    errorMessage: String? = null
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_TINY)
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = labelFontSize,
            fontWeight = FontWeight.SemiBold
        )

        Button(
            onClick = onClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(inputHeight),
            shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            border = BorderStroke(
                width = AppStyleDesignSystem.Sizes.BORDER_NORMAL,
                color = if (isError) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = 0.3f
                )
            ),
            contentPadding = PaddingValues(12.dp)
        ) {
            Row {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_MEDIUM),
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
private fun InputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    errorMessage: String? = null,
    isSmallScreen: Boolean = false,
    isMediumScreen: Boolean = true,
    labelFontSize: TextUnit = 14.sp,
    inputHeight: Dp = 56.dp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    Column(
        verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_TINY)
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = labelFontSize,
            fontWeight = FontWeight.SemiBold
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = inputHeight),
            interactionSource = interactionSource,
            placeholder = {
                if (!isFocused && value.isEmpty()) {
                    Text(placeholder)
                }
            },
            isError = errorMessage != null,
            shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF2A2A2A), // Grey when typing
                unfocusedContainerColor = Color.Black, // Black when empty
                focusedBorderColor = Color.White.copy(alpha = 0.3f), // Subtle white border when typing
                unfocusedBorderColor = Color.White.copy(alpha = 0.3f), // Subtle white border when empty
                focusedTextColor = MaterialTheme.colorScheme.onBackground,
                unfocusedTextColor = MaterialTheme.colorScheme.onBackground,
                errorBorderColor = Color.Red,
                errorContainerColor = Color.Black
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
    onClick: () -> Unit,
    isSmallScreen: Boolean = false,
    isMediumScreen: Boolean = true,
    inputHeight: Dp = 40.dp
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(inputHeight).border(
            width = AppStyleDesignSystem.Sizes.BORDER_NORMAL, // very thin
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), // white with alpha
            shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE)
        ),
        shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Black,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_MEDIUM)
            )
        }
    }
}

@Composable
private fun ReceiptUploadSection(
    onReceiptSelected: (String) -> Unit = {},
    isSmallScreen: Boolean = false,
    isMediumScreen: Boolean = true,
    receiptHeight: Dp = 80.dp,
    labelFontSize: TextUnit = 14.sp
) {
    var showImagePicker by remember { mutableStateOf(false) }
    var selectedImagePath by remember { mutableStateOf<String?>(null) }

    Column(
        verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_TINY)
    ) {
        Text(
            text = "Receipt (Optional)",
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = labelFontSize,
            fontWeight = FontWeight.SemiBold
        )

        if (selectedImagePath != null) {
            // Show selected image
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM))
                    .border(
                        width = 0.5.dp, // very thin border
                        color = Color.White.copy(alpha = 0.2f), // subtle white
                        shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
                    ),
                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
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
                        modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_XL),
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Receipt attached",
                            color = MaterialTheme.colorScheme.onBackground,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Tap to change",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                    }
                    IconButton(
                        onClick = { selectedImagePath = null }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_MEDIUM)
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
                    .height(receiptHeight)
                    .border(
                        width = AppStyleDesignSystem.Sizes.BORDER_THICK,
                        brush = SolidColor(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)),
                        shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE)
                    ),
                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Upload Receipt",
                        modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_XL)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Upload Receipt",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Images only",
                        style = AppStyleDesignSystem.Typography.CAPTION_1,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
    accountDatabaseManager: com.example.androidkmm.database.SQLiteAccountDatabase,
    onAddAccount: (() -> Unit)? = null
) {
    SharedAccountSelectionBottomSheet(
        onDismiss = onDismiss,
        title = title,
        subtitle = subtitle,
        onAccountSelected = onAccountSelected,
        accountDatabaseManager = accountDatabaseManager,
        onAddAccount = onAddAccount
    )
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
            .padding(vertical = 6.dp)
            .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM))
            .border(
                width = 0.5.dp, // very thin border
                color = Color.White.copy(alpha = 0.2f), // subtle white
                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
            ),
        shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.SCREEN_SECTION_SPACING)
        ) {
            // Account type icon
            Box(
                modifier = Modifier
                    .size(AppStyleDesignSystem.Sizes.ICON_BUTTON_SIZE)
                    .clip(CircleShape)
                    .background(getAccountTypeColor(account.type)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getAccountTypeIcon(account.type),
                    contentDescription = account.type,
                    tint = Color.White,
                    modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_LARGE)
                )
            }

            // Account details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_TINY)
            ) {
                Text(
                    text = account.name,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_TINY)
                ) {
                    // Green arrow up icon
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = TransactionColors.income,
                        modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_SMALL)
                    )

                    Text(
                        text = account.balance,
                        color = TransactionColors.income,
                        style = AppStyleDesignSystem.Typography.BODY,
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
    categoryDatabaseManager: com.example.androidkmm.database.SQLiteCategoryDatabase,
    transactionType: TransactionType,
    onAddCategory: (() -> Unit)? = null
) {
    val bottomSheetState = rememberModalBottomSheetState()
    val categoriesState = categoryDatabaseManager.getAllCategories()
        .collectAsState(initial = emptyList<com.example.androidkmm.models.Category>())

    // Filter categories based on transaction type
    val filteredCategories = categoriesState.value.filter { category ->
        when (transactionType) {
            TransactionType.EXPENSE -> category.type == com.example.androidkmm.models.CategoryType.EXPENSE
            TransactionType.INCOME -> category.type == com.example.androidkmm.models.CategoryType.INCOME
            TransactionType.TRANSFER -> false // No categories for transfers
        }
    }

    val categories = filteredCategories.map { category ->
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
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onBackground,
        dragHandle = {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_XL))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = when (transactionType) {
                            TransactionType.EXPENSE -> "Select Expense Category"
                            TransactionType.INCOME -> "Select Income Category"
                            TransactionType.TRANSFER -> "Select Category"
                        },
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Choose a category for your transaction",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_XL)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_MEDIUM)
                    )
                }
            }

            // Grid of categories
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_MEDIUM),
                verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.SCREEN_SECTION_SPACING)
            ) {
                items(categories) { category ->
                    CategoryGridCard(
                        category = category,
                        onClick = { onCategorySelected(category) }
                    )
                }

                // Add Category Button (only show for non-transfer transactions)
                if (transactionType != TransactionType.TRANSFER) {
                    item {
                        CategoryGridCard(
                            category = TransactionCategory(
                                id = "add_category",
                                name = "Add",
                                icon = Icons.Default.Add,
                                color = MaterialTheme.colorScheme.primary
                            ),
                            onClick = { onAddCategory?.invoke() },
                            isAddButton = true
                        )
                    }
                }
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
            .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM))
            .border(
                width = 0.5.dp, // very thin border
                color = Color.White.copy(alpha = 0.2f), // subtle white
                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
            ),
        shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
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
                    .size(AppStyleDesignSystem.Sizes.AVATAR_MEDIUM)
                    .clip(CircleShape)
                    .background(category.color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = category.name,
                    tint = Color.White,
                    modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_MEDIUM)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = category.name,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun CategoryGridCard(
    category: TransactionCategory,
    onClick: () -> Unit,
    isAddButton: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE))
            .border(
                width = if (isAddButton) 1.dp else 0.5.dp,
                color = if (isAddButton) MaterialTheme.colorScheme.primary else Color.White.copy(
                    alpha = 0.2f
                ),
                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE)
            ),
        shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE),
        colors = CardDefaults.cardColors(
            containerColor = if (isAddButton) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(AppStyleDesignSystem.Sizes.ICON_BUTTON_SIZE)
                    .clip(CircleShape)
                    .background(category.color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = category.name,
                    tint = Color.White,
                    modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_LARGE)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = category.name,
                color = if (isAddButton) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
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

@OptIn(ExperimentalTime::class)
private fun groupTransactionsByDay(transactions: List<Transaction>): List<DayGroup> {
    if (transactions.isEmpty()) {
        return emptyList()
    }

    // Group transactions by date
    val groupedByDate = transactions.groupBy { it.date }

    return groupedByDate.map { (date, dayTransactions) ->
        // Convert all dates to nice readable formats consistently
        val displayDate = formatDateForDisplay(date)

        DayGroup(
            date = date,
            displayDate = displayDate,
            transactions = dayTransactions,
            income = dayTransactions.filter { it.type == TransactionType.INCOME }
                .sumOf { it.amount },
            expense = dayTransactions.filter { it.type == TransactionType.EXPENSE }
                .sumOf { it.amount }
        )
    }.sortedByDescending { it.date }
}

@Composable
private fun AnimatedSummaryCard(
    transactions: List<Transaction>,
    currencySymbol: String,
    carryForwardAmount: Double = 0.0,
    totalAccountBalance: Double = 0.0,
    isCompact: Boolean
) {
    val totalIncome = transactions
        .filter { it.type == TransactionType.INCOME }
        .sumOf { it.amount }

    val totalExpense = transactions
        .filter { it.type == TransactionType.EXPENSE }
        .sumOf { it.amount }

    // Total calculation: income - expenses + carry forward
    val monthlyTotal = totalIncome - totalExpense
    val total = monthlyTotal + carryForwardAmount

    // Animate padding and icon size
    val animatedPadding by animateFloatAsState(
        targetValue = if (isCompact) 12f else 16f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "padding"
    )

    val animatedIconSize by animateFloatAsState(
        targetValue = if (isCompact) 0f else 48f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "iconSize"
    )

    val animatedAmountSize by animateFloatAsState(
        targetValue = if (isCompact) 16f else 18f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "amountSize"
    )

    val animatedLabelSize by animateFloatAsState(
        targetValue = if (isCompact) 12f else 14f,
        animationSpec = tween(300, easing = FastOutSlowInEasing),
        label = "labelSize"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM))
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
            ),
        shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = animatedPadding.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Income
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (animatedIconSize > 0f) {
                    Box(
                        modifier = Modifier
                            .size(animatedIconSize.dp)
                            .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE))
                            .background(TransactionColors.incomeBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = "Income",
                            tint = TransactionColors.income,
                            modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_LARGE)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    text = "$currencySymbol${formatDouble(totalIncome, 2)}",
                    color = TransactionColors.income,
                    fontSize = animatedAmountSize.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Income",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = animatedLabelSize.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            // Expenses
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (animatedIconSize > 0f) {
                    Box(
                        modifier = Modifier
                            .size(animatedIconSize.dp)
                            .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE))
                            .background(TransactionColors.expenseBackground),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingDown,
                            contentDescription = "Expenses",
                            tint = TransactionColors.expense,
                            modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_LARGE)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    text = "$currencySymbol${formatDouble(totalExpense, 2)}",
                    color = TransactionColors.expense,
                    fontSize = animatedAmountSize.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Expenses",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = animatedLabelSize.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            // Total
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (animatedIconSize > 0f) {
                    Box(
                        modifier = Modifier
                            .size(animatedIconSize.dp)
                            .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE))
                            .background((if (total >= 0) TransactionColors.incomeBackground else TransactionColors.expenseBackground)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Money,
                            contentDescription = "Total",
                            tint = (if (total >= 0) TransactionColors.income else TransactionColors.expense),
                            modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_LARGE)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Text(
                    text = if (total >= 0) "+$currencySymbol${
                        formatDouble(
                            total,
                            2
                        )
                    }" else "$currencySymbol${formatDouble(total, 2)}",
                    color = if (total >= 0) TransactionColors.income else TransactionColors.expense,
                    fontSize = animatedAmountSize.sp,
                    fontWeight = FontWeight.Normal
                )
                Text(
                    text = "Total",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = animatedLabelSize.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun CompactSummaryCard(
    transactions: List<Transaction>,
    currencySymbol: String,
    carryForwardAmount: Double = 0.0,
    totalAccountBalance: Double = 0.0
) {
    val totalIncome = transactions
        .filter { it.type == TransactionType.INCOME }
        .sumOf { it.amount }

    val totalExpense = transactions
        .filter { it.type == TransactionType.EXPENSE }
        .sumOf { it.amount }

    // Total calculation: income - expenses + carry forward
    val monthlyTotal = totalIncome - totalExpense
    val total = monthlyTotal + carryForwardAmount

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM))
            .border(
                width = 0.5.dp,
                color = Color.White.copy(alpha = 0.2f),
                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
            ),
        shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // Income - compact without icon
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$currencySymbol${formatDouble(totalIncome, 2)}",
                    color = TransactionColors.income,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Income",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Expenses - compact without icon
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "$currencySymbol${formatDouble(totalExpense, 2)}",
                    color = TransactionColors.expense,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Expenses",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Total - compact without icon
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (total >= 0) "+$currencySymbol${
                        formatDouble(
                            total,
                            2
                        )
                    }" else "$currencySymbol${formatDouble(total, 2)}",
                    color = if (total >= 0) TransactionColors.income else TransactionColors.expense,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Total",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun EmptyTransactionState(
    selectedMonth: Int,
    selectedYear: Int,
    onAddClick: () -> Unit
) {
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    val monthName = monthNames.getOrNull(selectedMonth - 1) ?: "Unknown"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            // Large icon
            Box(
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(32.dp)
                    )
                    .size(AppStyleDesignSystem.Sizes.ICON_SIZE_GIANT),

                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Receipt,
                    contentDescription = "No transactions",
                    modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_MASSIVE),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }


            // Title
            Text(
                text = "No transactions yet",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium
            )


            // Subtitle
            Text(
                text = "No transactions found for $monthName $selectedYear",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Add transaction button
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE),
                modifier = Modifier
                    .height(40.dp)
                    .padding(horizontal = 12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add transaction",
                    modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_SMALL)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Add Transaction",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}


@OptIn(ExperimentalTime::class)
private fun formatDateForDisplay(dateString: String): String {
    return try {
        // Parse the date string (format: "2025-09-10")
        val parts = dateString.split("-")
        if (parts.size >= 3) {
            val year = parts[0].toInt()
            val month = parts[1].toInt()
            val day = parts[2].toInt()

            // Check if this is today's date
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val currentDate = now.date

            if (year == currentDate.year && month == currentDate.monthNumber && day == currentDate.dayOfMonth) {
                return "Today"
            }

            // Format as "Sep 10, 2025" for other dates
            val monthNames = listOf(
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
            )
            val monthName = monthNames.getOrNull(month - 1) ?: "Unknown"
            "$monthName $day, $year"
        } else {
            dateString // fallback to original format
        }
    } catch (e: Exception) {
        dateString // fallback to original format
    }
}

private fun getSampleAccounts(currencySymbol: String): List<Account> {
    return listOf(
        Account(
            id = "1",
            name = "Personal Account",
            balance = "${currencySymbol}10,000",
            icon = Icons.Default.AccountBalance,
            color = AppColors.Info,
            type = "Savings"
        ),
        Account(
            id = "2",
            name = "Business Account",
            balance = "${currencySymbol}50,000",
            icon = Icons.Default.Business,
            color = Color(0xFF4CAF50), // green
            type = "Current"
        ),
        Account(
            id = "3",
            name = "Travel Fund",
            balance = "${currencySymbol}5,000",
            icon = Icons.Default.Flight,
            color = Color(0xFFFF9800), // orange
            type = "Savings"
        ),
        Account(
            id = "4",
            name = "Emergency Fund",
            balance = "${currencySymbol}25,000",
            icon = Icons.Default.Savings,
            color = Color(0xFF2196F3), // blue
            type = "Savings"
        ),
        Account(
            id = "5",
            name = "Joint Account",
            balance = "${currencySymbol}15,000",
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
        TransactionCategory(
            "7",
            "Health &\nFitness",
            Icons.Default.FitnessCenter,
            Color(0xFFF44336)
        ),
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
    onDateSelected: (String) -> Unit,
    initialDate: String = ""
) {
    val today = java.time.LocalDate.now()
    val todayMillis =
        today.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
    val tomorrowMillis =
        today.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = if (initialDate.isNotEmpty()) {
            try {
                val parts = initialDate.split("-")
                if (parts.size == 3) {
                    val year = parts[0].toInt()
                    val month = parts[1].toInt()
                    val day = parts[2].toInt()
                    val selectedDate = java.time.LocalDate.of(year, month, day)
                    // If the initial date is in the future, use today instead
                    if (selectedDate.isAfter(today)) {
                        todayMillis
                    } else {
                        selectedDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant()
                            .toEpochMilli()
                    }
                } else null
            } catch (e: Exception) {
                null
            }
        } else null,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                // Allow dates up to and including today
                return utcTimeMillis < tomorrowMillis
            }

            override fun isSelectableYear(year: Int): Boolean {
                // Only allow current year and previous years
                return year <= today.year
            }
        }
    )

    // Additional validation to prevent future date selection
    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { selectedMillis ->
            val selectedDate = java.time.Instant.ofEpochMilli(selectedMillis)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
            val today = java.time.LocalDate.now()

            // If a future date is somehow selected, reset to today
            if (selectedDate.isAfter(today)) {
                datePickerState.selectedDateMillis =
                    today.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            }
        }
    }

    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            title = {
                Text(
                    text = "Select Date",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                DatePicker(
                    state = datePickerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(horizontal = 0.dp),
                    colors = DatePickerDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        headlineContentColor = MaterialTheme.colorScheme.onBackground,
                        weekdayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        subheadContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        yearContentColor = MaterialTheme.colorScheme.onBackground,
                        currentYearContentColor = MaterialTheme.colorScheme.onBackground,
                        selectedYearContentColor = Color.White,
                        selectedYearContainerColor = Color(0xFF2196F3), // Blue accent for better visibility
                        dayContentColor = MaterialTheme.colorScheme.onBackground,
                        disabledDayContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        selectedDayContentColor = Color.White,
                        disabledSelectedDayContentColor = Color.White,
                        selectedDayContainerColor = Color(0xFF2196F3), // Blue accent for better visibility
                        disabledSelectedDayContainerColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        todayContentColor = Color(0xFF2196F3), // Blue accent for today
                        todayDateBorderColor = Color(0xFF2196F3),
                        dayInSelectionRangeContentColor = MaterialTheme.colorScheme.onBackground,
                        dayInSelectionRangeContainerColor = Color(0xFF2196F3).copy(alpha = 0.3f)
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val date = java.time.Instant.ofEpochMilli(millis)
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate()
                            val today = java.time.LocalDate.now()

                            // Check if selected date is in the future
                            if (date.isAfter(today)) {
                                // Don't allow future dates - just close dialog without selecting
                                showDialog = false
                                return@TextButton
                            }

                            val dateString = "${date.year}-${
                                date.monthValue.toString().padStart(2, '0')
                            }-${date.dayOfMonth.toString().padStart(2, '0')}"
                            onDateSelected(dateString)
                        }
                        showDialog = false
                    }
                ) {
                    Text(
                        text = "OK",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

// Time Picker Dialog
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onTimeSelected: (String) -> Unit,
    initialTime: String = ""
) {
    val timePickerState = rememberTimePickerState(
        initialHour = if (initialTime.isNotEmpty()) {
            try {
                val parts = initialTime.split(":")
                if (parts.size == 2) {
                    parts[0].toInt()
                } else 12
            } catch (e: Exception) {
                12
            }
        } else 12,
        initialMinute = if (initialTime.isNotEmpty()) {
            try {
                val parts = initialTime.split(":")
                if (parts.size == 2) {
                    parts[1].toInt()
                } else 0
            } catch (e: Exception) {
                0
            }
        } else 0,
        is24Hour = false
    )
    var showDialog by remember { mutableStateOf(true) }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
            title = {
                Text(
                    text = "Select Time",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                TimePicker(
                    state = timePickerState,
                    colors = TimePickerDefaults.colors(
                        clockDialColor = MaterialTheme.colorScheme.surface,
                        clockDialSelectedContentColor = Color.White,
                        clockDialUnselectedContentColor = MaterialTheme.colorScheme.onBackground,
                        selectorColor = Color(0xFF2196F3), // Blue accent for better visibility
                        periodSelectorBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        periodSelectorSelectedContainerColor = Color(0xFF2196F3),
                        periodSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surface,
                        periodSelectorSelectedContentColor = Color.White,
                        periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.onBackground,
                        timeSelectorSelectedContainerColor = Color(0xFF2196F3),
                        timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.surface,
                        timeSelectorSelectedContentColor = Color.White,
                        timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.onBackground
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
                        color = MaterialTheme.colorScheme.onBackground,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(
                        text = "Cancel",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }
}

// Function to calculate carry forward amount from previous months
private fun calculateCarryForwardAmount(
    allTransactions: List<Transaction>,
    currentMonth: Int,
    currentYear: Int
): Double {
    println("calculateCarryForwardAmount - Current month: $currentMonth, year: $currentYear")
    println("calculateCarryForwardAmount - Total transactions: ${allTransactions.size}")

    // Get all transactions from months before the current month
    val previousTransactions = allTransactions.filter { transaction ->
        try {
            val parts = transaction.date.split("-")
            if (parts.size >= 2) {
                val year = parts[0].toInt()
                val month = parts[1].toInt()

                // Include transactions from previous months and years
                val isPrevious = year < currentYear || (year == currentYear && month < currentMonth)
                if (isPrevious) {
                    println("calculateCarryForwardAmount - Previous transaction: ${transaction.title} - ${transaction.date} - ${transaction.amount}")
                }
                isPrevious
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }

    println("calculateCarryForwardAmount - Previous transactions count: ${previousTransactions.size}")

    // Calculate the net amount from previous transactions
    val totalIncome = previousTransactions
        .filter { it.type == TransactionType.INCOME }
        .sumOf { it.amount }

    val totalExpense = previousTransactions
        .filter { it.type == TransactionType.EXPENSE }
        .sumOf { it.amount }

    val netAmount = totalIncome - totalExpense
    println("calculateCarryForwardAmount - Total income: $totalIncome, Total expense: $totalExpense, Net: $netAmount")

    // Return the net amount (income - expense) from previous months
    return netAmount
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTransactionScreen(
    transaction: Transaction,
    onDismiss: () -> Unit,
    onSave: (Transaction) -> Unit,
    categoryDatabaseManager: com.example.androidkmm.database.SQLiteCategoryDatabase,
    accountDatabaseManager: com.example.androidkmm.database.SQLiteAccountDatabase
) {
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var title by remember { mutableStateOf(transaction.title) }
    var description by remember { mutableStateOf(transaction.description) }
    var date by remember { mutableStateOf(transaction.date) }
    var time by remember { mutableStateOf(transaction.time) }
    var selectedType by remember {
        mutableStateOf(
            when (transaction.type) {
                TransactionType.INCOME -> TransactionType.INCOME
                TransactionType.EXPENSE -> TransactionType.EXPENSE
                TransactionType.TRANSFER -> TransactionType.EXPENSE // Default to EXPENSE for transfer transactions
            }
        )
    }
    var selectedCategoryName by remember { mutableStateOf(transaction.category) }
    var selectedAccountName by remember { mutableStateOf(transaction.account) }

    // Store original category to restore when switching back
    var originalCategory by remember { mutableStateOf(transaction.category) }
    var hasStoredOriginalCategory by remember { mutableStateOf(false) }
    var showCategorySheet by remember { mutableStateOf(false) }
    var showFromAccountSheet by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // Track the original transaction type to restore category when switching back
    val originalTransactionType = remember {
        when (transaction.type) {
            TransactionType.INCOME -> TransactionType.INCOME
            TransactionType.EXPENSE -> TransactionType.EXPENSE
            TransactionType.TRANSFER -> TransactionType.EXPENSE
        }
    }

    // Restore original category when switching back to original type
    LaunchedEffect(selectedType) {
        if (selectedType == originalTransactionType && originalCategory.isNotEmpty() && selectedCategoryName.isEmpty()) {
            selectedCategoryName = originalCategory
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Edit Transaction",
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_LARGE)
                )
            }
        }


        // Form Content - Scrollable
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_MEDIUM)
        ) {
            // Transaction Type Toggle - 80% width with larger text
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                // Toggle Container - 80% width, even less tall
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE))
                        .background(Color(0xFF2A2A2A))
                        .padding(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Expense Toggle
                        val isExpenseSelected = selectedType == TransactionType.EXPENSE
                        Button(
                            onClick = {
                                if (selectedType != TransactionType.EXPENSE) {
                                    // Store original category only once when first switching away from original type
                                    if (!hasStoredOriginalCategory && selectedType == originalTransactionType) {
                                        originalCategory = selectedCategoryName
                                        hasStoredOriginalCategory = true
                                    }
                                    selectedType = TransactionType.EXPENSE
                                    selectedCategoryName = "" // Clear category when switching
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isExpenseSelected) Color(0xFF2196F3) else Color.Transparent,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 1.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingDown,
                                    contentDescription = "Expense",
                                    modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_SMALL)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Expense",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Income Toggle
                        val isIncomeSelected = selectedType == TransactionType.INCOME
                        Button(
                            onClick = {
                                if (selectedType != TransactionType.INCOME) {
                                    // Store original category only once when first switching away from original type
                                    if (!hasStoredOriginalCategory && selectedType == originalTransactionType) {
                                        originalCategory = selectedCategoryName
                                        hasStoredOriginalCategory = true
                                    }
                                    selectedType = TransactionType.INCOME
                                    selectedCategoryName = "" // Clear category when switching
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(18.dp)),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isIncomeSelected) Color(0xFF2196F3) else Color.Transparent,
                                contentColor = Color.White
                            ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 1.dp),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.TrendingUp,
                                    contentDescription = "Income",
                                    modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_SMALL)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Income",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Amount Input - Without $ sign
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BasicTextField(
                    value = amount,
                    onValueChange = { newValue: String ->
                        val filteredValue =
                            newValue.filter { char -> char.isDigit() || char == '.' }
                        val decimalCount = filteredValue.count { char -> char == '.' }
                        if (decimalCount <= 1) {
                            amount = filteredValue
                        }
                    },
                    textStyle = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Enter amount",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Category & Account Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_MEDIUM)
            ) {
                // Category
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Category",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE))
                            .clickable { showCategorySheet = true }
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                width = AppStyleDesignSystem.Sizes.BORDER_NORMAL,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = "Category",
                            modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_MEDIUM),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (selectedCategoryName.isNotEmpty()) {
                            Text(
                                text = selectedCategoryName,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                        } else {
                            Text(
                                text = "Select",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                }

                // Account
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Account",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE))
                            .clickable { showFromAccountSheet = true }
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                width = AppStyleDesignSystem.Sizes.BORDER_NORMAL,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = "Account",
                            modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_MEDIUM),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (selectedAccountName.isNotEmpty()) {
                            Text(
                                text = selectedAccountName,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 14.sp
                            )
                        } else {
                            Text(
                                text = "Select",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Date & Time Row
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Date & Time",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL)
                ) {
                    // Date Picker
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE))
                            .clickable { showDatePicker = true }
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                width = AppStyleDesignSystem.Sizes.BORDER_NORMAL,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_SMALL)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = date.split("-").let { parts ->
                                if (parts.size == 3) "${parts[2]}/${parts[1]}/${parts[0]}" else date
                            },
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                    }

                    // Time Picker
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE))
                            .clickable { showTimePicker = true }
                            .background(MaterialTheme.colorScheme.surface)
                            .border(
                                width = AppStyleDesignSystem.Sizes.BORDER_NORMAL,
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE)
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_SMALL)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = time,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Title
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Title",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        val limitedTitle = if (it.length <= 30) it else it.take(30)
                        title = limitedTitle
                    },
                    placeholder = { Text("Lunch at Subway") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        cursorColor = MaterialTheme.colorScheme.onBackground
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Description
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Description (Optional)",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                val interactionSource = remember { MutableInteractionSource() }
                val isFocused by interactionSource.collectIsFocusedAsState()

                OutlinedTextField(
                    value = description,
                    onValueChange = {
                        val limitedDescription = if (it.length <= 75) it else it.take(75)
                        description = limitedDescription
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 56.dp),
                    interactionSource = interactionSource,
                    placeholder = {
                        if (!isFocused && description.isEmpty()) {
                            Text("Provide description here")
                        }
                    },
                    shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF2A2A2A), // Grey when typing
                        unfocusedContainerColor = Color.Black, // Black when empty
                        focusedBorderColor = Color.White.copy(alpha = 0.3f), // Subtle white border when typing
                        unfocusedBorderColor = Color.White.copy(alpha = 0.3f), // Subtle white border when empty
                        focusedTextColor = MaterialTheme.colorScheme.onBackground,
                        unfocusedTextColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_MEDIUM)
            ) {
                // Cancel Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE))
                        .background(Color(0xFF2A2A2A))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cancel",
                        style = AppStyleDesignSystem.Typography.BODY,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }

                // Save Changes Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE))
                        .background(Color.White)
                        .clickable {
                            val updatedTransaction = transaction.copy(
                                amount = amount.toDoubleOrNull() ?: 0.0,
                                title = title,
                                description = description,
                                date = date,
                                time = time,
                                type = when (selectedType) {
                                    TransactionType.INCOME -> TransactionType.INCOME
                                    TransactionType.EXPENSE -> TransactionType.EXPENSE
                                    TransactionType.TRANSFER -> TransactionType.EXPENSE // Default to EXPENSE
                                },
                                category = selectedCategoryName,
                                account = selectedAccountName,
                                transferTo = null // Remove transfer support
                            )
                            onSave(updatedTransaction)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save",
                            modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_MEDIUM),
                            tint = Color.Black
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save Changes",
                            style = AppStyleDesignSystem.Typography.BODY,
                            fontWeight = FontWeight.Medium,
                            color = Color.Black
                        )
                    }
                }
            }
        }
    }

    // Bottom sheets and dialogs
    if (showCategorySheet) {
        CategorySelectionBottomSheet(
            onDismiss = { showCategorySheet = false },
            onCategorySelected = { category: TransactionCategory ->
                selectedCategoryName = category.name
                showCategorySheet = false
            },
            categoryDatabaseManager = categoryDatabaseManager,
            transactionType = when (selectedType) {
                TransactionType.INCOME -> TransactionType.INCOME
                TransactionType.EXPENSE -> TransactionType.EXPENSE
                TransactionType.TRANSFER -> TransactionType.TRANSFER
            }
        )
    }

    if (showFromAccountSheet) {
        AccountSelectionBottomSheet(
            onDismiss = { showFromAccountSheet = false },
            title = "Select Account",
            subtitle = "Choose an account",
            onAccountSelected = { account: Account ->
                selectedAccountName = account.name
                showFromAccountSheet = false
            },
            accountDatabaseManager = accountDatabaseManager
        )
    }


    if (showDatePicker) {
        DatePickerDialog(
            onDismiss = { showDatePicker = false },
            onDateSelected = { dateString ->
                date = dateString
                showDatePicker = false
            },
            initialDate = date
        )
    }

    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onTimeSelected = { timeString ->
                time = timeString
                showTimePicker = false
            },
            initialTime = time
        )
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun AddCategoryBottomSheetForTransaction(
    categoryType: com.example.androidkmm.models.CategoryTab,
    onDismiss: () -> Unit,
    onCategoryAdded: (com.example.androidkmm.models.Category, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf(Icons.Default.AttachMoney) }
    var selectedIconIndex by remember { mutableStateOf(0) }
    var selectedColor by remember { mutableStateOf(Color(0xFF2196F3)) }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add ${if (categoryType == com.example.androidkmm.models.CategoryTab.EXPENSE) "Expense" else "Income"} Category",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Category Name
        Text(
            text = "Category Name",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = categoryName,
            onValueChange = { categoryName = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            placeholder = { Text("Enter category name") },
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp
            )
        )

        // Icon Selection
        Text(
            text = "Choose Icon",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val icons = listOf(
            Icons.Default.AttachMoney,
            Icons.Default.ShoppingCart,
            Icons.Default.Restaurant,
            Icons.Default.LocalGasStation,
            Icons.Default.Home,
            Icons.Default.DirectionsCar,
            Icons.Default.Flight,
            Icons.Default.Movie,
            Icons.Default.Sports,
            Icons.Default.Favorite,
            Icons.Default.MusicNote,
            Icons.Default.Business,
            Icons.Default.LocalCafe,
            Icons.Default.Wallet
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            horizontalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL),
            verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL)
        ) {
            items(icons.size) { index ->
                val icon = icons[index]
                Box(
                    modifier = Modifier
                        .size(AppStyleDesignSystem.Sizes.ICON_BUTTON_SIZE)
                        .clip(CircleShape)
                        .background(
                            if (selectedIconIndex == index) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable {
                            selectedIcon = icon
                            selectedIconIndex = index
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (selectedIconIndex == index) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_LARGE)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Color Selection
        Text(
            text = "Choose Color",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val colors = listOf(
            Color(0xFF2196F3), Color(0xFF4CAF50), Color(0xFFFF9800),
            Color(0xFFF44336), Color(0xFF9C27B0), Color(0xFF00BCD4),
            Color(0xFF795548), Color(0xFF607D8B)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL)
        ) {
            colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(AppStyleDesignSystem.Sizes.AVATAR_MEDIUM)
                        .clip(CircleShape)
                        .background(
                            if (selectedColor == color) color
                            else color.copy(alpha = 0.3f)
                        )
                        .border(
                            width = if (selectedColor == color) 2.dp else 0.dp,
                            color = Color.White,
                            shape = CircleShape
                        )
                        .clickable { selectedColor = color },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedColor == color) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_MEDIUM)
                        )
                    }
                }
            }
        }

        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage,
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Add Category Button
        Button(
            onClick = {
                if (isLoading) return@Button

                errorMessage = "" // Clear previous error
                isLoading = true

                val category = com.example.androidkmm.models.Category(
                    id = Clock.System.now().toEpochMilliseconds().toString(),
                    name = categoryName,
                    icon = selectedIcon,
                    color = selectedColor,
                    type = if (categoryType == com.example.androidkmm.models.CategoryTab.EXPENSE) com.example.androidkmm.models.CategoryType.EXPENSE else com.example.androidkmm.models.CategoryType.INCOME,
                    isCustom = true
                )
                onCategoryAdded(
                    category,
                    {
                        isLoading = false
                        onDismiss()
                    },
                    { error ->
                        isLoading = false
                        errorMessage = error
                    }
                )
            },
            enabled = categoryName.isNotEmpty() && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (categoryName.isNotEmpty() && !isLoading) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant,
                contentColor = if (categoryName.isNotEmpty() && !isLoading) Color.White else Color(
                    0xFF666666
                )
            ),
            shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_MEDIUM),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Add Category",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Helper functions for account type icons and colors
private fun getAccountTypeIcon(type: String): ImageVector {
    return when (type) {
        "Bank Account" -> Icons.Default.AccountBalance
        "Credit/Debit Card" -> Icons.Default.CreditCard
        "Cash" -> Icons.Default.Money
        "Digital Wallet" -> Icons.Default.PhoneAndroid
        else -> Icons.Default.AccountBalance
    }
}

private fun getAccountTypeColor(type: String): Color {
    return when (type) {
        "Bank Account" -> Color(0xFF2196F3) // Blue
        "Credit/Debit Card" -> Color(0xFF4CAF50) // Green
        "Cash" -> Color(0xFFFF9800) // Orange
        "Digital Wallet" -> Color(0xFF9C27B0) // Purple
        else -> Color(0xFF2196F3)
    }
}



