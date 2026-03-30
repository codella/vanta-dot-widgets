package dk.codella.vantadot.banner.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import dk.codella.vantadot.common.GlanceText
import dk.codella.vantadot.common.VantaDotWidgetTheme
import dk.codella.vantadot.settings.AccentColorPreset
import dk.codella.vantadot.settings.FontSizePreset
import dk.codella.vantadot.settings.WidgetSettings

@Composable
fun BannerWidgetContent() {
    val context = LocalContext.current
    val size = LocalSize.current
    val prefs = currentState<Preferences>()
    val settings = WidgetSettings.fromPreferences(prefs)
    val fontScale = FontSizePreset.fromIndex(settings.bannerFontSizePreset).scaleFactor
    val accent = AccentColorPreset.fromIndex(settings.bannerAccentColorIndex)
    val messages = settings.bannerMessages.ifEmpty {
        WidgetSettings.DEFAULT_BANNER_MESSAGES
    }
    val messageIndex = prefs[BannerScrollTickHandler.MessageIndexKey] ?: 0
    val scrollOffset = prefs[BannerScrollTickHandler.ScrollOffsetKey] ?: 0
    val paused = prefs[BannerScrollTickHandler.PausedKey] ?: false
    val gapUntil = prefs[BannerScrollTickHandler.GapUntilKey] ?: 0L
    val inGap = gapUntil > 0L && System.currentTimeMillis() < gapUntil
    val currentMessage = messages.getOrElse(messageIndex % messages.size) { messages[0] }

    val padding = VantaDotWidgetTheme.Padding.value
    val viewportWidth = size.width.value - 2 * padding
    val viewportHeight = size.height.value - 2 * padding
    val textSizeSp = 18f * fontScale

    val displayText = currentMessage.text
    val effectiveOffset = scrollOffset

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(VantaDotWidgetTheme.CornerRadius)
            .background(VantaDotWidgetTheme.GreyDark)
            .padding(VantaDotWidgetTheme.Padding)
            .clickable(actionRunCallback<TapActionCallback>(
                actionParametersOf(TapParam to true)
            )),
        contentAlignment = Alignment.CenterStart,
    ) {
        if (!inGap && displayText.isNotEmpty()) {
            Image(
                provider = ImageProvider(
                    GlanceText.renderMarqueeFrame(
                        context = context,
                        text = displayText,
                        viewportWidthDp = viewportWidth,
                        viewportHeightDp = viewportHeight,
                        scrollOffset = effectiveOffset,
                        textSizeSp = textSizeSp,
                        color = accent.swatchColor.toArgb(),
                    )
                ),
                contentDescription = "Banner: ${currentMessage.text}",
            )
        }
    }
}
