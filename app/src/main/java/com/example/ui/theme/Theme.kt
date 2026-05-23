package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme =
  darkColorScheme(
    primary = SleekPrimary,
    secondary = SleekPrimaryDark,
    tertiary = SleekTextSecondary,
    background = SleekBackground,
    surface = SleekCard,
    onPrimary = SleekPrimaryDark,
    onSecondary = SleekTextPrimary,
    onBackground = SleekTextPrimary,
    onSurface = SleekTextPrimary,
    error = SleekError,
    outline = SleekBorder
  )

private val LightColorScheme = DarkColorScheme // Enforce dark theme per requirement "dukungan mode gelap"

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Force dark theme
  dynamicColor: Boolean = false, // Disable dynamic to keep brand
  content: @Composable () -> Unit,
) {
  val colorScheme = DarkColorScheme
  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
