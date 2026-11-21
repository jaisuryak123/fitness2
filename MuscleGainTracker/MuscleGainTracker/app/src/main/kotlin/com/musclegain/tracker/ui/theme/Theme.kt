package com.musclegain.tracker.ui.theme

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
    primary = ElectricLime,
    onPrimary = OnElectricLime,
    primaryContainer = ElectricLimeDark,
    onPrimaryContainer = ElectricLimeLight,
    secondary = CyanAccent,
    onSecondary = OnCyanAccent,
    secondaryContainer = CyanAccentDark,
    onSecondaryContainer = CyanAccent,
    tertiary = AmberAccent,
    onTertiary = OnElectricLime,
    tertiaryContainer = AmberAccentDark,
    onTertiaryContainer = AmberAccent,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkBorder,
    error = CoralError,
    onError = TextPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = LightElectricLime,
    onPrimary = LightSurface,
    secondary = CyanAccent,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = LightBorder,
    error = CoralError,
    onError = LightSurface
)

@Composable
fun MuscleGainTheme(
    darkTheme: Boolean = true, // Dark theme is preferred default for personal muscle gain fitness tracking
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
