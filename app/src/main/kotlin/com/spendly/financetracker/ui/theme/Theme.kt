package com.spendly.financetracker.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.darkColorScheme
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

private val DarkColorScheme = darkColorScheme(
    primary = SpendlyGreen,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF0F5F4D),
    onPrimaryContainer = Color(0xFFD8F5EA),
    secondary = SpendlyAmber,
    onSecondary = Color(0xFF2B1A00),
    error = SpendlyRed,
    errorContainer = Color(0xFF5D1F22),
    onErrorContainer = Color(0xFFFFDAD8),
    background = Color(0xFF101413),
    surface = Color(0xFF171C1A),
    surfaceVariant = Color(0xFF25302C),
    onSurface = Color(0xFFE7ECE9),
    onSurfaceVariant = Color(0xFFC0CBC5),
    outline = Color(0xFF46534E)
)

@Composable
fun FinanceTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    accentColorKey: String? = null,
    content: @Composable () -> Unit
) {
    val view = LocalView.current
    val accent = spendlyAccentPalette(accentColorKey)
    val base = if (darkTheme) DarkColorScheme else LightColorScheme
    val colorScheme = base.copy(
        primary = accent.primary,
        primaryContainer = if (darkTheme) accent.dark else accent.light,
        onPrimaryContainer = if (darkTheme) Color.White else accent.dark
    )

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SpendlyTypography,
        content = content
    )
}
