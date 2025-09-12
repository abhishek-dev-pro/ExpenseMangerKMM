@file:OptIn(ExperimentalMaterial3Api::class)

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.database.rememberSQLiteCategoryDatabase
import com.example.androidkmm.database.rememberSQLiteAccountDatabase
import com.example.androidkmm.design.DesignSystem
import com.example.androidkmm.models.Category
import com.example.androidkmm.models.Account
import com.example.androidkmm.models.TransactionType

// Filter data classes
data class FilterOptions(
    val transactionType: TransactionType? = null,
    val selectedCategories: Set<String> = emptySet(),
    val selectedAccounts: Set<String> = emptySet(),
    val dateRange: DateRange? = null,
    val amountRange: AmountRange? = null
)

data class DateRange(
    val from: String = "",
    val to: String = "",
    val predefined: PredefinedDateRange? = null
)

enum class PredefinedDateRange {
    TODAY, THIS_WEEK, THIS_MONTH, LAST_3_MONTHS
}

data class AmountRange(
    val min: Double? = null,
    val max: Double? = null,
    val predefined: PredefinedAmountRange? = null
)

enum class PredefinedAmountRange {
    UNDER_25, BETWEEN_25_100, BETWEEN_100_500, OVER_500
}

// Color definitions
object FilterColors {
    val background = Color(0xFF121212)
    val surface = Color(0xFF1E1E1E)
    val primaryText = Color(0xFFFFFFFF)
    val secondaryText = Color(0xFF8E8E93)
    val divider = Color(0xFF38383A)
    val selectedBorder = Color(0xFFFFFFFF)
    val unselectedBackground = Color(0xFF2C2C2E)
    val income = Color(0xFF10B981)
    val expense = Color(0xFFEF4444)
    val transfer = Color(0xFF3B82F6)
}

@Composable
fun FilterTransactionsBottomSheet(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onApplyFilters: (FilterOptions) -> Unit,
    initialFilters: FilterOptions = FilterOptions()
) {
    if (!isVisible) return
    
    var filterOptions by remember { mutableStateOf(initialFilters) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = FilterColors.background,
        contentColor = FilterColors.primaryText,
        dragHandle = {
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(4.dp)
                    .background(
                        color = FilterColors.secondaryText.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(2.dp)
                    )
            )
        }
    ) {
        FilterContent(
            filterOptions = filterOptions,
            onFilterOptionsChange = { filterOptions = it },
            onApplyFilters = { onApplyFilters(filterOptions) },
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun FilterContent(
    filterOptions: FilterOptions,
    onFilterOptionsChange: (FilterOptions) -> Unit,
    onApplyFilters: () -> Unit,
    onDismiss: () -> Unit
) {
    val categoryDatabaseManager = rememberSQLiteCategoryDatabase()
    val accountDatabaseManager = rememberSQLiteAccountDatabase()
    
    val categoriesState = categoryDatabaseManager.getAllCategories().collectAsState(initial = emptyList<Category>())
    val accountsState = accountDatabaseManager.getAllAccounts().collectAsState(initial = emptyList<Account>())
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        // Header
        FilterHeader(onDismiss = onDismiss)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Transaction Type Section
            item {
                TransactionTypeSection(
                    selectedType = filterOptions.transactionType,
                    onTypeSelected = { type ->
                        onFilterOptionsChange(filterOptions.copy(transactionType = type))
                    }
                )
            }
            
            // Categories Section
            item {
                CategoriesSection(
                    categories = categoriesState.value,
                    selectedCategories = filterOptions.selectedCategories,
                    onCategoryToggle = { categoryName ->
                        val newSelected = if (filterOptions.selectedCategories.contains(categoryName)) {
                            filterOptions.selectedCategories - categoryName
                        } else {
                            filterOptions.selectedCategories + categoryName
                        }
                        onFilterOptionsChange(filterOptions.copy(selectedCategories = newSelected))
                    }
                )
            }
            
            // Accounts Section
            item {
                AccountsSection(
                    accounts = accountsState.value,
                    selectedAccounts = filterOptions.selectedAccounts,
                    onAccountToggle = { accountName ->
                        val newSelected = if (filterOptions.selectedAccounts.contains(accountName)) {
                            filterOptions.selectedAccounts - accountName
                        } else {
                            filterOptions.selectedAccounts + accountName
                        }
                        onFilterOptionsChange(filterOptions.copy(selectedAccounts = newSelected))
                    }
                )
            }
            
            // Date Range Section
            item {
                DateRangeSection(
                    dateRange = filterOptions.dateRange ?: DateRange(),
                    onDateRangeChange = { newDateRange ->
                        onFilterOptionsChange(filterOptions.copy(dateRange = newDateRange))
                    }
                )
            }
            
            // Amount Range Section
            item {
                AmountRangeSection(
                    amountRange = filterOptions.amountRange ?: AmountRange(),
                    onAmountRangeChange = { newAmountRange ->
                        onFilterOptionsChange(filterOptions.copy(amountRange = newAmountRange))
                    }
                )
            }
            
            // Action Buttons
            item {
                ActionButtons(
                    onApplyFilters = onApplyFilters,
                    onCancel = onDismiss
                )
            }
        }
    }
}

