package com.example.androidkmm.repository.impl

import com.example.androidkmm.models.Transaction
import com.example.androidkmm.models.TransactionType
import com.example.androidkmm.repository.TransactionRepository
import com.example.androidkmm.cache.CacheManager
import com.example.androidkmm.utils.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.time.Duration.Companion.hours

/**
 * Cached implementation of TransactionRepository
 * 
 * Provides caching layer for transaction operations to improve performance.
 * Implements cache-aside pattern with TTL and eviction policies.
 */
class CachedTransactionRepository(
    private val delegate: TransactionRepository,
    private val cacheManager: CacheManager<List<Transaction>>
) : TransactionRepository {
    
    companion object {
        private const val ALL_TRANSACTIONS_KEY = "all_transactions"
        private const val TRANSACTION_BY_ID_PREFIX = "transaction_"
        private const val TRANSACTIONS_BY_TYPE_PREFIX = "transactions_type_"
        private const val TRANSACTIONS_BY_ACCOUNT_PREFIX = "transactions_account_"
        private const val SEARCH_TRANSACTIONS_PREFIX = "search_"
    }
    
    init {
        // Configure cache
        cacheManager.configure(
            maxSize = 1000,
            defaultTtl = 1.hours,
            evictionPolicy = CacheManager.EvictionPolicy.LRU
        )
    }
    
    override fun getAllTransactions(): Flow<List<Transaction>> {
        return flow {
            try {
                // Check cache first
                val cachedTransactions = cacheManager.get(ALL_TRANSACTIONS_KEY)
                if (cachedTransactions != null) {
                    Logger.debug("Cache hit for all transactions", "CachedTransactionRepository")
                    emit(cachedTransactions)
                    return@flow
                }
                
                // Cache miss, fetch from delegate
                Logger.debug("Cache miss for all transactions", "CachedTransactionRepository")
                delegate.getAllTransactions().collect { transactions ->
                    // Cache the result
                    cacheManager.put(ALL_TRANSACTIONS_KEY, transactions)
                    emit(transactions)
                }
            } catch (e: Exception) {
                Logger.error("Failed to get all transactions", "CachedTransactionRepository", e)
                throw e
            }
        }
    }
    
    override suspend fun getTransactionById(id: String): Transaction? {
        return try {
            val cacheKey = "$TRANSACTION_BY_ID_PREFIX$id"
            
            // Check cache first
            val cachedTransactions = cacheManager.get(cacheKey)
            if (cachedTransactions != null && cachedTransactions.isNotEmpty()) {
                Logger.debug("Cache hit for transaction $id", "CachedTransactionRepository")
                return cachedTransactions.first()
            }
            
            // Cache miss, fetch from delegate
            Logger.debug("Cache miss for transaction $id", "CachedTransactionRepository")
            val transaction = delegate.getTransactionById(id)
            
            // Cache the result if found
            if (transaction != null) {
                cacheManager.put(cacheKey, listOf(transaction))
            }
            
            transaction
        } catch (e: Exception) {
            Logger.error("Failed to get transaction by ID: $id", "CachedTransactionRepository", e)
            throw e
        }
    }
    
    override suspend fun addTransaction(transaction: Transaction): Result<Unit> {
        return try {
            val result = delegate.addTransaction(transaction)
            
            result.fold(
                onSuccess = {
                    // Invalidate relevant caches
                    invalidateTransactionCaches()
                    Logger.debug("Transaction added and caches invalidated", "CachedTransactionRepository")
                },
                onFailure = { /* Error handling is done by delegate */ }
            )
            
            result
        } catch (e: Exception) {
            Logger.error("Failed to add transaction", "CachedTransactionRepository", e)
            Result.failure(e)
        }
    }
    
    override suspend fun updateTransaction(transaction: Transaction): Result<Unit> {
        return try {
            val result = delegate.updateTransaction(transaction)
            
            result.fold(
                onSuccess = {
                    // Invalidate relevant caches
                    invalidateTransactionCaches()
                    Logger.debug("Transaction updated and caches invalidated", "CachedTransactionRepository")
                },
                onFailure = { /* Error handling is done by delegate */ }
            )
            
            result
        } catch (e: Exception) {
            Logger.error("Failed to update transaction", "CachedTransactionRepository")
            Result.failure(e)
        }
    }
    
    override suspend fun deleteTransaction(id: String): Result<Unit> {
        return try {
            val result = delegate.deleteTransaction(id)
            
            result.fold(
                onSuccess = {
                    // Invalidate relevant caches
                    invalidateTransactionCaches()
                    Logger.debug("Transaction deleted and caches invalidated", "CachedTransactionRepository")
                },
                onFailure = { /* Error handling is done by delegate */ }
            )
            
            result
        } catch (e: Exception) {
            Logger.error("Failed to delete transaction", "CachedTransactionRepository", e)
            Result.failure(e)
        }
    }
    
    override suspend fun addTransactionsBatch(transactions: List<Transaction>): Result<Unit> {
        return try {
            val result = delegate.addTransactionsBatch(transactions)
            
            result.fold(
                onSuccess = {
                    // Invalidate relevant caches
                    invalidateTransactionCaches()
                    Logger.debug("Batch transactions added and caches invalidated", "CachedTransactionRepository")
                },
                onFailure = { /* Error handling is done by delegate */ }
            )
            
            result
        } catch (e: Exception) {
            Logger.error("Failed to add batch transactions", "CachedTransactionRepository", e)
            Result.failure(e)
        }
    }
    
    override suspend fun getTransactionsByDateRange(startDate: String, endDate: String): Flow<List<Transaction>> {
        return flow {
            try {
                val cacheKey = "transactions_date_${startDate}_${endDate}"
                
                // Check cache first
                val cachedTransactions = cacheManager.get(cacheKey)
                if (cachedTransactions != null) {
                    Logger.debug("Cache hit for transactions by date range", "CachedTransactionRepository")
                    emit(cachedTransactions)
                    return@flow
                }
                
                // Cache miss, fetch from delegate
                Logger.debug("Cache miss for transactions by date range", "CachedTransactionRepository")
                delegate.getTransactionsByDateRange(startDate, endDate).collect { transactions ->
                    // Cache the result
                    cacheManager.put(cacheKey, transactions)
                    emit(transactions)
                }
            } catch (e: Exception) {
                Logger.error("Failed to get transactions by date range", "CachedTransactionRepository", e)
                throw e
            }
        }
    }
    
    override suspend fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>> {
        return flow {
            try {
                val cacheKey = "$TRANSACTIONS_BY_TYPE_PREFIX${type.name}"
                
                // Check cache first
                val cachedTransactions = cacheManager.get(cacheKey)
                if (cachedTransactions != null) {
                    Logger.debug("Cache hit for transactions by type: ${type.name}", "CachedTransactionRepository")
                    emit(cachedTransactions)
                    return@flow
                }
                
                // Cache miss, fetch from delegate
                Logger.debug("Cache miss for transactions by type: ${type.name}", "CachedTransactionRepository")
                delegate.getTransactionsByType(type).collect { transactions ->
                    // Cache the result
                    cacheManager.put(cacheKey, transactions)
                    emit(transactions)
                }
            } catch (e: Exception) {
                Logger.error("Failed to get transactions by type: ${type.name}", "CachedTransactionRepository", e)
                throw e
            }
        }
    }
    
    override suspend fun getTransactionsByAccount(accountName: String): Flow<List<Transaction>> {
        return flow {
            try {
                val cacheKey = "$TRANSACTIONS_BY_ACCOUNT_PREFIX$accountName"
                
                // Check cache first
                val cachedTransactions = cacheManager.get(cacheKey)
                if (cachedTransactions != null) {
                    Logger.debug("Cache hit for transactions by account: $accountName", "CachedTransactionRepository")
                    emit(cachedTransactions)
                    return@flow
                }
                
                // Cache miss, fetch from delegate
                Logger.debug("Cache miss for transactions by account: $accountName", "CachedTransactionRepository")
                delegate.getTransactionsByAccount(accountName).collect { transactions ->
                    // Cache the result
                    cacheManager.put(cacheKey, transactions)
                    emit(transactions)
                }
            } catch (e: Exception) {
                Logger.error("Failed to get transactions by account: $accountName", "CachedTransactionRepository", e)
                throw e
            }
        }
    }
    
    override suspend fun searchTransactions(query: String): Flow<List<Transaction>> {
        return flow {
            try {
                val cacheKey = "$SEARCH_TRANSACTIONS_PREFIX$query"
                
                // Check cache first
                val cachedTransactions = cacheManager.get(cacheKey)
                if (cachedTransactions != null) {
                    Logger.debug("Cache hit for search transactions: $query", "CachedTransactionRepository")
                    emit(cachedTransactions)
                    return@flow
                }
                
                // Cache miss, fetch from delegate
                Logger.debug("Cache miss for search transactions: $query", "CachedTransactionRepository")
                delegate.searchTransactions(query).collect { transactions ->
                    // Cache the result
                    cacheManager.put(cacheKey, transactions)
                    emit(transactions)
                }
            } catch (e: Exception) {
                Logger.error("Failed to search transactions: $query", "CachedTransactionRepository", e)
                throw e
            }
        }
    }
    
    override suspend fun getTransactionCount(): Int {
        return try {
            // This could be cached separately if needed
            delegate.getTransactionCount()
        } catch (e: Exception) {
            Logger.error("Failed to get transaction count", "CachedTransactionRepository", e)
            throw e
        }
    }
    
    override suspend fun clearAllTransactions(): Result<Unit> {
        return try {
            val result = delegate.clearAllTransactions()
            
            result.fold(
                onSuccess = {
                    // Clear all caches
                    cacheManager.clear()
                    Logger.debug("All transactions cleared and caches cleared", "CachedTransactionRepository")
                },
                onFailure = { /* Error handling is done by delegate */ }
            )
            
            result
        } catch (e: Exception) {
            Logger.error("Failed to clear all transactions", "CachedTransactionRepository", e)
            Result.failure(e)
        }
    }
    
    /**
     * Invalidate all transaction-related caches
     */
    private suspend fun invalidateTransactionCaches() {
        try {
            // Remove all transaction-related cache entries
            val keys = cacheManager.getKeys()
            keys.forEach { key ->
                if (key.startsWith(TRANSACTION_BY_ID_PREFIX) ||
                    key.startsWith(TRANSACTIONS_BY_TYPE_PREFIX) ||
                    key.startsWith(TRANSACTIONS_BY_ACCOUNT_PREFIX) ||
                    key.startsWith(SEARCH_TRANSACTIONS_PREFIX) ||
                    key == ALL_TRANSACTIONS_KEY) {
                    cacheManager.remove(key)
                }
            }
        } catch (e: Exception) {
            Logger.error("Failed to invalidate transaction caches", "CachedTransactionRepository", e)
        }
    }
    
    /**
     * Get cache statistics
     */
    suspend fun getCacheStats() = cacheManager.getStats()
    
    /**
     * Clear cache
     */
    suspend fun clearCache() = cacheManager.clear()
    
    /**
     * Clean expired cache entries
     */
    suspend fun cleanExpiredCache() = cacheManager.cleanExpired()
}
