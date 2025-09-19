@file:OptIn(ExperimentalMaterial3Api::class)

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.androidkmm.models.Account
import com.example.androidkmm.models.TransactionCategory
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.androidkmm.utils.formatDouble
import com.example.androidkmm.design.DesignSystem
import com.example.androidkmm.database.rememberSQLiteSettingsDatabase
import com.example.androidkmm.models.AppSettings

// Colors for Transaction Details
object TransactionDetailColors {
    val expense = Color(0xFFFF3B30)
    val income = Color(0xFF34C759)
    val transfer = Color(0xFF007AFF)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsBottomSheet(
    transaction: com.example.androidkmm.models.Transaction,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onEdit: (com.example.androidkmm.models.Transaction) -> Unit,
    onDelete: () -> Unit,
    onNavigateToLedger: (String, String) -> Unit = { _, _ -> },
    categoryDatabaseManager: com.example.androidkmm.database.SQLiteCategoryDatabase,
    accountDatabaseManager: com.example.androidkmm.database.SQLiteAccountDatabase
) {
    val settingsDatabaseManager = rememberSQLiteSettingsDatabase()
    val appSettings by settingsDatabaseManager.getAppSettings().collectAsState(initial = AppSettings())
    val currencySymbol = appSettings.currencySymbol
    
    var isEditMode by remember { mutableStateOf(false) }
    var editedTransaction by remember { mutableStateOf(transaction) }
    var showLedgerDialog by remember { mutableStateOf(false) }

    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = bottomSheetState,
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            if (isEditMode) {
                EditTransactionScreen(
                    transaction = editedTransaction,
                    onDismiss = {
                        isEditMode = false
                        editedTransaction = transaction
                    },
                    onSave = { updatedTransaction: com.example.androidkmm.models.Transaction ->
                        onEdit(updatedTransaction)
                        isEditMode = false
                    },
                    categoryDatabaseManager = categoryDatabaseManager,
                    accountDatabaseManager = accountDatabaseManager
                )
            } else {
                TransactionDetailsContent(
                    transaction = transaction,
                    currencySymbol = currencySymbol,
                    onEdit = {
                        // Check if this is a ledger transaction
                        if (transaction.category == "Ledger" && transaction.id.startsWith("main_")) {
                            showLedgerDialog = true
                        } else {
                            isEditMode = true
                        }
                    },
                    onDelete = {
                        // Check if this is a ledger transaction
                        if (transaction.category == "Ledger" && transaction.id.startsWith("main_")) {
                            showLedgerDialog = true
                        } else {
                            onDelete()
                        }
                    },
                    onDismiss = onDismiss
                )
            }
        }
    }
    
    // Ledger Navigation Dialog
    if (showLedgerDialog) {
        Dialog(onDismissRequest = { showLedgerDialog = false }) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = Color(0xFF1A1A1A),
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icon with background
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(
                                Color(0xFF2A2A2A),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = Color(0xFF007AFF),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Text(
                        text = "Ledger Transaction",
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "This transaction is part of your ledger. To edit or delete it, you need to go to the ledger section.",
                        color = Color(0xFF8E8E93),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp
                    )
                    
                    Spacer(modifier = Modifier.height(28.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showLedgerDialog = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color.White
                            ),
                            border = BorderStroke(1.5.dp, Color(0xFF3A3A3A)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Cancel",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        
                        Button(
                            onClick = {
                                showLedgerDialog = false
                                onDismiss()
                                // Extract person name from transaction title more reliably
                                val personName = when {
                                    transaction.title.startsWith("Sent to ") -> {
                                        transaction.title.replace("Sent to ", "").trim()
                                    }
                                    transaction.title.startsWith("Received from ") -> {
                                        transaction.title.replace("Received from ", "").trim()
                                    }
                                    transaction.title.contains(" - Transfer") -> {
                                        transaction.title.replace(" - Transfer", "").trim()
                                    }
                                    transaction.title.contains(" to ") -> {
                                        // Handle cases like "Money to John"
                                        val parts = transaction.title.split(" to ")
                                        if (parts.size > 1) parts[1].trim() else transaction.title
                                    }
                                    transaction.title.contains(" from ") -> {
                                        // Handle cases like "Money from John"
                                        val parts = transaction.title.split(" from ")
                                        if (parts.size > 1) parts[1].trim() else transaction.title
                                    }
                                    else -> {
                                        // For other cases, try to extract from description or use title as fallback
                                        val fallback = transaction.description.ifEmpty { transaction.title }
                                        fallback.trim()
                                    }
                                }
                                println("TransactionDetailsBottomSheet - Extracted person name: '$personName' from title: '${transaction.title}'")
                                println("TransactionDetailsBottomSheet - Transaction ID: '${transaction.id}'")
                                println("TransactionDetailsBottomSheet - Transaction amount: '${transaction.amount}', date: '${transaction.date}', time: '${transaction.time}'")
                                // Pass person name and transaction details for highlighting
                                onNavigateToLedger(personName, "${transaction.amount}|${transaction.date}|${transaction.time}")
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF007AFF),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Take me there",
                                fontSize = 16.sp,
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
private fun TransactionDetailsContent(
    transaction: com.example.androidkmm.models.Transaction,
    currencySymbol: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
        item {
            // Header with close buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = "Transaction Details",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${transaction.date}, ${transaction.time}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {


                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        item {
            // Category Icon and Amount - centered
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(if (transaction.type == com.example.androidkmm.models.TransactionType.TRANSFER) Color(0xFF3B82F6) else transaction.categoryColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (transaction.type == com.example.androidkmm.models.TransactionType.TRANSFER) Icons.Default.SwapHoriz else transaction.categoryIcon,
                        contentDescription = transaction.category,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val amountColor = when (transaction.type) {
                    com.example.androidkmm.models.TransactionType.INCOME -> TransactionDetailColors.income
                    com.example.androidkmm.models.TransactionType.EXPENSE -> TransactionDetailColors.expense
                    com.example.androidkmm.models.TransactionType.TRANSFER -> TransactionDetailColors.transfer
                }

                val amountPrefix = when (transaction.type) {
                    com.example.androidkmm.models.TransactionType.INCOME -> "+"
                    com.example.androidkmm.models.TransactionType.EXPENSE -> "-"
                    com.example.androidkmm.models.TransactionType.TRANSFER -> ""
                }

                Text(
                    text = "$currencySymbol${amountPrefix}${formatDouble(transaction.amount, 2)}",
                    color = amountColor,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = transaction.type.name.lowercase().replaceFirstChar { it.uppercase() },
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
        }

        item {
            // Title Section
            Column {
                Text(
                    text = "Title",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = transaction.title,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (transaction.description.isNotEmpty()) {
            item {
                // Description Section
                Column {
                    Text(
                        text = "Description",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Text(
                        text = transaction.description,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Category Section - Only show for non-transfer transactions
        if (transaction.type != com.example.androidkmm.models.TransactionType.TRANSFER) {
            item {
                Column {
                    Text(
                        text = "Category",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(transaction.categoryColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = transaction.categoryIcon,
                                contentDescription = transaction.category,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Text(
                            text = transaction.category,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Account Section(s) - Different for each transaction type
        when (transaction.type) {
            com.example.androidkmm.models.TransactionType.TRANSFER -> {
                // Transfer Summary Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(DesignSystem.CornerRadius.md))
                            .border(
                                width = 0.5.dp,
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(DesignSystem.CornerRadius.md)
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(DesignSystem.CornerRadius.md)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Transfer Icon
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF3B82F6)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = "Transfer",
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Transfer Direction
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // From Account
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "FROM",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = transaction.account,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                // Arrow
                                Icon(
                                    imageVector = Icons.Default.ArrowForward,
                                    contentDescription = "Transfer to",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(20.dp)
                                )

                                // To Account
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "TO",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = transaction.transferTo ?: "Unknown Account",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
            else -> {
                // Show single Account for Income/Expense
                item {
                    Column {
                        Text(
                            text = "Account",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(16.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(transaction.accountColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = transaction.accountIcon,
                                    contentDescription = transaction.account,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Text(
                                text = transaction.account,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        }
        
        // Action Buttons - Always at bottom
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                border = BorderStroke(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Delete",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = onEdit,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = "Edit",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Edit",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun EditTransactionContent(
    transaction: com.example.androidkmm.models.Transaction,
    currencySymbol: String,
    onSave: (com.example.androidkmm.models.Transaction) -> Unit,
    onCancel: () -> Unit,
    onTransactionChange: (com.example.androidkmm.models.Transaction) -> Unit,
    categoryDatabaseManager: com.example.androidkmm.database.SQLiteCategoryDatabase,
    accountDatabaseManager: com.example.androidkmm.database.SQLiteAccountDatabase
) {
    var selectedType by remember { mutableStateOf(transaction.type) }
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var title by remember { mutableStateOf(transaction.title) }
    var description by remember { mutableStateOf(transaction.description) }
    var validationErrors by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var selectedCategory by remember { mutableStateOf<TransactionCategory?>(
        TransactionCategory(
            id = "current",
            name = transaction.category,
            icon = transaction.categoryIcon,
            color = transaction.categoryColor
        )
    ) }
    var selectedAccount by remember { mutableStateOf<Account?>(
        Account(
            id = "current",
            name = transaction.account,
            balance = "",
            icon = Icons.Default.AccountBalance,
            color = Color.Blue,
            type = "Current"
        )
    ) }
    var selectedToAccount by remember { mutableStateOf<Account?>(null) }

    // Bottom sheet states
    var showCategorySheet by remember { mutableStateOf(false) }
    var showFromAccountSheet by remember { mutableStateOf(false) }
    var showToAccountSheet by remember { mutableStateOf(false) }
    
    // Validation function
    fun validateForm(): Boolean {
        val errors = mutableMapOf<String, String>()
        
        // Validate title
        if (title.isBlank()) {
            errors["title"] = "Title is required"
        }
        
        // Validate amount
        if (amount.isBlank()) {
            errors["amount"] = "Amount is required"
        } else {
            val amountValue = amount.toDoubleOrNull()
            if (amountValue == null || amountValue <= 0) {
                errors["amount"] = "Please enter a valid amount"
            }
        }
        
        // Validate category
        if (selectedCategory == null) {
            errors["category"] = "Category is required"
        }
        
        // Validate account
        if (selectedAccount == null) {
            errors["account"] = "Account is required"
        }
        
        // Validate transfer accounts for transfer type
        if (selectedType == com.example.androidkmm.models.TransactionType.TRANSFER) {
            if (selectedToAccount == null) {
                errors["transferTo"] = "Transfer to account is required"
            }
            if (selectedAccount?.name == selectedToAccount?.name) {
                errors["transferTo"] = "Transfer to account must be different from from account"
            }
        }
        
        validationErrors = errors
        return errors.isEmpty()
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
                    onClick = onCancel,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Edit Transaction",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Update your transaction details",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Normal
                    )
                }

                // Spacer instead of duplicate close button
                Spacer(modifier = Modifier.size(32.dp))
            }
        }

        item {
            // Transaction Type Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        RoundedCornerShape(12.dp)
                    )
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                com.example.androidkmm.models.TransactionType.values().forEach { type ->
                    val isSelected = selectedType == type
                    val (icon, text) = when (type) {
                        com.example.androidkmm.models.TransactionType.EXPENSE -> Icons.Default.TrendingDown to "Expense"
                        com.example.androidkmm.models.TransactionType.INCOME -> Icons.Default.TrendingUp to "Income"
                        com.example.androidkmm.models.TransactionType.TRANSFER -> Icons.Default.SwapHoriz to "Transfer"
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.surface
                                else Color.Transparent,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedType = type },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = type.name,
                                tint = if (isSelected) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = text,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 16.sp,
                                fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        item {
            // Amount Input
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = currencySymbol,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Light
                    )

                    OutlinedTextField(
                        value = amount,
                        onValueChange = { amount = it },
                        textStyle = LocalTextStyle.current.copy(
                            fontSize = 48.sp,
                            fontWeight = FontWeight.Light,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.onSurface,
                            focusedTextColor = MaterialTheme.colorScheme.onSurface,
                            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.width(200.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Enter amount",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 17.sp
                )
            }
        }

        item {
            // Category and Account Selection - different for transfer vs others
            if (selectedType == com.example.androidkmm.models.TransactionType.TRANSFER) {
                // From Account and To Account for transfers
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "From Account",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium
                    )

                    SelectionCard(
                        icon = selectedAccount?.let { Icons.Default.CreditCard } ?: Icons.Default.CreditCard,
                        iconColor = Color.Blue,
                        title = selectedAccount?.name ?: "HDFC Debit Card",
                        subtitle = "HDFC Bank",
                        isSelected = true,
                        onClick = { showFromAccountSheet = true }
                    )

                    Text(
                        text = "To Account",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium
                    )

                    SelectionCard(
                        icon = Icons.Default.CreditCard,
                        iconColor = Color.Green,
                        title = selectedToAccount?.name ?: "Select",
                        subtitle = selectedToAccount?.type,
                        isSelected = selectedToAccount != null,
                        onClick = { showToAccountSheet = true }
                    )
                }
            } else {
                // Category and Account for income/expense
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Category",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SelectionCard(
                            icon = selectedCategory?.icon ?: transaction.categoryIcon,
                            iconColor = selectedCategory?.color ?: transaction.categoryColor,
                            title = selectedCategory?.name ?: transaction.category,
                            isSelected = true,
                            onClick = { showCategorySheet = true }
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Account",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        SelectionCard(
                            icon = selectedAccount?.let { Icons.Default.CreditCard } ?: transaction.accountIcon,
                            iconColor = selectedAccount?.let { Color.Blue } ?: transaction.accountColor,
                            title = selectedAccount?.name?.let { if (it.length > 15) "${it.take(12)}..." else it }
                                ?: "HDFC Debit ...",
                            subtitle = "HDFC Bank",
                            isSelected = true,
                            onClick = { showFromAccountSheet = true }
                        )
                    }
                }
            }
        }

        item {
            // Date & Time
            Text(
                text = "Date & Time",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = transaction.date.ifEmpty { "Today" },
                    onValueChange = { },
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Date",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = transaction.time,
                    onValueChange = { },
                    modifier = Modifier.weight(1f),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Time",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.outline,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        item {
            // Title
            Text(
                text = "Title",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            // Description
            Text(
                text = "Description (Optional)",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.outline,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                    focusedTextColor = MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            // Receipt
            Text(
                text = "Receipt (Optional)",
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Add Receipt",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Add Receipt",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 17.sp
                    )
                }
            }
        }

        item {
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "Cancel",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Button(
                    onClick = {
                        if (validateForm()) {
                            val updatedTransaction = transaction.copy(
                                type = selectedType,
                                amount = amount.toDoubleOrNull() ?: transaction.amount,
                                title = title,
                                description = description.takeIf { it.isNotEmpty() } ?: "",
                                category = if (selectedType == com.example.androidkmm.models.TransactionType.TRANSFER) "Transfer" else (selectedCategory?.name ?: transaction.category),
                                categoryIcon = if (selectedType == com.example.androidkmm.models.TransactionType.TRANSFER) Icons.Default.SwapHoriz else (selectedCategory?.icon ?: transaction.categoryIcon),
                                categoryColor = if (selectedType == com.example.androidkmm.models.TransactionType.TRANSFER) Color(0xFF3B82F6) else (selectedCategory?.color ?: transaction.categoryColor),
                                account = selectedAccount?.name ?: transaction.account,
                                transferTo = if (selectedType == com.example.androidkmm.models.TransactionType.TRANSFER) selectedToAccount?.name else null
                            )
                            onSave(updatedTransaction)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save Changes",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Category Selection Sheet
    if (showCategorySheet) {
        CategorySelectionBottomSheet(
            onDismiss = { showCategorySheet = false },
            onCategorySelected = { category: TransactionCategory ->
                selectedCategory = category
                showCategorySheet = false
            },
            categoryDatabaseManager = categoryDatabaseManager,
            transactionType = transaction.type
        )
    }

    // From Account Selection Sheet
    if (showFromAccountSheet) {
        AccountSelectionBottomSheet(
            onDismiss = { showFromAccountSheet = false },
            title = "Select Account",
            subtitle = "Choose an account for your transaction",
            onAccountSelected = { account: Account ->
                selectedAccount = account
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
            onAccountSelected = { account: Account ->
                selectedToAccount = account
                showToAccountSheet = false
            },
            accountDatabaseManager = accountDatabaseManager
        )
    }
}

@Composable
private fun SelectionCard(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String? = null,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surfaceVariant,
                RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(iconColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )

            subtitle?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 15.sp
                )
            }
        }
    }
}

