package com.example.androidkmm.screens.ledger

import androidx.compose.runtime.Composable
import com.example.androidkmm.database.SQLiteAccountDatabase
import com.example.androidkmm.models.Account
import com.example.androidkmm.components.SharedAccountSelectionBottomSheet

// Re-export the existing AccountSelectionBottomSheet from TransactionListScreen
@Composable
fun AccountSelectionBottomSheet(
    onDismiss: () -> Unit,
    title: String = "Select Account",
    subtitle: String = "Choose an account for your transaction",
    onAccountSelected: (Account) -> Unit,
    accountDatabaseManager: SQLiteAccountDatabase,
    onAddAccount: (() -> Unit)? = null
) {
    SharedAccountSelectionBottomSheet(
        onDismiss = onDismiss,
        title = title,
        subtitle = subtitle,
        onAccountSelected = onAccountSelected,
        accountDatabaseManager = accountDatabaseManager,
        onAddAccount = onAddAccount
    )
}

