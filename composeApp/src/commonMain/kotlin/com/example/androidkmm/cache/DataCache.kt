package com.example.androidkmm.cache

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeSource

/**
 * Generic cache entry with expiration support
 */
data class CacheEntry<T>(
    val data: T,
    val timestamp: Long,
    val expirationTime: Long
) {
    fun isExpired(): Boolean = TimeSource.Monotonic.markNow().elapsedNow().inWholeMilliseconds > expirationTime
}

/**
 * Generic data cache with expiration and size limits
 */
@OptIn(ExperimentalTime::class)
open class DataCache<T>(
    protected val maxSize: Int = 100,
    protected val defaultExpiration: Duration = Duration.parse("1h")
) {
    protected val cache = mutableMapOf<String, CacheEntry<T>>()
    private val mutex = Mutex()
    private val _cacheState = MutableStateFlow<Map<String, T>>(emptyMap())
    val cacheState: StateFlow<Map<String, T>> = _cacheState.asStateFlow()
    
    /**
     * Get data from cache
     */
    suspend fun get(key: String): T? = mutex.withLock {
        val entry = cache[key]
        return when {
            entry == null -> null
            entry.isExpired() -> {
                cache.remove(key)
                updateCacheState()
                null
            }
            else -> entry.data
        }
    }
    
    /**
     * Put data into cache
     */
    suspend fun put(key: String, data: T, expiration: Duration = defaultExpiration) = mutex.withLock {
        val now = TimeSource.Monotonic.markNow().elapsedNow().inWholeMilliseconds
        val expirationTime = now + expiration.inWholeMilliseconds
        
        cache[key] = CacheEntry(data, now, expirationTime)
        
        // Remove oldest entries if cache is full
        if (cache.size > maxSize) {
            val oldestKey = cache.minByOrNull { it.value.timestamp }?.key
            oldestKey?.let { cache.remove(it) }
        }
        
        updateCacheState()
    }
    
    /**
     * Remove data from cache
     */
    suspend fun remove(key: String) = mutex.withLock {
        cache.remove(key)
        updateCacheState()
    }
    
    /**
     * Clear all cache
     */
    suspend fun clear() = mutex.withLock {
        cache.clear()
        updateCacheState()
    }
    
    /**
     * Check if key exists in cache
     */
    suspend fun contains(key: String): Boolean = mutex.withLock {
        val entry = cache[key]
        return when {
            entry == null -> false
            entry.isExpired() -> {
                cache.remove(key)
                updateCacheState()
                false
            }
            else -> true
        }
    }
    
    /**
     * Get cache size
     */
    suspend fun size(): Int = mutex.withLock {
        cache.size
    }
    
    /**
     * Clean expired entries
     */
    suspend fun cleanExpired() = mutex.withLock {
        val expiredKeys = cache.filter { it.value.isExpired() }.keys
        expiredKeys.forEach { cache.remove(it) }
        updateCacheState()
    }
    
    /**
     * Get all valid (non-expired) entries
     */
    suspend fun getAllValid(): Map<String, T> = mutex.withLock {
        cache.filter { !it.value.isExpired() }.mapValues { it.value.data }
    }
    
    /**
     * Update cache state flow
     */
    private fun updateCacheState() {
        _cacheState.value = cache.filter { !it.value.isExpired() }.mapValues { it.value.data }
    }
}

/**
 * Specialized cache for database queries
 */
class DatabaseCache<T>(
    maxSize: Int = 50,
    defaultExpiration: Duration = Duration.parse("30m")
) : DataCache<T>(maxSize, defaultExpiration) {
    
    /**
     * Get or fetch data with fallback
     */
    suspend fun getOrFetch(
        key: String,
        fetch: suspend () -> T,
        expiration: Duration = defaultExpiration
    ): T {
        return get(key) ?: run {
            val data = fetch()
            put(key, data, expiration)
            data
        }
    }
    
    /**
     * Invalidate cache for specific pattern
     */
    suspend fun invalidatePattern(pattern: String) {
        val keysToRemove = cache.keys.filter { it.contains(pattern) }
        keysToRemove.forEach { remove(it) }
    }
}

/**
 * Cache manager for different data types
 */
