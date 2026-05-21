package com.talmudfinance.app.ui.theme

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

private val LightColors = lightColorScheme(
    primary = SageGoldDark,
    onPrimary = Parchment,
    secondary = InkSoft,
    onSecondary = Parchment,
    background = Parchment,
    onBackground = InkDark,
    surface = ParchmentDeep,
    onSurface = InkDark,
    surfaceVariant = ParchmentDeep,
    onSurfaceVariant = InkSoft,
    primaryContainer = SageGold,
    onPrimaryContainer = InkDark
)

private val DarkColors = darkColorScheme(
    primary = SageGold,
    onPrimary = InkDark,
    secondary = ParchmentDeep,
    onSecondary = InkDark,
    background = InkDark,
    onBackground = Parchment,
    surface = InkSoft,
    onSurface = Parchment,
    surfaceVariant = InkSoft,
    onSurfaceVariant = ParchmentDeep,
    primaryContainer = SageGoldDark,
    onPrimaryContainer = Parchment
)

@Composable
fun TalmudFinanceTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
