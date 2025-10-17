package com.example.androidkmm.database

import androidx.compose.material.icons.Icons
import com.example.androidkmm.utils.TimeUtils
import kotlinx.datetime.*
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
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
import app.cash.sqldelight.coroutines.mapToOne
import com.example.androidkmm.models.Account
import com.example.androidkmm.models.AccountType
// import com.example.androidkmm.utils.formatDouble // Not needed for String.format

@Composable
fun rememberSQLiteAccountDatabase(): SQLiteAccountDatabase {
    val driverFactory = rememberDatabaseDriverFactory()
    val database = remember { CategoryDatabase(driverFactory.createDriver()) }
    val scope = rememberCoroutineScope()
    
    return remember {
        SQLiteAccountDatabase(database, scope)
    }
}

class SQLiteAccountDatabase(
    private val database: CategoryDatabase,
    private val scope: kotlinx.coroutines.CoroutineScope
) {
    
    init {
        // Initialize database with default accounts if empty
        scope.launch {
            try {
                val count = database.categoryDatabaseQueries.getAccountCount().executeAsOne()
                if (count == 0L) {
                    // Default accounts are already inserted via SQL schema
                    println("DEBUG: Database initialized with default accounts")
                } else {
                    println("DEBUG: Database loaded with $count accounts")
                }
            } catch (e: Exception) {
                // Account table doesn't exist yet, this is expected for existing databases
                println("DEBUG: Account table not found, will be created on next app restart")
            }
        }
        
    }
    
    fun getAllAccounts(): Flow<List<Account>> {
        return database.categoryDatabaseQueries.selectAllAccounts().asFlow().mapToList(Dispatchers.Default).map { list ->
            try {
                list.mapNotNull { dbAccount ->
                    try {
                        dbAccount.toAccount()
                    } catch (e: Exception) {
                        println("Error converting account ${dbAccount.id}: ${e.message}")
                        null // Skip invalid accounts
                    }
                }
            } catch (e: Exception) {
                println("Error processing account list: ${e.message}")
                e.printStackTrace()
                emptyList()
            }
        }
    }
    
    fun getAccountById(id: String): Flow<Account?> {
        return database.categoryDatabaseQueries.selectAccountById(id).asFlow().mapToList(Dispatchers.Default).map { list ->
            try {
                list.firstOrNull()?.let { dbAccount ->
                    try {
                        dbAccount.toAccount()
                    } catch (e: Exception) {
                        println("Error converting account ${dbAccount.id}: ${e.message}")
                        null
                    }
                }
            } catch (e: Exception) {
                println("Error processing account by id: ${e.message}")
                null
            }
        }
    }
    
    fun getCustomAccounts(): Flow<List<Account>> {
        return database.categoryDatabaseQueries.selectCustomAccounts().asFlow().mapToList(Dispatchers.Default).map { list ->
            try {
                list.mapNotNull { dbAccount ->
                    try {
                        dbAccount.toAccount()
                    } catch (e: Exception) {
                        println("Error converting custom account ${dbAccount.id}: ${e.message}")
                        null
                    }
                }
            } catch (e: Exception) {
                println("Error processing custom accounts: ${e.message}")
                emptyList()
            }
        }
    }
    
    fun getDefaultAccounts(): Flow<List<Account>> {
        return database.categoryDatabaseQueries.selectDefaultAccounts().asFlow().mapToList(Dispatchers.Default).map { list ->
            try {
                list.mapNotNull { dbAccount ->
                    try {
                        dbAccount.toAccount()
                    } catch (e: Exception) {
                        println("Error converting default account ${dbAccount.id}: ${e.message}")
                        null
                    }
                }
            } catch (e: Exception) {
                println("Error processing default accounts: ${e.message}")
                emptyList()
            }
        }
    }
    
    fun addAccount(account: Account, onSuccess: () -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        println("DEBUG: SQLiteAccountDatabase.addAccount called with: ${account.name}")
        println("DEBUG: Account details - ID: ${account.id}, Type: ${account.type}, Balance: ${account.balance}")
        scope.launch {
            try {
                // Check for duplicate account name
                println("DEBUG: Checking for duplicate account name: ${account.name}")
                val existing = database.categoryDatabaseQueries.selectAccountByName(account.name).executeAsOneOrNull()
                if (existing != null) {
                    println("DEBUG: Duplicate account name found: ${account.name}")
                    onError(Exception("Account with name '${account.name}' already exists"))
                    return@launch
                }
                
                println("DEBUG: No duplicate found, proceeding with account insertion")
                // Format balance to 2 decimal places before storing in database
                val formattedBalance = try {
                    val balanceValue = account.balance.toDoubleOrNull() ?: 0.0
                    String.format("%.2f", balanceValue)
                } catch (e: Exception) {
                    "0.00"
                }
                
                database.categoryDatabaseQueries.insertAccount(
                    id = account.id,
                    name = account.name,
                    balance = formattedBalance,
                    icon_name = getIconName(account.icon),
                    color_hex = account.color.toHexString(),
                    type = account.type,
                    is_custom = if (account.isCustom) 1L else 0L
                )
                println("DEBUG: Account inserted successfully into SQLite database")
                
                // Create account operation transaction
                println("DEBUG: Creating account operation transaction")
                createAccountOperationTransaction(
                    title = "New account '${account.name}' added with balance ${account.balance}",
                    amount = account.balance.toDouble(),
                    type = "INCOME"
                )
                
                println("DEBUG: Calling onSuccess callback")
                onSuccess()
            } catch (e: Exception) {
                println("DEBUG: Error inserting account: ${e.message}")
                println("DEBUG: Error stack trace: ${e.stackTraceToString()}")
                onError(e)
            }
        }
    }
    
    // Debug function to test database connectivity
    fun testDatabaseConnection() {
        println("DEBUG: Testing database connection...")
        scope.launch {
            try {
                val accountCount = database.categoryDatabaseQueries.getAccountCount().executeAsOne()
                println("DEBUG: Database connection successful. Account count: $accountCount")
                
                val allAccounts = database.categoryDatabaseQueries.selectAllAccounts().executeAsList()
                println("DEBUG: All accounts in database: ${allAccounts.map { it.name }}")
            } catch (e: Exception) {
                println("DEBUG: Database connection failed: ${e.message}")
                println("DEBUG: Error stack trace: ${e.stackTraceToString()}")
            }
        }
    }
    
    fun updateAccount(account: Account, onSuccess: () -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        scope.launch {
            try {
                println("DEBUG: updateAccount called for account: ${account.name}, balance: ${account.balance}")
                
                // Get old account data to track balance changes
                val oldAccount = database.categoryDatabaseQueries.selectAccountById(account.id).executeAsOneOrNull()
                println("DEBUG: Old account data: $oldAccount")
                
                database.categoryDatabaseQueries.updateAccount(
                    name = account.name,
                    balance = account.balance,
                    icon_name = getIconName(account.icon),
                    color_hex = account.color.toHexString(),
                    type = account.type,
                    is_custom = if (account.isCustom) 1L else 0L,
                    id = account.id
                )
                println("DEBUG: Account updated in database")
                
                // Create account operation transaction for update
                if (oldAccount != null) {
                    // Parse balances properly, handling currency symbols
                    val oldBalance = parseBalance(oldAccount.balance)
                    val newBalance = parseBalance(account.balance)
                    val balanceChange = newBalance - oldBalance
                    
                    println("DEBUG: Old balance: ${oldAccount.balance} -> parsed: $oldBalance")
                    println("DEBUG: New balance: ${account.balance} -> parsed: $newBalance")
                    println("DEBUG: Balance change: $balanceChange")
                    
                    // Only create account operation if there's an actual balance change
                    if (kotlin.math.abs(balanceChange) > 0.01) { // Use small threshold to avoid floating point issues
                        val transactionType = if (balanceChange > 0) "INCOME" else "EXPENSE"
                        println("DEBUG: Creating account operation transaction - balance change: $balanceChange, type: $transactionType")
                        createAccountOperationTransaction(
                            title = "Account '${account.name}' updated from ${oldAccount.balance} to ${account.balance} balance",
                            amount = kotlin.math.abs(balanceChange),
                            type = transactionType
                        )
                    } else {
                        println("DEBUG: No significant balance change detected, skipping account operation transaction")
                    }
                } else {
                    println("DEBUG: No old account data found, skipping account operation transaction")
                }
                
                onSuccess()
            } catch (e: Exception) {
                println("DEBUG: Error in updateAccount: ${e.message}")
                onError(e)
            }
        }
    }
    
    fun deleteAccount(account: Account, onSuccess: () -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        scope.launch {
            try {
                // Create account operation transaction before deleting
                createAccountOperationTransaction(
                    title = "Account '${account.name}' deleted with balance ${account.balance}",
                    amount = account.balance.toDouble(),
                    type = "EXPENSE"
                )
                
                database.categoryDatabaseQueries.deleteAccount(account.id)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
    
    fun updateAccountBalance(accountId: String, newBalance: Double, onSuccess: () -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        updateAccountBalanceInternal(accountId, newBalance, createAccountOperation = false, onSuccess, onError)
    }
    
    fun updateAccountBalanceWithOperation(accountId: String, newBalance: Double, onSuccess: () -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        updateAccountBalanceInternal(accountId, newBalance, createAccountOperation = true, onSuccess, onError)
    }
    
    private fun updateAccountBalanceInternal(accountId: String, newBalance: Double, createAccountOperation: Boolean, onSuccess: () -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        scope.launch {
            try {
                // Get old account data to track balance changes
                val oldAccount = database.categoryDatabaseQueries.selectAccountById(accountId).executeAsOneOrNull()
                
                // Round balance to 2 decimal places to avoid floating-point precision issues
                val roundedBalance = kotlin.math.round(newBalance * 100.0) / 100.0
                // Format to 2 decimal places to ensure consistent storage (e.g., 1.90 not 1.9)
                val formattedBalance = String.format("%.2f", roundedBalance)
                database.categoryDatabaseQueries.updateAccountBalance(formattedBalance, accountId)
                println("DEBUG: Account balance updated successfully: $accountId -> $newBalance")
                
                // Create account operation transaction only if explicitly requested
                if (createAccountOperation && oldAccount != null) {
                    val balanceChange = newBalance - oldAccount.balance.toDouble()
                    val transactionType = if (balanceChange > 0) "INCOME" else "EXPENSE"
                    createAccountOperationTransaction(
                        title = "Account '${oldAccount.name}' updated from ${oldAccount.balance} to $newBalance balance",
                        amount = kotlin.math.abs(balanceChange),
                        type = transactionType
                    )
                }
                
                onSuccess()
            } catch (e: Exception) {
                println("DEBUG: Error updating account balance: ${e.message}")
                onError(e)
            }
        }
    }
    
    suspend fun hasAccountTransactions(accountName: String): Boolean {
        return try {
            val transactionCount = database.categoryDatabaseQueries.getTransactionCountByAccount(accountName).executeAsOne()
            transactionCount > 0
        } catch (e: Exception) {
            println("DEBUG: Error checking transactions for account $accountName: ${e.message}")
            false
        }
    }
    
    /**
     * Creates a special account operation transaction that appears as a simple text line
     */
    @OptIn(ExperimentalTime::class)
    private fun createAccountOperationTransaction(
        title: String,
        amount: Double,
        type: String
    ) {
        try {
            val transactionId = TimeUtils.currentTimeMillis().toString() + "_" + (0..999).random()
            val currentTime = TimeUtils.currentTimeMillis()
            
            // Get current date dynamically using the same approach as other parts of the codebase
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val currentDate = "${now.date.year}-${now.date.monthNumber.toString().padStart(2, '0')}-${now.date.dayOfMonth.toString().padStart(2, '0')}"
            
            database.categoryDatabaseQueries.insertTransaction(
                id = transactionId,
                title = title,
                amount = amount,
                category_name = "Account Operation",
                category_icon_name = "account_balance",
                category_color_hex = "#FF6B6B",
                account_name = "System",
                account_icon_name = "account_balance",
                account_color_hex = "#FF6B6B",
                transfer_to = null,
                time = currentTime.toString(),
                type = type,
                description = "",
                date = currentDate,
                is_ledger_transaction = 0L,
                ledger_person_id = null,
                ledger_person_name = null
            )
            println("DEBUG: Account operation transaction created: $title with date: $currentDate")
        } catch (e: Exception) {
            println("DEBUG: Error creating account operation transaction: ${e.message}")
        }
    }
    
    fun getActiveAccounts(): Flow<List<Account>> {
        return database.categoryDatabaseQueries.selectActiveAccounts().asFlow().mapToList(Dispatchers.Default).map { list ->
            try {
                list.mapNotNull { dbAccount ->
                    try {
                        dbAccount.toAccount()
                    } catch (e: Exception) {
                        println("Error converting active account ${dbAccount.id}: ${e.message}")
                        null
                    }
                }
            } catch (e: Exception) {
                println("Error processing active account list: ${e.message}")
                emptyList()
            }
        }
    }
    
    fun getArchivedAccounts(): Flow<List<Account>> {
        return database.categoryDatabaseQueries.selectArchivedAccounts().asFlow().mapToList(Dispatchers.Default).map { list ->
            try {
                list.mapNotNull { dbAccount ->
                    try {
                        dbAccount.toAccount()
                    } catch (e: Exception) {
                        println("Error converting archived account ${dbAccount.id}: ${e.message}")
                        null
                    }
                }
            } catch (e: Exception) {
                println("Error processing archived account list: ${e.message}")
                emptyList()
            }
        }
    }
    
    
    suspend fun archiveAccount(accountId: String) {
        try {
            // Get account data before archiving to create operation message
            val account = database.categoryDatabaseQueries.selectAccountById(accountId).executeAsOneOrNull()
            
            database.categoryDatabaseQueries.archiveAccount(accountId)
            println("DEBUG: Account $accountId archived successfully")
            
            // Create account operation transaction for archive
            if (account != null) {
                createAccountOperationTransaction(
                    title = "Account '${account.name}' archived with balance ${account.balance}",
                    amount = 0.0, // Archive has no financial impact
                    type = "EXPENSE" // Use EXPENSE type but with 0 amount
                )
            }
        } catch (e: Exception) {
            println("DEBUG: Error archiving account $accountId: ${e.message}")
            throw e
        }
    }
    
    suspend fun unarchiveAccount(accountId: String) {
        try {
            // Get account data before unarchiving to create operation message
            val account = database.categoryDatabaseQueries.selectAccountById(accountId).executeAsOneOrNull()
            
            database.categoryDatabaseQueries.unarchiveAccount(accountId)
            println("DEBUG: Account $accountId unarchived successfully")
            
            // Create account operation transaction for unarchive
            if (account != null) {
                // Remove [ARCHIVED] prefix from name for display
                val displayName = if (account.name.startsWith("[ARCHIVED]")) {
                    account.name.substring(11)
                } else {
                    account.name
                }
                
                createAccountOperationTransaction(
                    title = "Account '${displayName}' unarchived with balance ${account.balance}",
                    amount = 0.0, // Unarchive has no financial impact
                    type = "INCOME" // Use INCOME type but with 0 amount
                )
            }
        } catch (e: Exception) {
            println("DEBUG: Error unarchiving account $accountId: ${e.message}")
            throw e
        }
    }
    
    /**
     * Parse balance string, handling currency symbols and formatting
     */
    private fun parseBalance(balanceString: String): Double {
        return try {
            // Remove common currency symbols and whitespace
            val cleaned = balanceString
                .replace(Regex("[₹$€£¥₽₩₪₫₴₸₺₼₾₿]"), "") // Remove currency symbols
                .replace(Regex("[,\\s]"), "") // Remove commas and whitespace
                .trim()
            
            cleaned.toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            println("DEBUG: Error parsing balance '$balanceString': ${e.message}")
            0.0
        }
    }
}

// Icon name to ImageVector mapping for accounts
private val accountIconMap = mapOf(
    "AttachMoney" to Icons.Default.AttachMoney,
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
    "ShoppingCart" to Icons.Default.ShoppingCart,
    "Movie" to Icons.Default.Movie,
    "Restaurant" to Icons.Default.Restaurant,
    "DirectionsCar" to Icons.Default.DirectionsCar,
    "Lightbulb" to Icons.Default.Lightbulb,
    "Stars" to Icons.Default.Stars,
    "Category" to Icons.Default.Category
)

// ImageVector to icon name mapping for accounts
private val accountNameMap = accountIconMap.entries.associate { (k, v) -> v to k }

private fun getIconByName(name: String): ImageVector {
    return accountIconMap[name] ?: Icons.Default.AccountBalance
}

private fun getIconName(icon: ImageVector): String {
    return accountNameMap[icon] ?: "AccountBalance"
}

// Helper function to convert Color to hex string
private fun Color.toHexString(): String {
    val alpha = (this.alpha * 255).toInt()
    val red = (this.red * 255).toInt()
    val green = (this.green * 255).toInt()
    val blue = (this.blue * 255).toInt()
    return "#${formatHex(alpha)}${formatHex(red)}${formatHex(green)}${formatHex(blue)}"
}

private fun formatHex(value: Int): String {
    val hex = value.toString(16).uppercase()
    return if (hex.length < 2) "0$hex" else hex
}


// Extension function to convert database row to Account
private fun com.example.androidkmm.database.Account.toAccount(): com.example.androidkmm.models.Account {
    val isArchived = this.name.startsWith("[ARCHIVED]")
    val displayName = if (isArchived) {
        this.name.removePrefix("[ARCHIVED]")
    } else {
        this.name
    }
    
    return com.example.androidkmm.models.Account(
        id = this.id,
        name = displayName,
        balance = this.balance,
        icon = getIconByName(this.icon_name),
        color = parseColorHex(this.color_hex),
        type = this.type,
        isCustom = this.is_custom == 1L,
        isArchived = isArchived
    )
}
