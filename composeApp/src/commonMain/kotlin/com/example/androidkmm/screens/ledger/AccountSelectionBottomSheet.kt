package com.example.androidkmm.screens.ledger

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Account Selection Bottom Sheet - Exact replica from TransactionListScreen.kt
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
        containerColor = LedgerTheme.backgroundColor,
        contentColor = LedgerTheme.textPrimary,
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
                        color = LedgerTheme.textPrimary,
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
                            tint = LedgerTheme.textPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            item {
                // Subtitle
                Text(
                    text = subtitle,
                    color = LedgerTheme.textSecondary,
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
            containerColor = Color(0xFF1A1A1A)
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
                    .background(Color(0xFF1F1F1F)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$",
                    color = LedgerTheme.textPrimary,
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
                    color = LedgerTheme.textPrimary,
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
                        tint = LedgerTheme.greenAmount,
                        modifier = Modifier.size(16.dp)
                    )

                    Text(
                        text = account.balance,
                        color = LedgerTheme.greenAmount,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

private fun getSampleAccounts(): List<Account> {
    return listOf(
        Account(
            id = "1",
            name = "Personal Account",
            balance = "₹10,000",
            icon = Icons.Default.AccountBalance,
            color = Color(0xFF3B82F6),
            type = "Savings"
        ),
        Account(
            id = "2",
            name = "Business Account",
            balance = "₹50,000",
            icon = Icons.Default.Business,
            color = Color(0xFF4CAF50),
            type = "Current"
        ),
        Account(
            id = "3",
            name = "Travel Fund",
            balance = "₹5,000",
            icon = Icons.Default.Flight,
            color = Color(0xFFFF9800),
            type = "Savings"
        ),
        Account(
            id = "4",
            name = "Emergency Fund",
            balance = "₹25,000",
            icon = Icons.Default.Savings,
            color = Color(0xFF2196F3),
            type = "Savings"
        ),
        Account(
            id = "5",
            name = "Joint Account",
            balance = "₹15,000",
            icon = Icons.Default.Group,
            color = Color(0xFFE91E63),
            type = "Shared"
        )
    )
}
