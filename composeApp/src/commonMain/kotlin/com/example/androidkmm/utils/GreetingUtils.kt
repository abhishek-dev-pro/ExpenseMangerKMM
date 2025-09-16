package com.example.androidkmm.utils

import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

object GreetingUtils {
    
    /**
     * Get time-based greeting based on current hour
     */
    @OptIn(kotlin.time.ExperimentalTime::class)
    fun getTimeBasedGreeting(): String {
        val currentTime = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        val hour = currentTime.hour
        
        return when (hour) {
            in 5..11 -> "Good morning"
            in 12..17 -> "Good afternoon"
            in 18..21 -> "Good evening"
            else -> "Good night"
        }
    }
}
