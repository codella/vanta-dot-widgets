package dk.codella.phosphor.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val PhosphorColorScheme = darkColorScheme(
    primary = PhosphorWhite,
    onPrimary = PhosphorBlack,
    secondary = PhosphorGreyLight,
    onSecondary = PhosphorBlack,
    tertiary = PhosphorRed,
    onTertiary = PhosphorWhite,
    background = PhosphorBlack,
    onBackground = PhosphorWhite,
    surface = PhosphorGreyDark,
    onSurface = PhosphorWhite,
    surfaceVariant = PhosphorGreyMedium,
    onSurfaceVariant = PhosphorGreyLight,
    error = PhosphorRed,
    onError = PhosphorWhite,
)

@Composable
fun PhosphorTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = PhosphorColorScheme,
        typography = PhosphorTypography,
        content = content,
    )
}
