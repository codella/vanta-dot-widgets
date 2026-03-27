package dk.codella.vantadot.timer.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
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
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import dk.codella.vantadot.common.GlanceText
import dk.codella.vantadot.common.VantaDotWidgetTheme
import dk.codella.vantadot.settings.AccentColorPreset
import dk.codella.vantadot.settings.FontSizePreset
import dk.codella.vantadot.timer.data.TimerStatus
import dk.codella.vantadot.timer.data.TimerWidgetState

@Composable
fun TimerWidgetContent(
    timerState: TimerWidgetState,
    fontSizePreset: Int = 1,
    accentColorIndex: Int = 0,
) {
    val size = LocalSize.current
    val isFull = size.height >= TimerWidgetSizes.FULL.height
    val fontScale = FontSizePreset.fromIndex(fontSizePreset).scaleFactor
    val accent = AccentColorPreset.fromIndex(accentColorIndex)
    val remainingMillis = timerState.remainingMillis

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
        ) {
            // Time display
            Spacer(modifier = GlanceModifier.defaultWeight())
            TimeDisplay(remainingMillis, timerState.status, fontScale, accent)
            Spacer(modifier = GlanceModifier.defaultWeight())

            // Progress bar (always present to avoid layout shift)
            ProgressBar(remainingMillis, timerState.durationMillis, timerState.status, accent)
            Spacer(modifier = GlanceModifier.height(8.dp))

            // Control buttons
            ControlButtons(timerState.status, fontScale, accent)

            // Preset buttons (only in FULL size)
            if (isFull) {
                Spacer(modifier = GlanceModifier.height(8.dp))
                PresetButtons(fontScale)
            }
        }
    }
}

@Composable
private fun TimeDisplay(remainingMillis: Long, status: TimerStatus, fontScale: Float, accent: AccentColorPreset) {
    val context = LocalContext.current
    val totalSeconds = ((remainingMillis + 999) / 1000).toInt()
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val timeText = "%02d:%02d".format(minutes, seconds)

    val color = when (status) {
        TimerStatus.RUNNING -> accent.swatchColor.toArgb()
        TimerStatus.PAUSED -> VantaDotWidgetTheme.GreyLightArgb
        TimerStatus.IDLE -> android.graphics.Color.WHITE
    }

    Image(
        provider = ImageProvider(
            GlanceText.renderDotoText(
                context = context,
                text = timeText,
                textSizeSp = 40f * fontScale,
                color = color,
            )
        ),
        contentDescription = "$minutes minutes $seconds seconds remaining",
    )
}

@Composable
private fun ProgressBar(remainingMillis: Long, durationMillis: Long, status: TimerStatus, accent: AccentColorPreset) {
    val barBg = if (status == TimerStatus.IDLE) VantaDotWidgetTheme.GreyDark else VantaDotWidgetTheme.GreyMedium
    val showFill = status != TimerStatus.IDLE && durationMillis > 0
    val fraction = if (showFill) (remainingMillis.toFloat() / durationMillis).coerceIn(0f, 1f) else 0f
    val availableWidth = LocalSize.current.width - VantaDotWidgetTheme.Padding * 2
    val filledWidth = (availableWidth.value * fraction).coerceAtLeast(0f)

    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .height(3.dp)
            .background(barBg),
    ) {
        if (filledWidth > 0f) {
            Box(
                modifier = GlanceModifier
                    .width(filledWidth.dp)
                    .height(3.dp)
                    .background(accent.swatchColor),
            ) {}
        }
    }
}

@Composable
private fun ControlButtons(status: TimerStatus, fontScale: Float, accent: AccentColorPreset) {
    val context = LocalContext.current
    val startPauseLabel = when (status) {
        TimerStatus.RUNNING -> "PAUSE"
        else -> "START"
    }

    val startPauseBg = when (status) {
        TimerStatus.IDLE -> accent.inProgressBg
        else -> VantaDotWidgetTheme.GreyMedium
    }
    val startPauseTextColor = when (status) {
        TimerStatus.IDLE -> accent.swatchColor.toArgb()
        else -> android.graphics.Color.WHITE
    }

    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Start/Pause button
        Box(
            modifier = GlanceModifier
                .cornerRadius(8.dp)
                .background(startPauseBg)
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clickable(actionRunCallback<StartPauseActionCallback>()),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(
                    GlanceText.renderDotoText(context, startPauseLabel, 12f * fontScale, startPauseTextColor)
                ),
                contentDescription = startPauseLabel,
            )
        }

        Spacer(modifier = GlanceModifier.width(8.dp))

        // Reset button
        Box(
            modifier = GlanceModifier
                .cornerRadius(8.dp)
                .background(VantaDotWidgetTheme.GreyMedium)
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clickable(actionRunCallback<ResetActionCallback>()),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(
                    GlanceText.renderDotoText(context, "RESET", 12f * fontScale, VantaDotWidgetTheme.GreyLightArgb)
                ),
                contentDescription = "Reset",
            )
        }
    }
}

@Composable
private fun PresetButtons(fontScale: Float) {
    val context = LocalContext.current
    val presets = listOf("1M" to 1L, "5M" to 5L, "15M" to 15L, "30M" to 30L)

    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        presets.forEachIndexed { index, (label, minutes) ->
            if (index > 0) Spacer(modifier = GlanceModifier.width(6.dp))
            Box(
                modifier = GlanceModifier
                    .cornerRadius(8.dp)
                    .background(VantaDotWidgetTheme.GreyMedium)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clickable(
                        actionRunCallback<PresetActionCallback>(
                            actionParametersOf(DurationParam to minutes * 60 * 1000L)
                        )
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    provider = ImageProvider(
                        GlanceText.renderDotoText(context, label, 11f * fontScale, VantaDotWidgetTheme.GreyLightArgb)
                    ),
                    contentDescription = "$label preset",
                )
            }
        }
    }
}
