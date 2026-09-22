package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val RtcColorScheme = darkColorScheme(
    primary = RtcCyan,
    onPrimary = RtcNavyDark,
    secondary = RtcAmber,
    onSecondary = RtcNavyDark,
    tertiary = RtcGreen,
    onTertiary = RtcNavyDark,
    background = RtcNavyDark,
    onBackground = RtcWhite,
    surface = RtcNavySurface,
    onSurface = RtcWhite,
    error = RtcRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = RtcColorScheme,
        typography = Typography,
        content = content
    )
}

