package dk.codella.phosphor.calendar.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import kotlinx.coroutines.delay

class RefreshActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[CalendarWidget.IsRefreshingKey] = true
        }
        CalendarWidget().update(context, glanceId)

        delay(800)

        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[CalendarWidget.IsRefreshingKey] = false
        }
        CalendarWidget().update(context, glanceId)
    }
}
