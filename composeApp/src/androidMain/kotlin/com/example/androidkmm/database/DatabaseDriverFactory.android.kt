package com.example.androidkmm.database

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(CategoryDatabase.Schema, context, "app_database_v13.db")
    }
}

@Composable
actual fun rememberDatabaseDriverFactory(): DatabaseDriverFactory {
    val context = LocalContext.current
    return remember { DatabaseDriverFactory(context) }
}

actual fun parseColorHex(hexString: String): Color {
    return Color(android.graphics.Color.parseColor(hexString))
}
