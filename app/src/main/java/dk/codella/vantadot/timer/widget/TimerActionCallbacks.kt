package dk.codella.vantadot.timer.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import dk.codella.vantadot.timer.data.TimerStatus
import dk.codella.vantadot.timer.data.TimerWidgetState
import dk.codella.vantadot.timer.service.TimerAlarmReceiver
import kotlinx.coroutines.delay

val DurationParam = ActionParameters.Key<Long>("duration_millis")

class StartPauseActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        var startTicking = false
        var stopTicking = false
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
                    stopTicking = true
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
            TimerAlarmReceiver.schedule(context, endTimeForAlarm)
        }
        if (stopTicking) {
            SecondTickHandler.stop()
            TimerAlarmReceiver.cancel(context)
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
        updateAppWidgetState(context, glanceId) { prefs ->
            val state = TimerWidgetState.fromPreferences(prefs)
            TimerWidgetState.writeTo(prefs, state.copy(
                status = TimerStatus.IDLE,
                remainingMillis = state.durationMillis,
                endTimeMillis = 0L,
            ))
        }

        SecondTickHandler.stop()
        TimerAlarmReceiver.cancel(context)
        TimerWidget().update(context, glanceId)
    }
}

class PresetActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val durationMillis = parameters[DurationParam] ?: return

        updateAppWidgetState(context, glanceId) { prefs ->
            val state = TimerWidgetState.fromPreferences(prefs)
            if (state.status == TimerStatus.RUNNING) {
                SecondTickHandler.stop()
                TimerAlarmReceiver.cancel(context)
            }
            TimerWidgetState.writeTo(prefs, TimerWidgetState(
                status = TimerStatus.IDLE,
                durationMillis = durationMillis,
                remainingMillis = durationMillis,
                endTimeMillis = 0L,
            ))
        }
        TimerWidget().update(context, glanceId)
    }
}
