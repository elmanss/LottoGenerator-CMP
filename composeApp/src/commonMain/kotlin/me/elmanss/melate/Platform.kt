package me.elmanss.melate

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform