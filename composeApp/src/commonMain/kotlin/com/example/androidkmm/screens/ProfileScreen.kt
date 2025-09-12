@file:OptIn(ExperimentalMaterial3Api::class)

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import com.example.androidkmm.database.rememberSQLiteCategoryDatabase
import com.example.androidkmm.database.rememberSQLiteAccountDatabase
import com.example.androidkmm.database.rememberSQLiteTransactionDatabase
import com.example.androidkmm.database.rememberSQLiteSettingsDatabase
import com.example.androidkmm.models.Category
import com.example.androidkmm.models.CategoryType
import com.example.androidkmm.models.CategoryTab
import com.example.androidkmm.models.Account

// Color definitions
val DarkBackground = Color(0xFF1A1A1A)
val DarkSurface = Color(0xFF2A2A2A)
val DarkSurfaceVariant = Color(0xFF3A3A3A)
val GreenSuccess = Color(0xFF4CAF50)
val BlueAccent = Color(0xFF2196F3)
val WhiteText = Color(0xFFFFFFFF)
val GrayText = Color(0xFF9E9E9E)

// Account data class is now defined in models/AccountModels.kt

// Category, CategoryType, and CategoryTab are now defined in models/CategoryModels.kt

// Bank options
val bankOptions = listOf(
    "HDFC Bank", "State Bank of India (SBI)", "ICICI Bank",
    "Axis Bank", "Bank of Baroda", "Punjab National Bank"
)

// Default categories are now loaded from database

// Icon options for categories
val categoryIcons = listOf(
    Icons.Default.AttachMoney, Icons.Default.Restaurant, Icons.Default.ShoppingCart,
    Icons.Default.DirectionsCar, Icons.Default.Home, Icons.Default.SportsEsports,
    Icons.Default.CardGiftcard, Icons.Default.Favorite, Icons.Default.Flight,
    Icons.Default.School, Icons.Default.MusicNote, Icons.Default.Business,
    Icons.Default.TrendingUp, Icons.Default.Stars, Icons.Default.LocalCafe,
    Icons.Default.Wallet
)

// Color options for categories
val categoryColors = listOf(
    Color(0xFFF44336), Color(0xFFFF9800), Color(0xFFFFC107), Color(0xFFCDDC39),
    Color(0xFF4CAF50), Color(0xFF009688), Color(0xFF00BCD4), Color(0xFF2196F3),
    Color(0xFF3F51B5), Color(0xFF9C27B0), Color(0xFFE91E63), Color(0xFFFF5722)
)

