package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = CyanNeon,
    onPrimary = Color(0xFF00363D),
    primaryContainer = CyanDark,
    onPrimaryContainer = CyanPale,
    secondary = TealMint,
    onSecondary = Color(0xFF003731),
    secondaryContainer = TealDark,
    onSecondaryContainer = TealPale,
    tertiary = SkyAccent,
    onTertiary = Color(0xFF003549),
    background = ObsidianBg,
    onBackground = TextPrimary,
    surface = SurfaceDark,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = TextSecondary,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceElevatedDark,
    outline = OutlineDark,
    outlineVariant = OutlineSubtle,
    error = SafetyRed,
    onError = Color.White,
    errorContainer = SafetyRedDark,
    onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun RoverPulseTheme(
    darkTheme: Boolean = true, // Default to polished robotics dark theme
    content: @Composable () -> Unit
) {
    // RC Rover Controller uses intentional, restrained robotics dark aesthetic
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
