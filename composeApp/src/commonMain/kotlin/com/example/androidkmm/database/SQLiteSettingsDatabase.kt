package com.example.androidkmm.database

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.androidkmm.models.AppSetting
import com.example.androidkmm.models.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers

class SQLiteSettingsDatabase(private val database: CategoryDatabase) {
    
    fun getAllSettings(): Flow<List<AppSetting>> {
        return database.categoryDatabaseQueries.selectAllSettings().asFlow().mapToList(Dispatchers.Default).map { rows ->
            println("Database Debug - Retrieved ${rows.size} settings from database")
            rows.forEach { row ->
                println("Database Debug - Setting: key='${row.key}', value='${row.value_}'")
            }
            rows.map { row ->
                AppSetting(
                    key = row.key,
                    value = row.value_,
                    updatedAt = row.updated_at
                )
            }
        }
    }
    
    fun getSetting(key: String): Flow<AppSetting?> {
        return database.categoryDatabaseQueries.selectSettingByKey(key).asFlow().mapToOneOrNull(Dispatchers.Default).map { row ->
            row?.let {
                AppSetting(
                    key = it.key,
                    value = it.value_,
                    updatedAt = it.updated_at
                )
            }
        }
    }
    
    suspend fun updateSetting(key: String, value: String) {
        println("Database Debug - Updating setting: key='$key', value='$value'")
        database.categoryDatabaseQueries.upsertSetting(key, value)
        println("Database Debug - Setting updated successfully")
    }
    
    fun getAppSettings(): Flow<AppSettings> {
        return getAllSettings().map { settings ->
            val settingsMap = settings.associate { it.key to it.value }
            val carryForwardValue = settingsMap["carry_forward_enabled"]
            val carryForwardEnabled = when (carryForwardValue) {
                "1" -> true
                "0" -> false
                null -> true // Default to true if not found
                else -> carryForwardValue.toBoolean()
            }
            println("Settings Debug - carry_forward_enabled: '$carryForwardValue', parsed: $carryForwardEnabled")
            println("Settings Debug - settingsMap: $settingsMap")
            println("Settings Debug - user_name from map: '${settingsMap["user_name"]}'")
            println("Settings Debug - user_email from map: '${settingsMap["user_email"]}'")
            
            val appSettings = AppSettings(
                carryForwardEnabled = carryForwardEnabled,
                currencySymbol = settingsMap["currency_symbol"] ?: "$",
                dateFormat = settingsMap["date_format"] ?: "MMM dd, yyyy",
                userName = settingsMap["user_name"] ?: "",
                userEmail = settingsMap["user_email"] ?: ""
            )
            
            println("Settings Debug - Final AppSettings: userName='${appSettings.userName}', userEmail='${appSettings.userEmail}'")
            appSettings
        }
    }
    
    suspend fun updateCarryForwardEnabled(enabled: Boolean) {
        val value = if (enabled) "1" else "0"
        println("Updating carry_forward_enabled to: $value")
        updateSetting("carry_forward_enabled", value)
    }
    
    suspend fun updateCurrencySymbol(symbol: String) {
        updateSetting("currency_symbol", symbol)
    }
    
    suspend fun updateDateFormat(format: String) {
        updateSetting("date_format", format)
    }
    
    suspend fun updateUserName(name: String) {
        updateSetting("user_name", name)
    }
    
    suspend fun updateUserEmail(email: String) {
        updateSetting("user_email", email)
    }
    
    // Debug function to check database state
    suspend fun debugDatabaseState() {
        try {
            val settings = database.categoryDatabaseQueries.selectAllSettings().executeAsList()
            println("Database Debug - Total settings in database: ${settings.size}")
            settings.forEach { setting ->
                println("Database Debug - Setting: ${setting.key} = '${setting.value_}'")
            }
        } catch (e: Exception) {
            println("Database Debug - Error reading database: ${e.message}")
            e.printStackTrace()
        }
    }
}

@Composable
fun rememberSQLiteSettingsDatabase(): SQLiteSettingsDatabase {
    println("Database Debug - Creating SQLiteSettingsDatabase")
    val driverFactory = rememberDatabaseDriverFactory()
    println("Database Debug - Driver factory created")
    val database = remember { 
        println("Database Debug - Creating CategoryDatabase")
        CategoryDatabase(driverFactory.createDriver())
    }
    println("Database Debug - CategoryDatabase created")
    return remember { 
        println("Database Debug - Creating SQLiteSettingsDatabase instance")
        SQLiteSettingsDatabase(database)
    }
}
