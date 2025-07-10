package me.elmanss.melate

import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import me.elmanss.melate.common.di.initializeKoin
import me.elmanss.melate.common.util.NetworkConnectivityObserver
import me.elmanss.melate.driver.DriverFactory

fun main() = application {
  initializeKoin(
      DriverFactory().createDriver(), connectivityObserver = NetworkConnectivityObserver())
  Window(
      onCloseRequest = ::exitApplication,
      title = "Melate CMP",
      state =
          rememberWindowState(
              position = WindowPosition.Aligned(Alignment.Center),
              height = 800.dp,
              width = 1280.dp),
  ) {
    App()
  }
}