class CacheManager {
    private val transactionCache = DatabaseCache<Any>(maxSize = 100, defaultExpiration = Duration.parse("15m"))
    private val accountCache = DatabaseCache<Any>(maxSize = 50, defaultExpiration = Duration.parse("1h"))
    private val categoryCache = DatabaseCache<Any>(maxSize = 30, defaultExpiration = Duration.parse("2h"))
    private val groupCache = DatabaseCache<Any>(maxSize = 30, defaultExpiration = Duration.parse("1h"))
    private val ledgerCache = DatabaseCache<Any>(maxSize = 50, defaultExpiration = Duration.parse("30m"))
    private val settingsCache = DatabaseCache<Any>(maxSize = 10, defaultExpiration = Duration.parse("6h"))
    
    /**
     * Get transaction cache
     */
    fun getTransactionCache(): DatabaseCache<Any> = transactionCache
    
    /**
     * Get account cache
     */
    fun getAccountCache(): DatabaseCache<Any> = accountCache
    
    /**
     * Get category cache
     */
    fun getCategoryCache(): DatabaseCache<Any> = categoryCache
    
    /**
     * Get group cache
     */
    fun getGroupCache(): DatabaseCache<Any> = groupCache
    
    /**
     * Get ledger cache
     */
    fun getLedgerCache(): DatabaseCache<Any> = ledgerCache
    
    /**
     * Get settings cache
     */
    fun getSettingsCache(): DatabaseCache<Any> = settingsCache
    
    /**
     * Clear all caches
     */
    suspend fun clearAllCaches() {
        transactionCache.clear()
        accountCache.clear()
        categoryCache.clear()
        groupCache.clear()
        ledgerCache.clear()
        settingsCache.clear()
    }
    
    /**
     * Clean expired entries from all caches
     */
    suspend fun cleanAllExpired() {
        transactionCache.cleanExpired()
        accountCache.cleanExpired()
        categoryCache.cleanExpired()
        groupCache.cleanExpired()
        ledgerCache.cleanExpired()
        settingsCache.cleanExpired()
    }
    
    /**
     * Get cache statistics
     */
    suspend fun getCacheStats(): Map<String, Int> {
        return mapOf(
            "transactions" to transactionCache.size(),
            "accounts" to accountCache.size(),
            "categories" to categoryCache.size(),
            "groups" to groupCache.size(),
            "ledger" to ledgerCache.size(),
            "settings" to settingsCache.size()
        )
    }
}

/**
 * Cache key generator for consistent key naming
 */
object CacheKeys {
    private const val PREFIX = "money_mate"
    
    // Transaction keys
    fun allTransactions() = "$PREFIX:transactions:all"
    fun transactionById(id: String) = "$PREFIX:transactions:id:$id"
    fun transactionsByDateRange(start: String, end: String) = "$PREFIX:transactions:date:$start:$end"
    fun transactionsByCategory(category: String) = "$PREFIX:transactions:category:$category"
    fun transactionsByAccount(account: String) = "$PREFIX:transactions:account:$account"
    
    // Account keys
    fun allAccounts() = "$PREFIX:accounts:all"
    fun accountById(id: String) = "$PREFIX:accounts:id:$id"
    fun accountByName(name: String) = "$PREFIX:accounts:name:$name"
    
    // Category keys
    fun allCategories() = "$PREFIX:categories:all"
    fun categoryById(id: String) = "$PREFIX:categories:id:$id"
    fun categoryByName(name: String) = "$PREFIX:categories:name:$name"
    
    // Group keys
    fun allGroups() = "$PREFIX:groups:all"
    fun groupById(id: String) = "$PREFIX:groups:id:$id"
    fun groupByName(name: String) = "$PREFIX:groups:name:$name"
    
    // Ledger keys
    fun allLedgerPersons() = "$PREFIX:ledger:persons:all"
    fun ledgerPersonById(id: String) = "$PREFIX:ledger:persons:id:$id"
    fun ledgerPersonByName(name: String) = "$PREFIX:ledger:persons:name:$name"
    fun ledgerTransactionsByPerson(personId: String) = "$PREFIX:ledger:transactions:person:$personId"
    
    // Settings keys
    fun appSettings() = "$PREFIX:settings:app"
    fun userSettings() = "$PREFIX:settings:user"
    
    // Balance keys
    fun totalBalance() = "$PREFIX:balance:total"
    fun accountBalance(accountId: String) = "$PREFIX:balance:account:$accountId"
    fun ledgerBalance() = "$PREFIX:balance:ledger"
    
