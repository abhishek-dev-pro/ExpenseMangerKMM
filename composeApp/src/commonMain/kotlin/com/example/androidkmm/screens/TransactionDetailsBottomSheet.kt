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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.utils.formatDouble

// Colors for Transaction Details
object TransactionDetailColors {
    val background = Color(0xFF1C1C1E)
    val surface = Color(0xFF2C2C2E)
    val primary = Color.White
    val secondary = Color(0xFF8E8E93)
    val expense = Color(0xFFFF3B30)
    val income = Color(0xFF34C759)
    val transfer = Color(0xFF007AFF)
    val divider = Color(0xFF38383A)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionDetailsBottomSheet(
    transaction: Transaction,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onEdit: (Transaction) -> Unit,
    onDelete: () -> Unit
) {
    var isEditMode by remember { mutableStateOf(false) }
    var editedTransaction by remember { mutableStateOf(transaction) }

    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = bottomSheetState,
            modifier = Modifier.fillMaxSize(),
            containerColor = TransactionDetailColors.background
        ) {
            if (isEditMode) {
                EditTransactionContent(
                    transaction = editedTransaction,
                    onSave = { updatedTransaction ->
                        onEdit(updatedTransaction)
                        isEditMode = false
                    },
                    onCancel = {
                        isEditMode = false
                        editedTransaction = transaction
                    },
                    onTransactionChange = { editedTransaction = it }
                )
            } else {
                TransactionDetailsContent(
                    transaction = transaction,
                    onEdit = { isEditMode = true },
                    onDelete = onDelete,
                    onDismiss = onDismiss
                )
            }
        }
    }
}

