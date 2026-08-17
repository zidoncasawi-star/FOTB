package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
  primary = AccentPitch,
  onPrimary = DarkBackground,
  primaryContainer = LiveGreenContainer,
  onPrimaryContainer = LiveGreen,
  secondary = AccentSky,
  onSecondary = DarkBackground,
  tertiary = LiveRed,
  onTertiary = DarkBackground,
  background = DarkBackground,
  onBackground = DarkTextPrimary,
  surface = DarkSurface,
  onSurface = DarkTextPrimary,
  surfaceVariant = DarkSurfaceElevated,
  onSurfaceVariant = DarkTextSecondary,
  outline = DarkBorder,
  outlineVariant = DarkSurfaceHighlight
)

private val LightColorScheme = lightColorScheme(
  primary = LiveGreenDark,
  onPrimary = LightSurface,
  primaryContainer = LiveGreenContainer,
  onPrimaryContainer = LiveGreenDark,
  secondary = AccentSky,
  onSecondary = LightSurface,
  tertiary = LiveRed,
  onTertiary = LightSurface,
  background = LightBackground,
  onBackground = LightTextPrimary,
  surface = LightSurface,
  onSurface = LightTextPrimary,
  surfaceVariant = LightSurfaceElevated,
  onSurfaceVariant = LightTextSecondary,
  outline = LightBorder,
  outlineVariant = LightSurfaceHighlight
)

@Composable
fun FootballTodayTheme(
  darkTheme: Boolean = true, // Dark by default for modern sports app
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as Activity).window
      window.statusBarColor = colorScheme.background.toArgb()
      window.navigationBarColor = colorScheme.background.toArgb()
      val insetsController = WindowCompat.getInsetsController(window, view)
      insetsController.isAppearanceLightStatusBars = !darkTheme
      insetsController.isAppearanceLightNavigationBars = !darkTheme
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

