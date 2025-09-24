package com.example.androidkmm.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.example.androidkmm.database.SQLiteAccountDatabase
import com.example.androidkmm.design.AppStyleDesignSystem
import com.example.androidkmm.models.Account

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SharedAccountSelectionBottomSheet(
    onDismiss: () -> Unit,
    title: String = "Select Account",
    subtitle: String = "Choose an account for your transaction",
    onAccountSelected: (Account) -> Unit,
    accountDatabaseManager: SQLiteAccountDatabase,
    onAddAccount: (() -> Unit)? = null
) {
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val accountsState = accountDatabaseManager.getActiveAccounts().collectAsState(initial = emptyList<Account>())
    val accounts = accountsState.value

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = bottomSheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onBackground,
        dragHandle = null
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding(),
            contentPadding = PaddingValues(0.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
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
                    Spacer(modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_XL))

                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_XL)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_LARGE)
                        )
                    }
                }
            }

            item {
                // Subtitle
                Text(
                    text = subtitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 24.dp),
                    textAlign = TextAlign.Center
                )
            }

            items(accounts) { account ->
                SharedAccountCard(
                    account = account,
                    onClick = { onAccountSelected(account) }
                )
            }

            item {
                // Add Account Button
                Card(
                    onClick = { onAddAccount?.invoke() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM))
                        .border(
                            width = AppStyleDesignSystem.Sizes.BORDER_NORMAL,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
                        ),
                    shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_MEDIUM)
                    ) {
                        // Plus icon
                        Box(
                            modifier = Modifier
                                .size(AppStyleDesignSystem.Sizes.AVATAR_LARGE)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Account",
                                tint = Color.White,
                                modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_LARGE)
                            )
                        }

                        // Add Account text
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_TINY)
                        ) {
                            Text(
                                text = "Add Account",
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Create a new account",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun SharedAccountCard(
    account: Account,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp)
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM))
            .border(
                width = 0.5.dp, // very thin border
                color = Color.White.copy(alpha = 0.2f), // subtle white
                shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM)
            ),
        shape = RoundedCornerShape(AppStyleDesignSystem.CornerRadius.MEDIUM),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Account type icon
            Box(
                modifier = Modifier
                    .size(AppStyleDesignSystem.Sizes.AVATAR_LARGE)
                    .clip(CircleShape)
                    .background(getAccountTypeColor(account.type)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getAccountTypeIcon(account.type),
                    contentDescription = account.type,
                    tint = Color.White,
                    modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_LARGE)
                )
            }

            // Account details
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_TINY)
            ) {
                Text(
                    text = account.name,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // Balance on the right
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppStyleDesignSystem.Padding.ARRANGEMENT_TINY)
            ) {
                // Green arrow up icon
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = Color(0xFF00A63E),
                    modifier = Modifier.size(AppStyleDesignSystem.Sizes.ICON_SIZE_SMALL)
                )

                Text(
                    text = account.balance,
                    color = Color(0xFF00A63E),
                    style = AppStyleDesignSystem.Typography.BODY,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// Helper functions for account type icons and colors
private fun getAccountTypeIcon(accountType: String): androidx.compose.ui.graphics.vector.ImageVector {
    return when (accountType.lowercase()) {
        "bank account" -> Icons.Default.AccountBalance
        "credit/debit card" -> Icons.Default.CreditCard
        "cash" -> Icons.Default.Money
        "digital wallet" -> Icons.Default.Wallet
        else -> Icons.Default.AccountBalance
    }
}

private fun getAccountTypeColor(accountType: String): Color {
    return when (accountType.lowercase()) {
        "bank account" -> Color(0xFF2196F3)
        "credit/debit card" -> Color(0xFFFF9800)
        "cash" -> Color(0xFF4CAF50)
        "digital wallet" -> Color(0xFF9C27B0)
        else -> Color(0xFF2196F3)
    }
}
