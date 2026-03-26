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
import dk.codella.vantadot.timer.widget.TimerWidget

class ModeSwitchActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, glanceId)
        val state = TimerState.fromPreferences(prefs)
        if (state.isRunning) return

        val nextMode = when (state.mode) {
            TimerMode.COUNTDOWN -> TimerMode.STOPWATCH
            TimerMode.STOPWATCH -> TimerMode.POMODORO
            TimerMode.POMODORO -> TimerMode.COUNTDOWN
        }
        val newState = TimerState(mode = nextMode)
        updateAppWidgetState(context, glanceId) { mutablePrefs ->
            TimerState.writeTo(mutablePrefs, newState)
        }
        TimerWidget().update(context, glanceId)
    }
}