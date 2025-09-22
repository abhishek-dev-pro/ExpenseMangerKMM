package com.example.androidkmm.repository

import com.example.androidkmm.models.Transaction
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for transaction operations
 * 
 * Provides a clean abstraction layer between UI and data sources.
 * Implements the Repository pattern for better testability and maintainability.
 */
interface TransactionRepository {
    
    /**
     * Get all transactions as a Flow
     */
    fun getAllTransactions(): Flow<List<Transaction>>
    
    /**
     * Get transaction by ID
     */
    suspend fun getTransactionById(id: String): Transaction?
    
    /**
     * Add a new transaction
     */
    suspend fun addTransaction(transaction: Transaction): Result<Unit>
    
    /**
     * Update an existing transaction
     */
    suspend fun updateTransaction(transaction: Transaction): Result<Unit>
    
    /**
     * Delete a transaction
     */
    suspend fun deleteTransaction(id: String): Result<Unit>
    
    /**
     * Add multiple transactions in batch
     */
    suspend fun addTransactionsBatch(transactions: List<Transaction>): Result<Unit>
    
    /**
     * Get transactions by date range
     */
    suspend fun getTransactionsByDateRange(startDate: String, endDate: String): Flow<List<Transaction>>
    
    /**
     * Get transactions by type
     */
    suspend fun getTransactionsByType(type: com.example.androidkmm.models.TransactionType): Flow<List<Transaction>>
    
    /**
     * Get transactions by account
     */
    suspend fun getTransactionsByAccount(accountName: String): Flow<List<Transaction>>
    
    /**
     * Search transactions by title or description
     */
    suspend fun searchTransactions(query: String): Flow<List<Transaction>>
    
    /**
     * Get transaction count
     */
    suspend fun getTransactionCount(): Int
    
    /**
     * Clear all transactions
     */
    suspend fun clearAllTransactions(): Result<Unit>
}
