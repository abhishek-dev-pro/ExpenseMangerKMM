package com.example.androidkmm.database

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.androidkmm.models.Transaction
import com.example.androidkmm.models.TransactionType
// import com.example.androidkmm.utils.formatDouble // Not needed for String.format

@Composable
fun rememberSQLiteTransactionDatabase(): SQLiteTransactionDatabase {
    val driverFactory = rememberDatabaseDriverFactory()
    val database = remember { CategoryDatabase(driverFactory.createDriver()) }
    val scope = rememberCoroutineScope()
    
    return remember {
        SQLiteTransactionDatabase(database, scope)
    }
}

class SQLiteTransactionDatabase(
    private val database: CategoryDatabase,
    private val scope: kotlinx.coroutines.CoroutineScope
) {
    
    init {
        // Initialize database
        scope.launch {
            try {
                // Add a small delay to ensure database is ready
                kotlinx.coroutines.delay(100)
                val count = database.categoryDatabaseQueries.getTransactionCount().executeAsOne()
                println("DEBUG: Database loaded with $count transactions")
            } catch (e: Exception) {
                println("DEBUG: Transaction table not found or error accessing it: ${e.message}")
                e.printStackTrace()
            }
        }
    }
    
    fun getAllTransactions(): Flow<List<Transaction>> {
        return database.categoryDatabaseQueries.selectAllTransactions().asFlow().mapToList(Dispatchers.Default).map { list ->
            try {
                list.mapNotNull { dbTransaction ->
                    try {
                        dbTransaction.toTransaction()
                    } catch (e: Exception) {
                        println("Error converting transaction ${dbTransaction.id}: ${e.message}")
                        null // Skip invalid transactions
                    }
                }
            } catch (e: Exception) {
                println("Error processing transaction list: ${e.message}")
                e.printStackTrace()
                emptyList()
            }
        }
    }
    
    // Function to fix existing transfer transactions that might not have proper transferTo field
    fun fixTransferTransactions() {
        scope.launch {
            try {
                val allTransactions = database.categoryDatabaseQueries.selectAllTransactions().executeAsList()
                allTransactions.forEach { transactionRow ->
                    if (transactionRow.type == "TRANSFER" && (transactionRow.transfer_to.isNullOrEmpty())) {
                        // Try to infer the transfer destination from the title or description
                        val title = transactionRow.title.lowercase()
                        val description = transactionRow.description?.lowercase() ?: ""
                        
                        // Common patterns for transfer descriptions
                        val transferTo = when {
                            title.contains("to savings") || description.contains("to savings") -> "Savings"
                            title.contains("to cash") || description.contains("to cash") -> "Cash"
                            title.contains("to checking") || description.contains("to checking") -> "Checking"
                            title.contains("to credit") || description.contains("to credit") -> "Credit Card"
                            title.contains("from savings") || description.contains("from savings") -> "Savings"
                            title.contains("from cash") || description.contains("from cash") -> "Cash"
                            title.contains("from checking") || description.contains("from checking") -> "Checking"
                            title.contains("from credit") || description.contains("from credit") -> "Credit Card"
                            else -> "Unknown Account"
                        }
                        
                        // Update the transaction with the inferred transfer destination
                        database.categoryDatabaseQueries.updateTransaction(
                            id = transactionRow.id,
                            title = transactionRow.title,
                            amount = transactionRow.amount,
                            category_name = transactionRow.category_name,
                            category_icon_name = transactionRow.category_icon_name,
                            category_color_hex = transactionRow.category_color_hex,
                            account_name = transactionRow.account_name,
                            account_icon_name = transactionRow.account_icon_name,
                            account_color_hex = transactionRow.account_color_hex,
                            transfer_to = transferTo,
                            time = transactionRow.time,
                            type = transactionRow.type,
                            description = transactionRow.description ?: "",
                            date = transactionRow.date
                        )
                        
                        println("DEBUG: Fixed transfer transaction ${transactionRow.id} with transferTo: $transferTo")
                    }
                }
            } catch (e: Exception) {
                println("Error fixing transfer transactions: ${e.message}")
            }
        }
    }
    
    fun getTransactionById(id: String): Flow<Transaction?> {
        return database.categoryDatabaseQueries.selectTransactionById(id).asFlow().mapToList(Dispatchers.Default).map { list ->
            list.firstOrNull()?.toTransaction()
        }
    }
    
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> {
        return database.categoryDatabaseQueries.selectTransactionsByType(type.name).asFlow().mapToList(Dispatchers.Default).map { list ->
            list.map { it.toTransaction() }
        }
    }
    
    fun getTransactionsByDateRange(startDate: String, endDate: String): Flow<List<Transaction>> {
        return database.categoryDatabaseQueries.selectTransactionsByDateRange(startDate, endDate).asFlow().mapToList(Dispatchers.Default).map { list ->
            list.map { it.toTransaction() }
        }
    }
    
    fun getTransactionsByCategory(categoryName: String): Flow<List<Transaction>> {
        return database.categoryDatabaseQueries.selectTransactionsByCategory(categoryName).asFlow().mapToList(Dispatchers.Default).map { list ->
            list.map { it.toTransaction() }
        }
    }
    
    fun getTransactionsByAccount(accountName: String): Flow<List<Transaction>> {
        return database.categoryDatabaseQueries.selectTransactionsByAccount(accountName).asFlow().mapToList(Dispatchers.Default).map { list ->
            list.map { it.toTransaction() }
        }
    }
    
    fun addTransaction(transaction: Transaction, onSuccess: () -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        println("DEBUG: SQLiteTransactionDatabase.addTransaction called with: ${transaction.title}")
        scope.launch {
            try {
                database.categoryDatabaseQueries.insertTransaction(
                    id = transaction.id,
                    title = transaction.title,
                    amount = transaction.amount,
                    category_name = transaction.category,
                    category_icon_name = getIconName(transaction.categoryIcon),
                    category_color_hex = transaction.categoryColor.toHexString(),
                    account_name = transaction.account,
                    account_icon_name = getIconName(transaction.accountIcon),
                    account_color_hex = transaction.accountColor.toHexString(),
                    transfer_to = transaction.transferTo ?: "",
                    time = transaction.time,
                    type = transaction.type.name,
                    description = transaction.description,
                    date = transaction.date,
                    is_ledger_transaction = 0, // Default to 0 for regular transactions
                    ledger_person_id = "", // Empty for regular transactions
                    ledger_person_name = "" // Empty for regular transactions
                )
                println("DEBUG: Transaction inserted successfully into SQLite database")
                onSuccess()
            } catch (e: Exception) {
                println("DEBUG: Error inserting transaction: ${e.message}")
                onError(e)
            }
        }
    }
    
    fun addLedgerTransaction(transaction: Transaction, ledgerPersonId: String, ledgerPersonName: String, onSuccess: () -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        println("DEBUG: SQLiteTransactionDatabase.addLedgerTransaction called with: ${transaction.title}")
        scope.launch {
            try {
                database.categoryDatabaseQueries.insertTransaction(
                    id = transaction.id,
                    title = transaction.title,
                    amount = transaction.amount,
                    category_name = transaction.category,
                    category_icon_name = getIconName(transaction.categoryIcon),
                    category_color_hex = transaction.categoryColor.toHexString(),
                    account_name = transaction.account,
                    account_icon_name = getIconName(transaction.accountIcon),
                    account_color_hex = transaction.accountColor.toHexString(),
                    transfer_to = transaction.transferTo ?: "",
                    time = transaction.time,
                    type = transaction.type.name,
                    description = transaction.description,
                    date = transaction.date,
                    is_ledger_transaction = 1, // Mark as ledger transaction
                    ledger_person_id = ledgerPersonId,
                    ledger_person_name = ledgerPersonName
                )
                println("DEBUG: Ledger transaction inserted successfully into SQLite database")
                onSuccess()
            } catch (e: Exception) {
                println("DEBUG: Error inserting ledger transaction: ${e.message}")
                onError(e)
            }
        }
    }
    
    fun updateTransaction(transaction: Transaction, onSuccess: () -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        scope.launch {
            try {
                database.categoryDatabaseQueries.updateTransaction(
                    title = transaction.title,
                    amount = transaction.amount,
                    category_name = transaction.category,
                    category_icon_name = getIconName(transaction.categoryIcon),
                    category_color_hex = transaction.categoryColor.toHexString(),
                    account_name = transaction.account,
                    account_icon_name = getIconName(transaction.accountIcon),
                    account_color_hex = transaction.accountColor.toHexString(),
                    transfer_to = transaction.transferTo ?: "",
                    time = transaction.time,
                    type = transaction.type.name,
                    description = transaction.description,
                    date = transaction.date,
                    id = transaction.id
                )
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
    
    fun deleteTransaction(transaction: Transaction, onSuccess: () -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        scope.launch {
            try {
                database.categoryDatabaseQueries.deleteTransaction(transaction.id)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
    
    fun addTransactionWithBalanceUpdate(
        transaction: Transaction, 
        accountDatabaseManager: com.example.androidkmm.database.SQLiteAccountDatabase,
        onSuccess: () -> Unit = {}, 
        onError: (Throwable) -> Unit = {}
    ) {
        scope.launch {
            try {
                // First, add the transaction
                database.categoryDatabaseQueries.insertTransaction(
                    id = transaction.id,
                    title = transaction.title,
                    amount = transaction.amount,
                    category_name = transaction.category,
                    category_icon_name = getIconName(transaction.categoryIcon),
                    category_color_hex = transaction.categoryColor.toHexString(),
                    account_name = transaction.account,
                    account_icon_name = getIconName(transaction.accountIcon),
                    account_color_hex = transaction.accountColor.toHexString(),
                    transfer_to = transaction.transferTo ?: "",
                    time = transaction.time,
                    type = transaction.type.name,
                    description = transaction.description,
                    date = transaction.date,
                    is_ledger_transaction = 0, // Default to 0 for regular transactions
                    ledger_person_id = "", // Empty for regular transactions
                    ledger_person_name = "" // Empty for regular transactions
                )
                
                // Then update account balances
                updateAccountBalancesForTransaction(transaction, accountDatabaseManager)
                
                println("DEBUG: Transaction added and balances updated successfully")
                onSuccess()
            } catch (e: Exception) {
                println("DEBUG: Error adding transaction with balance update: ${e.message}")
                onError(e)
            }
        }
    }
    
    fun updateTransactionWithBalanceUpdate(
        oldTransaction: Transaction,
        newTransaction: Transaction, 
        accountDatabaseManager: com.example.androidkmm.database.SQLiteAccountDatabase,
        onSuccess: () -> Unit = {}, 
        onError: (Throwable) -> Unit = {}
    ) {
        scope.launch {
            try {
                // First, reverse the old transaction's balance changes
                reverseAccountBalancesForTransaction(oldTransaction, accountDatabaseManager)
                
                // Then update the transaction
                database.categoryDatabaseQueries.updateTransaction(
                    title = newTransaction.title,
                    amount = newTransaction.amount,
                    category_name = newTransaction.category,
                    category_icon_name = getIconName(newTransaction.categoryIcon),
                    category_color_hex = newTransaction.categoryColor.toHexString(),
                    account_name = newTransaction.account,
                    account_icon_name = getIconName(newTransaction.accountIcon),
                    account_color_hex = newTransaction.accountColor.toHexString(),
                    transfer_to = newTransaction.transferTo ?: "",
                    time = newTransaction.time,
                    type = newTransaction.type.name,
                    description = newTransaction.description,
                    date = newTransaction.date,
                    id = newTransaction.id
                )
                
                // Then apply the new transaction's balance changes
                updateAccountBalancesForTransaction(newTransaction, accountDatabaseManager)
                
                println("DEBUG: Transaction updated and balances updated successfully")
                onSuccess()
            } catch (e: Exception) {
                println("DEBUG: Error updating transaction with balance update: ${e.message}")
                onError(e)
            }
        }
    }
    
    fun deleteTransactionWithBalanceUpdate(
        transaction: Transaction, 
        accountDatabaseManager: com.example.androidkmm.database.SQLiteAccountDatabase,
        onSuccess: () -> Unit = {}, 
        onError: (Throwable) -> Unit = {}
    ) {
        scope.launch {
            try {
                // First, reverse the balance changes
                reverseAccountBalancesForTransaction(transaction, accountDatabaseManager)
                
                // Then delete the transaction
                database.categoryDatabaseQueries.deleteTransaction(transaction.id)
                
                println("DEBUG: Transaction deleted and balances updated successfully")
                onSuccess()
            } catch (e: Exception) {
                println("DEBUG: Error deleting transaction with balance update: ${e.message}")
                onError(e)
            }
        }
    }
    
    private suspend fun updateAccountBalancesForTransaction(
        transaction: Transaction, 
        accountDatabaseManager: com.example.androidkmm.database.SQLiteAccountDatabase
    ) {
        when (transaction.type) {
            com.example.androidkmm.models.TransactionType.INCOME -> {
                // Add amount to account balance
                val account = getAccountByName(transaction.account)
                if (account != null) {
                    val currentBalance = account.balance.replace("₹", "").replace(",", "").toDoubleOrNull() ?: 0.0
                    val newBalance = currentBalance + transaction.amount
                    accountDatabaseManager.updateAccountBalance(account.id, newBalance)
                }
            }
            com.example.androidkmm.models.TransactionType.EXPENSE -> {
                // Subtract amount from account balance
                val account = getAccountByName(transaction.account)
                if (account != null) {
                    val currentBalance = account.balance.replace("₹", "").replace(",", "").toDoubleOrNull() ?: 0.0
                    val newBalance = currentBalance - transaction.amount
                    accountDatabaseManager.updateAccountBalance(account.id, newBalance)
                }
            }
            com.example.androidkmm.models.TransactionType.TRANSFER -> {
                // Subtract from source account, add to destination account
                val fromAccount = getAccountByName(transaction.account)
                val toAccount = transaction.transferTo?.let { getAccountByName(it) }
                
                if (fromAccount != null) {
                    val currentFromBalance = fromAccount.balance.replace("₹", "").replace(",", "").toDoubleOrNull() ?: 0.0
                    val newFromBalance = currentFromBalance - transaction.amount
                    accountDatabaseManager.updateAccountBalance(fromAccount.id, newFromBalance)
                }
                
                if (toAccount != null) {
                    val currentToBalance = toAccount.balance.replace("₹", "").replace(",", "").toDoubleOrNull() ?: 0.0
                    val newToBalance = currentToBalance + transaction.amount
                    accountDatabaseManager.updateAccountBalance(toAccount.id, newToBalance)
                }
            }
        }
    }
    
    private suspend fun reverseAccountBalancesForTransaction(
        transaction: Transaction, 
        accountDatabaseManager: com.example.androidkmm.database.SQLiteAccountDatabase
    ) {
        when (transaction.type) {
            com.example.androidkmm.models.TransactionType.INCOME -> {
                // Subtract amount from account balance (reverse income)
                val account = getAccountByName(transaction.account)
                if (account != null) {
                    val currentBalance = account.balance.replace("₹", "").replace(",", "").toDoubleOrNull() ?: 0.0
                    val newBalance = currentBalance - transaction.amount
                    accountDatabaseManager.updateAccountBalance(account.id, newBalance)
                }
            }
            com.example.androidkmm.models.TransactionType.EXPENSE -> {
                // Add amount to account balance (reverse expense)
                val account = getAccountByName(transaction.account)
                if (account != null) {
                    val currentBalance = account.balance.replace("₹", "").replace(",", "").toDoubleOrNull() ?: 0.0
                    val newBalance = currentBalance + transaction.amount
                    accountDatabaseManager.updateAccountBalance(account.id, newBalance)
                }
            }
            com.example.androidkmm.models.TransactionType.TRANSFER -> {
                // Add to source account, subtract from destination account (reverse transfer)
                val fromAccount = getAccountByName(transaction.account)
                val toAccount = transaction.transferTo?.let { getAccountByName(it) }
                
                if (fromAccount != null) {
                    val currentFromBalance = fromAccount.balance.replace("₹", "").replace(",", "").toDoubleOrNull() ?: 0.0
                    val newFromBalance = currentFromBalance + transaction.amount
                    accountDatabaseManager.updateAccountBalance(fromAccount.id, newFromBalance)
                }
                
                if (toAccount != null) {
                    val currentToBalance = toAccount.balance.replace("₹", "").replace(",", "").toDoubleOrNull() ?: 0.0
                    val newToBalance = currentToBalance - transaction.amount
                    accountDatabaseManager.updateAccountBalance(toAccount.id, newToBalance)
                }
            }
        }
    }
    
    private suspend fun getAccountByName(accountName: String): com.example.androidkmm.models.Account? {
        return try {
            val accountRow = database.categoryDatabaseQueries.selectAccountByName(accountName).executeAsOneOrNull()
            accountRow?.toAccount()
        } catch (e: Exception) {
            println("DEBUG: Error getting account by name: ${e.message}")
            null
        }
    }
    
    // Clear all data from all tables
    suspend fun clearAllData() {
        withContext(Dispatchers.Default) {
            database.transaction {
                // Delete all data from all tables
                database.categoryDatabaseQueries.deleteAllTransactions()
                database.categoryDatabaseQueries.deleteAllLedgerTransactions()
                database.categoryDatabaseQueries.deleteAllLedgerPersons()
                database.categoryDatabaseQueries.deleteAllGroupExpenseSplits()
                database.categoryDatabaseQueries.deleteAllGroupExpenses()
                database.categoryDatabaseQueries.deleteAllGroupMembers()
                database.categoryDatabaseQueries.deleteAllGroups()
                database.categoryDatabaseQueries.deleteAllCustomAccounts()
                database.categoryDatabaseQueries.deleteAllCustomCategories()
            }
        }
    }
}

// Icon name to ImageVector mapping for transactions
private val transactionIconMap = mapOf(
    "Restaurant" to Icons.Default.Restaurant,
    "AttachMoney" to Icons.Default.AttachMoney,
    "ShoppingCart" to Icons.Default.ShoppingCart,
    "SwapHoriz" to Icons.Default.SwapHoriz,
    "AccountBalance" to Icons.Default.AccountBalance,
    "CreditCard" to Icons.Default.CreditCard,
    "Wallet" to Icons.Default.Wallet,
    "Business" to Icons.Default.Business,
    "Flight" to Icons.Default.Flight,
    "Savings" to Icons.Default.Savings,
    "Group" to Icons.Default.Group,
    "CardGiftcard" to Icons.Default.CardGiftcard,
    "Work" to Icons.Default.Work,
    "TrendingUp" to Icons.Default.TrendingUp,
    "Home" to Icons.Default.Home,
    "School" to Icons.Default.School,
    "LocalHospital" to Icons.Default.LocalHospital,
    "Movie" to Icons.Default.Movie,
    "DirectionsCar" to Icons.Default.DirectionsCar,
    "Lightbulb" to Icons.Default.Lightbulb,
    "Stars" to Icons.Default.Stars,
    "Category" to Icons.Default.Category
)

// ImageVector to icon name mapping for transactions
private val transactionNameMap = transactionIconMap.entries.associate { (k, v) -> v to k }

private fun getIconByName(name: String): ImageVector {
    return transactionIconMap[name] ?: Icons.Default.Category
}

private fun getIconName(icon: ImageVector): String {
    return transactionNameMap[icon] ?: "Category"
}

// Helper function to convert Color to hex string
private fun Color.toHexString(): String {
    val alpha = (this.alpha * 255).toInt()
    val red = (this.red * 255).toInt()
    val green = (this.green * 255).toInt()
    val blue = (this.blue * 255).toInt()
    return "#${alpha.toString(16).padStart(2, '0')}${red.toString(16).padStart(2, '0')}${green.toString(16).padStart(2, '0')}${blue.toString(16).padStart(2, '0')}"
}

// Extension function to convert database row to Transaction
@OptIn(kotlin.time.ExperimentalTime::class)
private fun com.example.androidkmm.database.Transactions.toTransaction(): com.example.androidkmm.models.Transaction {
    return try {
        com.example.androidkmm.models.Transaction(
            id = this.id ?: "",
            title = this.title ?: "Unknown Transaction",
            amount = this.amount ?: 0.0,
            category = this.category_name ?: "Other",
            categoryIcon = try { getIconByName(this.category_icon_name ?: "Category") } catch (e: Exception) { Icons.Default.Category },
            categoryColor = try { parseColorHex(this.category_color_hex ?: "#FF607D8B") } catch (e: Exception) { Color(0xFF607D8B) },
            account = this.account_name ?: "Unknown Account",
            transferTo = this.transfer_to?.takeIf { it.isNotEmpty() },
            time = this.time ?: "00:00",
            type = try { TransactionType.valueOf(this.type ?: "EXPENSE") } catch (e: Exception) { TransactionType.EXPENSE },
            description = this.description ?: "",
            date = this.date ?: "2024-01-01",
            accountIcon = try { getIconByName(this.account_icon_name ?: "AccountBalance") } catch (e: Exception) { Icons.Default.AccountBalance },
            accountColor = try { parseColorHex(this.account_color_hex ?: "#FF2196F3") } catch (e: Exception) { Color(0xFF2196F3) }
        )
    } catch (e: Exception) {
        println("Error converting transaction: ${e.message}")
        e.printStackTrace()
        // Return a safe default transaction
        com.example.androidkmm.models.Transaction(
            id = this.id ?: "error_${kotlin.time.Clock.System.now().toEpochMilliseconds()}",
            title = "Error Loading Transaction",
            amount = 0.0,
            category = "Other",
            categoryIcon = Icons.Default.Error,
            categoryColor = Color(0xFF607D8B),
            account = "Unknown",
            transferTo = null,
            time = "00:00",
            type = TransactionType.EXPENSE,
            description = "Failed to load transaction data",
            date = "2024-01-01",
            accountIcon = Icons.Default.AccountBalance,
            accountColor = Color(0xFF2196F3)
        )
    }
}

// Extension function to convert database row to Account
private fun com.example.androidkmm.database.Account.toAccount(): com.example.androidkmm.models.Account {
    return try {
        com.example.androidkmm.models.Account(
            id = this.id ?: "unknown",
            name = this.name ?: "Unknown Account",
            balance = this.balance ?: "0.00",
            icon = try { getIconByName(this.icon_name ?: "AccountBalance") } catch (e: Exception) { Icons.Default.AccountBalance },
            color = try { parseColorHex(this.color_hex ?: "#FF2196F3") } catch (e: Exception) { Color(0xFF2196F3) },
            type = this.type ?: "Bank",
            isCustom = this.is_custom == 1L
        )
    } catch (e: Exception) {
        println("Error converting account: ${e.message}")
        e.printStackTrace()
        // Return a safe default account
        com.example.androidkmm.models.Account(
            id = this.id ?: "error_account",
            name = "Error Loading Account",
            balance = "0.00",
            icon = Icons.Default.Error,
            color = Color(0xFF607D8B),
            type = "Bank",
            isCustom = false
        )
    }
}
