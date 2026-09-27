package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
  primary = GoldAccent,
  onPrimary = NavyNight,
  primaryContainer = EmeraldDark,
  onPrimaryContainer = GoldLight,
  secondary = EmeraldLight,
  onSecondary = Color.White,
  secondaryContainer = DarkSurfaceVariant,
  onSecondaryContainer = Color.White,
  tertiary = GoldLight,
  onTertiary = NavyNight,
  background = DarkBackground,
  onBackground = DarkTextPrimary,
  surface = DarkSurface,
  onSurface = DarkTextPrimary,
  surfaceVariant = DarkSurfaceVariant,
  onSurfaceVariant = DarkTextSecondary,
  error = ErrorRed,
  onError = Color.White,
  errorContainer = Color(0xFF5C1D1D),
  onErrorContainer = ErrorRedContainer
)

private val LightColorScheme = lightColorScheme(
  primary = EmeraldPrimary,
  onPrimary = Color.White,
  primaryContainer = EmeraldContainer,
  onPrimaryContainer = EmeraldOnContainer,
  secondary = GoldDark,
  onSecondary = Color.White,
  secondaryContainer = GoldContainer,
  onSecondaryContainer = GoldOnContainer,
  tertiary = EmeraldDark,
  onTertiary = Color.White,
  background = CreamBackground,
  onBackground = CreamTextPrimary,
  surface = CreamSurface,
  onSurface = CreamTextPrimary,
  surfaceVariant = CreamSurfaceVariant,
  onSurfaceVariant = CreamTextSecondary,
  error = ErrorRed,
  onError = Color.White,
  errorContainer = ErrorRedContainer,
  onErrorContainer = Color(0xFF7F1D1D)
)

@Composable
fun IslamicChallengeTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content: @Composable () -> Unit
) = IslamicChallengeTheme(darkTheme = darkTheme, content = content)

