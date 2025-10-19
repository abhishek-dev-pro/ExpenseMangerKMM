package com.example.androidkmm.screens

import com.example.androidkmm.utils.DateFormatUtils
import DateRange
import FilterOptions
import SearchTransactionsScreen
import com.example.androidkmm.screens.goals.CreateGoalScreen
import com.example.androidkmm.screens.goals.GoalCard
import com.example.androidkmm.screens.goals.GoalActionBar
import com.example.androidkmm.screens.goals.AddMoneyFloatingBar
import com.example.androidkmm.data.SampleGoals
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.design.AppStyleDesignSystem
import com.example.androidkmm.utils.DateTimeUtils
import com.example.androidkmm.database.rememberSQLiteSettingsDatabase
import com.example.androidkmm.database.rememberSQLiteGoalsDatabase
import com.example.androidkmm.models.AppSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    transactionDatabaseManager: com.example.androidkmm.database.SQLiteTransactionDatabase? = null
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showSearchScreen by remember { mutableStateOf(false) }
    var showCreateGoalScreen by remember { mutableStateOf(false) }
    var goalsUpdateTrigger by remember { mutableStateOf(0) }
    var searchInitialFilters by remember { mutableStateOf<FilterOptions>(FilterOptions()) }
    
    // Get currency symbol from settings
    val settingsDatabaseManager = rememberSQLiteSettingsDatabase()
    val settings by settingsDatabaseManager.getAppSettings().collectAsState(initial = AppSettings())
    val currencySymbol = settings.currencySymbol
    
    // Get goals database manager
    val goalsDatabaseManager = rememberSQLiteGoalsDatabase()
    
    // Function to handle category click
    val handleCategoryClick = { category: String, currentMonth: Int, currentYear: Int ->
        // Set up filters for this category and current month
        // Calculate days in month (simplified approach)
        val daysInMonth = when (currentMonth) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (currentYear % 4 == 0 && (currentYear % 100 != 0 || currentYear % 400 == 0)) 29 else 28
            else -> 30
        }
        val monthStart = "${currentYear}-${DateFormatUtils.formatInt(currentMonth, "%02d")}-01"
        val monthEnd = "${currentYear}-${DateFormatUtils.formatInt(currentMonth, "%02d")}-${DateFormatUtils.formatInt(daysInMonth, "%02d")}"
        
        searchInitialFilters = FilterOptions(
            selectedCategories = setOf(category),
            dateRange = DateRange(
                from = monthStart,
                to = monthEnd
            )
        )
        showSearchScreen = true
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Title
        Text(
            text = "Financial Insights",
            style = AppStyleDesignSystem.Typography.MAIN_PAGE_HEADING_TITLE,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppStyleDesignSystem.Padding.LARGE, vertical = AppStyleDesignSystem.Padding.SMALL)
        )
        
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            listOf("Overview", "Goals", "Budgets", "Recurring").forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            style = AppStyleDesignSystem.Typography.BODY.copy(
                                fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }
        
        // Content based on selected tab
        when (selectedTab) {
            0 -> OverviewTab(
                transactionDatabaseManager = transactionDatabaseManager,
                currencySymbol = currencySymbol,
                onCategoryClick = handleCategoryClick
            )
            1 -> GoalsTab(
                onCreateGoalClick = { showCreateGoalScreen = true },
                goalsUpdateTrigger = goalsUpdateTrigger
            )
            2 -> BudgetsTab()
            3 -> RecurringTab()
        }
    }
    
    // Show Search Screen
    if (showSearchScreen) {
        SearchTransactionsScreen(
            onBackClick = { showSearchScreen = false },
            onCloseClick = { showSearchScreen = false },
            onTransactionClick = { /* Handled directly in SearchTransactionsScreen */ },
            initialFilters = searchInitialFilters
        )
    }
    
        // Show Create Goal Screen
        if (showCreateGoalScreen) {
            CreateGoalScreen(
                onClose = { showCreateGoalScreen = false },
                onGoalCreated = { goal ->
                    // Add the new goal directly to the database
                    goalsDatabaseManager.addGoal(
                        goal = goal,
                        onSuccess = { 
                            showCreateGoalScreen = false
                        },
                        onError = { /* Handle error */ }
                    )
                }
            )
        }
}

@Composable
private fun OverviewTab(
    transactionDatabaseManager: com.example.androidkmm.database.SQLiteTransactionDatabase? = null,
    currencySymbol: String = "$",
    onCategoryClick: (String, Int, Int) -> Unit = { _, _, _ -> }
) {
    // Get current date
    val currentDate = DateTimeUtils.getCurrentDate()
    var currentMonth by remember { mutableStateOf(currentDate.monthNumber) }
    var currentYear by remember { mutableStateOf(currentDate.year) }
    
    // Calculate monthly income for the current month
    val allTransactions = if (transactionDatabaseManager != null) {
        transactionDatabaseManager.getAllTransactions().collectAsState(initial = emptyList()).value
    } else {
        emptyList()
    }
    
    val monthlyIncome = remember(allTransactions, currentMonth, currentYear) {
        val monthTransactions = allTransactions.filter { transaction ->
            val transactionDate = kotlinx.datetime.LocalDate.parse(transaction.date)
            transactionDate.monthNumber == currentMonth && 
            transactionDate.year == currentYear
        }
        
        monthTransactions
            .filter { it.type == com.example.androidkmm.models.TransactionType.INCOME }
            .sumOf { it.amount }
    }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppStyleDesignSystem.Padding.SCREEN_HORIZONTAL, vertical = 0.dp)
    ) {
        
        // Month Selector
        item {
            MonthSelectorCard(
                currentMonth = currentMonth,
                currentYear = currentYear,
                onPreviousMonth = {
                    if (currentMonth > 1) {
                        currentMonth--
                    } else {
                        currentMonth = 12
                        currentYear--
                    }
                },
                onNextMonth = {
                    // Get current date to check if we can go to next month
                    val currentDate = DateTimeUtils.getCurrentDate()
                    val currentMonthNumber = currentDate.monthNumber
                    val currentYearNumber = currentDate.year
                    
                    // Only allow navigation to next month if we're not at the current month
                    if (!(currentMonth == currentMonthNumber && currentYear == currentYearNumber)) {
                        if (currentMonth < 12) {
                            currentMonth++
                        } else {
                            currentMonth = 1
                            currentYear++
                        }
                    }
                }
            )
        }
        
        item { Spacer(Modifier.height(AppStyleDesignSystem.Padding.LARGE)) }
        
        // Monthly Summary
        item {
            MonthlySummaryCard(
                currentMonth = currentMonth,
                currentYear = currentYear,
                transactionDatabaseManager = transactionDatabaseManager
            )
        }
        
        item { Spacer(Modifier.height(AppStyleDesignSystem.Padding.LARGE)) }
        
        // Recent Large Expenses
        item {
            RecentLargeExpensesSection(
                transactionDatabaseManager = transactionDatabaseManager,
                currentMonth = currentMonth,
                currentYear = currentYear,
                monthlyIncome = monthlyIncome,
                currencySymbol = currencySymbol
            )
        }
        
        item { Spacer(Modifier.height(AppStyleDesignSystem.Padding.LARGE)) }
        
        // Financial Trends Chart
        item {
            FinancialTrendsSection(
                transactionDatabaseManager = transactionDatabaseManager,
                currentMonth = currentMonth,
                currentYear = currentYear
            )
        }
        
        item { Spacer(Modifier.height(AppStyleDesignSystem.Padding.LARGE)) }
        
        // Spending by Category
        item {
            SpendingByCategorySection(
                transactionDatabaseManager = transactionDatabaseManager,
                currentMonth = currentMonth,
                currentYear = currentYear,
                currencySymbol = currencySymbol,
                onCategoryClick = { category -> onCategoryClick(category, currentMonth, currentYear) }
            )
        }
        
        item { Spacer(Modifier.height(AppStyleDesignSystem.Padding.LARGE)) }
        
        // Savings Rate
        item {
            SavingsRateSection(
                transactionDatabaseManager = transactionDatabaseManager,
                currentMonth = currentMonth,
                currentYear = currentYear
            )
        }
        
        item { Spacer(Modifier.height(AppStyleDesignSystem.Padding.LARGE)) }
        
        // Smart Insights
        item {
            SmartInsightsSection(
                transactionDatabaseManager = transactionDatabaseManager,
                currentMonth = currentMonth,
                currentYear = currentYear
            )
        }
        
        item { Spacer(Modifier.height(AppStyleDesignSystem.Padding.LARGE)) }
    }
}

