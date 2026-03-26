package dk.codella.vantadot.timer.widget

import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.toArgb
import dk.codella.vantadot.settings.AccentColorPreset
import dk.codella.vantadot.settings.FontSizePreset
import dk.codella.vantadot.timer.data.TimerMode
import dk.codella.vantadot.timer.data.TimerState
import dk.codella.vantadot.timer.widget.callbacks.ModeSwitchActionCallback
import dk.codella.vantadot.timer.widget.callbacks.PresetActionCallback
import dk.codella.vantadot.timer.widget.callbacks.ResetActionCallback
import dk.codella.vantadot.timer.widget.callbacks.StartPauseActionCallback

@Composable
fun TimerWidgetContent(
    timerState: TimerState,
    settings: TimerSettings,
) {
    val size = LocalSize.current
    val isFull = size.height >= TimerWidgetSizes.FULL.height
    val accent = AccentColorPreset.fromIndex(settings.accentColorIndex)
    val fontScale = FontSizePreset.fromIndex(settings.fontSizePreset).scaleFactor

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
            // Mode indicator
            ModeLabel(timerState.mode, fontScale)

            Spacer(modifier = GlanceModifier.height(if (isFull) 16.dp else 8.dp))

            // Large time display
            TimeDisplay(timerState, fontScale, accent)

            if (isFull) {
                // Progress bar (countdown/pomodoro only)
                if (timerState.mode != TimerMode.STOPWATCH && timerState.totalDurationMs > 0) {
                    Spacer(modifier = GlanceModifier.height(12.dp))
                    ProgressBar(timerState, size.width, accent)
                }

                Spacer(modifier = GlanceModifier.height(12.dp))

                // Preset buttons (countdown mode only, when not running)
                if (timerState.mode == TimerMode.COUNTDOWN && !timerState.isRunning && !timerState.isCompleted) {
                    PresetRow(fontScale)
                    Spacer(modifier = GlanceModifier.height(8.dp))
                }

                // Pomodoro phase indicator
                if (timerState.mode == TimerMode.POMODORO) {
                    PomodoroIndicator(timerState, fontScale, accent)
                    Spacer(modifier = GlanceModifier.height(8.dp))
                }
            }

            Spacer(modifier = GlanceModifier.defaultWeight())

            // Controls
            ControlRow(timerState, fontScale)
        }
    }
}

@Composable
private fun ModeLabel(mode: TimerMode, fontScale: Float) {
    val context = LocalContext.current
    val label = when (mode) {
        TimerMode.COUNTDOWN -> "COUNTDOWN"
        TimerMode.STOPWATCH -> "STOPWATCH"
        TimerMode.POMODORO -> "POMODORO"
    }
    Row(
        modifier = GlanceModifier.fillMaxWidth().padding(start = 10.dp)
            .clickable(actionRunCallback<ModeSwitchActionCallback>()),
    ) {
        Image(
            provider = ImageProvider(
                GlanceText.renderDotoText(context, label, 11f * fontScale, VantaDotWidgetTheme.GreyLightArgb)
            ),
            contentDescription = label,
        )
    }
}

@Composable
private fun TimeDisplay(timerState: TimerState, fontScale: Float, accent: AccentColorPreset) {
    val context = LocalContext.current
    val timeText = timerState.displayTime()
    val color = timerTimeColor(timerState, accent)
    Image(
        provider = ImageProvider(
            GlanceText.renderDotoText(context, timeText, 36f * fontScale, color)
        ),
        contentDescription = timeText,
    )
}

@Composable
private fun ProgressBar(timerState: TimerState, widgetWidth: androidx.compose.ui.unit.Dp, accent: AccentColorPreset) {
    val context = LocalContext.current
    val barWidth = (widgetWidth - 44.dp).value.coerceAtLeast(40f)
    Image(
        provider = ImageProvider(
            GlanceText.renderProgressBar(
                context = context,
                widthDp = barWidth,
                progress = timerState.progress(),
                filledColor = accent.swatchColor.toArgb(),
            )
        ),
        contentDescription = "Progress",
    )
}