@Composable
private fun FilterHeader(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Filter Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(FilterColors.surface)
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter",
                tint = FilterColors.primaryText,
                modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.Center)
            )
        }
        
        // Title and Subtitle
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Filter Transactions",
                color = FilterColors.primaryText,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Customize your transaction view",
                color = FilterColors.secondaryText,
                fontSize = 14.sp
            )
        }
        
        // Close Icon
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(FilterColors.surface)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close",
                tint = FilterColors.primaryText,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun TransactionTypeSection(
    selectedType: TransactionType?,
    onTypeSelected: (TransactionType?) -> Unit
) {
    Column {
        Text(
            text = "Transaction Type",
            color = FilterColors.primaryText,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // All Transactions
            FilterOptionCard(
                icon = Icons.Default.AttachMoney,
                iconColor = FilterColors.secondaryText,
                title = "All Transactions",
                subtitle = "Show all transactions",
                isSelected = selectedType == null,
                onClick = { onTypeSelected(null) }
            )
            
            // Expenses Only
            FilterOptionCard(
                icon = Icons.Default.TrendingDown,
                iconColor = FilterColors.expense,
                title = "Expenses Only",
                subtitle = "Show only expenses",
                isSelected = selectedType == TransactionType.EXPENSE,
                onClick = { onTypeSelected(TransactionType.EXPENSE) }
            )
            
            // Income Only
            FilterOptionCard(
                icon = Icons.Default.TrendingUp,
                iconColor = FilterColors.income,
                title = "Income Only",
                subtitle = "Show only income",
                isSelected = selectedType == TransactionType.INCOME,
                onClick = { onTypeSelected(TransactionType.INCOME) }
            )
            
            // Transfers Only
            FilterOptionCard(
                icon = Icons.Default.SwapHoriz,
                iconColor = FilterColors.transfer,
                title = "Transfers Only",
                subtitle = "Show only transfers",
                isSelected = selectedType == TransactionType.TRANSFER,
                onClick = { onTypeSelected(TransactionType.TRANSFER) }
            )
        }
    }
}

