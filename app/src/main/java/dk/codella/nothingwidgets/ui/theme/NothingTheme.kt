package dk.codella.nothingwidgets.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val NothingColorScheme = darkColorScheme(
    primary = NothingWhite,
    onPrimary = NothingBlack,
    secondary = NothingGreyLight,
    onSecondary = NothingBlack,
    tertiary = NothingRed,
    onTertiary = NothingWhite,
    background = NothingBlack,
    onBackground = NothingWhite,
    surface = NothingGreyDark,
    onSurface = NothingWhite,
    surfaceVariant = NothingGreyMedium,
    onSurfaceVariant = NothingGreyLight,
    error = NothingRed,
    onError = NothingWhite,
)

@Composable
fun NothingTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NothingColorScheme,
        typography = NothingTypography,
        content = content,
    )
}
