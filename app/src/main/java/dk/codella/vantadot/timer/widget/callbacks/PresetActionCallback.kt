package dk.codella.vantadot.timer.widget.callbacks

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import dk.codella.vantadot.timer.service.TimerService

class PresetActionCallback : ActionCallback {
    companion object {
        val DurationKey = ActionParameters.Key<Long>("preset_duration_ms")
    }

    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val durationMs = parameters[DurationKey] ?: return
        TimerService.startPreset(context, durationMs)
    }
}