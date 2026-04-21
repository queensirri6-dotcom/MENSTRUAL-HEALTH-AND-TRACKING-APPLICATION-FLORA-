package com.lunaflow.app.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = Color(0xFFFFB6C1), // Light Pink
    secondary = Color(0xFFB0E0E6), // Powder Blue
    tertiary = Color(0xFFE6E6FA), // Lavender
    background = Color(0xFFFFF9FB),
    surface = Color.White,
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFFF85A1),
    secondary = Color(0xFF87CEEB),
    tertiary = Color(0xFFD8BFD8),
)

@Composable
fun LunaFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
