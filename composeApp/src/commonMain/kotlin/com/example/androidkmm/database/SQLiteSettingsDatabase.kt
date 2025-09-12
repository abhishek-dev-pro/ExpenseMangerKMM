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
        database.categoryDatabaseQueries.upsertSetting(key, value)
    }
    
    fun getAppSettings(): Flow<AppSettings> {
        return getAllSettings().map { settings ->
            val settingsMap = settings.associate { it.key to it.value }
            AppSettings(
                carryForwardEnabled = settingsMap["carry_forward_enabled"]?.toBoolean() ?: false,
                currencySymbol = settingsMap["currency_symbol"] ?: "$",
                dateFormat = settingsMap["date_format"] ?: "MMM dd, yyyy"
            )
        }
    }
    
    suspend fun updateCarryForwardEnabled(enabled: Boolean) {
        updateSetting("carry_forward_enabled", if (enabled) "1" else "0")
    }
    
    suspend fun updateCurrencySymbol(symbol: String) {
        updateSetting("currency_symbol", symbol)
    }
    
    suspend fun updateDateFormat(format: String) {
        updateSetting("date_format", format)
    }
}

@Composable
fun rememberSQLiteSettingsDatabase(): SQLiteSettingsDatabase {
    val driverFactory = rememberDatabaseDriverFactory()
    val database = remember { CategoryDatabase(driverFactory.createDriver()) }
    return remember { SQLiteSettingsDatabase(database) }
}
