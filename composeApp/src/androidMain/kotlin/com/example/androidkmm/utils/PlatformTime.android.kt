package com.example.androidkmm.utils

actual object PlatformTime {
    actual fun currentTimeMillis(): Long {
        return System.currentTimeMillis()
    }
}

