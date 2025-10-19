@file:OptIn(ExperimentalMaterial3Api::class)

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.androidkmm.utils.CurrencyUtils.formatDouble
import com.example.androidkmm.database.rememberSQLiteSettingsDatabase
import com.example.androidkmm.models.AppSettings
import com.example.androidkmm.design.AppStyleDesignSystem
import androidx.compose.material3.MaterialTheme
import com.example.androidkmm.screens.EditTransferTransactionScreen

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
            modifier = Modifier.fillMaxWidth()
                .wrapContentHeight(),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            if (isEditMode) {
                if (editedTransaction.type == com.example.androidkmm.models.TransactionType.TRANSFER) {
                    EditTransferTransactionScreen(
                        transaction = editedTransaction,
                        onDismiss = {
                            isEditMode = false
                            editedTransaction = transaction
                        },
                        onSave = { updatedTransaction: com.example.androidkmm.models.Transaction ->
                            onEdit(updatedTransaction)
                            isEditMode = false
                        },
                        accountDatabaseManager = accountDatabaseManager
                    )
                } else {
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
                }
            } else {
                TransactionDetailsContent(
                    transaction = transaction,
                    currencySymbol = currencySymbol,
                    onEdit = {
                        if (transaction.category == "Ledger" && transaction.id.startsWith("main_")) {
                            showLedgerDialog = true
                        } else {
                            isEditMode = true
                        }
                    },
                    onDelete = {
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

    // Ledger Navigation Dialog (unchanged)
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
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(Color(0xFF2A2A2A), CircleShape),
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
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "This transaction is part of your ledger. To edit or delete it, you need to go to the ledger section.",
                        color = Color(0xFF8E8E93),
                        fontSize = 12.sp,
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
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            border = BorderStroke(1.5.dp, Color(0xFF3A3A3A)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Cancel", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        }
                        Button(
                            onClick = {
                                showLedgerDialog = false
                                onDismiss()
                                val personName = when {
                                    transaction.title.startsWith("Sent to ") ->
                                        transaction.title.replace("Sent to ", "").trim()
                                    transaction.title.startsWith("Received from ") ->
                                        transaction.title.replace("Received from ", "").trim()
                                    transaction.title.contains(" - Transfer") ->
                                        transaction.title.replace(" - Transfer", "").trim()
                                    transaction.title.contains(" to ") -> {
                                        val parts = transaction.title.split(" to ")
                                        if (parts.size > 1) parts[1].trim() else transaction.title
                                    }
                                    transaction.title.contains(" from ") -> {
                                        val parts = transaction.title.split(" from ")
                                        if (parts.size > 1) parts[1].trim() else transaction.title
                                    }
                                    else -> transaction.description.ifEmpty { transaction.title }.trim()
                                }
                                onNavigateToLedger(
                                    personName,
                                    "${transaction.amount}|${transaction.date}|${transaction.time}"
                                )
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF007AFF),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Move", fontSize = 16.sp, fontWeight = FontWeight.Medium)
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
            .fillMaxWidth()
            .wrapContentHeight()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        // Close button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "${transaction.type}",
                fontSize = 8.sp,
                color = Color.White,
                modifier = Modifier
                    .background(Color(0xFF101828), RoundedCornerShape(6.dp))
                    .padding(horizontal = 12.dp, vertical = 2.dp)
            )

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
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_MEDIUM)
                )
            }

        }

        Spacer(modifier = Modifier.height(8.dp))

        // Amount
        val amountColor = when (transaction.type) {
            com.example.androidkmm.models.TransactionType.INCOME -> Color(0xFF4CAF50)
            com.example.androidkmm.models.TransactionType.EXPENSE -> Color(0xFFE53935)
            com.example.androidkmm.models.TransactionType.TRANSFER -> Color(0xFF3B82F6)
        }
        val amountPrefix = when (transaction.type) {
            com.example.androidkmm.models.TransactionType.INCOME -> "+"
            com.example.androidkmm.models.TransactionType.EXPENSE -> "-"
            else -> ""
        }
        Text(
            text = "$currencySymbol$amountPrefix${formatDouble(transaction.amount, 2)}",
            color = amountColor,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Title
        Text(
            text = transaction.title.ifBlank { "Transaction" },
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal
        )


        // Date
        Text(
            text = "${transaction.date}, ${transaction.time}",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 10.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Tags (category + account or transfer accounts)
        if (transaction.type == com.example.androidkmm.models.TransactionType.TRANSFER) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                TagRow("From Account", transaction.account, transaction.accountIcon, transaction.accountColor, Modifier.weight(1f))
                TagRow("To Account", transaction.transferTo ?: "Unknown", Icons.Default.CreditCard, Color(0xFF757575), Modifier.weight(1f))
            }
        } else {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                TagRow(transaction.category, transaction.category, transaction.categoryIcon, transaction.categoryColor, Modifier.weight(1f))
                TagRow(transaction.account, transaction.account, transaction.accountIcon, transaction.accountColor, Modifier.weight(1f))
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Description
        if (transaction.description.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF101828), RoundedCornerShape(12.dp))
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Text(transaction.description, color = MaterialTheme.colorScheme.onSurface, fontSize = 10.sp)
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // Buttons
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier.weight(1f).height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Delete")
                }
            }
            Button(
                onClick = onEdit,
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Edit")
                }
            }
        }
    }
}

@Composable
private fun TagRow(
    label: String,
    value: String,
    icon: ImageVector,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(Color(0xFF101828), RoundedCornerShape(12.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Icon(icon, contentDescription = label, tint = bgColor, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            value, 
            color = MaterialTheme.colorScheme.onSurface, 
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}


