package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

private val ColorMixSurfaceHigh = androidx.compose.ui.graphics.Color(0xFF2B324B)

private val DroidPlayDarkColorScheme = darkColorScheme(
    primary = ElectricBlue,
    onPrimary = CharcoalVoid,
    primaryContainer = ElectricBlueDeep.copy(alpha = 0.24f),
    onPrimaryContainer = ElectricBlue,
    secondary = DeepPurple,
    onSecondary = TextPrimary,
    secondaryContainer = DeepPurpleMuted.copy(alpha = 0.32f),
    onSecondaryContainer = VioletGlow,
    tertiary = ConsoleGreen,
    onTertiary = CharcoalVoid,
    tertiaryContainer = ConsoleGreen.copy(alpha = 0.16f),
    onTertiaryContainer = ConsoleGreen,
    background = CharcoalVoid,
    onBackground = TextPrimary,
    surface = CharcoalSurface,
    onSurface = TextPrimary,
    surfaceVariant = CharcoalSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    surfaceContainerLowest = CharcoalVoid,
    surfaceContainerLow = CharcoalSurface,
    surfaceContainer = CharcoalSurfaceElevated,
    surfaceContainerHigh = CharcoalSurfaceHigh,
    surfaceContainerHighest = ColorMixSurfaceHigh,
    outline = CharcoalOutline,
    outlineVariant = CharcoalOutline.copy(alpha = 0.55f),
    error = ConsoleCoral,
    onError = CharcoalVoid,
    errorContainer = ConsoleCoral.copy(alpha = 0.18f),
    onErrorContainer = ConsoleCoral
)

val DroidPlayShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun DroidPlayTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DroidPlayDarkColorScheme,
        typography = DroidPlayTypography,
        shapes = DroidPlayShapes,
        content = content
    )
}
