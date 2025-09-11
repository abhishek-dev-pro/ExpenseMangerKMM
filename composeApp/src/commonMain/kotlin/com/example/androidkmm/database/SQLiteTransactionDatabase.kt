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
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.androidkmm.models.Transaction
import com.example.androidkmm.models.TransactionType

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
                val count = database.categoryDatabaseQueries.getTransactionCount().executeAsOne()
                println("DEBUG: Database loaded with $count transactions")
            } catch (e: Exception) {
                println("DEBUG: Transaction table not found, will be created on next app restart")
            }
        }
    }
    
    fun getAllTransactions(): Flow<List<Transaction>> {
        return database.categoryDatabaseQueries.selectAllTransactions().asFlow().mapToList(Dispatchers.IO).map { list ->
            list.map { it.toTransaction() }
        }
    }
    
    fun getTransactionById(id: String): Flow<Transaction?> {
        return database.categoryDatabaseQueries.selectTransactionById(id).asFlow().mapToList(Dispatchers.IO).map { list ->
            list.firstOrNull()?.toTransaction()
        }
    }
    
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> {
        return database.categoryDatabaseQueries.selectTransactionsByType(type.name).asFlow().mapToList(Dispatchers.IO).map { list ->
            list.map { it.toTransaction() }
        }
    }
    
    fun getTransactionsByDateRange(startDate: String, endDate: String): Flow<List<Transaction>> {
        return database.categoryDatabaseQueries.selectTransactionsByDateRange(startDate, endDate).asFlow().mapToList(Dispatchers.IO).map { list ->
            list.map { it.toTransaction() }
        }
    }
    
    fun getTransactionsByCategory(categoryName: String): Flow<List<Transaction>> {
        return database.categoryDatabaseQueries.selectTransactionsByCategory(categoryName).asFlow().mapToList(Dispatchers.IO).map { list ->
            list.map { it.toTransaction() }
        }
    }
    
    fun getTransactionsByAccount(accountName: String): Flow<List<Transaction>> {
        return database.categoryDatabaseQueries.selectTransactionsByAccount(accountName).asFlow().mapToList(Dispatchers.IO).map { list ->
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
                    date = transaction.date
                )
                println("DEBUG: Transaction inserted successfully into SQLite database")
                onSuccess()
            } catch (e: Exception) {
                println("DEBUG: Error inserting transaction: ${e.message}")
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
                    date = transaction.date
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
    return String.format("#%02X%02X%02X%02X", alpha, red, green, blue)
}

// Extension function to convert database row to Transaction
private fun com.example.androidkmm.database.Transactions.toTransaction(): com.example.androidkmm.models.Transaction {
    return com.example.androidkmm.models.Transaction(
        id = this.id,
        title = this.title,
        amount = this.amount,
        category = this.category_name,
        categoryIcon = getIconByName(this.category_icon_name),
        categoryColor = parseColorHex(this.category_color_hex),
        account = this.account_name,
        transferTo = this.transfer_to?.takeIf { it.isNotEmpty() },
        time = this.time,
        type = TransactionType.valueOf(this.type),
        description = this.description ?: "",
        date = this.date,
        accountIcon = getIconByName(this.account_icon_name),
        accountColor = parseColorHex(this.account_color_hex)
    )
}

// Extension function to convert database row to Account
private fun com.example.androidkmm.database.Account.toAccount(): com.example.androidkmm.models.Account {
    return com.example.androidkmm.models.Account(
        id = this.id,
        name = this.name,
        balance = this.balance,
        icon = getIconByName(this.icon_name),
        color = parseColorHex(this.color_hex),
        type = this.type,
        isCustom = this.is_custom == 1L
    )
}
