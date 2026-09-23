package com.marvis.aigroup.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val ChatGreen = Color(0xFF95EC69)
val BubbleLight = Color(0xFFFFFFFF)
val BubbleDark = Color(0xFF2B2B2B)
val BgLight = Color(0xFFEDEDED)
val BgDark = Color(0xFF111111)
val SystemGray = Color(0xFFAAAAAA)

private val LightColors = lightColorScheme(
    primary = Color(0xFF1E90FF),
    background = BgLight,
    surface = Color(0xFFFFFFFF),
    onBackground = Color(0xFF111111),
    onSurface = Color(0xFF111111)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF64B5F6),
    background = BgDark,
    surface = Color(0xFF1C1C1C),
    onBackground = Color(0xFFEEEEEE),
    onSurface = Color(0xFFEEEEEE)
)

@Composable
fun AiGroupTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
