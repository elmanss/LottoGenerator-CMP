package me.elmanss.melate.driver

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import me.elmanss.melate.Database
import java.util.Properties

actual class DriverFactory {
  actual fun createDriver(): SqlDriver {
    val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:favoritos.db", Properties(), Database.Schema)
    return driver
  }
}
