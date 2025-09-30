package com.example.androidkmm.utils

import kotlin.time.TimeSource
import kotlinx.datetime.*

/**
 * Platform-agnostic time utilities
 */
expect object PlatformTime {
    fun currentTimeMillis(): Long
}

/**
 * Common time utilities using platform-agnostic approach
 */
object TimeUtils {
    private val timeSource = TimeSource.Monotonic

    fun currentTimeMillis(): Long {
        return PlatformTime.currentTimeMillis()
    }

    fun currentTimeSeconds(): Long {
        return currentTimeMillis() / 1000
    }
}

/**
 * Enhanced time utilities using kotlinx-datetime
 */
object EnhancedTimeUtils {
    fun currentTimeMillis(): Long {
        return DateTimeUtils.getCurrentTimeMillis()
    }

    fun currentTimeSeconds(): Long {
        return currentTimeMillis() / 1000
    }

    fun getCurrentDate(): LocalDate {
        return DateTimeUtils.getCurrentDate()
    }

    fun getCurrentDateTime(): LocalDateTime {
        return DateTimeUtils.getCurrentDateTime()
    }

    fun getCurrentTime(): LocalTime {
        return DateTimeUtils.getCurrentTime()
    }

    fun formatDate(date: LocalDate): String {
        return DateTimeUtils.formatDate(date)
    }

    fun formatDateTime(dateTime: LocalDateTime): String {
        return DateTimeUtils.formatDateTime(dateTime)
    }
}

