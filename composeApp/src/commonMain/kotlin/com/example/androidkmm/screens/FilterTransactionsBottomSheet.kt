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
import com.example.androidkmm.utils.DateTimeUtils
import com.example.androidkmm.components.BeautifulDateSelector
import kotlinx.datetime.*
import kotlin.time.ExperimentalTime
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.example.androidkmm.database.rememberSQLiteCategoryDatabase
import com.example.androidkmm.database.rememberSQLiteAccountDatabase
import com.example.androidkmm.database.rememberSQLiteSettingsDatabase
import com.example.androidkmm.models.AppSettings
import com.example.androidkmm.design.AppStyleDesignSystem
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
    val selectedBorder = Color(0xFFFFFFFF)
    val unselectedBackground = Color(0xFF2C2C2E)
    val income = Color(0xFF10B981)
    val expense = Color(0xFFEF4444)
    val transfer = Color(0xFF3B82F6)
}

@OptIn(ExperimentalTime::class)
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
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxSize(),
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
    val settingsDatabaseManager = rememberSQLiteSettingsDatabase()

    // Get currency symbol from settings
    val appSettings = settingsDatabaseManager.getAppSettings().collectAsState(initial = AppSettings())
    val currencySymbol = appSettings.value.currencySymbol

    val categoriesState = categoryDatabaseManager.getAllCategories().collectAsState(initial = emptyList<Category>())
    val accountsState = accountDatabaseManager.getActiveAccounts().collectAsState(initial = emptyList<Account>())

    // Date picker states
    var showFromDatePicker by remember { mutableStateOf(false) }
    var showToDatePicker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Header
            FilterHeader(onDismiss = onDismiss)

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_XXL),
                modifier = Modifier.weight(1f)
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
                        },
                        onShowFromDatePicker = { showFromDatePicker = true },
                        onShowToDatePicker = { showToDatePicker = true }
                    )
                }

                // Amount Range Section
                item {
                    AmountRangeSection(
                        amountRange = filterOptions.amountRange ?: AmountRange(),
                        onAmountRangeChange = { newAmountRange ->
                            onFilterOptionsChange(filterOptions.copy(amountRange = newAmountRange))
                        },
                        currencySymbol = currencySymbol
                    )
                }
            }

            // Add extra space at the bottom to ensure content doesn't overlap with buttons
            Spacer(modifier = Modifier.height(100.dp))
        }

        // Action buttons fixed at the bottom
        ActionButtons(
            onApplyFilters = onApplyFilters,
            onCancel = onDismiss,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(16.dp)
        )
    }

    // Date Picker Dialogs
    if (showFromDatePicker) {
        FilterDatePickerDialog(
            onDismiss = { showFromDatePicker = false },
            onDateSelected = { selectedDate ->
                onFilterOptionsChange(
                    filterOptions.copy(
                        dateRange = filterOptions.dateRange?.copy(
                            from = selectedDate,
                            predefined = null
                        ) ?: DateRange(from = selectedDate)
                    )
                )
                showFromDatePicker = false
            },
            initialDate = filterOptions.dateRange?.from ?: ""
        )
    }

    if (showToDatePicker) {
        FilterDatePickerDialog(
            onDismiss = { showToDatePicker = false },
            onDateSelected = { selectedDate ->
                onFilterOptionsChange(
                    filterOptions.copy(
                        dateRange = filterOptions.dateRange?.copy(
                            to = selectedDate,
                            predefined = null
                        ) ?: DateRange(to = selectedDate)
                    )
                )
                showToDatePicker = false
            },
            initialDate = filterOptions.dateRange?.to ?: ""
        )
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
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Icon(
                imageVector = Icons.Default.FilterList,
                contentDescription = "Filter",
                tint = MaterialTheme.colorScheme.onSurface,
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
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Customize your transaction view",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp
            )
        }

        // Close Icon
        IconButton(
            onClick = onDismiss,
            modifier = Modifier
                .background(
                    Color(0xFF2C2C2E),
                    CircleShape
                )
                .size(AppStyleDesignSystem.Sizes.AVATAR_MEDIUM)
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_MEDIUM)
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
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Column(
            verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL)
        ) {
            // All Transactions
            FilterOptionCard(
                icon = Icons.Default.AttachMoney,
                iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
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
            .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM))
            .border(
                width = 0.5.dp, // very thin border
                color = Color.White.copy(alpha = 0.2f), // subtle white
                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
            ),
        shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else FilterColors.unselectedBackground
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
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold
                )
                
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp
                )
            }
            
            // Selection Indicator
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.onSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.surface,
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
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            // Show selected count
            if (selectedCategories.isNotEmpty()) {
                Text(
                    text = "${selectedCategories.size} selected",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Use LazyVerticalGrid for better UX with many categories
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL),
            verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL),
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
                horizontalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL)
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
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline
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
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline
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
            .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM))
            .border(
                width = 0.5.dp, // very thin border
                color = Color.White.copy(alpha = 0.2f), // subtle white
                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
            ),
        shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else FilterColors.unselectedBackground
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
                color = MaterialTheme.colorScheme.onSurface,
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
                        .background(MaterialTheme.colorScheme.onSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.surface,
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
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            // Show selected count
            if (selectedAccounts.isNotEmpty()) {
                Text(
                    text = "${selectedAccounts.size} selected",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Use LazyVerticalGrid for better UX with many accounts
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL),
            verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL),
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
                horizontalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL)
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
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline
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
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outline
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
            .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM))
            .border(
                width = 0.5.dp, // very thin border
                color = Color.White.copy(alpha = 0.2f), // subtle white
                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
            ),
        shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else FilterColors.unselectedBackground
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
                color = MaterialTheme.colorScheme.onSurface,
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
                        .background(MaterialTheme.colorScheme.onSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = MaterialTheme.colorScheme.surface,
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
    onDateRangeChange: (DateRange) -> Unit,
    onShowFromDatePicker: () -> Unit,
    onShowToDatePicker: () -> Unit
) {
    Column {
        Text(
            text = "Date Range",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Predefined Date Ranges
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL)
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
            horizontalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_MEDIUM)
        ) {
            // From Date - Clickable field
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onShowFromDatePicker() }
                    .background(
                        color = FilterColors.unselectedBackground,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = AppStyleDesignSystem.Sizes.BORDER_NORMAL,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "From Date",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (dateRange.from.isNotEmpty()) {
                            // Format date for display (assuming YYYY-MM-DD format)
                            try {
                                val parts = dateRange.from.split("-")
                                if (parts.size == 3) {
                                    "${parts[2]}/${parts[1]}/${parts[0]}"
                                } else {
                                    dateRange.from
                                }
                            } catch (e: Exception) {
                                dateRange.from
                            }
                        } else {
                            "From"
                        },
                        color = if (dateRange.from.isNotEmpty()) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontSize = 14.sp
                    )
                }
            }
            
            // To Date - Clickable field
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onShowToDatePicker() }
                    .background(
                        color = FilterColors.unselectedBackground,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = AppStyleDesignSystem.Sizes.BORDER_NORMAL,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "To Date",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (dateRange.to.isNotEmpty()) {
                            // Format date for display (assuming YYYY-MM-DD format)
                            try {
                                val parts = dateRange.to.split("-")
                                if (parts.size == 3) {
                                    "${parts[2]}/${parts[1]}/${parts[0]}"
                                } else {
                                    dateRange.to
                                }
                            } catch (e: Exception) {
                                dateRange.to
                            }
                        } else {
                            "To"
                        },
                        color = if (dateRange.to.isNotEmpty()) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        fontSize = 14.sp
                    )
                }
            }
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
            .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM))
            .border(
                width = 0.5.dp, // very thin border
                color = Color.White.copy(alpha = 0.2f), // subtle white
                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
            ),
        shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else FilterColors.unselectedBackground
        )
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun AmountRangeSection(
    amountRange: AmountRange,
    onAmountRangeChange: (AmountRange) -> Unit,
    currencySymbol: String
) {
    Column {
        Text(
            text = "Amount Range",
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Predefined Amount Ranges
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_SMALL)
        ) {
            items(PredefinedAmountRange.values().toList()) { predefinedRange ->
                AmountRangeChip(
                    label = when (predefinedRange) {
                        PredefinedAmountRange.UNDER_25 -> "Under $currencySymbol${25}"
                        PredefinedAmountRange.BETWEEN_25_100 -> "$currencySymbol${25} - $currencySymbol${100}"
                        PredefinedAmountRange.BETWEEN_100_500 -> "$currencySymbol${100} - $currencySymbol${500}"
                        PredefinedAmountRange.OVER_500 -> "Over $currencySymbol${500}"
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
            verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_MEDIUM)
        ) {
            // Min Amount
            OutlinedTextField(
                value = amountRange.min?.toString() ?: "",
                onValueChange = { newMin ->
                    // COMPLETELY REWRITTEN LOGIC - BULLETPROOF VALIDATION
                    val cleanInput = newMin.filter { it.isDigit() || it == '.' }
                    val decimalCount = cleanInput.count { it == '.' }
                    if (decimalCount <= 1) {
                        if (cleanInput.contains('.')) {
                            val parts = cleanInput.split('.')
                            if (parts.size == 2) {
                                val beforeDecimal = parts[0]
                                val afterDecimal = parts[1]
                                if (afterDecimal.length <= 2) {
                                    if (beforeDecimal.isEmpty() || !beforeDecimal.startsWith("0") || beforeDecimal == "0") {
                                        val minValue = cleanInput.toDoubleOrNull()
                                        onAmountRangeChange(amountRange.copy(min = minValue, predefined = null))
                                    }
                                }
                            }
                        } else {
                            if (cleanInput.length <= 8 && (cleanInput.isEmpty() || !cleanInput.startsWith("0") || cleanInput == "0")) {
                                val minValue = cleanInput.toDoubleOrNull()
                                onAmountRangeChange(amountRange.copy(min = minValue, predefined = null))
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Min Amount",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Text(
                        text = currencySymbol,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = FilterColors.unselectedBackground,
                    unfocusedContainerColor = FilterColors.unselectedBackground,
                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
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
                    // COMPLETELY REWRITTEN LOGIC - BULLETPROOF VALIDATION
                    val cleanInput = newMax.filter { it.isDigit() || it == '.' }
                    val decimalCount = cleanInput.count { it == '.' }
                    if (decimalCount <= 1) {
                        if (cleanInput.contains('.')) {
                            val parts = cleanInput.split('.')
                            if (parts.size == 2) {
                                val beforeDecimal = parts[0]
                                val afterDecimal = parts[1]
                                if (afterDecimal.length <= 2) {
                                    if (beforeDecimal.isEmpty() || !beforeDecimal.startsWith("0") || beforeDecimal == "0") {
                                        val maxValue = cleanInput.toDoubleOrNull()
                                        onAmountRangeChange(amountRange.copy(max = maxValue, predefined = null))
                                    }
                                }
                            }
                        } else {
                            if (cleanInput.length <= 8 && (cleanInput.isEmpty() || !cleanInput.startsWith("0") || cleanInput == "0")) {
                                val maxValue = cleanInput.toDoubleOrNull()
                                onAmountRangeChange(amountRange.copy(max = maxValue, predefined = null))
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "Max Amount",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Text(
                        text = currencySymbol,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = FilterColors.unselectedBackground,
                    unfocusedContainerColor = FilterColors.unselectedBackground,
                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface
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
            .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM))
            .border(
                width = 0.5.dp, // very thin border
                color = Color.White.copy(alpha = 0.2f), // subtle white
                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
            ),
        shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else FilterColors.unselectedBackground
        )
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 14.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun ActionButtons(
    onApplyFilters: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_MEDIUM)
    ) {
        // Cancel Button
        OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.onSurface
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline
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
                containerColor = MaterialTheme.colorScheme.onSurface,
                contentColor = MaterialTheme.colorScheme.surface
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Apply",
                modifier = Modifier.size(16.dp)
            )
            
            Spacer(modifier = Modifier.width(4.dp))
            
            Text(
                text = "Apply",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Date Picker Dialog for Filter
@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
private fun FilterDatePickerDialog(
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit,
    initialDate: String = ""
) {
    val today = DateTimeUtils.getCurrentDate()
    
    // Parse initial date or use today
    val initialParsedDate = if (initialDate.isNotEmpty()) {
        DateTimeUtils.parseDate(initialDate) ?: today
    } else {
        today
    }
    
    var selectedDate by remember { mutableStateOf(initialParsedDate) }

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
            BeautifulDateSelector(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it },
                maxDate = today
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    val formattedDate = DateTimeUtils.formatDate(selectedDate)
                    onDateSelected(formattedDate)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2196F3)
                )
            ) {
                Text(
                    text = "Select",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline
                )
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 14.sp
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}
