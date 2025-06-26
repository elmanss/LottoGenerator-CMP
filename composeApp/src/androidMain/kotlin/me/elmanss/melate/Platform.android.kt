package me.elmanss.melate

class AndroidPlatform : Platform {
  override val name: String = "Android"
}

actual fun getPlatform(): Platform = AndroidPlatform()
