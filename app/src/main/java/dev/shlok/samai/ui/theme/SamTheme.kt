package dev.shlok.samai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// SAM AI Color Palette
val SamBackground = Color(0xFF050A12)
val SamSurface = Color(0xFF0A1220)
val SamSurfaceVariant = Color(0xFF0F1A2E)
val SamCyan = Color(0xFF00D4FF)
val SamCyanDim = Color(0xFF0099BB)
val SamPurple = Color(0xFF7B2FFF)
val SamPurpleDim = Color(0xFF5520BB)
val SamBlue = Color(0xFF1A6FFF)
val SamBlueDim = Color(0xFF1050CC)
val SamGlow = Color(0x4400D4FF)
val SamGlassLight = Color(0x1AFFFFFF)
val SamGlassBorder = Color(0x3300D4FF)
val SamTextPrimary = Color(0xFFE8F4FF)
val SamTextSecondary = Color(0xFF7A9BB5)
val SamTextDim = Color(0xFF3D5A72)
val SamSuccess = Color(0xFF00FF9D)
val SamWarning = Color(0xFFFFAA00)
val SamError = Color(0xFFFF3366)

private val SamColorScheme = darkColorScheme(
    primary = SamCyan,
    secondary = SamPurple,
    tertiary = SamBlue,
    background = SamBackground,
    surface = SamSurface,
    surfaceVariant = SamSurfaceVariant,
    onBackground = SamTextPrimary,
    onSurface = SamTextPrimary,
    onPrimary = SamBackground,
    error = SamError
)

@Composable
fun SamTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = SamColorScheme,
        content = content
    )
}
