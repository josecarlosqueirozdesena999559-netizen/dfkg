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
    primary = DecisaoPrimaryDark,
    onPrimary = DecisaoOnPrimaryDark,
    secondary = DecisaoOnSecondaryDark,
    onSecondary = DecisaoOnSecondaryDark,
    background = DecisaoBackgroundDark,
    onBackground = DecisaoOnBackgroundDark,
    surface = DecisaoSurfaceDark,
    onSurface = DecisaoOnSurfaceDark,
    surfaceVariant = DecisaoSurfaceVariantDark,
    onSurfaceVariant = DecisaoOnSurfaceVariantDark,
    outline = DecisaoOutlineDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = DecisaoPrimary,
    onPrimary = DecisaoOnPrimary,
    secondary = DecisaoSecondary,
    onSecondary = DecisaoOnSecondary,
    background = DecisaoBackground,
    onBackground = DecisaoOnBackground,
    surface = DecisaoSurface,
    onSurface = DecisaoOnSurface,
    surfaceVariant = DecisaoSurfaceVariant,
    onSurfaceVariant = DecisaoOnSurfaceVariant,
    outline = DecisaoOutline
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = false,
  // Set to false by default to retain precise brand colors from design image
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
