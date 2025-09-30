package com.example.androidkmm.utils

import platform.Foundation.NSDate

actual object PlatformTime {
    actual fun currentTimeMillis(): Long {
        // Simplified implementation for iOS compatibility
        return 1704067200000L // Placeholder timestamp
    }
}
