package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val DompetKuDarkColorScheme = darkColorScheme(
    primary = Emerald500,
    onPrimary = Slate950,
    primaryContainer = Emerald500.copy(alpha = 0.18f),
    onPrimaryContainer = Emerald400,
    secondary = Amber500,
    onSecondary = Slate950,
    secondaryContainer = Amber500.copy(alpha = 0.18f),
    onSecondaryContainer = Amber400,
    tertiary = Rose500,
    onTertiary = Color.White,
    tertiaryContainer = Rose500.copy(alpha = 0.18f),
    onTertiaryContainer = Rose400,
    background = Slate950,
    onBackground = Slate50,
    surface = Slate900,
    onSurface = Slate50,
    surfaceVariant = Slate850,
    onSurfaceVariant = Slate400,
    surfaceContainerLow = Slate900,
    surfaceContainer = Slate850,
    surfaceContainerHigh = Slate800,
    outline = Slate700,
    outlineVariant = Slate800,
    error = Rose500,
    onError = Color.White,
)

private val DompetKuShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(28.dp),
)

@Composable
fun DompetKuTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DompetKuDarkColorScheme,
        typography = DompetKuTypography,
        shapes = DompetKuShapes,
        content = content,
    )
}
