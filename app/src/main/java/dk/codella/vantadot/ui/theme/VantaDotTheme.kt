package dk.codella.vantadot.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val VantaDotColorScheme = darkColorScheme(
    primary = VantaDotWhite,
    onPrimary = VantaDotBlack,
    secondary = VantaDotGreyLight,
    onSecondary = VantaDotBlack,
    tertiary = VantaDotRed,
    onTertiary = VantaDotWhite,
    background = VantaDotBlack,
    onBackground = VantaDotWhite,
    surface = VantaDotGreyDark,
    onSurface = VantaDotWhite,
    surfaceVariant = VantaDotGreyMedium,
    onSurfaceVariant = VantaDotGreyLight,
    error = VantaDotRed,
    onError = VantaDotWhite,
)

@Composable
fun VantaDotTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = VantaDotColorScheme,
        typography = VantaDotTypography,
        content = content,
    )
}