@Composable
private fun TransactionDetailsContent(
    transaction: Transaction,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 24.dp),
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
                        color = TransactionDetailColors.primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${transaction.date}, ${transaction.time}",
                        color = TransactionDetailColors.secondary,
                        fontSize = 14.sp
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
                            tint = TransactionDetailColors.primary,
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
                        .size(80.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(transaction.categoryColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = transaction.categoryIcon,
                        contentDescription = transaction.category,
                        tint = Color.White,
                        modifier = Modifier.size(60.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val amountColor = when (transaction.type) {
                    TransactionType.INCOME -> TransactionDetailColors.income
                    TransactionType.EXPENSE -> TransactionDetailColors.expense
                    TransactionType.TRANSFER -> TransactionDetailColors.transfer
                }

                val amountPrefix = when (transaction.type) {
                    TransactionType.INCOME -> "+"
                    TransactionType.EXPENSE -> "-"
                    TransactionType.TRANSFER -> ""
                }

                Text(
                    text = "$${amountPrefix}${formatDouble(transaction.amount, 2)}",
                    color = amountColor,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = transaction.type.name.lowercase().replaceFirstChar { it.uppercase() },
                    color = TransactionDetailColors.secondary,
                    fontSize = 17.sp,
                    modifier = Modifier
                        .background(
                            TransactionDetailColors.surface,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }

        item {
            // Title Section
            Column {
                Text(
                    text = "Title",
                    color = TransactionDetailColors.secondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = transaction.title,
                    color = TransactionDetailColors.primary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (transaction.description.isNotEmpty()) {
            item {
                // Description Section
                Column {
                    Text(
                        text = "Description",
                        color = TransactionDetailColors.secondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = transaction.description,
                        color = TransactionDetailColors.primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            // Category Section
            Column {
                Text(
                    text = "Category",
                    color = TransactionDetailColors.secondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            TransactionDetailColors.surface,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(transaction.categoryColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = transaction.categoryIcon,
                            contentDescription = transaction.category,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = transaction.category,
                        color = TransactionDetailColors.primary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        item {
            // Account Section
            Column {
                Text(
                    text = "Account",
                    color = TransactionDetailColors.secondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            TransactionDetailColors.surface,
                            RoundedCornerShape(16.dp)
                        )
                        .padding(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(transaction.accountColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = transaction.accountIcon,
                            contentDescription = transaction.account,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = transaction.account,
                            color = TransactionDetailColors.primary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "HDFC Bank",
                            color = TransactionDetailColors.secondary,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        item {
            // Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 40.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TransactionDetailColors.primary
                    ),
                    border = BorderStroke(
                        width = 2.dp,
                        color = TransactionDetailColors.primary
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Delete",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = onEdit,
                    modifier = Modifier
                        .weight(1f)
                        .height(64.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = "Edit",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Edit",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun EditTransactionContent(
    transaction: Transaction,
    onSave: (Transaction) -> Unit,
    onCancel: () -> Unit,
    onTransactionChange: (Transaction) -> Unit
) {
    var selectedType by remember { mutableStateOf(transaction.type) }
    var amount by remember { mutableStateOf(transaction.amount.toString()) }
    var title by remember { mutableStateOf(transaction.title) }
    var description by remember { mutableStateOf(transaction.description) }
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
            type = "Current"
        )
    ) }
    var selectedToAccount by remember { mutableStateOf<Account?>(null) }

    // Bottom sheet states
    var showCategorySheet by remember { mutableStateOf(false) }
    var showFromAccountSheet by remember { mutableStateOf(false) }
    var showToAccountSheet by remember { mutableStateOf(false) }

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
                        tint = TransactionDetailColors.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Edit Transaction",
                        color = TransactionDetailColors.primary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Update your transaction details",
                        color = TransactionDetailColors.secondary,
                        fontSize = 14.sp
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
                        TransactionDetailColors.surface,
                        RoundedCornerShape(12.dp)
                    )
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

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .background(
                                if (isSelected) TransactionDetailColors.background
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
                                tint = if (isSelected) TransactionDetailColors.primary
                                else TransactionDetailColors.secondary,
                                modifier = Modifier.size(18.dp)
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Text(
                                text = text,
                                color = if (isSelected) TransactionDetailColors.primary
                                else TransactionDetailColors.secondary,
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
                        text = "$",
                        color = TransactionDetailColors.primary,
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
                            color = TransactionDetailColors.primary
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            cursorColor = TransactionDetailColors.primary,
                            focusedTextColor = TransactionDetailColors.primary,
                            unfocusedTextColor = TransactionDetailColors.primary,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        modifier = Modifier.width(200.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Enter amount",
                    color = TransactionDetailColors.secondary,
                    fontSize = 17.sp
                )
            }
        }

        item {
            // Category and Account Selection - different for transfer vs others
            if (selectedType == TransactionType.TRANSFER) {
                // From Account and To Account for transfers
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "From Account",
                        color = TransactionDetailColors.primary,
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
                        color = TransactionDetailColors.primary,
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
                            color = TransactionDetailColors.primary,
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
                            color = TransactionDetailColors.primary,
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
                color = TransactionDetailColors.primary,
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
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TransactionDetailColors.divider,
                        unfocusedBorderColor = TransactionDetailColors.divider,
                        focusedTextColor = TransactionDetailColors.primary,
                        unfocusedTextColor = TransactionDetailColors.primary,
                        focusedContainerColor = TransactionDetailColors.surface,
                        unfocusedContainerColor = TransactionDetailColors.surface
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
                            tint = TransactionDetailColors.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = TransactionDetailColors.divider,
                        unfocusedBorderColor = TransactionDetailColors.divider,
                        focusedTextColor = TransactionDetailColors.primary,
                        unfocusedTextColor = TransactionDetailColors.primary,
                        focusedContainerColor = TransactionDetailColors.surface,
                        unfocusedContainerColor = TransactionDetailColors.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        item {
            // Title
            Text(
                text = "Title",
                color = TransactionDetailColors.primary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TransactionDetailColors.divider,
                    unfocusedBorderColor = TransactionDetailColors.divider,
                    focusedTextColor = TransactionDetailColors.primary,
                    unfocusedTextColor = TransactionDetailColors.primary,
                    focusedContainerColor = TransactionDetailColors.surface,
                    unfocusedContainerColor = TransactionDetailColors.surface
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            // Description
            Text(
                text = "Description (Optional)",
                color = TransactionDetailColors.primary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TransactionDetailColors.divider,
                    unfocusedBorderColor = TransactionDetailColors.divider,
                    focusedTextColor = TransactionDetailColors.primary,
                    unfocusedTextColor = TransactionDetailColors.primary,
                    focusedContainerColor = TransactionDetailColors.surface,
                    unfocusedContainerColor = TransactionDetailColors.surface
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        item {
            // Receipt
            Text(
                text = "Receipt (Optional)",
                color = TransactionDetailColors.primary,
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
                        color = TransactionDetailColors.secondary,
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
                        tint = TransactionDetailColors.secondary,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Add Receipt",
                        color = TransactionDetailColors.secondary,
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
                        contentColor = TransactionDetailColors.primary
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = TransactionDetailColors.primary
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
                        val updatedTransaction = transaction.copy(
                            type = selectedType,
                            amount = amount.toDoubleOrNull() ?: transaction.amount,
                            title = title,
                            description = description.takeIf { it.isNotEmpty() } ?: "",
                            category = selectedCategory?.name ?: transaction.category,
                            categoryIcon = selectedCategory?.icon ?: transaction.categoryIcon,
                            categoryColor = selectedCategory?.color ?: transaction.categoryColor,
                            account = selectedAccount?.name ?: transaction.account,
                            transferTo = if (selectedType == TransactionType.TRANSFER) selectedToAccount?.name else null
                        )
                        onSave(updatedTransaction)
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
            onCategorySelected = { category ->
                selectedCategory = category
                showCategorySheet = false
            }
        )
    }

    // From Account Selection Sheet
    if (showFromAccountSheet) {
        AccountSelectionBottomSheet(
            onDismiss = { showFromAccountSheet = false },
            title = "Select Account",
            subtitle = "Choose an account for your transaction",
            onAccountSelected = { account ->
                selectedAccount = account
                showFromAccountSheet = false
            }
        )
    }

    // To Account Selection Sheet
    if (showToAccountSheet) {
        AccountSelectionBottomSheet(
            onDismiss = { showToAccountSheet = false },
            title = "Select Account",
            subtitle = "Choose destination account",
            onAccountSelected = { account ->
                selectedToAccount = account
                showToAccountSheet = false
            }
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
                TransactionDetailColors.surface,
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
                color = TransactionDetailColors.primary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Medium
            )

            subtitle?.let {
                Text(
                    text = it,
                    color = TransactionDetailColors.secondary,
                    fontSize = 15.sp
                )
            }
        }
    }
}