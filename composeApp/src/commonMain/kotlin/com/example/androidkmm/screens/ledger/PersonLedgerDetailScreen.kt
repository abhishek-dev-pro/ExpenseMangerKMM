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
import com.example.androidkmm.utils.formatDouble
import com.example.androidkmm.database.rememberSQLiteLedgerDatabase
import com.example.androidkmm.database.rememberSQLiteTransactionDatabase
import com.example.androidkmm.design.DesignSystem
import com.example.androidkmm.screens.ledger.TransactionType
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
    val coroutineScope = rememberCoroutineScope()
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
        val latestPerson = ledgerDatabaseManager.getLedgerPersonById(person.id)
        if (latestPerson != null) {
            updatedPerson = latestPerson
        }
    }
    
    var showSentBottomSheet by remember { mutableStateOf(false) }
    var showReceivedBottomSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LedgerTheme.backgroundColor())
            .statusBarsPadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Back",
                    tint = LedgerTheme.textPrimary(),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

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
                    text = "${updatedPerson.transactionCount} transactions",
                    fontSize = 14.sp,
                    color = LedgerTheme.textSecondary()
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = LedgerTheme.textPrimary(),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Balance Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(DesignSystem.CornerRadius.md))
                .border(
                    width = 0.5.dp, // very thin border
                    color = Color.White.copy(alpha = 0.2f), // subtle white
                    shape = RoundedCornerShape(DesignSystem.CornerRadius.md)
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (updatedPerson.balance < 0) Color(0xFF0F2419) else Color(0xFF2A1919)
            ),
            shape = RoundedCornerShape(DesignSystem.CornerRadius.md)
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
                    text = "$${formatDouble(abs(updatedPerson.balance))}",
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

        Spacer(modifier = Modifier.height(24.dp))

        // Transaction Type Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { showSentBottomSheet = true },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF2A1919)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = LedgerTheme.redAmount,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "You Sent",
                    color = LedgerTheme.redAmount,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Button(
                onClick = { showReceivedBottomSheet = true },
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0F2419)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowDownward,
                    contentDescription = null,
                    tint = LedgerTheme.greenAmount,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "You Received",
                    color = LedgerTheme.greenAmount,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Transaction History Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
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

        // Transactions List
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 24.dp)
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
                    onDelete = {
                        coroutineScope.launch {
                            // Delete the ledger transaction and update person balance
                            ledgerDatabaseManager.deleteLedgerTransactionAndUpdatePerson(
                                transactionId = transaction.id,
                                personId = person.id
                            )
                            
                            // Also delete the corresponding main transaction
                            val mainTransactionId = "main_${transaction.id}"
                            transactionDatabaseManager.deleteTransaction(
                                transaction = com.example.androidkmm.models.Transaction(
                                    id = mainTransactionId,
                                    title = "",
                                    amount = 0.0,
                                    category = "",
                                    categoryIcon = Icons.Default.Category,
                                    categoryColor = Color.Transparent,
                                    account = "",
                                    accountIcon = Icons.Default.Wallet,
                                    accountColor = Color.Transparent,
                                    time = "",
                                    type = com.example.androidkmm.models.TransactionType.EXPENSE,
                                    description = "",
                                    date = ""
                                )
                            )
                        }
                    }
                )
                if (transaction != transactions.last()) {
                    Spacer(modifier = Modifier.height(6.dp))
                }
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
}
