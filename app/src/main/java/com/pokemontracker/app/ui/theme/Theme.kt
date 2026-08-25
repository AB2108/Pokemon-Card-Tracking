package com.pokemontracker.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColors = lightColorScheme(
    primary = PokeRed,
    onPrimary = Color.White,
    primaryContainer = PokeRedLight,
    onPrimaryContainer = PokeRedDark,
    secondary = PokeGold,
    onSecondary = Color.White,
    secondaryContainer = PokeGoldLight,
    background = NeutralLightBg,
    surface = NeutralLightSurface,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFF2B8B5),
    onPrimary = PokeRedDark,
    primaryContainer = PokeRedDark,
    onPrimaryContainer = PokeRedLight,
    secondary = PokeGoldLight,
    onSecondary = Color(0xFF3A2E00),
    secondaryContainer = Color(0xFF5A4600),
    background = NeutralDarkBg,
    surface = NeutralDarkSurface,
)

@Composable
fun PokemonCardTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content,
    )
}
