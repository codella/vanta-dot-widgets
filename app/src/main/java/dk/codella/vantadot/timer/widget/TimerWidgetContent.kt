package dk.codella.vantadot.timer.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
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
import androidx.glance.layout.size
import androidx.glance.layout.width
import dk.codella.vantadot.common.GlanceText
import dk.codella.vantadot.common.VantaDotWidgetTheme
import dk.codella.vantadot.settings.AccentColorPreset
import dk.codella.vantadot.settings.FontSizePreset
import dk.codella.vantadot.settings.TimerPreset
import dk.codella.vantadot.timer.data.TimerStatus
import dk.codella.vantadot.timer.data.TimerWidgetState

@Composable
fun TimerWidgetContent(
    timerState: TimerWidgetState,
    fontSizePreset: Int = 1,
    accentColorIndex: Int = 0,
    presets: List<TimerPreset> = emptyList(),
) {
    val fontScale = FontSizePreset.fromIndex(fontSizePreset).scaleFactor
    val accent = AccentColorPreset.fromIndex(accentColorIndex)
    val remainingMillis = timerState.remainingMillis

    // Find current preset name by matching duration
    val currentPresetName = presets.firstOrNull {
        it.seconds * 1000L == timerState.durationMillis
    }?.name

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
            TimeDisplay(remainingMillis, timerState.status, fontScale, accent, currentPresetName)
            Spacer(modifier = GlanceModifier.height(6.dp))
            ControlButtons(timerState.status, fontScale, accent)
        }
    }
}

@Composable
private fun TimeDisplay(
    remainingMillis: Long,
    status: TimerStatus,
    fontScale: Float,
    accent: AccentColorPreset,
    presetName: String?,
) {
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

    if (status == TimerStatus.IDLE) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Preset name above the time row
            if (presetName != null) {
                Image(
                    provider = ImageProvider(
                        GlanceText.renderDotoText(context, presetName.uppercase(), 11f * fontScale, VantaDotWidgetTheme.GreyLightArgb)
                    ),
                    contentDescription = presetName,
                )
                Spacer(modifier = GlanceModifier.height(2.dp))
            }

            // Chevrons + time on the same line
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = GlanceModifier
                        .size(24.dp)
                        .clickable(
                            actionRunCallback<CyclePresetActionCallback>(
                                actionParametersOf(ForwardParam to false)
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        provider = ImageProvider(
                            GlanceText.renderChevron(context, 16f, VantaDotWidgetTheme.GreyLightArgb, pointLeft = true)
                        ),
                        contentDescription = "Previous preset",
                        modifier = GlanceModifier.size(16.dp),
                    )
                }

                Spacer(modifier = GlanceModifier.width(8.dp))

                Image(
                    provider = ImageProvider(
                        GlanceText.renderDotoText(context, timeText, 40f * fontScale, color)
                    ),
                    contentDescription = "$minutes minutes $seconds seconds",
                )

                Spacer(modifier = GlanceModifier.width(8.dp))

                Box(
                    modifier = GlanceModifier
                        .size(24.dp)
                        .clickable(
                            actionRunCallback<CyclePresetActionCallback>(
                                actionParametersOf(ForwardParam to true)
                            )
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        provider = ImageProvider(
                            GlanceText.renderChevron(context, 16f, VantaDotWidgetTheme.GreyLightArgb, pointLeft = false)
                        ),
                        contentDescription = "Next preset",
                        modifier = GlanceModifier.size(16.dp),
                    )
                }
            }
        }
    } else {
        Image(
            provider = ImageProvider(
                GlanceText.renderDotoText(context, timeText, 40f * fontScale, color)
            ),
            contentDescription = "$minutes minutes $seconds seconds remaining",
        )
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
