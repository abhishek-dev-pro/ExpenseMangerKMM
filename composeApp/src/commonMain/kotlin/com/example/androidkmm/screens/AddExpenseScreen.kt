package com.example.androidkmm.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.database.SQLiteCategoryDatabase
import com.example.androidkmm.database.SQLiteAccountDatabase
import com.example.androidkmm.database.SQLiteTransactionDatabase
import com.example.androidkmm.database.rememberSQLiteSettingsDatabase
import com.example.androidkmm.models.*
import androidx.compose.runtime.collectAsState
import kotlinx.datetime.toLocalDateTime
import com.example.androidkmm.components.SharedAccountSelectionBottomSheet
import com.example.androidkmm.design.AppStyleDesignSystem

@OptIn(ExperimentalMaterial3Api::class, kotlin.time.ExperimentalTime::class)
@Composable
fun AddExpenseScreen(
    onBack: () -> Unit,
    categoryDatabaseManager: SQLiteCategoryDatabase,
    accountDatabaseManager: SQLiteAccountDatabase,
    transactionDatabaseManager: SQLiteTransactionDatabase
) {
    // Get currency symbol from settings
    val settingsDatabaseManager = rememberSQLiteSettingsDatabase()
    val appSettings = settingsDatabaseManager.getAppSettings().collectAsState(initial = AppSettings())
    val currencySymbol = appSettings.value.currencySymbol
    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedAccount by remember { mutableStateOf<Account?>(null) }
    var showCategoryPicker by remember { mutableStateOf(false) }
    var showAccountPicker by remember { mutableStateOf(false) }

    // Get current date and time
    val currentDate = remember {
        val now = kotlin.time.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
        "${now.date.year}-${now.date.monthNumber.toString().padStart(2, '0')}-${now.date.dayOfMonth.toString().padStart(2, '0')}"
    }
    val currentTime = remember {
        val now = kotlin.time.Clock.System.now().toLocalDateTime(kotlinx.datetime.TimeZone.currentSystemDefault())
        "${now.time.hour.toString().padStart(2, '0')}:${now.time.minute.toString().padStart(2, '0')}"
    }

    // Enhanced form validation with detailed error tracking
    var validationErrors by remember { mutableStateOf(mapOf<String, String>()) }
    
    fun validateForm(): Boolean {
        val validationResult = com.example.androidkmm.utils.FormValidation.validateExpenseForm(
            amount = amount,
            title = title,
            category = selectedCategory,
            account = selectedAccount,
            description = description
        )
        
        validationErrors = validationResult.errors
        return validationResult.isValid
    }
    
    val isFormValid = validateForm()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = "Add Expense",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(24.dp))

            // Amount Section
            Text(
                text = "Amount",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(8.dp))
            BasicTextField(
                value = amount,
                onValueChange = { 
                    val sanitized = com.example.androidkmm.utils.InputSanitizer.sanitizeAmount(it)
                    amount = sanitized
                },
                singleLine = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = AppStyleDesignSystem.Sizes.BORDER_NORMAL,
                        color = if (validationErrors.containsKey("amount")) Color.Red else Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) { innerTextField ->
                if (amount.isEmpty()) {
                    Text(
                        text = "$currencySymbol${0.00}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                innerTextField()
            }
            
            // Amount error message
            if (validationErrors.containsKey("amount")) {
                Spacer(Modifier.height(4.dp))
                com.example.androidkmm.components.FieldErrorMessage(
                    message = validationErrors["amount"] ?: ""
                )
            }

            Spacer(Modifier.height(32.dp))

            // Title Section
            Text(
                text = "Title",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(8.dp))
            BasicTextField(
                value = title,
                onValueChange = { 
                    val sanitized = com.example.androidkmm.utils.InputSanitizer.sanitizeTitle(it)
                    title = sanitized
                },
                singleLine = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = AppStyleDesignSystem.Sizes.BORDER_NORMAL,
                        color = if (validationErrors.containsKey("title")) Color.Red else Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) { innerTextField ->
                if (title.isEmpty()) {
                    Text(
                        text = "Enter expense title...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                innerTextField()
            }
            
            // Title error message
            if (validationErrors.containsKey("title")) {
                Spacer(Modifier.height(4.dp))
                com.example.androidkmm.components.FieldErrorMessage(
                    message = validationErrors["title"] ?: ""
                )
            }

            Spacer(Modifier.height(24.dp))

            // Category Section
            Text(
                text = "Category",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = AppStyleDesignSystem.Sizes.BORDER_NORMAL,
                        color = if (validationErrors.containsKey("category")) Color.Red else Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { showCategoryPicker = true }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (selectedCategory != null) {
                            Box(
                                modifier = Modifier
                                    .size(AppStyleDesignSystem.Sizes.ICON_SIZE_XL)
                                    .clip(CircleShape)
                                    .background(selectedCategory!!.color),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = selectedCategory!!.icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_SMALL)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = selectedCategory!!.name,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 16.sp
                            )
                        } else {
                            Text(
                                text = "Select category",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 16.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select category",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Category error message
            if (validationErrors.containsKey("category")) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = validationErrors["category"] ?: "",
                    color = Color.Red,
                    fontSize = 12.sp
                )
            }

            Spacer(Modifier.height(24.dp))

            // Account Section
            Text(
                text = "Account",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = AppStyleDesignSystem.Sizes.BORDER_NORMAL,
                        color = Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { showAccountPicker = true }
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (selectedAccount != null) {
                            Box(
                                modifier = Modifier
                                    .size(AppStyleDesignSystem.Sizes.ICON_SIZE_XL)
                                    .clip(CircleShape)
                                    .background(selectedAccount!!.color),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = selectedAccount!!.icon,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_SMALL)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = selectedAccount!!.name,
                                color = MaterialTheme.colorScheme.onBackground,
                                fontSize = 16.sp
                            )
                        } else {
                            Text(
                                text = "Select account",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 16.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Select account",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // Description Section
            Text(
                text = "Description (Optional)",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(8.dp))
            BasicTextField(
                value = description,
                onValueChange = { 
                    val sanitized = com.example.androidkmm.utils.InputSanitizer.sanitizeDescription(it)
                    description = sanitized
                },
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 16.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(12.dp)
                    )
                    .border(
                        width = AppStyleDesignSystem.Sizes.BORDER_NORMAL,
                        color = Color.White.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) { innerTextField ->
                if (description.isEmpty()) {
                    Text(
                        text = "Add a note...",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                innerTextField()
            }

            Spacer(Modifier.height(40.dp))

            // Save Button
            Button(
                onClick = {
                    if (isFormValid) {
                        val transaction = Transaction(
                            id = "${kotlin.time.Clock.System.now().toEpochMilliseconds()}",
                            title = title,
                            amount = amount.toDoubleOrNull() ?: 0.0,
                            category = selectedCategory!!.name,
                            categoryIcon = selectedCategory!!.icon,
                            categoryColor = selectedCategory!!.color,
                            account = selectedAccount!!.name,
                            transferTo = null,
                            time = currentTime,
                            type = TransactionType.EXPENSE,
                            description = description,
                            date = currentDate,
                            accountIcon = selectedAccount!!.icon,
                            accountColor = selectedAccount!!.color
                        )

                        transactionDatabaseManager.addTransactionWithBalanceUpdate(
                            transaction = transaction,
                            accountDatabaseManager = accountDatabaseManager
                        )
                        onBack()
                    }
                },
                enabled = isFormValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEF4444), // Red color for expense
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add Expense", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // Category Picker Bottom Sheet
    if (showCategoryPicker) {
        CategorySelectionBottomSheet(
            onDismiss = { showCategoryPicker = false },
            onCategorySelected = { category ->
                selectedCategory = category
                showCategoryPicker = false
            },
            categoryDatabaseManager = categoryDatabaseManager
        )
    }

    // Account Picker Bottom Sheet
    if (showAccountPicker) {
        AccountSelectionBottomSheet(
            onDismiss = { showAccountPicker = false },
            onAccountSelected = { account ->
                selectedAccount = account
                showAccountPicker = false
            },
            accountDatabaseManager = accountDatabaseManager
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategorySelectionBottomSheet(
    onDismiss: () -> Unit,
    onCategorySelected: (Category) -> Unit,
    categoryDatabaseManager: SQLiteCategoryDatabase
) {
    val categories = categoryDatabaseManager.getCategoriesByType(CategoryType.EXPENSE).collectAsState(initial = emptyList())

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Select Category",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(16.dp))
            
            categories.value.forEach { category ->
                CategoryItem(
                    category = category,
                    onClick = { onCategorySelected(category) }
                )
                Spacer(Modifier.height(8.dp))
            }
            
            Spacer(Modifier.height(24.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AccountSelectionBottomSheet(
    onDismiss: () -> Unit,
    onAccountSelected: (Account) -> Unit,
    accountDatabaseManager: SQLiteAccountDatabase
) {
    SharedAccountSelectionBottomSheet(
        onDismiss = onDismiss,
        title = "Select Account",
        subtitle = "Choose an account for your transaction",
        onAccountSelected = onAccountSelected,
        accountDatabaseManager = accountDatabaseManager,
        onAddAccount = null // No add account functionality in AddExpenseScreen
    )
}

@Composable
private fun CategoryItem(
    category: Category,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
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
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = category.name,
            fontSize = 16.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun AccountItem(
    account: Account,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(account.color),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = account.icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                text = account.name,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Balance: ${account.balance}",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
