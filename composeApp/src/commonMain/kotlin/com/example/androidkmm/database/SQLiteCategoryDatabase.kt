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
import com.example.androidkmm.models.Category
import com.example.androidkmm.models.CategoryType
// import com.example.androidkmm.utils.formatDouble // Not needed for String.format

@Composable
fun rememberSQLiteCategoryDatabase(): SQLiteCategoryDatabase {
    val driverFactory = rememberDatabaseDriverFactory()
    val database = remember { CategoryDatabase(driverFactory.createDriver()) }
    val scope = rememberCoroutineScope()
    
    return remember {
        SQLiteCategoryDatabase(database, scope)
    }
}

class SQLiteCategoryDatabase(
    private val database: CategoryDatabase,
    private val scope: kotlinx.coroutines.CoroutineScope
) {
    
    fun getDatabase(): CategoryDatabase = database
    
    init {
        // Initialize database with default categories if empty
        scope.launch {
            val count = database.categoryDatabaseQueries.getCategoryCount().executeAsOne()
            if (count == 0L) {
                // Default categories are already inserted via SQL schema
                println("DEBUG: Database initialized with default categories")
            } else {
                println("DEBUG: Database loaded with $count categories")
            }
        }
    }
    
    fun getAllCategories(): Flow<List<Category>> {
        return database.categoryDatabaseQueries.selectAll().asFlow().mapToList(Dispatchers.Default).map { list ->
            try {
                list.mapNotNull { dbCategory ->
                    try {
                        dbCategory.toCategory()
                    } catch (e: Exception) {
                        println("Error converting category ${dbCategory.id}: ${e.message}")
                        null // Skip invalid categories
                    }
                }
            } catch (e: Exception) {
                println("Error processing category list: ${e.message}")
                e.printStackTrace()
                emptyList()
            }
        }
    }
    
    fun getCategoriesByType(type: CategoryType): Flow<List<Category>> {
        return database.categoryDatabaseQueries.selectByType(type.name).asFlow().mapToList(Dispatchers.Default).map { list ->
            try {
                list.mapNotNull { dbCategory ->
                    try {
                        dbCategory.toCategory()
                    } catch (e: Exception) {
                        println("Error converting category ${dbCategory.id}: ${e.message}")
                        null
                    }
                }
            } catch (e: Exception) {
                println("Error processing categories by type: ${e.message}")
                emptyList()
            }
        }
    }
    
    fun getCustomCategories(): Flow<List<Category>> {
        return database.categoryDatabaseQueries.selectCustomCategories().asFlow().mapToList(Dispatchers.Default).map { list ->
            try {
                list.mapNotNull { dbCategory ->
                    try {
                        dbCategory.toCategory()
                    } catch (e: Exception) {
                        println("Error converting custom category ${dbCategory.id}: ${e.message}")
                        null
                    }
                }
            } catch (e: Exception) {
                println("Error processing custom categories: ${e.message}")
                emptyList()
            }
        }
    }
    
    fun getDefaultCategories(): Flow<List<Category>> {
        return database.categoryDatabaseQueries.selectDefaultCategories().asFlow().mapToList(Dispatchers.Default).map { list ->
            try {
                list.mapNotNull { dbCategory ->
                    try {
                        dbCategory.toCategory()
                    } catch (e: Exception) {
                        println("Error converting default category ${dbCategory.id}: ${e.message}")
                        null
                    }
                }
            } catch (e: Exception) {
                println("Error processing default categories: ${e.message}")
                emptyList()
            }
        }
    }
    
    fun addCategory(category: Category, onSuccess: () -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        println("DEBUG: SQLiteCategoryDatabase.addCategory called with: ${category.name}")
        scope.launch {
            try {
                // Check for duplicate category name
                val existing = database.categoryDatabaseQueries.selectByNameAndType(category.name, category.type.name).executeAsOneOrNull()
                if (existing != null) {
                    println("DEBUG: Duplicate category name found: ${category.name}")
                    onError(Exception("Category with name '${category.name}' already exists"))
                    return@launch
                }
                
                database.categoryDatabaseQueries.insertCategory(
                    id = category.id,
                    name = category.name,
                    icon_name = getIconName(category.icon),
                    color_hex = category.color.toHexString(),
                    type = category.type.name,
                    is_custom = if (category.isCustom) 1L else 0L
                )
                println("DEBUG: Category inserted successfully into SQLite database")
                onSuccess()
            } catch (e: Exception) {
                println("DEBUG: Error inserting category: ${e.message}")
                onError(e)
            }
        }
    }
    
    fun updateCategory(category: Category, onSuccess: () -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        scope.launch {
            try {
                database.categoryDatabaseQueries.updateCategory(
                    name = category.name,
                    icon_name = getIconName(category.icon),
                    color_hex = category.color.toHexString(),
                    type = category.type.name,
                    is_custom = if (category.isCustom) 1L else 0L,
                    id = category.id
                )
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
    
    fun deleteCategory(category: Category, onSuccess: () -> Unit = {}, onError: (Throwable) -> Unit = {}) {
        scope.launch {
            try {
                database.categoryDatabaseQueries.deleteCategory(category.id)
                onSuccess()
            } catch (e: Exception) {
                onError(e)
            }
        }
    }
}

// Platform-specific database driver factory
@Composable
expect fun rememberDatabaseDriverFactory(): DatabaseDriverFactory

// Icon name to ImageVector mapping
private val iconMap = mapOf(
    "Restaurant" to Icons.Default.Restaurant,
    "DirectionsCar" to Icons.Default.DirectionsCar,
    "Home" to Icons.Default.Home,
    "Lightbulb" to Icons.Default.Lightbulb,
    "LocalHospital" to Icons.Default.LocalHospital,
    "ShoppingCart" to Icons.Default.ShoppingCart,
    "Movie" to Icons.Default.Movie,
    "Flight" to Icons.Default.Flight,
    "School" to Icons.Default.School,
    "Savings" to Icons.Default.Savings,
    "AccountBalance" to Icons.Default.AccountBalance,
    "CardGiftcard" to Icons.Default.CardGiftcard,
    "Category" to Icons.Default.Category,
    "AttachMoney" to Icons.Default.AttachMoney,
    "Work" to Icons.Default.Work,
    "TrendingUp" to Icons.Default.TrendingUp,
    "Stars" to Icons.Default.Stars,
    "SportsEsports" to Icons.Default.SportsEsports,
    "Favorite" to Icons.Default.Favorite,
    "MusicNote" to Icons.Default.MusicNote,
    "Business" to Icons.Default.Business,
    "LocalCafe" to Icons.Default.LocalCafe,
    "Wallet" to Icons.Default.Wallet
)

// ImageVector to icon name mapping
private val nameMap = iconMap.entries.associate { (k, v) -> v to k }

private fun getIconByName(name: String): ImageVector {
    return iconMap[name] ?: Icons.Default.Category
}

private fun getIconName(icon: ImageVector): String {
    return nameMap[icon] ?: "Category"
}

// Helper function to convert Color to hex string
private fun Color.toHexString(): String {
    val alpha = (this.alpha * 255).toInt()
    val red = (this.red * 255).toInt()
    val green = (this.green * 255).toInt()
    val blue = (this.blue * 255).toInt()
    return "#${alpha.toString(16).padStart(2, '0')}${red.toString(16).padStart(2, '0')}${green.toString(16).padStart(2, '0')}${blue.toString(16).padStart(2, '0')}"
}

// Platform-specific color parsing
expect fun parseColorHex(hexString: String): Color

// Extension function to convert database row to Category
private fun com.example.androidkmm.database.Category.toCategory(): com.example.androidkmm.models.Category {
    return try {
        com.example.androidkmm.models.Category(
            id = this.id ?: "unknown",
            name = this.name ?: "Unknown Category",
            icon = try { getIconByName(this.icon_name ?: "Category") } catch (e: Exception) { Icons.Default.Category },
            color = try { parseColorHex(this.color_hex ?: "#FF607D8B") } catch (e: Exception) { Color(0xFF607D8B) },
            type = try { CategoryType.valueOf(this.type ?: "EXPENSE") } catch (e: Exception) { CategoryType.EXPENSE },
            isCustom = this.is_custom == 1L
        )
    } catch (e: Exception) {
        println("Error converting category: ${e.message}")
        e.printStackTrace()
        // Return a safe default category
        com.example.androidkmm.models.Category(
            id = this.id ?: "error_category",
            name = "Error Loading Category",
            icon = Icons.Default.Error,
            color = Color(0xFF607D8B),
            type = CategoryType.EXPENSE,
            isCustom = false
        )
    }
}

@Composable
fun rememberSQLiteLedgerDatabase(): SQLiteLedgerDatabase {
    val database = rememberSQLiteCategoryDatabase()
    return remember { SQLiteLedgerDatabase(database.getDatabase()) }
}
