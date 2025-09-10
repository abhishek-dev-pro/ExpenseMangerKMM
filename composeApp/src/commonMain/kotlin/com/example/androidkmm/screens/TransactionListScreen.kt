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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.utils.formatDouble
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

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
    val transactions: List<Transaction>,
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



@Composable
fun TransactionsScreen() {
    val transactions = remember { getSampleTransactions() }
    val dayGroups = remember { groupTransactionsByDay(transactions) }

    var showAddSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(TransactionColors.background)
            .statusBarsPadding()
    ) {
        // Header Section
        TransactionHeader(onAddClick = { showAddSheet = true })

        // Month Navigation
        MonthNavigation()

        // Summary Card
        SummaryCard()

        // Search and Filter
        SearchAndFilter()

        // Transaction List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(dayGroups) { dayGroup ->
                DayGroupSection(dayGroup)
            }
        }
    }

    // Show bottom sheet
    if (showAddSheet) {
        AddTransactionBottomSheet(
            onDismiss = { showAddSheet = false },
            onSave = { transaction ->
                // handle save logic here
                showAddSheet = false
            }
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
private fun MonthNavigation() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = { },
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
            text = "September 2025",
            color = TransactionColors.primaryText,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold
        )

        IconButton(
            onClick = { },
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
private fun SummaryCard() {
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
                amount = "$2500.00",
                label = "Income",
                amountColor = TransactionColors.income
            )

            SummaryColumn(
                icon = Icons.Default.TrendingDown,
                iconColor = TransactionColors.expense,
                amount = "$249.79",
                label = "Expenses",
                amountColor = TransactionColors.expense
            )

            SummaryColumn(
                icon = Icons.Default.AttachMoney,
                iconColor = TransactionColors.income,
                amount = "+$2294.51",
                label = "Total",
                amountColor = TransactionColors.income
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
private fun SearchAndFilter() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
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
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = TransactionColors.searchBackground,
                unfocusedContainerColor = TransactionColors.searchBackground,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = TransactionColors.primaryText,
                unfocusedTextColor = TransactionColors.primaryText
            )
        )

        IconButton(
            onClick = { },
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
private fun DayGroupSection(dayGroup: DayGroup) {
    var selectedTransaction by remember { mutableStateOf<Transaction?>(null) }
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
                // Handle transaction edit
                println("Transaction edited: ${editedTransaction.title}")
            },
            onDelete = {
                // Handle transaction delete
                println("Transaction deleted")
                showBottomSheet = false
                selectedTransaction = null
            }
        )
    }
}

@Composable
fun TransactionCard(
    transaction: Transaction,
    onClick: (Transaction) -> Unit = {}
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
                    TransactionType.INCOME -> TransactionColors.income
                    TransactionType.EXPENSE -> TransactionColors.expense
                    TransactionType.TRANSFER -> TransactionColors.transfer
                }

                val amountText = when (transaction.type) {
                    TransactionType.INCOME -> "+${formatDouble(transaction.amount, 2)}"
                    TransactionType.EXPENSE -> "-${formatDouble(transaction.amount, 2)}"
                    TransactionType.TRANSFER -> "${formatDouble(transaction.amount, 2)}"
                }

                Text(
                    text = amountText,
                    color = amountColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )

                val accountText = if (transaction.type == TransactionType.TRANSFER && transaction.transferTo != null) {
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
    onSave: (TransactionFormData) -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var formData by remember { mutableStateOf(TransactionFormData()) }
    var showCategorySheet by remember { mutableStateOf(false) }
    var showFromAccountSheet by remember { mutableStateOf(false) }
    var showToAccountSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Set default time
        val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val currentTime = now.time // this is a kotlinx.datetime.LocalTime
        formData = formData.copy(time = currentTime.toString())
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
                formData = formData.copy(account = account)
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
                formData = formData.copy(toAccount = account)
                showToAccountSheet = false
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
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
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
                }
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
                        icon = Icons.Default.CreditCard,
                        onClick = onShowFromAccountSheet
                    )

                    CategoryAccountSelector(
                        modifier = Modifier.weight(1f),
                        title = "To Account",
                        selectedText = formData.toAccount?.name ?: "Select",
                        icon = Icons.Default.CreditCard,
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
                        icon = Icons.Default.AttachMoney,
                        onClick = onShowCategorySheet
                    )

                    CategoryAccountSelector(
                        modifier = Modifier.weight(1f),
                        title = "Account",
                        selectedText = formData.account?.name ?: "Select",
                        icon = Icons.Default.CreditCard,
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
                placeholder = "e.g., Lunch at Subway"
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
                    value = formData.date.ifEmpty { "Today" },
                    icon = Icons.Default.DateRange,
                    isSelected = true,
                    onClick = { /* TODO: Open date picker */ }
                )

                DateTimeSelector(
                    modifier = Modifier.weight(1f),
                    value = formData.time.ifEmpty { "01:31" },
                    icon = Icons.Default.Schedule,
                    isSelected = false,
                    onClick = { /* TODO: Open time picker */ }
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
                onClick = onSave,
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
    onAmountChange: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$",
                color = TransactionColors.primaryText,
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = amount.ifEmpty { "0" },
                color = TransactionColors.primaryText,
                fontSize = 32.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Text(
            text = "Enter amount",
            color = TransactionColors.secondaryText,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CategoryAccountSelector(
    modifier: Modifier = Modifier,
    title: String,
    selectedText: String,
    icon: ImageVector,
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
                    modifier = Modifier.size(20.dp)
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
    placeholder: String
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
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = TransactionColors.cardBackground,
                unfocusedContainerColor = TransactionColors.cardBackground,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = TransactionColors.primaryText,
                unfocusedTextColor = TransactionColors.primaryText
            )
        )
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
private fun ReceiptUploadSection() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Receipt (Optional)",
            color = TransactionColors.primaryText,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )

        Button(
            onClick = { /* TODO: Open camera/gallery */ },
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
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSelectionBottomSheet(
    onDismiss: () -> Unit,
    title: String = "Select Account",
    subtitle: String = "Choose an account for your transaction",
    onAccountSelected: (Account) -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val accounts = remember { getSampleAccounts() }

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
    onCategorySelected: (TransactionCategory) -> Unit
) {
    val bottomSheetState = rememberModalBottomSheetState()
    val categories = remember { getSampleCategories() }

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

            items(categories.chunked(2)) { categoryPair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    categoryPair.forEach { category ->
                        CategoryCard(
                            modifier = Modifier.weight(1f),
                            category = category,
                            onClick = { onCategorySelected(category) }
                        )
                    }

                    // Fill remaining space if odd number
                    if (categoryPair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
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
            .aspectRatio(1f)
            .border(
                width = 0.3.dp, // thin border
                color = Color.White,
                shape = RoundedCornerShape(16.dp) // same shape as card
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Black
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
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
                    contentDescription = category.name,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = category.name,
                color = TransactionColors.primaryText,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
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

private fun groupTransactionsByDay(transactions: List<Transaction>): List<DayGroup> {
    val today = listOf(transactions[0], transactions[1])
    val yesterday = listOf(transactions[2], transactions[3])

    return listOf(
        DayGroup(
            date = "2025-09-10",
            displayDate = "Today",
            transactions = today,
            income = today.filter { it.type == TransactionType.INCOME }.sumOf { it.amount },
            expense = today.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        ),
        DayGroup(
            date = "2025-09-09",
            displayDate = "Yesterday",
            transactions = yesterday,
            income = 0.0,
            expense = yesterday.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
        )
    )
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