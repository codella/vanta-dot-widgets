package dk.codella.vantadot.binaryclock.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import dk.codella.vantadot.common.GlanceText
import dk.codella.vantadot.common.VantaDotWidgetTheme
import dk.codella.vantadot.settings.AccentColorPreset
import dk.codella.vantadot.settings.FontSizePreset
import dk.codella.vantadot.settings.WidgetSettings
import java.util.Calendar
import kotlin.math.min

@Composable
fun BinaryClockWidgetContent() {
    val context = LocalContext.current
    val size = LocalSize.current
    val prefs = currentState<Preferences>()
    val settings = WidgetSettings.fromPreferences(prefs)
    val fontScale = FontSizePreset.fromIndex(settings.binaryClockFontSizePreset).scaleFactor
    val accent = AccentColorPreset.fromIndex(settings.binaryClockAccentColorIndex)

    // Read time from state (written by tick handler / minute tick)
    // Fall back to current time if not yet written
    val fallback = Calendar.getInstance()
    val hour24 = prefs[BinaryClockSecondTickHandler.HourKey] ?: fallback.get(Calendar.HOUR_OF_DAY)
    val minute = prefs[BinaryClockSecondTickHandler.MinuteKey] ?: fallback.get(Calendar.MINUTE)
    val second = prefs[BinaryClockSecondTickHandler.SecondKey] ?: fallback.get(Calendar.SECOND)

    val hours = hour24

    // Calculate optimal dot size to fill available space
    val padding = VantaDotWidgetTheme.Padding.value
    val availableWidth = size.width.value - 2 * padding
    val digitalTimeHeight = if (settings.binaryClockShowDigitalTime) 18f else 0f
    val availableHeight = size.height.value - 2 * padding - digitalTimeHeight

    val numCols = if (settings.binaryClockShowSeconds) 6 else 4
    val numGroups = numCols / 2
    val widthFactor = (if (settings.binaryClockShowBitLabels) 1.5f else 0f) +
        numCols + numGroups * 0.5f + (numGroups - 1) * 1.0f
    val heightFactor = (if (settings.binaryClockShowColumnLabels) 1.3f else 0f) +
        4f + 3f * 0.5f
    val dotSizeDp = (min(availableWidth / widthFactor, availableHeight / heightFactor) * fontScale)
        .coerceAtLeast(4f)

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(VantaDotWidgetTheme.CornerRadius)
            .background(VantaDotWidgetTheme.GreyDark)
            .padding(VantaDotWidgetTheme.Padding),
    ) {
        Column(
            modifier = GlanceModifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                provider = ImageProvider(
                    GlanceText.renderBinaryClockFace(
                        context = context,
                        hours = hours,
                        minutes = minute,
                        seconds = second,
                        showSeconds = settings.binaryClockShowSeconds,
                        showBitLabels = settings.binaryClockShowBitLabels,
                        showColumnLabels = settings.binaryClockShowColumnLabels,
                        dotShape = settings.binaryClockDotShape,
                        onColor = accent.swatchColor.toArgb(),
                        offColor = 0xFF222222.toInt(),
                        labelColor = VantaDotWidgetTheme.GreyLightArgb,
                        dotSizeDp = dotSizeDp,
                    )
                ),
                contentDescription = "Binary clock",
            )

            if (settings.binaryClockShowDigitalTime) {
                Spacer(modifier = GlanceModifier.height(4.dp))

                val timeString = buildString {
                    append(String.format("%02d:%02d", hours, minute))
                    if (settings.binaryClockShowSeconds) {
                        append(String.format(":%02d", second))
                    }
                }

                Image(
                    provider = ImageProvider(
                        GlanceText.renderDotoText(
                            context,
                            timeString,
                            12f * fontScale,
                            VantaDotWidgetTheme.GreyLightArgb,
                        )
                    ),
                    contentDescription = timeString,
                )
            }
        }
    }
}
