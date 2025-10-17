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
import com.example.androidkmm.utils.CurrencyUtils.removeCurrencySymbols
import com.example.androidkmm.utils.CurrencyUtils.getCurrencySymbol
import com.example.androidkmm.utils.Logger
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

/**
 * SQLite Transaction Database Manager
 * 
 * Handles all transaction-related database operations including:
 * - Adding transactions with automatic balance updates
 * - Updating transactions with rollback capability
 * - Deleting transactions with balance restoration
 * - Transfer operations with atomic balance management
 * - Comprehensive error handling and recovery
 * 
 * @param database The SQLite database instance
 * @param scope Coroutine scope for async operations
 */
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
                // Use parallel processing for large datasets
                if (list.size > 100) {
                    list.mapNotNull { dbTransaction ->
                        try {
                            dbTransaction.toTransaction()
                        } catch (e: Exception) {
                            println("ERROR: Failed to convert transaction ${dbTransaction.id}: ${e.message}")
                            Logger.error("Transaction conversion failed", "SQLiteTransactionDatabase", e)
                            null
                        }
                    }
                } else {
                    list.mapNotNull { dbTransaction ->
                        try {
                            dbTransaction.toTransaction()
                        } catch (e: Exception) {
                            println("ERROR: Failed to convert transaction ${dbTransaction.id}: ${e.message}")
                            Logger.error("Transaction conversion failed", "SQLiteTransactionDatabase", e)
                            null
                        }
                    }
                }
            } catch (e: Exception) {
                println("ERROR: Failed to process transaction list: ${e.message}")
                Logger.error("Transaction list processing failed", "SQLiteTransactionDatabase", e)
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
    
    fun updateAccountBalancesForLedgerTransaction(
        transaction: Transaction,
        accountDatabaseManager: com.example.androidkmm.database.SQLiteAccountDatabase
    ) {
        // Update account balances based on transaction type
        when (transaction.type) {
            com.example.androidkmm.models.TransactionType.INCOME -> {
                // Add amount to account balance (money received)
                val account = getAccountByName(transaction.account)
                if (account != null) {
                    val currentBalance = removeCurrencySymbols(account.balance).toDoubleOrNull() ?: 0.0
                    val newBalance = currentBalance + transaction.amount
                    accountDatabaseManager.updateAccountBalance(account.id, newBalance)
                    println("DEBUG: Updated account ${account.name} balance from $currentBalance to $newBalance (INCOME: +${transaction.amount})")
                }
            }
            com.example.androidkmm.models.TransactionType.EXPENSE -> {
                // Subtract amount from account balance (money sent)
                val account = getAccountByName(transaction.account)
                if (account != null) {
                    val currentBalance = removeCurrencySymbols(account.balance).toDoubleOrNull() ?: 0.0
                    val newBalance = currentBalance - transaction.amount
                    accountDatabaseManager.updateAccountBalance(account.id, newBalance)
                    println("DEBUG: Updated account ${account.name} balance from $currentBalance to $newBalance (EXPENSE: -${transaction.amount})")
                }
            }
            com.example.androidkmm.models.TransactionType.TRANSFER -> {
                // Subtract from source account, add to destination account
                val fromAccount = getAccountByName(transaction.account)
                val toAccount = transaction.transferTo?.let { getAccountByName(it) }
                
                if (fromAccount != null) {
                    val currentFromBalance = removeCurrencySymbols(fromAccount.balance).toDoubleOrNull() ?: 0.0
                    val newFromBalance = currentFromBalance - transaction.amount
                    accountDatabaseManager.updateAccountBalance(fromAccount.id, newFromBalance)
                    println("DEBUG: Updated from account ${fromAccount.name} balance from $currentFromBalance to $newFromBalance (TRANSFER: -${transaction.amount})")
                }
                
                if (toAccount != null) {
                    val currentToBalance = removeCurrencySymbols(toAccount.balance).toDoubleOrNull() ?: 0.0
                    val newToBalance = currentToBalance + transaction.amount
                    accountDatabaseManager.updateAccountBalance(toAccount.id, newToBalance)
                    println("DEBUG: Updated to account ${toAccount.name} balance from $currentToBalance to $newToBalance (TRANSFER: +${transaction.amount})")
                }
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
    
    // Batch operations for better performance
    fun addTransactionsBatch(
        transactions: List<Transaction>,
        accountDatabaseManager: com.example.androidkmm.database.SQLiteAccountDatabase,
        onSuccess: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        try {
            database.transaction {
                transactions.forEach { transaction ->
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
                        is_ledger_transaction = 0,
                        ledger_person_id = "",
                        ledger_person_name = ""
                    )
                }
            }
            
            // Update account balances outside of transaction
            transactions.forEach { transaction ->
                updateAccountBalancesForTransaction(transaction, accountDatabaseManager)
            }
            onSuccess()
        } catch (e: Exception) {
            onError(e)
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
                println("DEBUG: addTransactionWithBalanceUpdate called for transaction: ${transaction.title}")
                println("DEBUG: Transaction type: ${transaction.type}, Amount: ${transaction.amount}")
                println("DEBUG: From account: ${transaction.account}, To account: ${transaction.transferTo}")
                
                // Validate transaction data before processing
                if (transaction.id.isBlank()) {
                    throw IllegalArgumentException("Transaction ID cannot be blank")
                }
                if (transaction.amount <= 0) {
                    throw IllegalArgumentException("Transaction amount must be positive")
                }
                if (transaction.account.isBlank()) {
                    throw IllegalArgumentException("Transaction account cannot be blank")
                }
                
                // First, add the transaction
                println("DEBUG: Inserting transaction into database...")
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
                println("DEBUG: Transaction inserted successfully")
                
                // Then update account balances
                println("DEBUG: Updating account balances...")
                updateAccountBalancesForTransaction(transaction, accountDatabaseManager)
                println("DEBUG: Account balances updated successfully")
                
                println("DEBUG: Transaction added and balances updated successfully")
                Logger.info("Transaction added successfully: ${transaction.id}", "SQLiteTransactionDatabase")
                onSuccess()
            } catch (error: Exception) {
                println("DEBUG: Exception caught in addTransactionWithBalanceUpdate: ${error.message}")
                println("DEBUG: Exception type: ${error::class.simpleName}")
                error.printStackTrace()
                
                // Rollback the transaction if balance update fails
                try {
                    println("DEBUG: Attempting to rollback transaction...")
                    database.categoryDatabaseQueries.deleteTransaction(transaction.id)
                    println("DEBUG: Transaction rollback successful")
                    Logger.info("Transaction rollback successful: ${transaction.id}", "SQLiteTransactionDatabase")
                } catch (rollbackError: Exception) {
                    println("ERROR: Failed to rollback transaction: ${rollbackError.message}")
                    Logger.error("Failed to rollback transaction", "SQLiteTransactionDatabase", rollbackError)
                }
                
                println("ERROR: Failed to add transaction with balance update: ${error.message}")
                Logger.error("Failed to add transaction with balance update", "SQLiteTransactionDatabase", error)
                println("DEBUG: Calling onError callback...")
                onError(error)
                println("DEBUG: onError callback called")
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
                println("DEBUG: === TRANSACTION EDIT DEBUG (FIXED APPROACH) ===")
                println("DEBUG: Old transaction - ID: ${oldTransaction.id}, Amount: ${oldTransaction.amount}, Type: ${oldTransaction.type}, Account: ${oldTransaction.account}")
                println("DEBUG: New transaction - ID: ${newTransaction.id}, Amount: ${newTransaction.amount}, Type: ${newTransaction.type}, Account: ${newTransaction.account}")
                
                // Step 1: Update the transaction in database first
                println("DEBUG: Step 1 - Updating transaction in database...")
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
                
                // Step 2: For EXPENSE transactions, use simplified logic
                // Add back old amount, then subtract new amount
                println("DEBUG: Step 2 - Updating account balance using simplified logic...")
                
                if (oldTransaction.account == newTransaction.account) {
                    // Same account - handle all type combinations
                    val account = getAccountByName(newTransaction.account)
                    if (account != null) {
                        val currentBalance = removeCurrencySymbols(account.balance).toDoubleOrNull() ?: 0.0
                        println("DEBUG: Current balance: $currentBalance")
                        println("DEBUG: Old transaction: ${oldTransaction.type} - ${oldTransaction.amount}")
                        println("DEBUG: New transaction: ${newTransaction.type} - ${newTransaction.amount}")
                        
                        val newBalance = when {
                            // INCOME -> INCOME: subtract old, add new
                            oldTransaction.type == com.example.androidkmm.models.TransactionType.INCOME && 
                            newTransaction.type == com.example.androidkmm.models.TransactionType.INCOME -> {
                                currentBalance - oldTransaction.amount + newTransaction.amount
                            }
                            // INCOME -> EXPENSE: subtract old, subtract new
                            oldTransaction.type == com.example.androidkmm.models.TransactionType.INCOME && 
                            newTransaction.type == com.example.androidkmm.models.TransactionType.EXPENSE -> {
                                currentBalance - oldTransaction.amount - newTransaction.amount
                            }
                            // EXPENSE -> INCOME: add back old, add new
                            oldTransaction.type == com.example.androidkmm.models.TransactionType.EXPENSE && 
                            newTransaction.type == com.example.androidkmm.models.TransactionType.INCOME -> {
                                currentBalance + oldTransaction.amount + newTransaction.amount
                            }
                            // EXPENSE -> EXPENSE: add back old, subtract new
                            oldTransaction.type == com.example.androidkmm.models.TransactionType.EXPENSE && 
                            newTransaction.type == com.example.androidkmm.models.TransactionType.EXPENSE -> {
                                currentBalance + oldTransaction.amount - newTransaction.amount
                            }
                            // TRANSFER -> anything: use undo/apply logic
                            oldTransaction.type == com.example.androidkmm.models.TransactionType.TRANSFER || 
                            newTransaction.type == com.example.androidkmm.models.TransactionType.TRANSFER -> {
                                // Handle transfer separately - this will be handled in the else block
                                currentBalance
                            }
                            else -> currentBalance
                        }
                        
                        if (oldTransaction.type == com.example.androidkmm.models.TransactionType.TRANSFER || 
                            newTransaction.type == com.example.androidkmm.models.TransactionType.TRANSFER) {
                            // Use undo/apply logic for transfers
                            undoTransaction(oldTransaction, accountDatabaseManager)
                            applyTransaction(newTransaction, accountDatabaseManager)
                        } else {
                            // Update balance with calculated value
                            accountDatabaseManager.updateAccountBalance(account.id, newBalance)
                            println("DEBUG: Updated ${account.name}: $currentBalance -> $newBalance")
                        }
                    }
                } else {
                    // Different account - use undo/apply logic
                    undoTransaction(oldTransaction, accountDatabaseManager)
                    applyTransaction(newTransaction, accountDatabaseManager)
                }
                
                println("DEBUG: Transaction updated and balances recalculated successfully")
                println("DEBUG: === END TRANSACTION EDIT DEBUG ===")
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
    
    private fun updateAccountBalancesForTransaction(
        transaction: Transaction,
        accountDatabaseManager: com.example.androidkmm.database.SQLiteAccountDatabase
    ) {
        try {
            println("DEBUG: updateAccountBalancesForTransaction called for transaction type: ${transaction.type}")
            
            when (transaction.type) {
                com.example.androidkmm.models.TransactionType.INCOME -> {
                    println("DEBUG: Processing INCOME transaction")
                    // Add amount to account balance
                    val account = getAccountByName(transaction.account)
                    if (account != null) {
                        val currentBalance = removeCurrencySymbols(account.balance).toDoubleOrNull() ?: 0.0
                        val newBalance = currentBalance + transaction.amount
                        accountDatabaseManager.updateAccountBalance(account.id, newBalance)
                        println("DEBUG: Updated account ${account.name} balance from $currentBalance to $newBalance (INCOME: +${transaction.amount})")
                    } else {
                        throw IllegalStateException("Account '${transaction.account}' not found for INCOME transaction")
                    }
                }
                com.example.androidkmm.models.TransactionType.EXPENSE -> {
                    println("DEBUG: Processing EXPENSE transaction")
                    // Subtract amount from account balance
                    val account = getAccountByName(transaction.account)
                    if (account != null) {
                        val currentBalance = removeCurrencySymbols(account.balance).toDoubleOrNull() ?: 0.0
                        val newBalance = currentBalance - transaction.amount
                        accountDatabaseManager.updateAccountBalance(account.id, newBalance)
                        println("DEBUG: Updated account ${account.name} balance from $currentBalance to $newBalance (EXPENSE: -${transaction.amount})")
                    } else {
                        throw IllegalStateException("Account '${transaction.account}' not found for EXPENSE transaction")
                    }
                }
                com.example.androidkmm.models.TransactionType.TRANSFER -> {
                    println("DEBUG: Processing TRANSFER transaction")
                    println("DEBUG: From account: ${transaction.account}, To account: ${transaction.transferTo}")
                    
                    // Validate transfer requirements
                    if (transaction.transferTo.isNullOrBlank()) {
                        throw IllegalStateException("Transfer destination account is required for TRANSFER transaction")
                    }
                    
                    val fromAccount = getAccountByName(transaction.account)
                    val toAccount = getAccountByName(transaction.transferTo!!)
                    
                    println("DEBUG: From account found: ${fromAccount != null}, To account found: ${toAccount != null}")
                    
                    if (fromAccount == null) {
                        throw IllegalStateException("Source account '${transaction.account}' not found for TRANSFER transaction")
                    }
                    if (toAccount == null) {
                        throw IllegalStateException("Destination account '${transaction.transferTo}' not found for TRANSFER transaction")
                    }
                    
                    // Prevent self-transfer
                    if (fromAccount.id == toAccount.id) {
                        throw IllegalStateException("Cannot transfer to the same account")
                    }
                    
                    // Validate amount
                    if (transaction.amount <= 0) {
                        throw IllegalStateException("Transfer amount must be positive")
                    }
                    
                    // Check if source account has sufficient balance
                    val currentFromBalance = removeCurrencySymbols(fromAccount.balance).toDoubleOrNull() ?: 0.0
                    println("DEBUG: Current from account balance: $currentFromBalance, Required amount: ${transaction.amount}")
                    
                    if (currentFromBalance < transaction.amount) {
                        println("DEBUG: Insufficient balance detected - throwing exception")
                        throw IllegalStateException("Insufficient balance in source account '${fromAccount.name}'. Available: $currentFromBalance, Required: ${transaction.amount}")
                    }
                    
                    // Perform the transfer atomically
                    try {
                        println("DEBUG: Performing transfer - updating from account balance")
                        val newFromBalance = currentFromBalance - transaction.amount
                        accountDatabaseManager.updateAccountBalance(fromAccount.id, newFromBalance)
                        println("DEBUG: Updated from account ${fromAccount.name} balance from $currentFromBalance to $newFromBalance (TRANSFER: -${transaction.amount})")
                        
                        println("DEBUG: Performing transfer - updating to account balance")
                        val currentToBalance = removeCurrencySymbols(toAccount.balance).toDoubleOrNull() ?: 0.0
                        val newToBalance = currentToBalance + transaction.amount
                        accountDatabaseManager.updateAccountBalance(toAccount.id, newToBalance)
                        println("DEBUG: Updated to account ${toAccount.name} balance from $currentToBalance to $newToBalance (TRANSFER: +${transaction.amount})")
                    } catch (e: Exception) {
                        println("DEBUG: Error during transfer - attempting rollback")
                        // If the second update fails, try to rollback the first update
                        try {
                            accountDatabaseManager.updateAccountBalance(fromAccount.id, currentFromBalance)
                            println("DEBUG: Rolled back from account balance due to error")
                        } catch (rollbackError: Exception) {
                            println("ERROR: Failed to rollback from account balance: ${rollbackError.message}")
                            Logger.error("Failed to rollback from account balance", "SQLiteTransactionDatabase", rollbackError)
                        }
                        throw e
                    }
                }
            }
            println("DEBUG: updateAccountBalancesForTransaction completed successfully")
        } catch (e: Exception) {
            println("ERROR: Failed to update account balances for transaction ${transaction.id}: ${e.message}")
            println("ERROR: Exception type: ${e::class.simpleName}")
            e.printStackTrace()
            Logger.error("Failed to update account balances", "SQLiteTransactionDatabase", e)
            throw e
        }
    }
    
    private suspend fun reverseAccountBalancesForTransaction(
        transaction: Transaction, 
        accountDatabaseManager: com.example.androidkmm.database.SQLiteAccountDatabase
    ) {
        println("DEBUG: Reversing transaction - ID: ${transaction.id}, Amount: ${transaction.amount}, Type: ${transaction.type}, Account: ${transaction.account}")
        when (transaction.type) {
            com.example.androidkmm.models.TransactionType.INCOME -> {
                // Subtract amount from account balance (reverse income)
                val account = getAccountByName(transaction.account)
                if (account != null) {
                    val currentBalance = removeCurrencySymbols(account.balance).toDoubleOrNull() ?: 0.0
                    val newBalance = currentBalance - transaction.amount
                    accountDatabaseManager.updateAccountBalance(account.id, newBalance)
                    println("DEBUG: REVERSE INCOME - Account: ${account.name}, Balance: $currentBalance -> $newBalance (reversed +${transaction.amount})")
                }
            }
            com.example.androidkmm.models.TransactionType.EXPENSE -> {
                // Add amount to account balance (reverse expense)
                val account = getAccountByName(transaction.account)
                if (account != null) {
                    val currentBalance = removeCurrencySymbols(account.balance).toDoubleOrNull() ?: 0.0
                    val newBalance = currentBalance + transaction.amount
                    accountDatabaseManager.updateAccountBalance(account.id, newBalance)
                    println("DEBUG: REVERSE EXPENSE - Account: ${account.name}, Balance: $currentBalance -> $newBalance (reversed -${transaction.amount})")
                }
            }
            com.example.androidkmm.models.TransactionType.TRANSFER -> {
                // Add to source account, subtract from destination account (reverse transfer)
                val fromAccount = getAccountByName(transaction.account)
                val toAccount = transaction.transferTo?.let { getAccountByName(it) }
                
                if (fromAccount != null) {
                    val currentFromBalance = removeCurrencySymbols(fromAccount.balance).toDoubleOrNull() ?: 0.0
                    val newFromBalance = currentFromBalance + transaction.amount
                    accountDatabaseManager.updateAccountBalance(fromAccount.id, newFromBalance)
                    println("DEBUG: REVERSE TRANSFER FROM - Account: ${fromAccount.name}, Balance: $currentFromBalance -> $newFromBalance (reversed -${transaction.amount})")
                }
                
                if (toAccount != null) {
                    val currentToBalance = removeCurrencySymbols(toAccount.balance).toDoubleOrNull() ?: 0.0
                    val newToBalance = currentToBalance - transaction.amount
                    accountDatabaseManager.updateAccountBalance(toAccount.id, newToBalance)
                    println("DEBUG: REVERSE TRANSFER TO - Account: ${toAccount.name}, Balance: $currentToBalance -> $newToBalance (reversed +${transaction.amount})")
                }
            }
        }
    }
    
    fun getAccountByName(accountName: String): com.example.androidkmm.models.Account? {
        return try {
            val accountRow = database.categoryDatabaseQueries.selectAccountByName(accountName).executeAsOneOrNull()
            accountRow?.toAccount()
        } catch (e: Exception) {
            println("DEBUG: Error getting account by name: ${e.message}")
            null
        }
    }
    
    private suspend fun undoTransaction(
        transaction: Transaction,
        accountDatabaseManager: com.example.androidkmm.database.SQLiteAccountDatabase
    ) {
        try {
            println("DEBUG: Undoing transaction: ${transaction.title} (${transaction.type}) - ${transaction.amount}")
            
            when (transaction.type) {
                com.example.androidkmm.models.TransactionType.INCOME -> {
                    // Undo income: subtract the amount (reverse the original addition)
                    val account = getAccountByName(transaction.account)
                    if (account != null) {
                        val currentBalance = removeCurrencySymbols(account.balance).toDoubleOrNull() ?: 0.0
                        val newBalance = currentBalance - transaction.amount
                        accountDatabaseManager.updateAccountBalance(account.id, newBalance)
                        println("DEBUG: UNDO INCOME - Account ${account.name}: $currentBalance -> $newBalance (reversed +${transaction.amount})")
                    }
                }
                com.example.androidkmm.models.TransactionType.EXPENSE -> {
                    // Undo expense: add the amount back (reverse the original subtraction)
                    val account = getAccountByName(transaction.account)
                    if (account != null) {
                        val currentBalance = removeCurrencySymbols(account.balance).toDoubleOrNull() ?: 0.0
                        val newBalance = currentBalance + transaction.amount
                        accountDatabaseManager.updateAccountBalance(account.id, newBalance)
                        println("DEBUG: UNDO EXPENSE - Account ${account.name}: $currentBalance -> $newBalance (reversed -${transaction.amount})")
                    }
                }
                com.example.androidkmm.models.TransactionType.TRANSFER -> {
                    // Undo transfer: reverse the source and destination changes
                    val fromAccount = getAccountByName(transaction.account)
                    val toAccount = transaction.transferTo?.let { getAccountByName(it) }
                    
                    if (fromAccount != null) {
                        val currentFromBalance = removeCurrencySymbols(fromAccount.balance).toDoubleOrNull() ?: 0.0
                        val newFromBalance = currentFromBalance + transaction.amount
                        accountDatabaseManager.updateAccountBalance(fromAccount.id, newFromBalance)
                        println("DEBUG: UNDO TRANSFER FROM - Account ${fromAccount.name}: $currentFromBalance -> $newFromBalance (reversed -${transaction.amount})")
                    }
                    
                    if (toAccount != null) {
                        val currentToBalance = removeCurrencySymbols(toAccount.balance).toDoubleOrNull() ?: 0.0
                        val newToBalance = currentToBalance - transaction.amount
                        accountDatabaseManager.updateAccountBalance(toAccount.id, newToBalance)
                        println("DEBUG: UNDO TRANSFER TO - Account ${toAccount.name}: $currentToBalance -> $newToBalance (reversed +${transaction.amount})")
                    }
                }
            }
        } catch (e: Exception) {
            println("ERROR: Failed to undo transaction: ${e.message}")
            throw e
        }
    }
    
    private suspend fun applyTransaction(
        transaction: Transaction,
        accountDatabaseManager: com.example.androidkmm.database.SQLiteAccountDatabase
    ) {
        try {
            println("DEBUG: Applying transaction: ${transaction.title} (${transaction.type}) - ${transaction.amount}")
            
            when (transaction.type) {
                com.example.androidkmm.models.TransactionType.INCOME -> {
                    // Apply income: add the amount
                    val account = getAccountByName(transaction.account)
                    if (account != null) {
                        val currentBalance = removeCurrencySymbols(account.balance).toDoubleOrNull() ?: 0.0
                        val newBalance = currentBalance + transaction.amount
                        accountDatabaseManager.updateAccountBalance(account.id, newBalance)
                        println("DEBUG: APPLY INCOME - Account ${account.name}: $currentBalance -> $newBalance (+${transaction.amount})")
                    }
                }
                com.example.androidkmm.models.TransactionType.EXPENSE -> {
                    // Apply expense: subtract the amount
                    val account = getAccountByName(transaction.account)
                    if (account != null) {
                        val currentBalance = removeCurrencySymbols(account.balance).toDoubleOrNull() ?: 0.0
                        val newBalance = currentBalance - transaction.amount
                        accountDatabaseManager.updateAccountBalance(account.id, newBalance)
                        println("DEBUG: APPLY EXPENSE - Account ${account.name}: $currentBalance -> $newBalance (-${transaction.amount})")
                    }
                }
                com.example.androidkmm.models.TransactionType.TRANSFER -> {
                    // Apply transfer: subtract from source, add to destination
                    val fromAccount = getAccountByName(transaction.account)
                    val toAccount = transaction.transferTo?.let { getAccountByName(it) }
                    
                    if (fromAccount != null) {
                        val currentFromBalance = removeCurrencySymbols(fromAccount.balance).toDoubleOrNull() ?: 0.0
                        val newFromBalance = currentFromBalance - transaction.amount
                        accountDatabaseManager.updateAccountBalance(fromAccount.id, newFromBalance)
                        println("DEBUG: APPLY TRANSFER FROM - Account ${fromAccount.name}: $currentFromBalance -> $newFromBalance (-${transaction.amount})")
                    }
                    
                    if (toAccount != null) {
                        val currentToBalance = removeCurrencySymbols(toAccount.balance).toDoubleOrNull() ?: 0.0
                        val newToBalance = currentToBalance + transaction.amount
                        accountDatabaseManager.updateAccountBalance(toAccount.id, newToBalance)
                        println("DEBUG: APPLY TRANSFER TO - Account ${toAccount.name}: $currentToBalance -> $newToBalance (+${transaction.amount})")
                    }
                }
            }
        } catch (e: Exception) {
            println("ERROR: Failed to apply transaction: ${e.message}")
            throw e
        }
    }
    
    private suspend fun recalculateSpecificAccountBalance(
        accountName: String,
        accountDatabaseManager: com.example.androidkmm.database.SQLiteAccountDatabase
    ) {
        try {
            println("DEBUG: Recalculating balance for account: $accountName")
            
            // Get all transactions for this specific account
            val allTransactions = database.categoryDatabaseQueries.selectAllTransactions().executeAsList()
            val accountTransactions = allTransactions.filter { transaction ->
                transaction.account_name == accountName
            }
            
            println("DEBUG: Found ${accountTransactions.size} transactions for account $accountName")
            
            // Calculate balance from transactions
            var balance = 0.0
            accountTransactions.forEach { transaction ->
                when (transaction.type) {
                    "INCOME" -> balance += transaction.amount
                    "EXPENSE" -> balance -= transaction.amount
                    "TRANSFER" -> {
                        // For transfers, subtract from source account
                        balance -= transaction.amount
                    }
                }
                println("DEBUG: Transaction ${transaction.title} (${transaction.type}): ${transaction.amount} -> Balance: $balance")
            }
            
            // Also handle transfers TO this account
            val transfersToThisAccount = allTransactions.filter { transaction ->
                transaction.type == "TRANSFER" && transaction.transfer_to == accountName
            }
            
            transfersToThisAccount.forEach { transaction ->
                balance += transaction.amount
                println("DEBUG: Transfer TO account ${transaction.title}: ${transaction.amount} -> Balance: $balance")
            }
            
            // Get the account and update its balance
            val account = getAccountByName(accountName)
            if (account != null) {
                accountDatabaseManager.updateAccountBalance(account.id, balance)
                println("DEBUG: Updated account $accountName balance to $balance")
            } else {
                println("DEBUG: Account $accountName not found")
            }
            
        } catch (e: Exception) {
            println("ERROR: Failed to recalculate account balance for $accountName: ${e.message}")
            throw e
        }
    }
    
    private suspend fun recalculateAccountBalancesFromTransactions(
        accountDatabaseManager: com.example.androidkmm.database.SQLiteAccountDatabase
    ) {
        try {
            println("DEBUG: Recalculating all account balances from transactions...")
            
            // Get all transactions
            val allTransactions = database.categoryDatabaseQueries.selectAllTransactions().executeAsList()
            println("DEBUG: Found ${allTransactions.size} transactions to recalculate")
            
            // Get all accounts
            val allAccounts = database.categoryDatabaseQueries.selectAllAccounts().executeAsList()
            println("DEBUG: Found ${allAccounts.size} accounts to recalculate")
            
            // Reset all account balances to 0
            allAccounts.forEach { accountRow ->
                accountDatabaseManager.updateAccountBalance(accountRow.id, 0.0)
                println("DEBUG: Reset account ${accountRow.name} balance to 0.0")
            }
            
            // Recalculate each account balance from transactions
            allAccounts.forEach { accountRow ->
                val accountTransactions = allTransactions.filter { transaction ->
                    transaction.account_name == accountRow.name
                }
                
                var balance = 0.0
                accountTransactions.forEach { transaction ->
                    when (transaction.type) {
                        "INCOME" -> balance += transaction.amount
                        "EXPENSE" -> balance -= transaction.amount
                        "TRANSFER" -> {
                            // For transfers, subtract from source account
                            balance -= transaction.amount
                        }
                    }
                }
                
                // Also handle transfers TO this account
                val transfersToThisAccount = allTransactions.filter { transaction ->
                    transaction.type == "TRANSFER" && transaction.transfer_to == accountRow.name
                }
                
                transfersToThisAccount.forEach { transaction ->
                    balance += transaction.amount
                }
                
                accountDatabaseManager.updateAccountBalance(accountRow.id, balance)
                println("DEBUG: Recalculated account ${accountRow.name} balance to $balance")
            }
            
            println("DEBUG: All account balances recalculated successfully")
        } catch (e: Exception) {
            println("ERROR: Failed to recalculate account balances: ${e.message}")
            throw e
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

    suspend fun resetToDefaults() {
        withContext(Dispatchers.Default) {
            database.transaction {
                // Use default currency symbol for now (will be updated when user changes currency)
                val currencySymbol = getCurrencySymbol("INR") // Default to INR
                
                // Clear all data first
                database.categoryDatabaseQueries.deleteAllTransactions()
                database.categoryDatabaseQueries.deleteAllLedgerTransactions()
                database.categoryDatabaseQueries.deleteAllLedgerPersons()
                database.categoryDatabaseQueries.deleteAllGroupExpenseSplits()
                database.categoryDatabaseQueries.deleteAllGroupExpenses()
                database.categoryDatabaseQueries.deleteAllGroupMembers()
                database.categoryDatabaseQueries.deleteAllGroups()
                database.categoryDatabaseQueries.deleteAllCustomAccounts()
                database.categoryDatabaseQueries.deleteAllCustomCategories()
                
                // Delete ALL categories first (including default ones) to avoid constraint conflicts
                database.categoryDatabaseQueries.deleteAllCategories()
                
                // Re-insert default categories (expense and income)
                // Expense Categories
                database.categoryDatabaseQueries.insertCategory("1", "Food", "Restaurant", "#FFFF9800", "EXPENSE", 0)
                database.categoryDatabaseQueries.insertCategory("2", "Transport", "DirectionsCar", "#FF2196F3", "EXPENSE", 0)
                database.categoryDatabaseQueries.insertCategory("3", "Housing", "Home", "#FF4CAF50", "EXPENSE", 0)
                database.categoryDatabaseQueries.insertCategory("4", "Utilities", "Lightbulb", "#FFFFC107", "EXPENSE", 0)
                database.categoryDatabaseQueries.insertCategory("5", "Health", "LocalHospital", "#FFE91E63", "EXPENSE", 0)
                database.categoryDatabaseQueries.insertCategory("6", "Shopping", "ShoppingCart", "#FF9C27B0", "EXPENSE", 0)
                database.categoryDatabaseQueries.insertCategory("7", "Entertainment", "Movie", "#FF673AB7", "EXPENSE", 0)
                database.categoryDatabaseQueries.insertCategory("8", "Travel", "Flight", "#FF3F51B5", "EXPENSE", 0)
                database.categoryDatabaseQueries.insertCategory("9", "Education", "School", "#FF009688", "EXPENSE", 0)
                database.categoryDatabaseQueries.insertCategory("10", "Savings", "Savings", "#FF4CAF50", "EXPENSE", 0)
                database.categoryDatabaseQueries.insertCategory("11", "Loans", "AccountBalance", "#FFF44336", "EXPENSE", 0)
                database.categoryDatabaseQueries.insertCategory("12", "Gifts", "CardGiftcard", "#FFE91E63", "EXPENSE", 0)
                database.categoryDatabaseQueries.insertCategory("13", "Others", "Category", "#FF607D8B", "EXPENSE", 0)
                
                // Income Categories
                database.categoryDatabaseQueries.insertCategory("14", "Salary", "AttachMoney", "#FF4CAF50", "INCOME", 0)
                database.categoryDatabaseQueries.insertCategory("15", "Freelance", "Work", "#FF2196F3", "INCOME", 0)
                database.categoryDatabaseQueries.insertCategory("16", "Investment", "TrendingUp", "#FF9C27B0", "INCOME", 0)
                database.categoryDatabaseQueries.insertCategory("17", "Rental Income", "Home", "#FF4CAF50", "INCOME", 0)
                database.categoryDatabaseQueries.insertCategory("18", "Gift", "CardGiftcard", "#FFE91E63", "INCOME", 0)
                database.categoryDatabaseQueries.insertCategory("19", "Bonus", "Stars", "#FFFF9800", "INCOME", 0)
                
                // Delete ALL accounts first (including default ones) to avoid constraint conflicts
                database.categoryDatabaseQueries.deleteAllAccounts()
                
                // Re-insert default account (Cash) with dynamic currency symbol
                database.categoryDatabaseQueries.insertAccount("1", "Cash", "${currencySymbol}0", "Money", "#FF4CAF50", "CASH", 0)
            }
        }
    }
}

// Icon name to ImageVector mapping for transactions
private val transactionIconMap = mapOf(
    "Restaurant" to Icons.Default.Restaurant,
    "AttachMoney" to Icons.Default.AttachMoney,
    "Money" to Icons.Default.Money,
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
