package com.example.androidkmm.database

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.example.androidkmm.database.DatabaseDriverFactory
import com.example.androidkmm.database.CategoryDatabase

@Composable
fun rememberSQLiteGroupDatabase(): SQLiteGroupDatabase {
    val driverFactory = rememberDatabaseDriverFactory()
    val database = remember { CategoryDatabase(driverFactory.createDriver()) }
    return remember { SQLiteGroupDatabase(database) }
}
