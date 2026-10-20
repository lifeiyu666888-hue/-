package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LiquidGlassColorScheme = darkColorScheme(
    primary = Color(0xFF38BDF8),
    onPrimary = Color(0xFF061320),
    secondary = Color(0xFF2DD4BF),
    onSecondary = Color(0xFF042F2E),
    tertiary = Color(0xFFFBBF24),
    background = Color(0xFF060911),
    onBackground = Color.White,
    surface = Color(0xFF0F172A),
    onSurface = Color.White,
    surfaceVariant = Color(0x26FFFFFF),
    onSurfaceVariant = Color(0xCCFFFFFF)
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LiquidGlassColorScheme,
        typography = Typography,
        content = content
    )
}
