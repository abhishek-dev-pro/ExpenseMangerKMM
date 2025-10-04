package com.example.androidkmm.utils

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

actual object PlatformTime {
    actual fun currentTimeMillis(): Long {
        // Use NSDate for actual system time on iOS
        return (NSDate().timeIntervalSince1970 * 1000).toLong()
    }
}
