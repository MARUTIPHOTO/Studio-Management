package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
  primary = StudioRedPrimary,
  onPrimary = Color.White,
  primaryContainer = StudioRedLight,
  onPrimaryContainer = StudioRedDark,
  secondary = StudioSecondaryGray,
  onSecondary = Color.White,
  secondaryContainer = StudioRedLight,
  onSecondaryContainer = StudioDarkText,
  tertiary = StudioGreenSuccess,
  onTertiary = Color.White,
  background = StudioBackground,
  onBackground = StudioDarkText,
  surface = StudioSurface,
  onSurface = StudioDarkText,
  surfaceVariant = StudioSurfaceVariant,
  onSurfaceVariant = StudioSecondaryGray,
  outline = StudioLightBorder,
  error = StudioRedPrimary,
  onError = Color.White
)

@Composable
fun MarutiPhotoStudioTheme(
  content: @Composable () -> Unit
) {
  MaterialTheme(
    colorScheme = LightColorScheme,
    typography = Typography,
    content = content
  )
}

