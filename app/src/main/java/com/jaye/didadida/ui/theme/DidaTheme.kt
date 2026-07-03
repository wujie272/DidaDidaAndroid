package com.jaye.didadida.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// 暖色调 — 符合打工人早起晚归的调性
private val LightColors = lightColorScheme(
    primary = Color(0xFF5B6ABF),        // 靛蓝
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDEE0FF),
    secondary = Color(0xFFC06B4E),       // 暖橙
    onSecondary = Color.White,
    surface = Color(0xFFFEFBFF),
    onSurface = Color(0xFF1B1B21),
    surfaceVariant = Color(0xFFE4E1EC),
    outline = Color(0xFF76757D),
    background = Color(0xFFFEFBFF),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFBBC3FF),
    onPrimary = Color(0xFF252B6B),
    primaryContainer = Color(0xFF3D4382),
    secondary = Color(0xFFFFB59B),
    onSecondary = Color(0xFF481F11),
    surface = Color(0xFF1B1B21),
    onSurface = Color(0xFFE5E1E9),
    surfaceVariant = Color(0xFF47464E),
    outline = Color(0xFF78787F),
    background = Color(0xFF1B1B21),
)

@Composable
fun DidaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
