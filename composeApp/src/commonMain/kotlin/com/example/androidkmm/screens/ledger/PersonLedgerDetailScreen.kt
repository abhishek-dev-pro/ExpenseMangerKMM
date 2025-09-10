package com.example.androidkmm.screens.ledger

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidkmm.utils.formatDouble
import kotlin.math.abs

// PersonLedgerDetailScreen.kt
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonLedgerDetailScreen(
    person: LedgerPerson,
    onBack: () -> Unit,
    onAddTransaction: () -> Unit
) {
    var showSentBottomSheet by remember { mutableStateOf(false) }
    var showReceivedBottomSheet by remember { mutableStateOf(false) }
    val transactions = remember {
        listOf(
            LedgerTransaction(
                id = "1",
                amount = 10.00,
                description = "",
                date = "Sep 10, 2025",
                time = "01:38 PM",
                type = TransactionType.SENT
            ),
            LedgerTransaction(
                id = "2",
                amount = 40.00,
                description = "Emergency cash",
                date = "Sep 6, 2024",
                time = "05:30 AM",
                type = TransactionType.RECEIVED
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LedgerTheme.backgroundColor)
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
                    tint = LedgerTheme.textPrimary,
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
                    text = person.name.split(" ").map { it.first() }.joinToString(""),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    text = person.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LedgerTheme.textPrimary
                )
                Text(
                    text = "${transactions.size} transactions",
                    fontSize = 14.sp,
                    color = LedgerTheme.textSecondary
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = LedgerTheme.textPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Balance Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (person.balance < 0) Color(0xFF2A1919) else Color(0xFF0F2419)
            ),
            shape = RoundedCornerShape(20.dp)
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
                        imageVector = if (person.balance < 0) Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (person.balance < 0) LedgerTheme.redAmount else LedgerTheme.greenAmount,
                        modifier = Modifier.size(16.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Current Balance",
                        fontSize = 14.sp,
                        color = LedgerTheme.textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$${formatDouble(abs(person.balance))}",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (person.balance < 0) LedgerTheme.redAmount else LedgerTheme.greenAmount
                )

                Text(
                    text = if (person.balance < 0) "You owe ${person.name}" else "${person.name} owes you",
                    fontSize = 16.sp,
                    color = LedgerTheme.textSecondary
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
                    text = "You Sent",
                    color = LedgerTheme.greenAmount,
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
                    text = "You Received",
                    color = LedgerTheme.redAmount,
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
                color = LedgerTheme.textPrimary
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
                    text = "${transactions.size} entries",
                    fontSize = 12.sp,
                    color = LedgerTheme.textSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Transactions List
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            items(transactions) { transaction ->
                TransactionItem(transaction = transaction)
                if (transaction != transactions.last()) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
    
    // Show sent money bottom sheet
    if (showSentBottomSheet) {
        AddLedgerEntryBottomSheet(
            onDismiss = { showSentBottomSheet = false },
            person = person
        )
    }
    
    // Show received money bottom sheet
    if (showReceivedBottomSheet) {
        AddLedgerEntryBottomSheet(
            onDismiss = { showReceivedBottomSheet = false },
            person = person,
            transactionType = TransactionType.RECEIVED
        )
    }
}
