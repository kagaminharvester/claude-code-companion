package com.claude.codecompanion.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ClaudePurple = Color(0xFF7C3AED)
private val ClaudeOrange = Color(0xFFD97706)
private val ClaudeDark = Color(0xFF1F2937)
private val ClaudeLight = Color(0xFFF3F4F6)

private val DarkColorScheme = darkColorScheme(
    primary = ClaudePurple,
    secondary = ClaudeOrange,
    tertiary = Color(0xFF60A5FA),
    background = ClaudeDark,
    surface = Color(0xFF374151),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = ClaudePurple,
    secondary = ClaudeOrange,
    tertiary = Color(0xFF3B82F6),
    background = ClaudeLight,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = ClaudeDark,
    onSurface = ClaudeDark,
)

@Composable
fun ClaudeCodeCompanionTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