@Composable
private fun MonthSelectorCard(
    currentMonth: Int,
    currentYear: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    
    // Get current date to determine if we're at the current month
    val currentDate = DateTimeUtils.getCurrentDate()
    val currentMonthNumber = currentDate.monthNumber
    val currentYearNumber = currentDate.year
    
    // Check if we're at the current month (can't go to future)
    val isAtCurrentMonth = currentMonth == currentMonthNumber && currentYear == currentYearNumber
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppStyleDesignSystem.Padding.LARGE, vertical = AppStyleDesignSystem.Padding.SMALL),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(
                    Icons.Default.KeyboardArrowLeft,
                    contentDescription = "Previous month",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${monthNames[currentMonth - 1]} $currentYear",
                    style = AppStyleDesignSystem.Typography.MAIN_PAGE_SUBHEADING_TITLE,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isAtCurrentMonth) "Current Month" else "Historic Month",
                    style = AppStyleDesignSystem.Typography.CAPTION_1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(
                onClick = onNextMonth,
                enabled = !isAtCurrentMonth
            ) {
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = "Next month",
                    tint = if (isAtCurrentMonth) 
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f) 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun MonthlySummaryCard(
    currentMonth: Int,
    currentYear: Int,
    transactionDatabaseManager: com.example.androidkmm.database.SQLiteTransactionDatabase? = null
) {
    // Get currency symbol from settings
    val settingsDatabaseManager = rememberSQLiteSettingsDatabase()
    val appSettings = settingsDatabaseManager.getAppSettings().collectAsState(initial = AppSettings())
    val currencySymbol = appSettings.value.currencySymbol
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    
    // Get actual transaction data if database manager is available
    val allTransactions = if (transactionDatabaseManager != null) {
        transactionDatabaseManager.getAllTransactions().collectAsState(initial = emptyList()).value
    } else {
        emptyList()
    }
    
    // Calculate monthly data from actual transactions
    val (income, expenses, savings) = remember(allTransactions, currentMonth, currentYear) {
        val monthTransactions = allTransactions.filter { transaction ->
            val transactionDate = kotlinx.datetime.LocalDate.parse(transaction.date)
            transactionDate.monthNumber == currentMonth && 
            transactionDate.year == currentYear
        }
        
        val totalIncome = monthTransactions
            .filter { it.type == com.example.androidkmm.models.TransactionType.INCOME }
            .sumOf { it.amount }
        
        val totalExpenses = monthTransactions
            .filter { it.type == com.example.androidkmm.models.TransactionType.EXPENSE }
            .sumOf { it.amount }
        
        val totalSavings = totalIncome - totalExpenses
        
        Triple(totalIncome, totalExpenses, totalSavings)
    }
    
    val savingsRate = if (income > 0) {
        ((savings / income) * 100).toInt()
    } else if (income == 0.0 && expenses > 0) {
        -100 // When there's no income but there are expenses, savings rate is -100%
    } else {
        0 // When there's no income and no expenses, savings rate is 0%
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF7B1FA2) // Vibrant purple background
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with month
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monthly Summary",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = monthNames[currentMonth - 1],
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Income and Expenses Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Income Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Income",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "$currencySymbol${DateFormatUtils.formatDouble(income, "%.2f")}",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(Modifier.width(12.dp))
                
                // Expenses Card
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Expenses",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "$currencySymbol${DateFormatUtils.formatDouble(expenses, "%.2f")}",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(Modifier.height(12.dp))
            
            // Highlighted Savings Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (savings >= 0) {
                        Color(0xFF10B981).copy(alpha = 0.2f) // Green for positive savings
                    } else {
                        Color(0xFFEF4444).copy(alpha = 0.2f) // Red for negative savings
                    }
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Savings",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "$currencySymbol${DateFormatUtils.formatDouble(savings, "%.2f")}",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // Savings percentage badge
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (savings >= 0) {
                                Color(0xFF10B981) // Green background
                            } else {
                                Color(0xFFEF4444) // Red background
                            }
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "${if (savingsRate >= 0) "" else "-"}${kotlin.math.abs(savingsRate)}%",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "savings this month",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FinancialMetricCard(
    label: String,
    amount: String,
    color: Color,
    backgroundColor: Color,
    showThisMonth: Boolean = false
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            // Label
            Text(
                text = label,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(Modifier.height(2.dp))
            
            // Amount
            Text(
                text = amount,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            
            // "this month" text for savings
            if (showThisMonth) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "this month",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun FinancialSummaryColumn(
    icon: ImageVector,
    label: String,
    amount: String,
    color: Color,
    savingsRate: String? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_MEDIUM)
        )
        
        Spacer(Modifier.height(4.dp))
        
        // Label
        Text(
            text = label,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        
        Spacer(Modifier.height(4.dp))
        
        // Amount
        Text(
            text = amount,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        
        // Savings rate (only for savings column)
        if (savingsRate != null) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = savingsRate,
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun FinancialSummaryRow(
    icon: ImageVector,
    label: String,
    amount: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_SMALL)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$label: $amount",
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

// Data class for insights
data class SmartInsight(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val iconColor: Color,
    val backgroundColor: Color
)

@Composable
private fun SmartInsightsSection(
    transactionDatabaseManager: com.example.androidkmm.database.SQLiteTransactionDatabase? = null,
    currentMonth: Int,
    currentYear: Int
) {
    // Get transaction data
    val allTransactions = if (transactionDatabaseManager != null) {
        transactionDatabaseManager.getAllTransactions().collectAsState(initial = emptyList()).value
    } else {
        emptyList()
    }
    
    // Generate insights based on current month data
    val insights = remember(allTransactions, currentMonth, currentYear) {
        generateSmartInsights(allTransactions, currentMonth, currentYear)
    }
    
    Column {
        Text(
            text = "Smart Insights",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(Modifier.height(12.dp))
        
        // Display generated insights
        insights.forEachIndexed { index, insight ->
            InsightCard(
                icon = insight.icon,
                title = insight.title,
                description = insight.description,
                iconColor = insight.iconColor,
                backgroundColor = insight.backgroundColor
            )
            
            if (index < insights.size - 1) {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// Function to generate smart insights based on transaction data
private fun generateSmartInsights(
    allTransactions: List<com.example.androidkmm.models.Transaction>,
    currentMonth: Int,
    currentYear: Int
): List<SmartInsight> {
    val insights = mutableListOf<SmartInsight>()
    
    // Filter transactions for current month
    val monthTransactions = allTransactions.filter { transaction ->
        val transactionDate = kotlinx.datetime.LocalDate.parse(transaction.date)
        transactionDate.monthNumber == currentMonth && 
        transactionDate.year == currentYear
    }
    
    if (monthTransactions.isEmpty()) {
        return insights
    }
    
    // Calculate basic metrics
    val income = monthTransactions
        .filter { it.type == com.example.androidkmm.models.TransactionType.INCOME }
        .sumOf { it.amount }
    
    val expenses = monthTransactions
        .filter { it.type == com.example.androidkmm.models.TransactionType.EXPENSE }
        .sumOf { it.amount }
    
    val savings = income - expenses
    val savingsRate = if (income > 0) (savings / income) * 100 else 0.0
    
    // Insight 1: Savings Performance
    when {
        savingsRate >= 50 -> {
            insights.add(
                SmartInsight(
                    title = "Savings Champion! 🏆",
                    description = "You've saved ${DateFormatUtils.formatDouble(savingsRate, "%.1f")}% of your income this month.\nYour future self is doing a happy dance!",
                    icon = Icons.Default.CheckCircle,
                    iconColor = Color(0xFF4CAF50),
                    backgroundColor = Color(0xFFE8F5E8)
                )
            )
        }
        savingsRate >= 20 -> {
            insights.add(
                SmartInsight(
                    title = "Financial Rockstar! 💪",
                    description = "You've saved ${DateFormatUtils.formatDouble(savingsRate, "%.1f")}% of your income this month.\nThe savings account is feeling the love!",
                    icon = Icons.Default.CheckCircle,
                    iconColor = Color(0xFF4CAF50),
                    backgroundColor = Color(0xFFE8F5E8)
                )
            )
        }
        savingsRate >= 0 -> {
            insights.add(
                SmartInsight(
                    title = "Positive Progress! 📈",
                    description = "You've saved ${DateFormatUtils.formatDouble(savingsRate, "%.1f")}% of your income this month.\nEvery penny saved is a penny earned!",
                    icon = Icons.Default.Info,
                    iconColor = Color(0xFF2196F3),
                    backgroundColor = Color(0xFFE3F2FD)
                )
            )
        }
        else -> {
            insights.add(
                SmartInsight(
                    title = "Spending Spree Alert! 💸",
                    description = "You've spent ${DateFormatUtils.formatDouble(kotlin.math.abs(savingsRate), "%.1f")}% more than your income this month.\nYour wallet is having trust issues!",
                    icon = Icons.Default.Warning,
                    iconColor = Color(0xFFFF9800),
                    backgroundColor = Color(0xFFFFF3E0)
                )
            )
        }
    }
    
    // Insight 2: Category Analysis
    val expenseTransactions = monthTransactions.filter { it.type == com.example.androidkmm.models.TransactionType.EXPENSE }
    val categorySpending = expenseTransactions.groupBy { it.category }
        .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }
    
    if (categorySpending.isNotEmpty()) {
        val topCategory = categorySpending.first()
        val topCategoryPercentage = if (expenses > 0) (topCategory.second / expenses) * 100 else 0.0
        
        when {
            topCategoryPercentage >= 50 -> {
                insights.add(
                    SmartInsight(
                        title = "Category Spotlight: ${topCategory.first} 🌟",
                        description = "${DateFormatUtils.formatDouble(topCategoryPercentage, "%.1f")}% of your expenses went to ${topCategory.first}.\n${topCategory.first} is clearly your favorite!",
                        icon = Icons.Default.PieChart,
                        iconColor = Color(0xFF9C27B0),
                        backgroundColor = Color(0xFFF3E5F5)
                    )
                )
            }
            topCategoryPercentage >= 30 -> {
                insights.add(
                    SmartInsight(
                        title = "Top Spending Category 📊",
                        description = "${topCategory.first} leads your expenses at ${DateFormatUtils.formatDouble(topCategoryPercentage, "%.1f")}%.\nThe ${topCategory.first} category is living its best life!",
                        icon = Icons.Default.TrendingUp,
                        iconColor = Color(0xFF607D8B),
                        backgroundColor = Color(0xFFECEFF1)
                    )
                )
            }
        }
    }
    
    // Insight 3: Income vs Expenses Analysis
    if (income > 0 && expenses > 0) {
        val expenseToIncomeRatio = (expenses / income) * 100
        when {
            expenseToIncomeRatio >= 90 -> {
                insights.add(
                    SmartInsight(
                        title = "High Spending Mode! 💸",
                        description = "You've spent ${DateFormatUtils.formatDouble(expenseToIncomeRatio, "%.1f")}% of your income this month.\nYour bank account is working overtime!",
                        icon = Icons.Default.Warning,
                        iconColor = Color(0xFFFF5722),
                        backgroundColor = Color(0xFFFFEBEE)
                    )
                )
            }
            expenseToIncomeRatio <= 30 -> {
                insights.add(
                    SmartInsight(
                        title = "Financial Efficiency Master! 🎯",
                        description = "You only spent ${DateFormatUtils.formatDouble(expenseToIncomeRatio, "%.1f")}% of your income this month.\nYour wallet is practically untouched!",
                        icon = Icons.Default.Star,
                        iconColor = Color(0xFFFFC107),
                        backgroundColor = Color(0xFFFFFDE7)
                    )
                )
            }
        }
    }
    
    // Insight 4: Transaction Frequency
    val transactionCount = monthTransactions.size
    when {
        transactionCount >= 50 -> {
            insights.add(
                SmartInsight(
                    title = "Transaction Marathon! 🏃‍♂️",
                    description = "You've made $transactionCount transactions this month!\nYour card is getting a serious workout!",
                    icon = Icons.Default.Receipt,
                    iconColor = Color(0xFF795548),
                    backgroundColor = Color(0xFFEFEBE9)
                )
            )
        }
        transactionCount <= 5 -> {
            insights.add(
                SmartInsight(
                    title = "Minimalist Master! ✨",
                    description = "Only $transactionCount transactions this month!\nYou're the zen master of spending!",
                    icon = Icons.Default.CheckCircle,
                    iconColor = Color(0xFF4CAF50),
                    backgroundColor = Color(0xFFE8F5E8)
                )
            )
        }
    }
    
    // Additional quirky insights for special scenarios
    if (insights.size < 3) {
        // Insight 5: Zero Income Scenario
        if (income == 0.0 && expenses > 0) {
            insights.add(
                SmartInsight(
                    title = "Mystery Income! 🕵️",
                    description = "No income recorded this month, but you have expenses.\nYour money has a secret source!",
                    icon = Icons.Default.Info,
                    iconColor = Color(0xFF2196F3),
                    backgroundColor = Color(0xFFE3F2FD)
                )
            )
        }
        
        // Insight 6: Perfect Balance
        if (income > 0 && expenses > 0 && kotlin.math.abs(income - expenses) < 10) {
            insights.add(
                SmartInsight(
                    title = "Perfect Balance! ⚖️",
                    description = "Your income and expenses are nearly perfectly balanced.\nYou've achieved financial zen!",
                    icon = Icons.Default.CheckCircle,
                    iconColor = Color(0xFF4CAF50),
                    backgroundColor = Color(0xFFE8F5E8)
                )
            )
        }
        
        // Insight 7: High Income, Low Expenses
        if (income > 0 && expenses > 0 && (income / expenses) >= 3) {
            insights.add(
                SmartInsight(
                    title = "Income Powerhouse! 💰",
                    description = "Your income is ${formatDouble1Decimal(income / expenses)}x your expenses.\nYou're basically a money-making machine!",
                    icon = Icons.Default.Star,
                    iconColor = Color(0xFFFFC107),
                    backgroundColor = Color(0xFFFFFDE7)
                )
            )
        }
        
        // Insight 8: Single Category Dominance
        if (categorySpending.size == 1 && categorySpending.isNotEmpty()) {
            val singleCategory = categorySpending.first()
            insights.add(
                SmartInsight(
                    title = "One-Category Wonder! 🎯",
                    description = "All your expenses went to ${singleCategory.first} this month.\nYou're a ${singleCategory.first} specialist!",
                    icon = Icons.Default.PieChart,
                    iconColor = Color(0xFF9C27B0),
                    backgroundColor = Color(0xFFF3E5F5)
                )
            )
        }
        
        // Insight 9: New Month with No Data
        if (monthTransactions.isEmpty()) {
            insights.add(
                SmartInsight(
                    title = "Clean Slate! 🌱",
                    description = "No transactions recorded this month yet.\nYour financial canvas is ready for a masterpiece!",
                    icon = Icons.Default.Info,
                    iconColor = Color(0xFF2196F3),
                    backgroundColor = Color(0xFFE3F2FD)
                )
            )
        }
    }
    
    // Return up to 3 insights
    return insights.take(3)
}

@Composable
private fun InsightCard(
    icon: ImageVector,
    title: String,
    description: String,
    iconColor: Color,
    backgroundColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_LARGE)
            )
            
            Spacer(Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = iconColor
                )
                // Split description into exactly 2 lines
                val descriptionLines = description.split("\n")
                if (descriptionLines.size >= 2) {
                    Text(
                        text = descriptionLines[0],
                        fontSize = 12.sp,
                        color = iconColor.copy(alpha = 0.8f)
                    )
                    Text(
                        text = descriptionLines[1],
                        fontSize = 12.sp,
                        color = iconColor.copy(alpha = 0.8f)
                    )
                } else {
                    Text(
                        text = description,
                        fontSize = 12.sp,
                        color = iconColor.copy(alpha = 0.8f)
                    )
                }
            }
            
            Icon(
                Icons.Default.AttachMoney,
                contentDescription = null,
                tint = iconColor.copy(alpha = 0.6f),
                modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_MEDIUM)
            )
        }
    }
}

@Composable
private fun GoalsTab(
    onCreateGoalClick: () -> Unit,
    goalsUpdateTrigger: Int
) {
    var showAddMoneySheet by remember { mutableStateOf(false) }
    var selectedGoalId by remember { mutableStateOf(0L) }
    var selectedGoalTitle by remember { mutableStateOf("") }
    val settingsDatabaseManager = rememberSQLiteSettingsDatabase()
    val appSettings = settingsDatabaseManager.getAppSettings().collectAsState(initial = AppSettings())
    val currencySymbol = appSettings.value.currencySymbol
    
    // Get goals database manager
    val goalsDatabaseManager = rememberSQLiteGoalsDatabase()
    
    // Get goals from database with reactive updates
    val goals by goalsDatabaseManager.getAllGoals().collectAsState(initial = emptyList())
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = AppStyleDesignSystem.Padding.LARGE),
        verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.MEDIUM)
    ) {
        item {
            Spacer(Modifier.height(AppStyleDesignSystem.Padding.MEDIUM))
            
            // Goals Overview Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.MEDIUM)
            ) {
                // Active Goals Card (1x width)
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF3B82F6) // Blue background
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
                            text = "${goals.size}",
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White,
                            style = AppStyleDesignSystem.Typography.LARGE_TITLE,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Active",
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White.copy(alpha = 0.8f),
                            style = AppStyleDesignSystem.Typography.FOOTNOTE,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                // Money Saved Card (2x width)
                Card(
                    modifier = Modifier.weight(2f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF10B981) // Green background
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "$currencySymbol${DateFormatUtils.formatDouble(goals.sumOf { it.currentAmount }, "%.0f")}",
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White,
                            style = AppStyleDesignSystem.Typography.LARGE_TITLE,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Saved till now",
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White.copy(alpha = 0.8f),
                            style = AppStyleDesignSystem.Typography.FOOTNOTE,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                
                // Percentage Card (1x width)
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF8B5CF6) // Purple background
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${if (goals.isNotEmpty()) goals.map { it.progressPercentage }.average().toInt() else 0}%",
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White,
                            style = AppStyleDesignSystem.Typography.LARGE_TITLE,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Percentage",
                            modifier = Modifier.fillMaxWidth(),
                            color = Color.White.copy(alpha = 0.8f),
                            style = AppStyleDesignSystem.Typography.FOOTNOTE,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
        
        item {
            // Create New Goal Button
            Button(
                onClick = onCreateGoalClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp
                )
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.Black,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Create New Goal",
                        color = Color.Black,
                        style = AppStyleDesignSystem.Typography.BODY
                    )
                }
            }
        }
        
        item {
            Spacer(Modifier.height(24.dp))
        }
        
            // Goals List
            if (goals.isEmpty()) {
                item {
                    // Empty state
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No goals yet",
                            style = AppStyleDesignSystem.Typography.HEADLINE,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Create your first goal to get started",
                            style = AppStyleDesignSystem.Typography.CALL_OUT,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                items(goals.size) { index ->
                    Column {
                        GoalCard(
                            goal = goals[index],
                            currencySymbol = currencySymbol
                        )
                        GoalActionBar(
                            goalId = goals[index].id,
                            goalTitle = goals[index].title,
                            onAddClick = { 
                                selectedGoalId = goals[index].id
                                selectedGoalTitle = goals[index].title
                                showAddMoneySheet = true
                            },
                            onWithdrawClick = { /* TODO: Handle withdraw money from goal */ },
                            onDeleteClick = { 
                                goalsDatabaseManager.deleteGoal(
                                    goalId = goals[index].id,
                                    onSuccess = { /* Goal deleted successfully */ },
                                    onError = { /* Handle error */ }
                                )
                            }
                        )
                    }
                }
            }
        }
        
        // Show Add Money to Goal Floating Bar
        if (showAddMoneySheet) {
            AddMoneyFloatingBar(
                goalId = selectedGoalId,
                goalTitle = selectedGoalTitle,
                onDismiss = { showAddMoneySheet = false },
                onSuccess = { 
                    // Goals will update automatically via the database flow
                }
            )
        }
    }

