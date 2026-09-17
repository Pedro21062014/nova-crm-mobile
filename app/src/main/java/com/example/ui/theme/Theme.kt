package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CosmicColorScheme = lightColorScheme(
    primary = CosmicPrimary,
    onPrimary = Color.White,
    secondary = CosmicSecondary,
    onSecondary = Color.White,
    tertiary = CosmicTertiary,
    onTertiary = Color.White,
    background = CosmicBackground,
    onBackground = TextWhite,
    surface = CosmicSurface,
    onSurface = TextWhite,
    surfaceVariant = CosmicSurfaceSelected,
    onSurfaceVariant = TextWhite,
    outline = BorderDark,
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false, // Set default to false for premium light "clara" theme
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = CosmicColorScheme,
        typography = Typography,
        content = content
    )
}