@Composable
private fun PresetRow(fontScale: Float) {
    val context = LocalContext.current
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        listOf("1M" to 1L, "5M" to 5L, "15M" to 15L, "25M" to 25L).forEachIndexed { index, (label, minutes) ->
            if (index > 0) Spacer(modifier = GlanceModifier.width(8.dp))
            Box(
                modifier = GlanceModifier
                    .cornerRadius(6.dp)
                    .background(VantaDotWidgetTheme.GreyMedium)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
                    .clickable(actionRunCallback<PresetActionCallback>(
                        actionParametersOf(PresetActionCallback.DurationKey to minutes * 60 * 1000L)
                    )),
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    provider = ImageProvider(
                        GlanceText.renderDotoText(context, label, 11f * fontScale, VantaDotWidgetTheme.GreyLightArgb)
                    ),
                    contentDescription = label,
                )
            }
        }
    }
}

@Composable
private fun PomodoroIndicator(timerState: TimerState, fontScale: Float, accent: AccentColorPreset) {
    val context = LocalContext.current
    val phaseLabel = when (timerState.pomodoroPhase) {
        dk.codella.vantadot.timer.data.PomodoroPhase.WORK -> "WORK"
        dk.codella.vantadot.timer.data.PomodoroPhase.BREAK -> "BREAK"
    }
    val text = "$phaseLabel ${timerState.pomodoroCurrentCycle}/${timerState.pomodoroTotalCycles}"
    val color = when (timerState.pomodoroPhase) {
        dk.codella.vantadot.timer.data.PomodoroPhase.WORK -> accent.swatchColor.toArgb()
        dk.codella.vantadot.timer.data.PomodoroPhase.BREAK -> VantaDotWidgetTheme.PomodoroBreakArgb
    }
    Image(
        provider = ImageProvider(
            GlanceText.renderDotoText(context, text, 11f * fontScale, color)
        ),
        contentDescription = text,
    )
}

@Composable
private fun ControlRow(timerState: TimerState, fontScale: Float) {
    val context = LocalContext.current
    val startPauseLabel = when {
        timerState.isCompleted -> "RESTART"
        timerState.isRunning -> "PAUSE"
        else -> "START"
    }

    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Start/Pause button
        Box(
            modifier = GlanceModifier
                .cornerRadius(8.dp)
                .background(VantaDotWidgetTheme.GreyMedium)
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .clickable(actionRunCallback<StartPauseActionCallback>()),
            contentAlignment = Alignment.Center,
        ) {
            Image(
                provider = ImageProvider(
                    GlanceText.renderDotoText(context, startPauseLabel, 12f * fontScale, android.graphics.Color.WHITE)
                ),
                contentDescription = startPauseLabel,
            )
        }

        Spacer(modifier = GlanceModifier.width(12.dp))

        // Reset button
        Box(
            modifier = GlanceModifier
                .cornerRadius(8.dp)
                .background(VantaDotWidgetTheme.GreyMedium)
                .padding(horizontal = 16.dp, vertical = 6.dp)
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

private fun timerTimeColor(timerState: TimerState, accent: AccentColorPreset): Int {
    if (timerState.mode == TimerMode.STOPWATCH) return android.graphics.Color.WHITE
    if (timerState.isCompleted) return VantaDotWidgetTheme.TimerCompleteArgb
    if (!timerState.isRunning) return android.graphics.Color.WHITE

    val remainingSeconds = timerState.remainingMs / 1000
    return when {
        remainingSeconds > 60 -> android.graphics.Color.WHITE
        remainingSeconds > 30 -> VantaDotWidgetTheme.TimerWarningArgb
        remainingSeconds > 10 -> VantaDotWidgetTheme.TimerUrgentArgb
        else -> VantaDotWidgetTheme.TimerCriticalArgb
    }
}