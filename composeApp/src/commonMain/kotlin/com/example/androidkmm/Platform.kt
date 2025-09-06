package com.example.androidkmm

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform