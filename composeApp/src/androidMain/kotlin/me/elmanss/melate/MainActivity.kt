package me.elmanss.melate

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import me.elmanss.melate.common.di.initializeKoin
import me.elmanss.melate.common.util.NetworkConnectivityObserver
import me.elmanss.melate.driver.DriverFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    initializeKoin(DriverFactory(this).createDriver(), NetworkConnectivityObserver(this))
    setContent {
      App()
      val darkTheme = isSystemInDarkTheme()
      val view = LocalView.current
      if (!view.isInEditMode) {
        SideEffect {
          val window = this.window
          window.statusBarColor = Color.Transparent.toArgb()
          window.navigationBarColor = Color.Transparent.toArgb()

          val wic = WindowCompat.getInsetsController(window, view)
          wic.isAppearanceLightStatusBars = false
          wic.isAppearanceLightNavigationBars = !darkTheme
        }
      }
    }
  }
}

@Preview
@Composable
fun AppAndroidPreview() {
  App()
}
