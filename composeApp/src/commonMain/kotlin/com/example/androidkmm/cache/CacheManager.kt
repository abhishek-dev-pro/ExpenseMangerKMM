package com.example.androidkmm.cache

import com.example.androidkmm.utils.Logger
import com.example.androidkmm.utils.TimeUtils
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Duration
import kotlin.time.TimeSource

/**
 * Comprehensive cache management system
 * 
 * Features:
 * - In-memory caching with TTL
 * - Cache size limits
 * - Cache eviction policies
 * - Thread-safe operations
 * - Cache statistics
 * - Cache warming
 * - Cache invalidation
 */
class CacheManager<T> {
    
    private val cache = mutableMapOf<String, CacheEntry<T>>()
    private val mutex = Mutex()
    
    // Cache configuration
    private var maxSize: Int = 1000
    private var defaultTtl: Duration = Duration.parse("1h")
    private var evictionPolicy: EvictionPolicy = EvictionPolicy.LRU
    
    // Cache statistics
    private var hits = 0L
    private var misses = 0L
    private var evictions = 0L
    
    /**
     * Cache entry with metadata
     */
    private data class CacheEntry<T>(
        val value: T,
        val createdAt: Long,
        val lastAccessed: Long,
        val ttl: Duration,
        val accessCount: Long = 0
    ) {
        fun isExpired(): Boolean {
            return (TimeUtils.currentTimeMillis() - createdAt) > ttl.inWholeMilliseconds
        }
        
        fun updateAccess(): CacheEntry<T> {
            return copy(
                lastAccessed = TimeUtils.currentTimeMillis(),
                accessCount = accessCount + 1
            )
        }
    }
    
    /**
     * Cache eviction policies
     */
    enum class EvictionPolicy {
        LRU, // Least Recently Used
        LFU, // Least Frequently Used
        FIFO, // First In First Out
        TTL // Time To Live
    }
    
    /**
     * Cache statistics
     */
    data class CacheStats(
        val size: Int,
        val maxSize: Int,
        val hits: Long,
        val misses: Long,
        val evictions: Long,
        val hitRate: Double
    )
    
    /**
     * Configure cache settings
     */
    fun configure(
        maxSize: Int = 1000,
        defaultTtl: Duration = Duration.parse("1h"),
        evictionPolicy: EvictionPolicy = EvictionPolicy.LRU
    ) {
        this.maxSize = maxSize
        this.defaultTtl = defaultTtl
        this.evictionPolicy = evictionPolicy
    }
    
    /**
     * Get value from cache
     */
    suspend fun get(key: String): T? {
        return mutex.withLock {
            val entry = cache[key]
            
            when {
                entry == null -> {
                    misses++
                    null
                }
                entry.isExpired() -> {
                    cache.remove(key)
                    misses++
                    null
                }
                else -> {
                    hits++
                    val updatedEntry = entry.updateAccess()
                    cache[key] = updatedEntry
                    updatedEntry.value
                }
            }
        }
    }
    
    /**
     * Put value in cache
     */
    suspend fun put(key: String, value: T, ttl: Duration? = null) {
        mutex.withLock {
            val entry = CacheEntry(
                value = value,
                createdAt = TimeUtils.currentTimeMillis(),
                lastAccessed = TimeUtils.currentTimeMillis(),
                ttl = ttl ?: defaultTtl,
                accessCount = 0
            )
            
            cache[key] = entry
            
            // Evict if cache is full
            if (cache.size > maxSize) {
                evict()
            }
        }
    }
    
    /**
     * Remove value from cache
     */
    suspend fun remove(key: String): T? {
        return mutex.withLock {
            cache.remove(key)?.value
        }
    }
    
    /**
     * Clear all cache
     */
    suspend fun clear() {
        mutex.withLock {
            cache.clear()
        }
    }
    