@Composable
private fun FilterOptionCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .clip(RoundedCornerShape(DesignSystem.CornerRadius.md))
            .border(
                width = 0.5.dp, // very thin border
                color = Color.White.copy(alpha = 0.2f), // subtle white
                shape = RoundedCornerShape(DesignSystem.CornerRadius.md)
            ),
        shape = RoundedCornerShape(DesignSystem.CornerRadius.md),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) FilterColors.surface else FilterColors.unselectedBackground
        )
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
                    .clip(CircleShape)
                    .background(iconColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Text Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = FilterColors.primaryText,
                    fontSize = 16.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                )
                
                Text(
                    text = subtitle,
                    color = FilterColors.secondaryText,
                    fontSize = 14.sp
                )
            }
            
            // Selection Indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(FilterColors.primaryText),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = FilterColors.background,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoriesSection(
    categories: List<Category>,
    selectedCategories: Set<String>,
    onCategoryToggle: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Categories",
                color = FilterColors.primaryText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            // Show selected count
            if (selectedCategories.isNotEmpty()) {
                Text(
                    text = "${selectedCategories.size} selected",
                    color = FilterColors.secondaryText,
                    fontSize = 12.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Use LazyVerticalGrid for better UX with many categories
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 200.dp) // Limit height to prevent bottom sheet from becoming too tall
        ) {
            items(categories) { category ->
                CategoryChip(
                    category = category,
                    isSelected = selectedCategories.contains(category.name),
                    onClick = { onCategoryToggle(category.name) }
                )
            }
        }
        
        // Quick action buttons
        if (categories.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Select All button
                OutlinedButton(
                    onClick = { 
                        categories.forEach { category ->
                            if (!selectedCategories.contains(category.name)) {
                                onCategoryToggle(category.name)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = FilterColors.primaryText
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        FilterColors.divider
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Select All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Clear All button
                OutlinedButton(
                    onClick = { 
                        selectedCategories.forEach { categoryName ->
                            onCategoryToggle(categoryName)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = FilterColors.primaryText
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        FilterColors.divider
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Clear All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .clip(RoundedCornerShape(DesignSystem.CornerRadius.md))
            .border(
                width = 0.5.dp, // very thin border
                color = Color.White.copy(alpha = 0.2f), // subtle white
                shape = RoundedCornerShape(DesignSystem.CornerRadius.md)
            ),
        shape = RoundedCornerShape(DesignSystem.CornerRadius.md),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) FilterColors.surface else FilterColors.unselectedBackground
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Category Icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(category.color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = category.name,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(6.dp))
            
            Text(
                text = category.name,
                color = FilterColors.primaryText,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                maxLines = 2,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            
            // Selection indicator for categories
            if (isSelected) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(FilterColors.primaryText),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = FilterColors.background,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountsSection(
    accounts: List<Account>,
    selectedAccounts: Set<String>,
    onAccountToggle: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Accounts",
                color = FilterColors.primaryText,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            // Show selected count
            if (selectedAccounts.isNotEmpty()) {
                Text(
                    text = "${selectedAccounts.size} selected",
                    color = FilterColors.secondaryText,
                    fontSize = 12.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Use LazyVerticalGrid for better UX with many accounts
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.heightIn(max = 120.dp) // Limit height since accounts are usually fewer
        ) {
            items(accounts) { account ->
                AccountChip(
                    account = account,
                    isSelected = selectedAccounts.contains(account.name),
                    onClick = { onAccountToggle(account.name) }
                )
            }
        }
        
        // Quick action buttons for accounts
        if (accounts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Select All button
                OutlinedButton(
                    onClick = { 
                        accounts.forEach { account ->
                            if (!selectedAccounts.contains(account.name)) {
                                onAccountToggle(account.name)
                            }
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = FilterColors.primaryText
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        FilterColors.divider
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Select All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Clear All button
                OutlinedButton(
                    onClick = { 
                        selectedAccounts.forEach { accountName ->
                            onAccountToggle(accountName)
                        }
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = FilterColors.primaryText
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        FilterColors.divider
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Clear All",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountChip(
    account: Account,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .clip(RoundedCornerShape(DesignSystem.CornerRadius.md))
            .border(
                width = 0.5.dp, // very thin border
                color = Color.White.copy(alpha = 0.2f), // subtle white
                shape = RoundedCornerShape(DesignSystem.CornerRadius.md)
            ),
        shape = RoundedCornerShape(DesignSystem.CornerRadius.md),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) FilterColors.surface else FilterColors.unselectedBackground
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Account Icon
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(account.color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = account.icon,
                    contentDescription = account.name,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = account.name,
                color = FilterColors.primaryText,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            
            // Selection indicator for accounts
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(FilterColors.primaryText),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = FilterColors.background,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DateRangeSection(
    dateRange: DateRange,
    onDateRangeChange: (DateRange) -> Unit
) {
    Column {
        Text(
            text = "Date Range",
            color = FilterColors.primaryText,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Predefined Date Ranges
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(PredefinedDateRange.values().toList()) { predefinedRange ->
                DateRangeChip(
                    label = when (predefinedRange) {
                        PredefinedDateRange.TODAY -> "Today"
                        PredefinedDateRange.THIS_WEEK -> "This Week"
                        PredefinedDateRange.THIS_MONTH -> "This Month"
                        PredefinedDateRange.LAST_3_MONTHS -> "Last 3 Months"
                    },
                    isSelected = dateRange.predefined == predefinedRange,
                    onClick = {
                        onDateRangeChange(dateRange.copy(predefined = predefinedRange))
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Custom Date Range
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // From Date
            OutlinedTextField(
                value = dateRange.from,
                onValueChange = { newFrom ->
                    onDateRangeChange(dateRange.copy(from = newFrom, predefined = null))
                },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "From",
                        color = FilterColors.secondaryText
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "From Date",
                        tint = FilterColors.secondaryText,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = FilterColors.unselectedBackground,
                    unfocusedContainerColor = FilterColors.unselectedBackground,
                    focusedBorderColor = FilterColors.divider,
                    unfocusedBorderColor = FilterColors.divider,
                    focusedTextColor = FilterColors.primaryText,
                    unfocusedTextColor = FilterColors.primaryText
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            
            // To Date
            OutlinedTextField(
                value = dateRange.to,
                onValueChange = { newTo ->
                    onDateRangeChange(dateRange.copy(to = newTo, predefined = null))
                },
                modifier = Modifier.weight(1f),
                placeholder = {
                    Text(
                        text = "To",
                        color = FilterColors.secondaryText
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "To Date",
                        tint = FilterColors.secondaryText,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = FilterColors.unselectedBackground,
                    unfocusedContainerColor = FilterColors.unselectedBackground,
                    focusedBorderColor = FilterColors.divider,
                    unfocusedBorderColor = FilterColors.divider,
                    focusedTextColor = FilterColors.primaryText,
                    unfocusedTextColor = FilterColors.primaryText
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }
    }
}

@Composable
private fun DateRangeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .clip(RoundedCornerShape(DesignSystem.CornerRadius.md))
            .border(
                width = 0.5.dp, // very thin border
                color = Color.White.copy(alpha = 0.2f), // subtle white
                shape = RoundedCornerShape(DesignSystem.CornerRadius.md)
            ),
        shape = RoundedCornerShape(DesignSystem.CornerRadius.md),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) FilterColors.surface else FilterColors.unselectedBackground
        )
    ) {
        Text(
            text = label,
            color = FilterColors.primaryText,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun AmountRangeSection(
    amountRange: AmountRange,
    onAmountRangeChange: (AmountRange) -> Unit
) {
    Column {
        Text(
            text = "Amount Range",
            color = FilterColors.primaryText,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Predefined Amount Ranges
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(PredefinedAmountRange.values().toList()) { predefinedRange ->
                AmountRangeChip(
                    label = when (predefinedRange) {
                        PredefinedAmountRange.UNDER_25 -> "Under $25"
                        PredefinedAmountRange.BETWEEN_25_100 -> "$25 - $100"
                        PredefinedAmountRange.BETWEEN_100_500 -> "$100 - $500"
                        PredefinedAmountRange.OVER_500 -> "Over $500"
                    },
                    isSelected = amountRange.predefined == predefinedRange,
                    onClick = {
                        onAmountRangeChange(amountRange.copy(predefined = predefinedRange))
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Custom Amount Range
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Min Amount
            OutlinedTextField(
                value = amountRange.min?.toString() ?: "",
                onValueChange = { newMin ->
                    val minValue = newMin.toDoubleOrNull()
                    onAmountRangeChange(amountRange.copy(min = minValue, predefined = null))
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Min Amount",
                        color = FilterColors.secondaryText
                    )
                },
                leadingIcon = {
                    Text(
                        text = "$",
                        color = FilterColors.primaryText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = FilterColors.unselectedBackground,
                    unfocusedContainerColor = FilterColors.unselectedBackground,
                    focusedBorderColor = FilterColors.divider,
                    unfocusedBorderColor = FilterColors.divider,
                    focusedTextColor = FilterColors.primaryText,
                    unfocusedTextColor = FilterColors.primaryText
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                singleLine = true
            )
            
            // Max Amount
            OutlinedTextField(
                value = amountRange.max?.toString() ?: "",
                onValueChange = { newMax ->
                    val maxValue = newMax.toDoubleOrNull()
                    onAmountRangeChange(amountRange.copy(max = maxValue, predefined = null))
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Max Amount",
                        color = FilterColors.secondaryText
                    )
                },
                leadingIcon = {
                    Text(
                        text = "$",
                        color = FilterColors.primaryText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = FilterColors.unselectedBackground,
                    unfocusedContainerColor = FilterColors.unselectedBackground,
                    focusedBorderColor = FilterColors.divider,
                    unfocusedBorderColor = FilterColors.divider,
                    focusedTextColor = FilterColors.primaryText,
                    unfocusedTextColor = FilterColors.primaryText
                ),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                singleLine = true
            )
        }
    }
}

@Composable
private fun AmountRangeChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .clickable { onClick() }
            .clip(RoundedCornerShape(DesignSystem.CornerRadius.md))
            .border(
                width = 0.5.dp, // very thin border
                color = Color.White.copy(alpha = 0.2f), // subtle white
                shape = RoundedCornerShape(DesignSystem.CornerRadius.md)
            ),
        shape = RoundedCornerShape(DesignSystem.CornerRadius.md),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) FilterColors.surface else FilterColors.unselectedBackground
        )
    ) {
        Text(
            text = label,
            color = FilterColors.primaryText,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun ActionButtons(
    onApplyFilters: () -> Unit,
    onCancel: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Cancel Button
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = FilterColors.primaryText
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                FilterColors.divider
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Cancel",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
        
        // Apply Filters Button
        Button(
            onClick = onApplyFilters,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = FilterColors.primaryText,
                contentColor = FilterColors.background
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Apply",
                modifier = Modifier.size(16.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "Apply Filters",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