@Composable
fun ProfileMainScreen() {
    var currentScreen by remember { mutableStateOf("profile") }
    var showAccountSheet by remember { mutableStateOf(false) }
    var showCategorySheet by remember { mutableStateOf(false) }
    var showAddAccountSheet by remember { mutableStateOf(false) }
    var showAddCategorySheet by remember { mutableStateOf(false) }
    var selectedCategoryTab by remember { mutableStateOf(CategoryTab.EXPENSE) }

    // Database managers
    val categoryDatabaseManager = rememberSQLiteCategoryDatabase()
    val accountDatabaseManager = rememberSQLiteAccountDatabase()
    val transactionDatabaseManager = rememberSQLiteTransactionDatabase()
    val settingsDatabaseManager = rememberSQLiteSettingsDatabase()
    val scope = rememberCoroutineScope()
    
    // Flow for categories from database
    val expenseCategories = categoryDatabaseManager.getCategoriesByType(CategoryType.EXPENSE).collectAsState(initial = emptyList<Category>())
    val incomeCategories = categoryDatabaseManager.getCategoriesByType(CategoryType.INCOME).collectAsState(initial = emptyList<Category>())
    val customCategories = categoryDatabaseManager.getCustomCategories().collectAsState(initial = emptyList<Category>())
    
    // Flow for accounts from database
    val accountsState = accountDatabaseManager.getAllAccounts().collectAsState(initial = emptyList<Account>())
    val accounts = accountsState.value
    
    // Flow for settings from database
    val appSettings = settingsDatabaseManager.getAppSettings().collectAsState(initial = com.example.androidkmm.models.AppSettings())

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        when (currentScreen) {
            "profile" -> ProfileScreen(
                onAccountsClick = { showAccountSheet = true },
                onCategoriesClick = { showCategorySheet = true },
                onClearDataClick = {
                    // Clear all data from database
                    scope.launch {
                        transactionDatabaseManager.clearAllData()
                    }
                }
            )
        }

        // Account Management Bottom Sheet
        if (showAccountSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAccountSheet = false },
                containerColor = DarkSurface,
                dragHandle = null
            ) {
                AccountsBottomSheet(
                    accounts = accounts,
                    onDismiss = { showAccountSheet = false },
                    onAddAccount = { showAddAccountSheet = true }
                )
            }
        }

        // Add Account Bottom Sheet
        if (showAddAccountSheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddAccountSheet = false },
                containerColor = DarkSurface,
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
                    }
                )
            }
        }

        // Category Management Bottom Sheet
        if (showCategorySheet) {
            ModalBottomSheet(
                onDismissRequest = { showCategorySheet = false },
                containerColor = DarkSurface,
                dragHandle = null
            ) {
                CategoriesBottomSheet(
                    expenseCategories = expenseCategories.value,
                    incomeCategories = incomeCategories.value,
                    customCategories = customCategories.value,
                    selectedTab = selectedCategoryTab,
                    onDismiss = { showCategorySheet = false },
                    onTabSelected = { tab -> selectedCategoryTab = tab },
                    onAddCustomCategory = { showAddCategorySheet = true },
                    onEditCategory = { category ->
                        // Handle edit category
                    },
                    onDeleteCategory = { category ->
                        categoryDatabaseManager.deleteCategory(category)
                    }
                )
            }
        }

        // Add Category Bottom Sheet
        if (showAddCategorySheet) {
            ModalBottomSheet(
                onDismissRequest = { showAddCategorySheet = false },
                containerColor = DarkSurface,
                dragHandle = null
            ) {
                AddCategoryBottomSheet(
                    categoryType = selectedCategoryTab,
                    onDismiss = { showAddCategorySheet = false },
                    onCategoryAdded = { category, onSuccess, onError ->
                        println("DEBUG: onCategoryAdded callback called with: ${category.name}")
                        categoryDatabaseManager.addCategory(
                            category = category,
                            onSuccess = {
                                println("DEBUG: Category added successfully")
                                onSuccess()
                            },
                            onError = { error ->
                                println("DEBUG: Error adding category: ${error.message}")
                                onError(error.message ?: "Unknown error occurred")
                            }
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun ProfileScreen(
    onAccountsClick: () -> Unit,
    onCategoriesClick: () -> Unit,
    onClearDataClick: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Profile Header
            Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Picture
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "AK",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Abhishek Kumar",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = WhiteText
                )

                Text(
                    text = "abhishek@example.com",
                    fontSize = 16.sp,
                    color = GrayText
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Member since March 2024",
                    fontSize = 14.sp,
                    color = GrayText
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatCard("234", "Transactions")
                    Spacer(modifier = Modifier.width(12.dp))
                    StatCard("5", "Groups")
                }
            }
        }
        }

        item {
            // Menu Items
            MenuCard(
            icon = Icons.Default.AccountBalance,
            title = "Accounts",
            subtitle = "Manage bank accounts and wallets",
            onClick = onAccountsClick
        )

        MenuCard(
            icon = Icons.Default.Category,
            title = "Categories",
            subtitle = "Manage expense and income categories",
            onClick = onCategoriesClick
        )

        MenuCard(
            icon = Icons.Default.Security,
            title = "Privacy & Security",
            subtitle = "Account security settings",
            onClick = { }
        )

        MenuCard(
            icon = Icons.Default.Help,
            title = "Help & Support",
            subtitle = "Get help and contact support",
            onClick = { }
        )
        }

        item {
            // Account Information
            Text(
            text = "Account Information",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = WhiteText,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = GrayText,
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Email",
                        fontSize = 14.sp,
                        color = GrayText
                    )
                    Text(
                        text = "abhishek@example.com",
                        fontSize = 16.sp,
                        color = WhiteText
                    )
                }
            }
        }
        }
        
        item {
            // Clear Data Button
            MenuCard(
                icon = Icons.Default.Delete,
                title = "Clear All Data",
                subtitle = "Delete all transactions, accounts, and categories",
                onClick = onClearDataClick
            )
        }
    }
}

@Composable
fun StatCard(value: String, label: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp, 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = WhiteText
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = GrayText
            )
        }
    }
}

@Composable
fun MenuCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = WhiteText,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = WhiteText
                )
                Text(
                    text = subtitle,
                    fontSize = 14.sp,
                    color = GrayText
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = GrayText
            )
        }
    }
}