@Composable
private fun BudgetsTab() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Budgets Tab - Coming Soon",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun RecentLargeExpensesSection(
    transactionDatabaseManager: com.example.androidkmm.database.SQLiteTransactionDatabase? = null,
    currentMonth: Int,
    currentYear: Int,
    monthlyIncome: Double,
    currencySymbol: String = "$"
) {
    Column(
        modifier = Modifier.padding(horizontal = 0.dp)
    ) {
        Text(
            text = "Large Expenses (>8% of income)",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(Modifier.height(12.dp))
        
        // Get actual transaction data if database manager is available
        val allTransactions = if (transactionDatabaseManager != null) {
            transactionDatabaseManager.getAllTransactions().collectAsState(initial = emptyList()).value
        } else {
            emptyList()
        }
        
        // Filter transactions for the current month and get expenses > 8% of income
        val recentExpenses = remember(allTransactions, currentMonth, currentYear, monthlyIncome) {
            val monthTransactions = allTransactions.filter { transaction ->
                val transactionDate = kotlinx.datetime.LocalDate.parse(transaction.date)
                transactionDate.monthNumber == currentMonth && 
                transactionDate.year == currentYear &&
                transaction.type == com.example.androidkmm.models.TransactionType.EXPENSE
            }
            
            // Calculate 8% threshold of monthly income
            val threshold = monthlyIncome * 0.08
            
            // Filter expenses that are more than 8% of income, then sort by amount (highest first)
            monthTransactions
                .filter { it.amount > threshold }
                .sortedByDescending { it.amount }
                .take(5) // Still limit to top 5 for display
                .map { transaction ->
                    ExpenseData(
                        title = transaction.title,
                        category = transaction.category,
                        amount = transaction.amount,
                        iconColor = transaction.categoryColor,
                        icon = transaction.categoryIcon
                    )
                }
        }
        
        if (recentExpenses.isNotEmpty()) {
            recentExpenses.forEachIndexed { index, expense ->
                ExpenseCard(expense = expense, currencySymbol = currencySymbol)
                if (index < recentExpenses.size - 1) {
                    Spacer(Modifier.height(2.dp)) // Further reduced gap between cards
                }
            }
        } else {
            // Show placeholder when no data
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                            Text(
                                text = "No expenses over 8% of income this month",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                }
            }
        }
    }
}

