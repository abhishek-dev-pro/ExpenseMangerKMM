package com.example.androidkmm.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.design.DesignSystem
import com.example.androidkmm.utils.TextUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen() {
    var selectedTab by remember { mutableStateOf(0) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Financial Insights",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
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
                            fontSize = 14.sp,
                            fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                        )
                    }
                )
            }
        }
        
        // Content based on selected tab
        when (selectedTab) {
            0 -> OverviewTab()
            1 -> GoalsTab()
            2 -> BudgetsTab()
            3 -> RecurringTab()
        }
    }
}

@Composable
private fun OverviewTab() {
    var currentMonth by remember { mutableStateOf(9) } // September = 9
    var currentYear by remember { mutableStateOf(2025) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(DesignSystem.Spacing.safeAreaPadding),
        verticalArrangement = Arrangement.spacedBy(DesignSystem.Spacing.sm)
    ) {
        item { Spacer(Modifier.height(DesignSystem.Spacing.sm)) }
        
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
                    if (currentMonth < 12) {
                        currentMonth++
                    } else {
                        currentMonth = 1
                        currentYear++
                    }
                }
            )
        }
        
        // Monthly Summary
        item {
            MonthlySummaryCard(currentMonth = currentMonth)
        }
        
        // Smart Insights
        item {
            SmartInsightsSection()
        }
        
        // Recent Large Expenses
        item {
            RecentLargeExpensesSection()
        }
        
        // Financial Trends Chart
        item {
            FinancialTrendsSection()
        }
        
        item { Spacer(Modifier.height(DesignSystem.Spacing.lg)) }
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
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(DesignSystem.CornerRadius.lg)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
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
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Current Month",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(onClick = onNextMonth) {
                Icon(
                    Icons.Default.KeyboardArrowRight,
                    contentDescription = "Next month",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun MonthlySummaryCard(currentMonth: Int) {
    val monthNames = listOf(
        "January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December"
    )
    
    // Sample data - in real app, this would come from database
    val monthlyData = mapOf(
        1 to Triple(3000.0, 1200.0, 1800.0), // Jan: income, expenses, savings
        2 to Triple(3200.0, 1100.0, 2100.0), // Feb
        3 to Triple(3300.0, 1300.0, 2000.0), // Mar
        4 to Triple(3100.0, 1400.0, 1700.0), // Apr
        5 to Triple(3500.0, 1200.0, 2300.0), // May
        6 to Triple(3000.0, 1100.0, 1900.0), // Jun
        7 to Triple(3200.0, 1300.0, 1900.0), // Jul
        8 to Triple(3400.0, 1200.0, 2200.0), // Aug
        9 to Triple(3200.0, 1121.0, 2079.0), // Sep
        10 to Triple(3100.0, 1300.0, 1800.0), // Oct
        11 to Triple(3300.0, 1400.0, 1900.0), // Nov
        12 to Triple(3600.0, 1500.0, 2100.0)  // Dec
    )
    
    val (income, expenses, savings) = monthlyData[currentMonth] ?: Triple(3200.0, 1121.0, 2079.0)
    val savingsRate = ((savings / income) * 100).toInt()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Monthly Summary",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = monthNames[currentMonth - 1],
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Savings rate badge
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF10B981).copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "$savingsRate%",
                        color = Color(0xFF10B981),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Financial metrics
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Income
                FinancialMetricCard(
                    icon = Icons.Default.TrendingUp,
                    label = "Income",
                    amount = "$${income.toInt()}",
                    color = Color(0xFF10B981),
                    backgroundColor = Color(0xFF10B981).copy(alpha = 0.1f)
                )
                
                // Expenses
                FinancialMetricCard(
                    icon = Icons.Default.TrendingDown,
                    label = "Expenses",
                    amount = "$${expenses.toInt()}",
                    color = Color(0xFFEF4444),
                    backgroundColor = Color(0xFFEF4444).copy(alpha = 0.1f)
                )
                
                // Savings
                FinancialMetricCard(
                    icon = Icons.Default.AccountBalance,
                    label = "Savings",
                    amount = "$${savings.toInt()}",
                    color = Color(0xFF3B82F6),
                    backgroundColor = Color(0xFF3B82F6).copy(alpha = 0.1f)
                )
            }
        }
    }
}

