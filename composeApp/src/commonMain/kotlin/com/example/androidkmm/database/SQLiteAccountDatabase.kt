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
import app.cash.sqldelight.coroutines.mapToOne
import com.example.androidkmm.models.Account
import com.example.androidkmm.models.AccountType

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
        return database.categoryDatabaseQueries.selectAllAccounts().asFlow().mapToList(Dispatchers.IO).map { list ->
            list.map { it.toAccount() }
        }
    }
    
    fun getAccountById(id: String): Flow<Account?> {
        return database.categoryDatabaseQueries.selectAccountById(id).asFlow().mapToList(Dispatchers.IO).map { list ->
            list.firstOrNull()?.toAccount()
        }
    }
    
    fun getCustomAccounts(): Flow<List<Account>> {
        return database.categoryDatabaseQueries.selectCustomAccounts().asFlow().mapToList(Dispatchers.IO).map { list ->
            list.map { it.toAccount() }
        }
    }
    
    fun getDefaultAccounts(): Flow<List<Account>> {
        return database.categoryDatabaseQueries.selectDefaultAccounts().asFlow().mapToList(Dispatchers.IO).map { list ->
            list.map { it.toAccount() }
        }
    }
    
    fun addAccount(account: Account, onSuccess: () -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        println("DEBUG: SQLiteAccountDatabase.addAccount called with: ${account.name}")
        scope.launch {
            try {
                // Check for duplicate account name
                val existing = database.categoryDatabaseQueries.selectAccountByName(account.name).executeAsOneOrNull()
                if (existing != null) {
                    println("DEBUG: Duplicate account name found: ${account.name}")
                    onError(Exception("Account with name '${account.name}' already exists"))
                    return@launch
                }
                
                database.categoryDatabaseQueries.insertAccount(
                    id = account.id,
                    name = account.name,
                    balance = account.balance,
                    icon_name = getIconName(account.icon),
                    color_hex = account.color.toHexString(),
                    type = account.type,
                    is_custom = if (account.isCustom) 1L else 0L
                )
                println("DEBUG: Account inserted successfully into SQLite database")
                onSuccess()
            } catch (e: Exception) {
                println("DEBUG: Error inserting account: ${e.message}")
                onError(e)
            }
        }
    }
    
    fun updateAccount(account: Account, onSuccess: () -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        scope.launch {
            try {
                database.categoryDatabaseQueries.updateAccount(
                    name = account.name,
                    balance = account.balance,
                    icon_name = getIconName(account.icon),
                    color_hex = account.color.toHexString(),
                    type = account.type,
                    is_custom = if (account.isCustom) 1L else 0L,
                    id = account.id
                )
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
    
    fun deleteAccount(account: Account, onSuccess: () -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        scope.launch {
            try {
                database.categoryDatabaseQueries.deleteAccount(account.id)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
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
    return String.format("#%02X%02X%02X%02X", alpha, red, green, blue)
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