@Composable
private fun ExpenseCard(expense: ExpenseData, currencySymbol: String = "$") {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(AppStyleDesignSystem.Sizes.BUTTON_HEIGHT)
                    .background(
                        expense.iconColor.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    expense.icon,
                    contentDescription = null,
                    tint = expense.iconColor,
                    modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_MEDIUM)
                )
            }
            
            Spacer(Modifier.width(14.dp))
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = expense.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = expense.category,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Amount
            Text(
                text = "-$currencySymbol${formatDouble2Decimals(expense.amount)}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFF44336)
            )
        }
    }
}

@Composable
private fun FinancialTrendsSection(
    transactionDatabaseManager: com.example.androidkmm.database.SQLiteTransactionDatabase? = null,
    currentMonth: Int,
    currentYear: Int
) {
    Column {
        Text(
            text = "Spending Trend",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(Modifier.height(12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                // Line Chart
                SpendingTrendLineChart(
                    transactionDatabaseManager = transactionDatabaseManager,
                    currentMonth = currentMonth,
                    currentYear = currentYear
                )
            }
        }
    }
}

@Composable
private fun SpendingByCategorySection(
    transactionDatabaseManager: com.example.androidkmm.database.SQLiteTransactionDatabase? = null,
    currentMonth: Int,
    currentYear: Int,
    currencySymbol: String = "$",
    onCategoryClick: (String) -> Unit = {}
) {
    Column {
        Text(
            text = "Spending by Category",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(Modifier.height(12.dp))
        
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.LARGE)
        ) {
            Column(
                modifier = Modifier.padding(8.dp)
            ) {
                // Get actual transaction data if database manager is available
                val allTransactions = if (transactionDatabaseManager != null) {
                    transactionDatabaseManager.getAllTransactions().collectAsState(initial = emptyList()).value
                } else {
                    emptyList()
                }
                
                // Calculate category spending for the current month
                val categoryData = remember(allTransactions, currentMonth, currentYear) {
                    val monthTransactions = allTransactions.filter { transaction ->
                        val transactionDate = kotlinx.datetime.LocalDate.parse(transaction.date)
                        transactionDate.monthNumber == currentMonth && 
                        transactionDate.year == currentYear &&
                        transaction.type == com.example.androidkmm.models.TransactionType.EXPENSE
                    }
                    
                    // Group by category and sum amounts
                    val categoryMap = monthTransactions.groupBy { it.category }
                        .mapValues { (_, transactions) -> transactions.sumOf { it.amount } }
                        .toList()
                        .sortedByDescending { it.second }
                    
                    categoryMap
                }
                
                if (categoryData.isNotEmpty()) {
                    // Donut Chart
                    SpendingDonutChart(categoryData = categoryData, currencySymbol = currencySymbol)
                    
                    Spacer(Modifier.height(20.dp))
                    
                    // Category List
                    categoryData.forEach { (category, amount) ->
                        val percentage = if (categoryData.sumOf { it.second } > 0) {
                            (amount / categoryData.sumOf { it.second } * 100).toInt()
                        } else 0
                        
                        CategorySpendingItem(
                            category = category,
                            amount = amount,
                            percentage = percentage,
                            currencySymbol = currencySymbol,
                            onCategoryClick = {
                                onCategoryClick(category)
                            }
                        )
                        
                        if (categoryData.indexOf(category to amount) < categoryData.size - 1) {
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                } else {
                    // No data placeholder
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No spending data for this month",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SpendingDonutChart(categoryData: List<Pair<String, Double>>, currencySymbol: String = "$") {
    val colors = listOf(
        Color(0xFF4CAF50), // Green
        Color(0xFFF44336), // Red
        Color(0xFF9C27B0), // Purple
        Color(0xFFE91E63), // Pink
        Color(0xFF2196F3), // Blue
        Color(0xFFFF9800), // Orange
        Color(0xFF795548), // Brown
        Color(0xFF607D8B)  // Blue Grey
    )
    
    val totalAmount = categoryData.sumOf { it.second }
    var hoveredSegment by remember { mutableStateOf<Int?>(null) }
    
    if (totalAmount > 0) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(AppStyleDesignSystem.Sizes.ICON_SIZE_GIANT),
                contentAlignment = Alignment.Center
            ) {
                // Donut chart using Canvas
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onLongPress = { offset ->
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val radius = kotlin.math.min(size.width, size.height) / 2f - 20f
                                    
                                    // Calculate which segment is being long pressed
                                    val angle = kotlin.math.atan2(
                                        offset.y - center.y,
                                        offset.x - center.x
                                    ) * 180f / kotlin.math.PI.toFloat()
                                    
                                    val normalizedAngle = (angle + 90f + 360f) % 360f
                                    
                                    var currentAngle = 0f
                                    categoryData.forEachIndexed { index, (_, amount) ->
                                        val sweepAngle = (amount / totalAmount * 360f).toFloat()
                                        
                                        if (normalizedAngle >= currentAngle && normalizedAngle < currentAngle + sweepAngle) {
                                            hoveredSegment = index
                                        }
                                        
                                        currentAngle += sweepAngle
                                    }
                                    
                                    // Keep tooltip visible while long pressing
                                    // Tooltip will be hidden when long press ends
                                },
                                onPress = { offset ->
                                    // Clear tooltip on regular press
                                    hoveredSegment = null
                                }
                            )
                        }
                ) {
                    val center = Offset(size.width / 2, size.height / 2)
                    val radius = kotlin.math.min(size.width, size.height) / 2f - 20f
                    val innerRadius = radius * 0.6f
                    
                    var startAngle = -90f
                    
                    categoryData.forEachIndexed { index, (_, amount) ->
                        val sweepAngle = (amount / totalAmount * 360f).toFloat()
                        val color = colors[index % colors.size]
                        val isHovered = hoveredSegment == index
                        
                        // Draw outer arc with straight edges and enhanced stroke for hovered segment
                        drawArc(
                            color = color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(center.x - radius, center.y - radius),
                            size = Size(radius * 2, radius * 2),
                            style = Stroke(
                                width = if (isHovered) 32.dp.toPx() else 24.dp.toPx(), 
                                cap = StrokeCap.Butt,
                                join = StrokeJoin.Miter
                            )
                        )
                        
                        startAngle += sweepAngle
                    }
                }
                
                // Center text
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "$currencySymbol${formatDouble0Decimals(totalAmount)}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Total Spent",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Tooltip for hovered segment - positioned next to the segment
                hoveredSegment?.let { index ->
                    val (category, amount) = categoryData[index]
                    val percentage = (amount / totalAmount * 100).toInt()
                    
                    // Calculate position based on segment angle
                    var currentAngle = -90f
                    for (i in 0 until index) {
                        currentAngle += (categoryData[i].second / totalAmount * 360f).toFloat()
                    }
                    val segmentAngle = currentAngle + (amount / totalAmount * 360f / 2f) // Middle of segment
                    
                    // Smart positioning to utilize black spaces on left and right
                    val radius = AppStyleDesignSystem.Sizes.ICON_SIZE_GIANT / 2f
                    val angleRad = kotlin.math.PI * segmentAngle.toDouble() / 180.0
                    
                    // Determine if segment is on left or right side of chart
                    val isLeftSide = segmentAngle in 90f..270f
                    
                    // Position tooltip in black spaces on left or right
                    val offsetX = if (isLeftSide) {
                        -120.dp // Left side black space
                    } else {
                        120.dp // Right side black space
                    }
                    
                    // Center vertically with slight adjustment based on segment position
                    val baseY = (radius.value * kotlin.math.sin(angleRad)).toFloat()
                    val offsetY = baseY.dp
                    
                    Card(
                        modifier = Modifier
                            .offset(x = offsetX, y = offsetY)
                            .padding(8.dp)
                            .width(140.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF1E1E1E)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                        border = BorderStroke(1.dp, Color(0xFF333333))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Category name with icon
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(
                                            color = colors[index % colors.size],
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = category,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Amount
                            Text(
                                text = "$currencySymbol${formatDouble0Decimals(amount)}",
                                color = Color.White,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            
                            // Percentage
                            Text(
                                text = "$percentage% of total",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategorySpendingItem(
    category: String,
    amount: Double,
    percentage: Int,
    currencySymbol: String = "$",
    onCategoryClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCategoryClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category icon (placeholder)
            Box(
                modifier = Modifier
                    .size(AppStyleDesignSystem.Sizes.AVATAR_MEDIUM)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Category,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_MEDIUM)
                )
            }
            
            Spacer(Modifier.width(12.dp))
            
            // Category info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = category,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "$currencySymbol${formatDouble2Decimals(amount)} • $percentage%",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Arrow icon
            Icon(
                Icons.Default.ArrowForward,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_SMALL)
            )
        }
    }
}

@Composable
private fun SpendingTrendLineChart(
    transactionDatabaseManager: com.example.androidkmm.database.SQLiteTransactionDatabase? = null,
    currentMonth: Int,
    currentYear: Int
) {
    // Get actual transaction data if database manager is available
    val allTransactions = if (transactionDatabaseManager != null) {
        transactionDatabaseManager.getAllTransactions().collectAsState(initial = emptyList()).value
    } else {
        emptyList()
    }
    
    // Calculate data for the past 6 months
    val chartData = remember(allTransactions, currentMonth, currentYear) {
        val monthNames = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val months = mutableListOf<String>()
        val spendingData = mutableListOf<Double>()
        
        // Calculate the past 6 months from current month
        for (i in 5 downTo 0) {
            var month = currentMonth - i
            var year = currentYear
            
            // Handle year rollover
            if (month <= 0) {
                month += 12
                year--
            }
            
            months.add(monthNames[month - 1])
            
            // Filter transactions for this month
            val monthTransactions = allTransactions.filter { transaction ->
                val transactionDate = kotlinx.datetime.LocalDate.parse(transaction.date)
                transactionDate.monthNumber == month && 
                transactionDate.year == year
            }
            
            // Calculate expenses for this month
            val expenses = monthTransactions
                .filter { it.type == com.example.androidkmm.models.TransactionType.EXPENSE }
                .sumOf { it.amount }
            
            spendingData.add(expenses)
        }
        
        Pair(months, spendingData)
    }
    
    val (months, spendingData) = chartData
    val maxValue = spendingData.maxOrNull() ?: 0.0
    // Dynamic Y-axis scaling: max value + 25% buffer, divided into 5 chunks
    val yAxisMax = if (maxValue > 0) maxValue * 1.25 else 1000.0
    val yAxisStep = yAxisMax / 4.0 // 5 chunks (0, 1, 2, 3, 4)
    
    var hoveredIndex by remember { mutableStateOf<Int?>(null) }
    var hoveredPointOffset by remember { mutableStateOf<Offset?>(null) }
    
    Column {
        // Y-axis labels on the left side
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Y-axis labels
            Column(
                modifier = Modifier
                    .width(50.dp)
                    .height(120.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(5) { index ->
                    val value = yAxisMax - (yAxisStep * index)
                    val displayValue = if (value >= 1000) {
                        "${formatDouble0Decimals(value / 1000)}k"
                    } else {
                        "${value.toInt()}"
                    }
                    Text(
                        text = displayValue,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
            
            // Chart area with line graph
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp)
            ) {
                val chartWidthPx = constraints.maxWidth.toFloat()
                val chartHeightPx = constraints.maxHeight.toFloat()
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            detectTapGestures { offset ->
                                val chartWidth = size.width - 20.dp.toPx()
                                val chartHeight = size.height - 20.dp.toPx()
                                val startX = 10.dp.toPx()
                                val startY = 10.dp.toPx()
                                // Use the same dynamic Y-axis scale
                                
                                // Calculate which data point is closest to the tap
                                val tapX = offset.x
                                var closestIndex = 0
                                var minDistance = Float.MAX_VALUE
                                var closestPoint = Offset.Zero
                                
                                spendingData.forEachIndexed { index, value ->
                                    val pointX = startX + (chartWidth * index.toFloat() / (spendingData.size - 1).toFloat())
                                    val calculatedHeight = if (yAxisMax > 0) (value / yAxisMax * chartHeight).toFloat() else 0f
                                    val pointY = (startY + chartHeight).toFloat() - calculatedHeight
                                    val point = Offset(pointX, pointY)
                                    
                                    val distance = kotlin.math.abs(tapX - pointX)
                                    if (distance < minDistance) {
                                        minDistance = distance
                                        closestIndex = index
                                        closestPoint = point
                                    }
                                }
                                
                                if (minDistance < 30.dp.toPx()) {
                                    hoveredIndex = closestIndex
                                    hoveredPointOffset = closestPoint
                                } else {
                                    hoveredIndex = null
                                    hoveredPointOffset = null
                                }
                            }
                        }
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val chartHeight = canvasHeight - 20.dp.toPx() // Leave space for labels
                    val chartWidth = canvasWidth - 20.dp.toPx() // Leave space for margins
                    val startX = 10.dp.toPx()
                    val startY = 10.dp.toPx()
                    
                    // Draw grid lines
                    // Horizontal grid lines
                    repeat(5) { index ->
                        val y = startY + (chartHeight * index.toFloat() / 4f)
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.2f),
                            start = Offset(startX, y),
                            end = Offset(startX + chartWidth, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    
                    // Vertical grid lines
                    repeat(spendingData.size) { index ->
                        val x = startX + (chartWidth * index.toFloat() / (spendingData.size - 1).toFloat())
                        drawLine(
                            color = Color.Gray.copy(alpha = 0.2f),
                            start = Offset(x, startY),
                            end = Offset(x, startY + chartHeight),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    
                    // Calculate points for the line
                    // Use dynamic scale for Y-axis to match the Y-axis labels
                    val points = spendingData.mapIndexed { index, value ->
                        val x = startX + (chartWidth * index.toFloat() / (spendingData.size - 1).toFloat())
                        val calculatedHeight = if (yAxisMax > 0) (value / yAxisMax * chartHeight).toFloat() else 0f
                        val y = (startY + chartHeight).toFloat() - calculatedHeight
                        Offset(x, y)
                    }
                    
                    // Draw the line
                    if (points.size > 1) {
                        drawLine(
                            color = Color(0xFF2196F3), // Blue color
                            start = points[0],
                            end = points[1],
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                        
                        for (i in 1 until points.size - 1) {
                            drawLine(
                                color = Color(0xFF2196F3),
                                start = points[i],
                                end = points[i + 1],
                                strokeWidth = 3.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        }
                    }
                    
                    // Draw data points
                    points.forEachIndexed { index, point ->
                        val isHovered = hoveredIndex == index
                        drawCircle(
                            color = Color(0xFF2196F3),
                            radius = if (isHovered) 6.dp.toPx() else 4.dp.toPx(),
                            center = point
                        )
                        
                        // Draw white border for hovered point
                        if (isHovered) {
                            drawCircle(
                                color = Color.White,
                                radius = if (isHovered) 6.dp.toPx() else 4.dp.toPx(),
                                center = point,
                                style = Stroke(width = AppStyleDesignSystem.Sizes.BORDER_THICK.toPx())
                            )
                        }
                    }
                    
                    // Draw vertical line for hovered point
                    hoveredIndex?.let { index ->
                        if (index < points.size) {
                            val point = points[index]
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.5f),
                                start = Offset(point.x, startY),
                                end = Offset(point.x, startY + chartHeight),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }
                }
                
                // Tooltip for hovered point
                hoveredIndex?.let { index ->
                    hoveredPointOffset?.let { pointOffset ->
                        if (index < spendingData.size) {
                            val value = spendingData[index]
                            val month = months[index]
                            
                            // Convert the Canvas pixel coordinates to dp for positioning
                            // The pointOffset is in pixels from the Canvas
                            val pointXDp = (pointOffset.x / 3.5f).dp // Convert pixels to dp
                            val pointYDp = (pointOffset.y / 3.5f).dp // Convert pixels to dp
                            
                            // Position tooltip right next to the actual data point
                            val tooltipX = if (index < spendingData.size / 2) {
                                pointXDp + 8.dp // Right side for left half
                            } else {
                                pointXDp - 70.dp // Left side for right half (increased for wider tooltip)
                            }
                            
                            val tooltipY = pointYDp - 45.dp // Above the point (increased for taller tooltip)
                            
                            Card(
                                modifier = Modifier
                                    .width(65.dp) // Fixed width for better appearance
                                    .offset(
                                        x = tooltipX,
                                        y = tooltipY
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.95f)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = month,
                                        fontSize = 10.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (value >= 1000) {
                                            val kValue = value / 1000.0
                                            if (kValue % 1.0 == 0.0) {
                                                "${kValue.toInt()}k" // Show "2k" for 2000
                                            } else {
                                                "${formatDouble1Decimal(kValue)}k" // Show "2.6k" for 2633
                                            }
                                        } else {
                                            "${value.toInt()}" // Show "500" for 500
                                        },
                                        fontSize = 12.sp,
                                        color = Color(0xFF2196F3),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                    }
                }
                }            }
        }
        
        // X-axis labels (months)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 50.dp), // Align with chart area
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            months.forEach { month ->
                Text(
                    text = month,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RecurringTab() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Recurring Tab - Coming Soon",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp
        )
    }
}

// Helper function to get icon from name
private fun getIconFromName(iconName: String): ImageVector {
    return when (iconName) {
        "Restaurant" -> Icons.Default.Restaurant
        "AttachMoney" -> Icons.Default.AttachMoney
        "ShoppingCart" -> Icons.Default.ShoppingCart
        "SwapHoriz" -> Icons.Default.SwapHoriz
        "AccountBalance" -> Icons.Default.AccountBalance
        "CreditCard" -> Icons.Default.CreditCard
        "Wallet" -> Icons.Default.Wallet
        "Business" -> Icons.Default.Business
        "Flight" -> Icons.Default.Flight
        "Savings" -> Icons.Default.Savings
        "Group" -> Icons.Default.Group
        "CardGiftcard" -> Icons.Default.CardGiftcard
        "Work" -> Icons.Default.Work
        "TrendingUp" -> Icons.Default.TrendingUp
        "Home" -> Icons.Default.Home
        "School" -> Icons.Default.School
        "LocalHospital" -> Icons.Default.LocalHospital
        "Movie" -> Icons.Default.Movie
        "DirectionsCar" -> Icons.Default.DirectionsCar
        "Lightbulb" -> Icons.Default.Lightbulb
        "Stars" -> Icons.Default.Stars
        "Category" -> Icons.Default.Category
        "Favorite" -> Icons.Default.Favorite
        else -> Icons.Default.Category
    }
}

// Savings Rate Section
@Composable
private fun SavingsRateSection(
    transactionDatabaseManager: com.example.androidkmm.database.SQLiteTransactionDatabase?,
    currentMonth: Int,
    currentYear: Int
) {
    val allTransactions = if (transactionDatabaseManager != null) {
        transactionDatabaseManager.getAllTransactions().collectAsState(initial = emptyList()).value
    } else {
        emptyList()
    }
    
    // Calculate savings rate for the previous 4 months (in reverse order: current month first)
    val savingsData = remember(allTransactions, currentMonth, currentYear) {
        val months = mutableListOf<Pair<String, Double>>()
        
        for (i in 0..3) {
            val targetMonth = if (currentMonth - i > 0) currentMonth - i else currentMonth - i + 12
            val targetYear = if (currentMonth - i > 0) currentYear else currentYear - 1
            
            val monthTransactions = allTransactions.filter { transaction ->
                val transactionDate = kotlinx.datetime.LocalDate.parse(transaction.date)
                transactionDate.monthNumber == targetMonth && 
                transactionDate.year == targetYear
            }
            
            val income = monthTransactions
                .filter { it.type == com.example.androidkmm.models.TransactionType.INCOME }
                .sumOf { it.amount }
            
            val expenses = monthTransactions
                .filter { it.type == com.example.androidkmm.models.TransactionType.EXPENSE }
                .sumOf { it.amount }
            
            val savings = income - expenses
            val savingsRate = if (income > 0) (savings / income) * 100 else 0.0
            
            val monthName = when (targetMonth) {
                1 -> "Jan"
                2 -> "Feb"
                3 -> "Mar"
                4 -> "Apr"
                5 -> "May"
                6 -> "Jun"
                7 -> "Jul"
                8 -> "Aug"
                9 -> "Sep"
                10 -> "Oct"
                11 -> "Nov"
                12 -> "Dec"
                else -> "Unknown"
            }
            
            months.add(Pair(monthName, savingsRate))
        }
        
        months
    }
    
    Column {
        Text(
            text = "Savings Rate",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        savingsData.forEach { (month, rate) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Savings rate at the start (left side)
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (rate >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        contentDescription = "Savings Rate",
                        tint = if (rate >= 0) Color(0xFF4CAF50) else Color(0xFFF44336), // Green for positive, red for negative
                        modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_SMALL)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${formatDouble1Decimal(rate)}%",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (rate >= 0) Color(0xFF4CAF50) else Color(0xFFF44336), // Green for positive, red for negative
                        fontWeight = FontWeight.Medium
                    )
                }

                // Month name at the end (right side)
                Text(
                    text = month,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// Data classes
data class ExpenseData(
    val title: String,
    val category: String,
    val amount: Double,
    val iconColor: Color,
    val icon: ImageVector
)

private fun formatDouble2Decimals(value: Double): String {
    return String.format("%.2f", value)
}

private fun formatDouble1Decimal(value: Double): String {
    return String.format("%.1f", value)
}

private fun formatDouble0Decimals(value: Double): String {
    return value.toLong().toString()
}
