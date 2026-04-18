package com.yediaz.jefac.core

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
