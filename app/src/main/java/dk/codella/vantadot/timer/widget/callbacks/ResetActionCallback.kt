package dk.codella.vantadot.timer.widget.callbacks

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import dk.codella.vantadot.timer.service.TimerService

class ResetActionCallback : ActionCallback {
    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        TimerService.reset(context)
    }
}