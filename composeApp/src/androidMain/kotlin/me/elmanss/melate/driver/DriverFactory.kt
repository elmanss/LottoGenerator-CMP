package me.elmanss.melate.driver

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import me.elmanss.melate.Database

actual class DriverFactory(private val context: Context) {
  actual fun createDriver(): SqlDriver {
    return AndroidSqliteDriver(schema = Database.Schema, context = context, name = "favoritos.db")
  }
}
