package com.example.androidkmm.screens.ledger

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.utils.CurrencyUtils.formatDouble
import com.example.androidkmm.database.rememberSQLiteLedgerDatabase
import com.example.androidkmm.database.rememberSQLiteTransactionDatabase
import com.example.androidkmm.database.rememberSQLiteSettingsDatabase
import com.example.androidkmm.database.rememberSQLiteAccountDatabase
import com.example.androidkmm.models.AppSettings
import com.example.androidkmm.design.AppStyleDesignSystem
import androidx.compose.runtime.collectAsState
import kotlin.math.abs
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

// PersonLedgerDetailScreen.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonLedgerDetailScreen(
    person: LedgerPerson,
    onBack: () -> Unit,
    onAddTransaction: () -> Unit,
    highlightTransactionId: String? = null
) {
    val ledgerDatabaseManager = rememberSQLiteLedgerDatabase()
    val transactionDatabaseManager = rememberSQLiteTransactionDatabase()
    val settingsDatabaseManager = rememberSQLiteSettingsDatabase()
    val accountDatabaseManager = rememberSQLiteAccountDatabase()
    val coroutineScope = rememberCoroutineScope()
    
    // Get currency symbol from settings
    val appSettings = settingsDatabaseManager.getAppSettings().collectAsState(initial = AppSettings())
    val currencySymbol = appSettings.value.currencySymbol
    val transactionsState = ledgerDatabaseManager.getLedgerTransactionsByPerson(person.id).collectAsState(initial = emptyList<LedgerTransaction>())
    val transactions = transactionsState.value
    
    // State for temporary highlighting
    var tempHighlightedId by remember { mutableStateOf<String?>(null) }
    
    // Handle temporary highlighting when highlightTransactionId changes
    LaunchedEffect(highlightTransactionId) {
        if (highlightTransactionId != null) {
            println("PersonLedgerDetailScreen - Starting 3-second highlight for: '$highlightTransactionId'")
            // Set the highlighted transaction immediately
            tempHighlightedId = highlightTransactionId
            
            // Clear highlighting after 3 seconds
            kotlinx.coroutines.delay(3000)
            tempHighlightedId = null
            println("PersonLedgerDetailScreen - Highlighting cleared after 3 seconds")
        } else {
            // Clear highlighting immediately if highlightTransactionId is null
            tempHighlightedId = null
        }
    }
    
    // Get updated person data from database - use LaunchedEffect to get the latest person data
    var updatedPerson by remember { mutableStateOf(person) }
    
    // Update person data when transactions change (this will trigger balance recalculation)
    LaunchedEffect(person.id, transactions) {
        println("DEBUG: PersonLedgerDetailScreen - Transactions changed, updating person data")
        val latestPerson = ledgerDatabaseManager.getLedgerPersonById(person.id)
        if (latestPerson != null) {
            println("DEBUG: PersonLedgerDetailScreen - Updated person balance: ${latestPerson.balance}")
            updatedPerson = latestPerson
        }
    }
    
    var showSentBottomSheet by remember { mutableStateOf(false) }
    var showReceivedBottomSheet by remember { mutableStateOf(false) }
    var showEditBottomSheet by remember { mutableStateOf(false) }
    var transactionToEdit by remember { mutableStateOf<LedgerTransaction?>(null) }
    var selectedTransactionType by remember { mutableStateOf("sent") } // "sent" or "received"
    
    // Delete confirmation dialog state
    var showDeleteTransactionDialog by remember { mutableStateOf(false) }
    var transactionToDelete by remember { mutableStateOf<LedgerTransaction?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LedgerTheme.backgroundColor())
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(person.avatarColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (updatedPerson.name.isNotBlank()) {
                        updatedPerson.name.split(" ").mapNotNull { if (it.isNotBlank()) it.first() else null }.joinToString("")
                    } else {
                        "?"
                    },
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = updatedPerson.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LedgerTheme.textPrimary()
                )
                Text(
                    text = "${updatedPerson.transactionCount} entr${if (updatedPerson.transactionCount > 1) "ies" else "y"}",
                    fontSize = 14.sp,
                    color = LedgerTheme.textSecondary()
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Back Button (moved to right side)
            IconButton(
                onClick = { onBack() },
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        Color(0xFF2A2A2A),
                        CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Balance Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM))
                .border(
                    width = 0.5.dp, // very thin border
                    color = Color.White.copy(alpha = 0.2f), // subtle white
                    shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (updatedPerson.balance < 0) Color(0xFF0F1619) else Color(0xFF2A1919)
            ),
            shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (updatedPerson.balance < 0) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (updatedPerson.balance < 0) LedgerTheme.greenAmount else LedgerTheme.redAmount,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Current Balance",
                        fontSize = 14.sp,
                        color = LedgerTheme.textSecondary()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$currencySymbol${formatDouble(abs(updatedPerson.balance))}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        updatedPerson.balance == 0.0 -> Color(0xFF2196F3) // Blue for settled up
                        updatedPerson.balance < 0 -> LedgerTheme.redAmount
                        else -> LedgerTheme.greenAmount
                    }
                )

                Text(
                    text = when {
                        updatedPerson.balance == 0.0 -> "Settled up"
                        updatedPerson.balance < 0 -> "You will get"
                        else -> "You will give"
                    },
                    fontSize = 16.sp,
                    color = LedgerTheme.textSecondary()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transaction Type Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // You Sent Button
            Button(
                onClick = { 
                    selectedTransactionType = "sent"
                    showSentBottomSheet = true 
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .border(
                        width = AppStyleDesignSystem.Sizes.BORDER_NORMAL,
                        color = if (selectedTransactionType == "sent") 
                            Color(0xFF2196F3) 
                        else 
                            Color(0xFF4A4A4A),
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTransactionType == "sent") 
                        Color(0xFF2196F3) 
                    else 
                        Color(0xFF1A1A1A)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "You Sent",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // You Received Button
            Button(
                onClick = { 
                    selectedTransactionType = "received"
                    showReceivedBottomSheet = true 
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp)
                    .border(
                        width = AppStyleDesignSystem.Sizes.BORDER_NORMAL,
                        color = if (selectedTransactionType == "received") 
                            Color(0xFF4CAF50) 
                        else 
                            Color(0xFF4A4A4A),
                        shape = RoundedCornerShape(16.dp)
                    ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedTransactionType == "received") 
                        Color(0xFF4CAF50) 
                    else 
                        Color(0xFF1A1A1A)
                ),
                shape = RoundedCornerShape(16.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 2.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "You Received",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transaction History Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Transaction History",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = LedgerTheme.textPrimary()
            )

            Box(
                modifier = Modifier
                    .background(
                        Color(0xFF1F1F1F),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "${updatedPerson.transactionCount} entries",
                    fontSize = 12.sp,
                    color = LedgerTheme.textSecondary()
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Received",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = LedgerTheme.textPrimary()
            )

            Text(
                text = "Transaction",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = LedgerTheme.textPrimary()
            )

            Text(
                text = "Sent  ",
                fontSize = 12.sp,
                color = LedgerTheme.textSecondary()
            )
        }



        // Transactions List
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(transactions.withIndex().toList()) { (index, transaction) ->
                // Use the stored balance at the time of this transaction
                val balanceAtTransaction = transaction.balanceAtTime
                
                // Enhanced highlighting logic - match by amount, date, and time using temporary state
                val currentHighlightId = tempHighlightedId
                val isHighlighted = if (currentHighlightId != null) {
                    val parts = currentHighlightId.split("|")
                    if (parts.size == 3) {
                        val highlightAmount = parts[0].toDoubleOrNull()
                        val highlightDate = parts[1]
                        val highlightTime = parts[2]
                        val matches = highlightAmount == transaction.amount && highlightDate == transaction.date && highlightTime == transaction.time
                        if (matches) {
                            println("PersonLedgerDetailScreen - MATCH FOUND! Transaction amount: '${transaction.amount}', date: '${transaction.date}', time: '${transaction.time}'")
                        }
                        matches
                    } else {
                        false
                    }
                } else {
                    false
                }
                
                // Debug current state for first transaction
                if (index == 0) {
                    println("PersonLedgerDetailScreen - Transaction 0: isHighlighted=$isHighlighted, tempHighlightedId='$tempHighlightedId', highlightTransactionId='$highlightTransactionId'")
                }
                
                TransactionItem(
                    transaction = transaction,
                    balanceAtTransaction = balanceAtTransaction,
                    isHighlighted = isHighlighted,
                    onEdit = {
                        transactionToEdit = transaction
                        showEditBottomSheet = true
                    },
                    onDelete = {
                        // Show confirmation dialog before deleting
                        transactionToDelete = transaction
                        showDeleteTransactionDialog = true
                    },
                    onLongPress = {
                        // Show confirmation dialog before deleting on long press
                        transactionToDelete = transaction
                        showDeleteTransactionDialog = true
                    }
                )

            }
        }
    }
    
    // Show sent money bottom sheet
    if (showSentBottomSheet) {
        AddLedgerEntryBottomSheet(
            onDismiss = { 
                showSentBottomSheet = false
                // Refresh person data when bottom sheet is dismissed
                coroutineScope.launch {
                    val latestPerson = ledgerDatabaseManager.getLedgerPersonById(person.id)
                    if (latestPerson != null) {
                        updatedPerson = latestPerson
                    }
                }
            },
            person = updatedPerson
        )
    }
    
    // Show received money bottom sheet
    if (showReceivedBottomSheet) {
        AddLedgerEntryBottomSheet(
            onDismiss = { 
                showReceivedBottomSheet = false
                // Refresh person data when bottom sheet is dismissed
                coroutineScope.launch {
                    val latestPerson = ledgerDatabaseManager.getLedgerPersonById(person.id)
                    if (latestPerson != null) {
                        updatedPerson = latestPerson
                    }
                }
            },
            person = updatedPerson,
            transactionType = TransactionType.RECEIVED
        )
    }
    
    // Show edit transaction bottom sheet
    if (showEditBottomSheet && transactionToEdit != null) {
        EditLedgerEntryBottomSheet(
            onDismiss = { 
                showEditBottomSheet = false
                transactionToEdit = null
                // Refresh person data when bottom sheet is dismissed
                coroutineScope.launch {
                    val latestPerson = ledgerDatabaseManager.getLedgerPersonById(person.id)
                    if (latestPerson != null) {
                        updatedPerson = latestPerson
                    }
                }
            },
            transaction = transactionToEdit!!,
            person = updatedPerson
        )
    }
    
    // Delete Transaction Confirmation Dialog
    if (showDeleteTransactionDialog && transactionToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteTransactionDialog = false
                transactionToDelete = null
            },
            title = {
                Text(
                    text = "Delete Transaction",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete this transaction? This action cannot be undone and will update your account balance.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                val transaction = transactionToDelete!!
                                
                                // Create a transaction object to reverse the account balance
                                val transactionToReverse = com.example.androidkmm.models.Transaction(
                                    id = "main_${transaction.id}",
                                    title = transaction.description,
                                    amount = transaction.amount,
                                    category = "",
                                    categoryIcon = Icons.Default.Category,
                                    categoryColor = Color.Transparent,
                                    account = transaction.account ?: "Cash",
                                    accountIcon = Icons.Default.Wallet,
                                    accountColor = Color.Transparent,
                                    time = transaction.time,
                                    type = if (transaction.type == com.example.androidkmm.screens.ledger.TransactionType.SENT) 
                                        com.example.androidkmm.models.TransactionType.EXPENSE 
                                    else 
                                        com.example.androidkmm.models.TransactionType.INCOME,
                                    description = transaction.description,
                                    date = transaction.date
                                )
                                
                                // Reverse the account balance changes
                                transactionDatabaseManager.deleteTransactionWithBalanceUpdate(
                                    transaction = transactionToReverse,
                                    accountDatabaseManager = accountDatabaseManager,
                                    onSuccess = {
                                        println("DEBUG: Account balance reversed successfully for transaction ${transaction.id}")
                                    },
                                    onError = { error ->
                                        println("DEBUG: Error reversing account balance: ${error.message}")
                                    }
                                )
                                
                                // Delete the ledger transaction and update person balance
                                ledgerDatabaseManager.deleteLedgerTransactionAndUpdatePerson(
                                    transactionId = transaction.id,
                                    personId = person.id
                                )
                                
                                println("DEBUG: Ledger transaction deleted successfully")
                                
                                // Close dialog and refresh person data
                                showDeleteTransactionDialog = false
                                transactionToDelete = null
                                
                                // Refresh person data
                                val latestPerson = ledgerDatabaseManager.getLedgerPersonById(person.id)
                                if (latestPerson != null) {
                                    updatedPerson = latestPerson
                                }
                            } catch (e: Exception) {
                                println("DEBUG: Error deleting ledger transaction: ${e.message}")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Delete",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteTransactionDialog = false
                        transactionToDelete = null
                    },
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        )
    }
}
