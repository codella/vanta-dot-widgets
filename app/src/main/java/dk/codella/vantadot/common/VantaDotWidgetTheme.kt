package dk.codella.vantadot.common

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

object VantaDotWidgetTheme {
    val GreyDark = Color(0xFF1A1A1A)
    val GreyMedium = Color(0xFF333333)
    val GreyLight = Color(0xFF999999)

    // Urgency highlight backgrounds (against GreyDark #1A1A1A)
    val HighlightSubtle = Color(0xFF222222)
    val HighlightLow = Color(0xFF2A2A2A)
    val HighlightMedium = Color(0xFF333333)
    val HighlightHigh = Color(0xFF3D3D3D)
    val HighlightInProgress = Color(0xFF2D1518)

    // Accent bar colors
    val AccentSubtle = Color(0xFF444444)
    val AccentLow = Color(0xFF666666)
    val AccentMedium = Color(0xFF999999)
    val AccentHigh = Color(0xFFCCCCCC)
    val AccentInProgress = Color(0xFFE8212A)

    val CornerRadius = 16.dp
    val Padding = 12.dp
}
