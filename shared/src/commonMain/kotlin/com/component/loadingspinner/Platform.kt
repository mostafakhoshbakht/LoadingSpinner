package com.component.loadingspinner

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform