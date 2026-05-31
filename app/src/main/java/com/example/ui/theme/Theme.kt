package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = FrostedDarkPrimary,
    secondary = FrostedDarkSecondary,
    tertiary = FrostedDarkAccent,
    background = FrostedDarkBackground,
    surface = FrostedDarkSurface,
    onBackground = FrostedDarkOnBackground,
    onSurface = FrostedDarkOnSurface,
    surfaceVariant = Color(0xFF16181C), // Slightly deeper surfacevariant for background of text fields
    primaryContainer = Color(0xFF0F1115),
    onPrimaryContainer = FrostedDarkPrimary
)

private val LightColorScheme = lightColorScheme(
    primary = FrostedLightSecondary,
    secondary = FrostedLightPrimary,
    tertiary = Color(0xFF4B5563),
    background = FrostedLightBackground,
    surface = FrostedLightSurface,
    onBackground = FrostedLightOnBackground,
    onSurface = FrostedLightOnSurface,
    surfaceVariant = Color(0xFFE5E7EB),
    primaryContainer = Color(0xFFFFFFFF),
    onPrimaryContainer = FrostedLightSecondary
)

private val ColorfulColorScheme = darkColorScheme(
    primary = FrostedColorfulPrimary,
    secondary = FrostedColorfulSecondary,
    tertiary = FrostedColorfulAccent,
    background = FrostedColorfulBackground,
    surface = FrostedColorfulSurface,
    onBackground = FrostedColorfulOnBackground,
    onSurface = FrostedColorfulOnSurface,
    surfaceVariant = Color(0xFF271B3D),
    primaryContainer = Color(0xFF1C1330),
    onPrimaryContainer = FrostedColorfulPrimary
)

@Composable
fun PedaratAITheme(
    themeName: String = "dark",
    content: @Composable () -> Unit
) {
    val colorScheme = when (themeName) {
        "white" -> LightColorScheme
        "colorful" -> ColorfulColorScheme
        else -> DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