@Composable
private fun FinancialMetricCard(
    icon: ImageVector,
    label: String,
    amount: String,
    color: Color,
    backgroundColor: Color
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.padding(horizontal = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon with background
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color.copy(alpha = 0.2f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            // Label
            Text(
                text = label,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(Modifier.height(2.dp))
            
            // Amount
            Text(
                text = amount,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
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
            modifier = Modifier.size(20.dp)
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
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "$label: $amount",
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun SmartInsightsSection() {
    Column {
        Text(
            text = "Smart Insights",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(Modifier.height(12.dp))
        
        // Excellent savings card
        InsightCard(
            icon = Icons.Default.CheckCircle,
            title = "Excellent savings!",
            description = "You saved 65.0% of your income this month",
            iconColor = Color(0xFF4CAF50),
            backgroundColor = Color(0xFFE8F5E8)
        )
        
        Spacer(Modifier.height(8.dp))
        
        // Budget alert card
        InsightCard(
            icon = Icons.Default.Warning,
            title = "Transportation budget alert",
            description = "You've used 93% of your Transportation budget",
            iconColor = Color(0xFFFF9800),
            backgroundColor = Color(0xFFFFF3E0)
        )
    }
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
        shape = RoundedCornerShape(DesignSystem.CornerRadius.md)
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
                modifier = Modifier.size(24.dp)
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
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = iconColor.copy(alpha = 0.8f)
                )
            }
            
            Icon(
                Icons.Default.AttachMoney,
                contentDescription = null,
                tint = iconColor.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun GoalsTab() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Goals Tab - Coming Soon",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 16.sp
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
private fun RecentLargeExpensesSection() {
    Column {
        Text(
            text = "Recent Large Expenses",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(Modifier.height(12.dp))
        
        // Sample expense data
        val recentExpenses = listOf(
            ExpenseData("Rent", "Home & Utilities", 650.00, Color(0xFF4CAF50), Icons.Default.Home),
            ExpenseData("Gym & Supplements", "Health & Fitness", 180.30, Color(0xFFF44336), Icons.Default.Favorite),
            ExpenseData("Clothes", "Shopping", 120.00, Color(0xFF9C27B0), Icons.Default.ShoppingCart)
        )
        
        recentExpenses.forEach { expense ->
            ExpenseCard(expense = expense)
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ExpenseCard(expense: ExpenseData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(DesignSystem.CornerRadius.md)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
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
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(Modifier.width(12.dp))
            
            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = expense.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = expense.category,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Amount
            Text(
                text = "-$${String.format("%.2f", expense.amount)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFF44336)
            )
        }
    }
}

@Composable
private fun FinancialTrendsSection() {
    Column {
        Text(
            text = "Financial Trends",
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
            shape = RoundedCornerShape(DesignSystem.CornerRadius.lg)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Income vs Expenses",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                Spacer(Modifier.height(16.dp))
                
                // Bar Chart
                IncomeExpenseBarChart()
            }
        }
    }
}

@Composable
private fun IncomeExpenseBarChart() {
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
    val incomeData = listOf(3000, 3000, 3300, 3000, 3500, 3000)
    val expenseData = listOf(2000, 2200, 1900, 2700, 2000, 1900)
    val maxValue = maxOf(incomeData.maxOrNull() ?: 0, expenseData.maxOrNull() ?: 0)
    
    Column {
        // Y-axis labels
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            repeat(5) { index ->
                val value = (maxValue * (4 - index) / 4)
                Text(
                    text = value.toString(),
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(Modifier.height(8.dp))
        
        // Chart area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Bottom
        ) {
            months.forEachIndexed { index, month ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Bars
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        // Income bar (green)
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height((incomeData[index] * 100 / maxValue).dp)
                                .background(
                                    Color(0xFF4CAF50),
                                    RoundedCornerShape(2.dp)
                                )
                        )
                        
                        // Expense bar (red)
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height((expenseData[index] * 100 / maxValue).dp)
                                .background(
                                    Color(0xFFF44336),
                                    RoundedCornerShape(2.dp)
                                )
                        )
                    }
                    
                    Spacer(Modifier.height(4.dp))
                    
                    // Month label
                    Text(
                        text = month,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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

// Data classes
data class ExpenseData(
    val title: String,
    val category: String,
    val amount: Double,
    val iconColor: Color,
    val icon: ImageVector
)