    /**
     * Check if key exists in cache
     */
    suspend fun contains(key: String): Boolean {
        return mutex.withLock {
            val entry = cache[key]
            entry != null && !entry.isExpired()
        }
    }
    
    /**
     * Get cache size
     */
    suspend fun size(): Int {
        return mutex.withLock {
            cache.size
        }
    }
    
    /**
     * Get cache statistics
     */
    suspend fun getStats(): CacheStats {
        return mutex.withLock {
            val totalRequests = hits + misses
            val hitRate = if (totalRequests > 0) hits.toDouble() / totalRequests else 0.0
            
            CacheStats(
                size = cache.size,
                maxSize = maxSize,
                hits = hits,
                misses = misses,
                evictions = evictions,
                hitRate = hitRate
            )
        }
    }
    
    /**
     * Evict entries based on policy
     */
    private suspend fun evict() {
        when (evictionPolicy) {
            EvictionPolicy.LRU -> evictLRU()
            EvictionPolicy.LFU -> evictLFU()
            EvictionPolicy.FIFO -> evictFIFO()
            EvictionPolicy.TTL -> evictTTL()
        }
    }
    
    /**
     * Evict least recently used entry
     */
    private suspend fun evictLRU() {
        val oldestEntry = cache.minByOrNull { it.value.lastAccessed }
        oldestEntry?.let {
            cache.remove(it.key)
            evictions++
        }
    }
    
    /**
     * Evict least frequently used entry
     */
    private suspend fun evictLFU() {
        val leastFrequentEntry = cache.minByOrNull { it.value.accessCount }
        leastFrequentEntry?.let {
            cache.remove(it.key)
            evictions++
        }
    }
    
    /**
     * Evict first in first out entry
     */
    private suspend fun evictFIFO() {
        val oldestEntry = cache.minByOrNull { it.value.createdAt }
        oldestEntry?.let {
            cache.remove(it.key)
            evictions++
        }
    }
    
    /**
     * Evict expired entries
     */
    private suspend fun evictTTL() {
        val expiredKeys = cache.filter { it.value.isExpired() }.keys
        expiredKeys.forEach { key ->
            cache.remove(key)
            evictions++
        }
    }
    
    /**
     * Clean expired entries
     */
    suspend fun cleanExpired() {
        mutex.withLock {
            val expiredKeys = cache.filter { it.value.isExpired() }.keys
            expiredKeys.forEach { key ->
                cache.remove(key)
            }
        }
    }
    
    /**
     * Warm cache with data
     */
    suspend fun warmCache(data: Map<String, T>, ttl: Duration? = null) {
        data.forEach { (key, value) ->
            put(key, value, ttl)
        }
    }
    
    /**
     * Invalidate cache by pattern
     */
    suspend fun invalidatePattern(pattern: Regex) {
        mutex.withLock {
            val keysToRemove = cache.keys.filter { pattern.matches(it) }
            keysToRemove.forEach { key ->
                cache.remove(key)
            }
        }
    }
    
    /**
     * Get all cache keys
     */
    suspend fun getKeys(): Set<String> {
        return mutex.withLock {
            cache.keys.toSet()
        }
    }
    
    /**
     * Get cache entry metadata
     */
    suspend fun getEntryMetadata(key: String): CacheEntryMetadata? {
        return mutex.withLock {
            val entry = cache[key]
            if (entry != null && !entry.isExpired()) {
                CacheEntryMetadata(
                    key = key,
                    createdAt = entry.createdAt,
                    lastAccessed = entry.lastAccessed,
                    ttl = entry.ttl,
                    accessCount = entry.accessCount,
                    isExpired = entry.isExpired()
                )
            } else null
        }
    }
    
    /**
     * Cache entry metadata
     */
    data class CacheEntryMetadata(
        val key: String,
        val createdAt: Long,
        val lastAccessed: Long,
        val ttl: Duration,
        val accessCount: Long,
        val isExpired: Boolean
    )
}
