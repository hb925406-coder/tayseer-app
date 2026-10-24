package com.tayseer.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = Color.White,
    primaryContainer = TealLight,
    onPrimaryContainer = TealDark,
    secondary = EmeraldAccent,
    onSecondary = Color.White,
    secondaryContainer = EmeraldLight,
    onSecondaryContainer = Color(0xFF064E3B),
    background = SlateBackground,
    surface = SlateSurface,
    onBackground = SlateTextPrimary,
    onSurface = SlateTextPrimary,
    error = RedDanger,
    onError = Color.White,
    outline = SlateBorder
)

@Composable
fun TayseerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
