package dk.codella.vantadot.calendar.widget

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
        for (phase in 0..2) {
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[CalendarWidget.IsRefreshingKey] = true
                prefs[CalendarWidget.RefreshPhaseKey] = phase
            }
            CalendarWidget().update(context, glanceId)
            delay(300)
        }

        // Refresh events from calendar, then clear loading state
        CalendarWidget.refreshEventsIntoState(context, glanceId)
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[CalendarWidget.IsRefreshingKey] = false
            prefs[CalendarWidget.RefreshPhaseKey] = 0
        }
        CalendarWidget().update(context, glanceId)
    }
}
