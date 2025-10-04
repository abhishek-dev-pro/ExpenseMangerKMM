package com.example.androidkmm.utils

import kotlinx.datetime.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlin.time.ExperimentalTime

/**
 * Cross-platform date and time utilities using kotlinx-datetime
 * This replaces all Java time API usage for iOS compatibility
 */
@OptIn(ExperimentalTime::class)
object DateTimeUtils {
    
    // 1. Get current instant (timestamp)
    fun getCurrentInstant(): Instant {
        return Instant.fromEpochMilliseconds(TimeUtils.currentTimeMillis())
    }

    // 2. Get current local date and time
    fun getCurrentDateTime(): LocalDateTime {
        // Use platform-specific time and convert to LocalDateTime
        val currentMillis = TimeUtils.currentTimeMillis()
        val instant = Instant.fromEpochMilliseconds(currentMillis)
        return instant.toLocalDateTime(TimeZone.currentSystemDefault())
    }

    // 3. Get current date only
    fun getCurrentDate(): LocalDate {
        return getCurrentDateTime().date
    }

    // 4. Get current time only
    fun getCurrentTime(): LocalTime {
        return getCurrentDateTime().time
    }

    // 5. Format current time
    fun getFormattedCurrentTime(): String {
        val localDateTime = getCurrentDateTime()
        
        // Manual formatting (works cross-platform)
        return "${localDateTime.hour.toString().padStart(2, '0')}:" +
               "${localDateTime.minute.toString().padStart(2, '0')}:" +
               "${localDateTime.second.toString().padStart(2, '0')}"
    }

    // 6. Get timestamp in milliseconds
    fun getCurrentTimeMillis(): Long {
        return TimeUtils.currentTimeMillis()
    }

    // 7. Get different time components
    fun getTimeComponents(): TimeComponents {
        val now = getCurrentDateTime()
        
        return TimeComponents(
            hour = now.hour,          // 0-23
            minute = now.minute,      // 0-59
            second = now.second,      // 0-59
            day = now.dayOfMonth,     // 1-31
            month = now.monthNumber,   // 1-12
            year = now.year           // e.g., 2025
        )
    }

    // 8. Format date as string
    fun formatDate(date: LocalDate): String {
        return "${date.year}-${date.monthNumber.toString().padStart(2, '0')}-${date.dayOfMonth.toString().padStart(2, '0')}"
    }

    // 9. Format date time as string
    fun formatDateTime(dateTime: LocalDateTime): String {
        val date = formatDate(dateTime.date)
        val time = "${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}"
        return "$date $time"
    }

    // 10. Parse date from string
    fun parseDate(dateString: String): LocalDate? {
        return try {
            LocalDate.parse(dateString)
        } catch (e: Exception) {
            null
        }
    }

    // 11. Create date from components
    fun createDate(year: Int, month: Int, day: Int): LocalDate? {
        return try {
            LocalDate(year, month, day)
        } catch (e: Exception) {
            null
        }
    }

    // 12. Compare dates
    fun isDateAfter(date1: LocalDate, date2: LocalDate): Boolean {
        return date1 > date2
    }

    fun isDateBefore(date1: LocalDate, date2: LocalDate): Boolean {
        return date1 < date2
    }

    fun isDateEqual(date1: LocalDate, date2: LocalDate): Boolean {
        return date1 == date2
    }

    // 13. Get days between dates
    fun getDaysBetween(date1: LocalDate, date2: LocalDate): Int {
        return kotlin.math.abs((date1.toEpochDays() - date2.toEpochDays()).toInt())
    }

    // 14. Add days to date
    fun addDays(date: LocalDate, days: Int): LocalDate {
        return date.plus(DatePeriod(days = days))
    }

    // 15. Get start of day as instant
    fun getStartOfDay(date: LocalDate): Instant {
        return Instant.fromEpochMilliseconds(date.toEpochDays() * 24 * 60 * 60 * 1000L)
    }

    // 16. Get end of day as instant
    fun getEndOfDay(date: LocalDate): Instant {
        return Instant.fromEpochMilliseconds((date.toEpochDays() + 1) * 24 * 60 * 60 * 1000L)
    }

    // 17. Convert instant to local date
    fun instantToLocalDate(instant: Instant): LocalDate {
        return LocalDate.fromEpochDays(instant.toEpochMilliseconds() / (24 * 60 * 60 * 1000L))
    }

    // 18. Convert instant to local date time
    fun instantToLocalDateTime(instant: Instant): LocalDateTime {
        return instant.toLocalDateTime(TimeZone.currentSystemDefault())
    }

    // 19. Convert local date to instant
    fun localDateToInstant(date: LocalDate): Instant {
        return Instant.fromEpochMilliseconds(date.toEpochDays() * 24 * 60 * 60 * 1000L)
    }

    // 20. Get current timezone
    fun getCurrentTimeZone(): TimeZone {
        return TimeZone.currentSystemDefault()
    }
}

/**
 * Data class for time components
 */
data class TimeComponents(
    val hour: Int,
    val minute: Int,
    val second: Int,
    val day: Int,
    val month: Int,
    val year: Int
)
