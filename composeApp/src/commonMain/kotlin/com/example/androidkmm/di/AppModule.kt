package com.example.androidkmm.di

import com.example.androidkmm.database.SQLiteTransactionDatabase
import com.example.androidkmm.database.SQLiteAccountDatabase
import com.example.androidkmm.database.SQLiteCategoryDatabase
import com.example.androidkmm.database.SQLiteGroupDatabase
import com.example.androidkmm.database.SQLiteSettingsDatabase
import com.example.androidkmm.repository.TransactionRepository
import com.example.androidkmm.repository.impl.SQLiteTransactionRepository
import com.example.androidkmm.viewmodel.TransactionViewModel

/**
 * Dependency Injection module for the application
 * 
 * Provides centralized dependency management.
 * Implements dependency injection for better testability and maintainability.
 */
object AppModule {
    
    // Database managers
    fun getTransactionDatabase(): SQLiteTransactionDatabase {
        // TODO: Implement proper dependency injection
        throw NotImplementedError("Dependency injection not implemented yet")
    }
    
    fun getAccountDatabase(): SQLiteAccountDatabase {
        // TODO: Implement proper dependency injection
        throw NotImplementedError("Dependency injection not implemented yet")
    }
    
    fun getCategoryDatabase(): SQLiteCategoryDatabase {
        // TODO: Implement proper dependency injection
        throw NotImplementedError("Dependency injection not implemented yet")
    }
    
    fun getGroupDatabase(): SQLiteGroupDatabase {
        // TODO: Implement proper dependency injection
        throw NotImplementedError("Dependency injection not implemented yet")
    }
    
    fun getSettingsDatabase(): SQLiteSettingsDatabase {
        // TODO: Implement proper dependency injection
        throw NotImplementedError("Dependency injection not implemented yet")
    }
    
    // Repositories
    fun getTransactionRepository(): TransactionRepository {
        // TODO: Implement proper dependency injection
        throw NotImplementedError("Dependency injection not implemented yet")
    }
    
    // ViewModels
    fun getTransactionViewModel(): TransactionViewModel {
        // TODO: Implement proper dependency injection
        throw NotImplementedError("Dependency injection not implemented yet")
    }
}
