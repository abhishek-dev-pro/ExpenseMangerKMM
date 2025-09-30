package com.example.androidkmm.utils

import kotlin.random.Random

actual object PlatformTime {
    private var counter = 0L
    
    actual fun currentTimeMillis(): Long {
        // Use a counter-based approach for unique IDs on iOS
        counter++
        return 1704067200000L + counter
    }
}
