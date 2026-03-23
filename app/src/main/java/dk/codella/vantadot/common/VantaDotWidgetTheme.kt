package dk.codella.vantadot.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp

object VantaDotWidgetTheme {
    val GreyDark = Color(0xFF1A1A1A)
    val GreyMedium = Color(0xFF333333)
    val GreyLight = Color(0xFF999999)

    // Highlight backgrounds (against GreyDark #1A1A1A)
    val HighlightSubtle = Color(0xFF222222)
    val HighlightLow = Color(0xFF2A2A2A)
    val HighlightMedium = Color(0xFF333333)
    val HighlightHigh = Color(0xFF3D3D3D)

    val TentativeText = Color(0x99FFFFFF)

    // Pre-computed ARGB values for GlanceText bitmap rendering
    val GreyLightArgb = GreyLight.toArgb()
    val GreyMediumArgb = GreyMedium.toArgb()
    val TentativeTextArgb = TentativeText.toArgb()

    val CornerRadius = 16.dp
    val Padding = 12.dp
}
