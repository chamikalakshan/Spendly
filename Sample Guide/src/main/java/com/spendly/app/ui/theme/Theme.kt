package com.spendly.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = SpendlyGreen,
    onPrimary = Color.White,
    primaryContainer = SpendlyGreenLight,
    onPrimaryContainer = SpendlyGreenDark,
    secondary = SpendlyAmber,
    onSecondary = Color.White,
    error = SpendlyRed,
    errorContainer = SpendlyRedLight,
    onErrorContainer = SpendlyRedDark,
    background = Color.White,
    surface = Color.White,
    surfaceVariant = SpendlyGray50,
    onSurface = SpendlyGray900,
    onSurfaceVariant = SpendlyGray700,
    outline = SpendlyGray300
)

@Composable
fun SpendlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    // Supporting light mode only for now as requested
    val colorScheme = LightColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SpendlyTypography,
        content = content
    )
}