@Composable
fun AccountsBottomSheet(
    accounts: List<Account>,
    onDismiss: () -> Unit,
    onAddAccount: () -> Unit
) {
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
            Column {
                Text(
                    text = "Manage Accounts",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = WhiteText
                )
                Text(
                    text = "Add, edit, or manage your financial accounts",
                    fontSize = 14.sp,
                    color = GrayText
                )
            }

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = WhiteText
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Accounts List
        accounts.forEach { account ->
            AccountItem(account = account)
            Spacer(modifier = Modifier.height(12.dp))
        }

        // Add New Account Button
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAddAccount() },
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            shape = RoundedCornerShape(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, DarkSurfaceVariant, RoundedCornerShape(16.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = WhiteText,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Add New Account",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = WhiteText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun AccountItem(account: Account) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(account.color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = account.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = WhiteText
                )
                Text(
                    text = account.balance,
                    fontSize = 14.sp,
                    color = GreenSuccess
                )
            }

            Row {
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = GrayText
                    )
                }
                IconButton(onClick = { }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFE57373)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoriesBottomSheet(
    expenseCategories: List<Category>,
    incomeCategories: List<Category>,
    customCategories: List<Category>,
    selectedTab: CategoryTab = CategoryTab.EXPENSE,
    onDismiss: () -> Unit,
    onTabSelected: (CategoryTab) -> Unit,
    onAddCustomCategory: () -> Unit,
    onEditCategory: (Category) -> Unit,
    onDeleteCategory: (Category) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Manage Categories",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = WhiteText
                )
                Text(
                    text = "Add, edit, or organize your transaction categories",
                    fontSize = 14.sp,
                    color = GrayText
                )
            }

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = WhiteText
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Tab Row
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(modifier = Modifier.padding(4.dp)) {
                TabButton(
                    text = "Expense Categories",
                    isSelected = selectedTab == CategoryTab.EXPENSE,
                    onClick = { onTabSelected(CategoryTab.EXPENSE) },
                    modifier = Modifier.weight(1f)
                )
                TabButton(
                    text = "Income Categories",
                    isSelected = selectedTab == CategoryTab.INCOME,
                    onClick = { onTabSelected(CategoryTab.INCOME) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Content based on selected tab
        LazyColumn {
            when (selectedTab) {
                CategoryTab.EXPENSE -> {
                    item {
                        ExpenseCategoriesContent(
                            categories = expenseCategories,
                            customCategories = customCategories.filter { it.type == CategoryType.EXPENSE },
                            onAddCustomCategory = onAddCustomCategory,
                            onEditCategory = onEditCategory,
                            onDeleteCategory = onDeleteCategory
                        )
                    }
                }
                CategoryTab.INCOME -> {
                    item {
                        IncomeCategoriesContent(
                            categories = incomeCategories,
                            customCategories = customCategories.filter { it.type == CategoryType.INCOME },
                            onAddCustomCategory = onAddCustomCategory,
                            onEditCategory = onEditCategory,
                            onDeleteCategory = onDeleteCategory
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun IncomeCategoriesContent(
    categories: List<Category>,
    customCategories: List<Category>,
    onAddCustomCategory: () -> Unit,
    onEditCategory: (Category) -> Unit,
    onDeleteCategory: (Category) -> Unit
) {
    Column {
        // Default Categories Section
        Text(
            text = "Default Categories",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = GrayText,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Default Categories Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(200.dp)
        ) {
            items(categories) { category ->
                CategoryCard(
                    category = category,
                    isCustom = false,
                    onEdit = { onEditCategory(category) },
                    onDelete = { onDeleteCategory(category) }
                )
            }
        }

        // Custom Categories Section
        if (customCategories.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Custom Categories",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = GrayText,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            customCategories.forEach { category ->
                CustomCategoryItem(
                    category = category,
                    onEdit = { onEditCategory(category) },
                    onDelete = { onDeleteCategory(category) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Add Custom Category Button
        AddCustomCategoryButton(
            text = "Add Custom Income Category",
            onClick = onAddCustomCategory
        )
    }
}

@Composable
private fun ExpenseCategoriesContent(
    categories: List<Category>,
    customCategories: List<Category>,
    onAddCustomCategory: () -> Unit,
    onEditCategory: (Category) -> Unit,
    onDeleteCategory: (Category) -> Unit
) {
    Column {
        // Default Categories Section
        Text(
            text = "Default Categories",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = GrayText,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Default Categories Grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(200.dp)
        ) {
            items(categories) { category ->
                CategoryCard(
                    category = category,
                    isCustom = false,
                    onEdit = { onEditCategory(category) },
                    onDelete = { onDeleteCategory(category) }
                )
            }
        }

        // Custom Categories Section
        if (customCategories.isNotEmpty()) {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Custom Categories",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = GrayText,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            customCategories.forEach { category ->
                CustomCategoryItem(
                    category = category,
                    onEdit = { onEditCategory(category) },
                    onDelete = { onDeleteCategory(category) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Add Custom Category Button
        AddCustomCategoryButton(
            text = "Add Custom Expense Category",
            onClick = onAddCustomCategory
        )
    }
}

@Composable
private fun CategoryCard(
    category: Category,
    isCustom: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(category.color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = category.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = WhiteText,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CustomCategoryItem(
    category: Category,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(category.color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = WhiteText
                )
                Text(
                    text = "Custom",
                    fontSize = 14.sp,
                    color = GrayText
                )
            }

            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = GrayText
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFE57373)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddCustomCategoryButton(
    text: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .dashedBorder(2.dp, DarkSurfaceVariant, RoundedCornerShape(16.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = WhiteText,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = text,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = WhiteText
                )
            }
        }
    }
}

@Composable
private fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) DarkSurface else Color.Transparent,
            contentColor = WhiteText
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier.height(40.dp),
        contentPadding = PaddingValues(12.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

// Extension function for dashed border
fun Modifier.dashedBorder(
    width: Dp,
    color: Color,
    shape: RoundedCornerShape
) = this.then(
    Modifier.border(width, color, shape)
)

@OptIn(ExperimentalTime::class)
@Composable
fun AddAccountBottomSheet(
    onDismiss: () -> Unit,
    onAccountAdded: (Account) -> Unit
) {
    var accountName by remember { mutableStateOf("") }
    var selectedAccountType by remember { mutableStateOf("Bank Account") }
    var selectedBank by remember { mutableStateOf("HDFC Bank") }
    var initialBalance by remember { mutableStateOf("0.00") }

    val bankOptions = listOf(
        "HDFC Bank", "State Bank of India (SBI)",
        "ICICI Bank", "Axis Bank",
        "Bank of Baroda", "Punjab National Bank"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        // Account Name Section
        Text(
            text = "Account Name",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = WhiteText
        )

        Spacer(modifier = Modifier.height(8.dp))

        BasicTextField(
            value = accountName,
            onValueChange = { accountName = it },
            textStyle = TextStyle(color = WhiteText, fontSize = 16.sp),
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceVariant, RoundedCornerShape(12.dp))
                .padding(16.dp),
            decorationBox = { innerTextField ->
                if (accountName.isEmpty()) {
                    Text(
                        text = "e.g. HDFC Savings, Cash Wallet",
                        color = GrayText,
                        fontSize = 16.sp
                    )
                }
                innerTextField()
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Account Type Section
        Text(
            text = "Account Type",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = WhiteText
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                AccountTypeCard(
                    title = "Bank Account",
                    icon = Icons.Default.AccountBalance,
                    iconColor = Color(0xFF4285F4),
                    isSelected = selectedAccountType == "Bank Account",
                    onClick = { selectedAccountType = "Bank Account" }
                )
            }
            item {
                AccountTypeCard(
                    title = "Credit/Debit Card",
                    icon = Icons.Default.CreditCard,
                    iconColor = Color(0xFF34A853),
                    isSelected = selectedAccountType == "Credit/Debit Card",
                    onClick = { selectedAccountType = "Credit/Debit Card" }
                )
            }
            item {
                AccountTypeCard(
                    title = "Cash",
                    icon = Icons.Default.AttachMoney,
                    iconColor = Color(0xFFFF6D01),
                    isSelected = selectedAccountType == "Cash",
                    onClick = { selectedAccountType = "Cash" }
                )
            }
            item {
                AccountTypeCard(
                    title = "Digital Wallet",
                    icon = Icons.Default.Wallet,
                    iconColor = Color(0xFF9C27B0),
                    isSelected = selectedAccountType == "Digital Wallet",
                    onClick = { selectedAccountType = "Digital Wallet" }
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Bank Name Section (only show if Bank Account is selected)
        if (selectedAccountType == "Bank Account") {
            Text(
                text = "Bank Name",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = WhiteText
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(bankOptions) { bank ->
                    BankCard(
                        bankName = bank,
                        isSelected = selectedBank == bank,
                        onClick = { selectedBank = bank }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }

        // Initial Balance Section
        Text(
            text = "Initial Balance",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = WhiteText
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceVariant, RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Text(
                text = "₹",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = WhiteText
            )
            Spacer(modifier = Modifier.width(8.dp))
            BasicTextField(
                value = initialBalance,
                onValueChange = { newValue ->
                    if (newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                        initialBalance = newValue
                    }
                },
                textStyle = TextStyle(
                    color = WhiteText,
                    fontSize = 16.sp
                ),
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Add Account Button
        Button(
            onClick = {
                val account = Account(
                    id = Clock.System.now().toEpochMilliseconds().toString(),
                    name = accountName.ifEmpty {
                        if (selectedAccountType == "Bank Account") selectedBank else selectedAccountType
                    },
                    balance = "₹$initialBalance",
                    icon = when (selectedAccountType) {
                        "Bank Account" -> Icons.Default.AccountBalance
                        "Credit/Debit Card" -> Icons.Default.CreditCard
                        "Cash" -> Icons.Default.AttachMoney
                        "Digital Wallet" -> Icons.Default.Wallet
                        else -> Icons.Default.AccountBalance
                    },
                    color = BlueAccent,
                    type = selectedAccountType
                )
                onAccountAdded(account)
            },
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF6C6C6C)
            )
        ) {
            Text(
                text = "Add Account",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

@Composable
fun AccountTypeCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) DarkSurfaceVariant else Color(0xFF2A2A2A)
        ),
        border = if (isSelected) BorderStroke(2.dp, Color.White) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = WhiteText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BankCard(
    bankName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) DarkSurfaceVariant else Color(0xFF2A2A2A)
        ),
        border = if (isSelected) BorderStroke(1.dp, Color.White) else null
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = bankName,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = WhiteText,
                textAlign = TextAlign.Center
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
fun AddCategoryBottomSheet(
    categoryType: CategoryTab,
    onDismiss: () -> Unit,
    onCategoryAdded: (Category, onSuccess: () -> Unit, onError: (String) -> Unit) -> Unit
) {
    var categoryName by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf(Icons.Default.AttachMoney) }
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
                        tint = WhiteText
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Add ${if (categoryType == CategoryTab.EXPENSE) "Expense" else "Income"} Category",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = WhiteText
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Category Name
        Text(
            text = "Category Name",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = WhiteText,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        BasicTextField(
            value = categoryName,
            onValueChange = { categoryName = it },
            textStyle = TextStyle(color = WhiteText, fontSize = 16.sp),
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurfaceVariant, RoundedCornerShape(12.dp))
                .padding(16.dp),
            decorationBox = { innerTextField ->
                if (categoryName.isEmpty()) {
                    Text(
                        text = "Enter category name",
                        color = GrayText,
                        fontSize = 16.sp
                    )
                }
                innerTextField()
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Choose Icon
        Text(
            text = "Choose Icon",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = WhiteText,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(120.dp)
        ) {
            items(categoryIcons) { icon ->
                IconSelector(
                    icon = icon,
                    color = selectedColor,
                    isSelected = selectedIcon == icon,
                    onClick = { selectedIcon = icon }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Choose Color
        Text(
            text = "Choose Color",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = WhiteText,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(6),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(120.dp)
        ) {
            items(categoryColors) { color ->
                ColorSelector(
                    color = color,
                    isSelected = selectedColor == color,
                    onClick = { selectedColor = color }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Preview
        Text(
            text = "Preview",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = WhiteText,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(selectedColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = selectedIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = categoryName.ifEmpty { "Category Name" },
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = WhiteText
                    )
                    Text(
                        text = "${if (categoryType == CategoryTab.EXPENSE) "Expense" else "Income"} Category",
                        fontSize = 14.sp,
                        color = GrayText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Error Message
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // Add Category Button
        Button(
            onClick = {
                if (isLoading) return@Button
                
                errorMessage = "" // Clear previous error
                isLoading = true
                
                println("DEBUG: Add Category button clicked with name: '$categoryName'")
                val category = Category(
                    id = Clock.System.now().toEpochMilliseconds().toString(),
                    name = categoryName,
                    icon = selectedIcon,
                    color = selectedColor,
                    type = if (categoryType == CategoryTab.EXPENSE) CategoryType.EXPENSE else CategoryType.INCOME,
                    isCustom = true
                )
                println("DEBUG: Created category object: ${category.name}, type: ${category.type}, isCustom: ${category.isCustom}")
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
                containerColor = if (categoryName.isNotEmpty() && !isLoading) Color(0xFF4CAF50) else GrayText,
                contentColor = if (categoryName.isNotEmpty() && !isLoading) Color.White else Color(0xFF666666)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
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

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun IconSelector(
    icon: ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) color else DarkSurfaceVariant)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun ColorSelector(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


