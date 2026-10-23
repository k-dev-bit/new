package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val JarvisColorScheme = darkColorScheme(
  primary = CyanPrimary,
  onPrimary = Color(0xFF001E28),
  primaryContainer = Color(0xFF003848),
  onPrimaryContainer = CyanGlow,
  secondary = SkySecondary,
  onSecondary = Color(0xFF001F29),
  secondaryContainer = Color(0xFF07384D),
  onSecondaryContainer = Color(0xFFBCE8FF),
  tertiary = StarkGold,
  onTertiary = Color(0xFF332000),
  tertiaryContainer = Color(0xFF553600),
  onTertiaryContainer = StarkAmber,
  background = ObsidianBackground,
  onBackground = TextPrimary,
  surface = TitaniumSurface,
  onSurface = TextPrimary,
  surfaceVariant = CyberSurfaceVariant,
  onSurfaceVariant = TextSecondary,
  outline = CyberBorder,
  error = DangerRed,
  onError = Color.White
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true, // Jarvis defaults to futuristic dark HUD
  dynamicColor: Boolean = false, // Keep Stark HUD branding consistent
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = JarvisColorScheme,
    typography = Typography,
    content = content
  )
}
