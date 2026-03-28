package dk.codella.vantadot.timer.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import dk.codella.vantadot.timer.data.TimerStatus
import dk.codella.vantadot.timer.data.TimerWidgetState
import dk.codella.vantadot.timer.service.TimerAlarmReceiver

class StartPauseActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)
        var startTicking = false
        var endTimeForAlarm = 0L

        updateAppWidgetState(context, glanceId) { prefs ->
            val state = TimerWidgetState.fromPreferences(prefs)
            val newState = when (state.status) {
                TimerStatus.IDLE, TimerStatus.PAUSED -> {
                    val endTime = System.currentTimeMillis() + state.remainingMillis
                    startTicking = true
                    endTimeForAlarm = endTime
                    state.copy(
                        status = TimerStatus.RUNNING,
                        endTimeMillis = endTime,
                    )
                }
                TimerStatus.RUNNING -> {
                    TimerAlarmReceiver.cancel(context, appWidgetId)
                    state.copy(
                        status = TimerStatus.PAUSED,
                        remainingMillis = (state.endTimeMillis - System.currentTimeMillis()).coerceAtLeast(0),
                        endTimeMillis = 0L,
                    )
                }
            }
            TimerWidgetState.writeTo(prefs, newState)
        }

        if (startTicking) {
            SecondTickHandler.start(context)
            TimerAlarmReceiver.schedule(context, endTimeForAlarm, appWidgetId)
        }

        TimerWidget().update(context, glanceId)
    }
}

class ResetActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)

        updateAppWidgetState(context, glanceId) { prefs ->
            val state = TimerWidgetState.fromPreferences(prefs)
            TimerWidgetState.writeTo(prefs, state.copy(
                status = TimerStatus.IDLE,
                remainingMillis = state.durationMillis,
                endTimeMillis = 0L,
            ))
        }

        TimerAlarmReceiver.cancel(context, appWidgetId)
        TimerWidget().update(context, glanceId)
    }
}

class CyclePresetActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        updateAppWidgetState(context, glanceId) { prefs ->
            val state = TimerWidgetState.fromPreferences(prefs)
            if (state.status != TimerStatus.IDLE) return@updateAppWidgetState

            val settings = dk.codella.vantadot.settings.WidgetSettings.fromPreferences(prefs)
            val presetMillis = settings.timerPresets.map { it * 60 * 1000L }
            if (presetMillis.isEmpty()) return@updateAppWidgetState

            // Find next preset after current duration, wrapping around
            val currentIndex = presetMillis.indexOf(state.durationMillis)
            val nextIndex = if (currentIndex == -1) 0 else (currentIndex + 1) % presetMillis.size
            val nextDuration = presetMillis[nextIndex]

            TimerWidgetState.writeTo(prefs, state.copy(
                durationMillis = nextDuration,
                remainingMillis = nextDuration,
            ))
        }
        TimerWidget().update(context, glanceId)
    }
}
