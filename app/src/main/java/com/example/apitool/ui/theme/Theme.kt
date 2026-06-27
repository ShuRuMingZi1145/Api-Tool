package com.example.apitool.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AppColorScheme = darkColorScheme(
    primary = Color(0xFF569CD6),
    secondary = Color(0xFFC586C0),
    tertiary = Color(0xFF4EC9B0),
    background = Color(0xFF26282C),
    surface = Color(0xFF26282C),
    surfaceContainerHigh = Color(0xFF2D2F34),
    surfaceContainerLow = Color(0xFF1E1F22),
    surfaceContainerLowest = Color(0xFF191A1C),
    onBackground = Color(0xFFD4D4D4),
    onSurface = Color(0xFFD4D4D4),
    onSurfaceVariant = Color(0xFF9C9C9C),
    outline = Color(0xFF3C3C3C),
    outlineVariant = Color(0xFF3C3C3C),
    primaryContainer = Color(0xFF2D2F34),
    error = Color(0xFFF14C4C)
)

@Composable
fun ApiToolTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = Typography,
        content = content
    )
}