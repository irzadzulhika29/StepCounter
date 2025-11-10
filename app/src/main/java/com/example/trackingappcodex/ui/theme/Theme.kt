package com.example.trackingappcodex.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = SecondaryGreen,
    secondary = AccentYellow,
    tertiary = PrimaryGreen
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryGreen,
    secondary = SecondaryGreen,
    tertiary = AccentYellow
)

@Composable
fun StepTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme: ColorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