    // Insights keys
    fun monthlyInsights(year: Int, month: Int) = "$PREFIX:insights:monthly:$year:$month"
    fun categoryInsights(year: Int, month: Int) = "$PREFIX:insights:categories:$year:$month"
    fun spendingInsights(year: Int, month: Int) = "$PREFIX:insights:spending:$year:$month"
}

/**
 * Cache configuration
 */
object CacheConfig {
    const val DEFAULT_MAX_SIZE = 100
    val DEFAULT_EXPIRATION = Duration.parse("1h")
    val SHORT_EXPIRATION = Duration.parse("15m")
    val LONG_EXPIRATION = Duration.parse("6h")
    
    // Cache sizes for different data types
    const val TRANSACTION_CACHE_SIZE = 100
    const val ACCOUNT_CACHE_SIZE = 50
    const val CATEGORY_CACHE_SIZE = 30
    const val GROUP_CACHE_SIZE = 30
    const val LEDGER_CACHE_SIZE = 50
    const val SETTINGS_CACHE_SIZE = 10
    
    // Cache expiration times
    val TRANSACTION_CACHE_EXPIRATION = Duration.parse("15m")
    val ACCOUNT_CACHE_EXPIRATION = Duration.parse("1h")
    val CATEGORY_CACHE_EXPIRATION = Duration.parse("2h")
    val GROUP_CACHE_EXPIRATION = Duration.parse("1h")
    val LEDGER_CACHE_EXPIRATION = Duration.parse("30m")
    val SETTINGS_CACHE_EXPIRATION = Duration.parse("6h")
}

/**
 * Cache utilities
 */
object CacheUtils {
    /**
     * Generate cache key with parameters
     */
    fun generateKey(prefix: String, vararg params: Any): String {
        return "$prefix:${params.joinToString(":")}"
    }
    
    /**
     * Check if cache key is valid
     */
    fun isValidKey(key: String): Boolean {
        return key.isNotBlank() && key.length <= 255
    }
    
    /**
     * Sanitize cache key
     */
    fun sanitizeKey(key: String): String {
        return key.replace(Regex("[^a-zA-Z0-9:_-]"), "_")
    }
    
    /**
     * Get cache key for date range
     */
    fun getDateRangeKey(prefix: String, startDate: String, endDate: String): String {
        return generateKey(prefix, startDate, endDate)
    }
    
    /**
     * Get cache key for pagination
     */
    fun getPaginationKey(prefix: String, page: Int, pageSize: Int): String {
        return generateKey(prefix, page, pageSize)
    }
    
    /**
     * Get cache key for filtering
     */
    fun getFilterKey(prefix: String, filters: Map<String, Any>): String {
        val sortedFilters = filters.toSortedMap()
        val filterString = sortedFilters.entries.joinToString(":") { "${it.key}=${it.value}" }
        return generateKey(prefix, filterString)
    }
}

/**
 * Cache statistics
 */
data class CacheStats(
    val totalEntries: Int,
    val expiredEntries: Int,
    val hitRate: Double,
    val missRate: Double,
    val averageAccessTime: Long,
    val memoryUsage: Long
)

/**
 * Cache performance monitor
 */
class CachePerformanceMonitor {
    private var hits = 0L
    private var misses = 0L
    private var totalAccessTime = 0L
    private var accessCount = 0L
    
    fun recordHit(accessTime: Long) {
        hits++
        totalAccessTime += accessTime
        accessCount++
    }
    
    fun recordMiss(accessTime: Long) {
        misses++
        totalAccessTime += accessTime
        accessCount++
    }
    
    fun getStats(): CacheStats {
        val totalRequests = hits + misses
        val hitRate = if (totalRequests > 0) hits.toDouble() / totalRequests else 0.0
        val missRate = if (totalRequests > 0) misses.toDouble() / totalRequests else 0.0
        val averageAccessTime = if (accessCount > 0) totalAccessTime / accessCount else 0L
        
        return CacheStats(
            totalEntries = (hits + misses).toInt(),
            expiredEntries = 0, // This would need to be tracked separately
            hitRate = hitRate,
            missRate = missRate,
            averageAccessTime = averageAccessTime,
            memoryUsage = 0L // This would need to be calculated based on actual memory usage
        )
    }
    
    fun reset() {
        hits = 0L
        misses = 0L
        totalAccessTime = 0L
        accessCount = 0L
    }
}
