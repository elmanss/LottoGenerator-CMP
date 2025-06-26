package me.elmanss.melate.common.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xfff44336)
val PurpleGrey80 = Color(0xFFdc336e)
val Pink80 = Color(0xFFa9458f)

val Purple40 = Color(0xFF6b5192)
val PurpleGrey40 = Color(0xFF3a517b)
val Pink40 = Color(0xFF2f4858)

val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)
val Gray = Color(0xffe3e3e3)

@Composable
fun melateRed(): Color {
  return if (isSystemInDarkTheme()) Purple40 else Purple80
}

@Composable
fun melateDarkRed(): Color {
  return if (isSystemInDarkTheme()) PurpleGrey40 else PurpleGrey80
}

@Composable
fun melateAccent(): Color {
  return if (isSystemInDarkTheme()) Pink40 else Pink80
}
