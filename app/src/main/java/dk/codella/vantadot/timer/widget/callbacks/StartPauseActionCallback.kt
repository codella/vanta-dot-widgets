package dk.codella.vantadot.timer.widget.callbacks

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import dk.codella.vantadot.timer.data.TimerMode
import dk.codella.vantadot.timer.data.TimerState
import dk.codella.vantadot.timer.service.TimerService
import dk.codella.vantadot.timer.widget.TimerWidget

class StartPauseActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId)
        val state = TimerState.fromPreferences(prefs)

        if (state.isRunning) {
            TimerService.pause(context)
        } else if (state.isCompleted) {
            // Restart with same duration
            TimerService.start(context)
        } else {
            // For countdown, need a duration. Default to 5 min if none set.
            if (state.mode == TimerMode.COUNTDOWN && state.totalDurationMs <= 0 && state.pausedRemainingMs <= 0) {
                TimerService.startPreset(context, 5 * 60 * 1000L)
            } else {
                TimerService.start(context)
            }
        }
    }
}