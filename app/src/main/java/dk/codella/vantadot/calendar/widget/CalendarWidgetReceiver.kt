package dk.codella.vantadot.calendar.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.state.PreferencesGlanceStateDefinition
import dk.codella.vantadot.calendar.data.CalendarEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class CalendarWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = CalendarWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        MinuteTickReceiver.register(context)
        // Pre-load events so the widget renders with data immediately
        val result = goAsync()
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val manager = GlanceAppWidgetManager(context)
                for (id in manager.getGlanceIds(CalendarWidget::class.java)) {
                    CalendarWidget.refreshEventsIntoState(context, id)
                }
                CalendarWidget().updateAll(context)
            } finally {
                result.finish()
            }
        }
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        MinuteTickReceiver.unregister(context)
    }
}

internal object MinuteTickReceiver : android.content.BroadcastReceiver() {
    private var registered = false
    private var lastDate: Int = -1

    fun register(context: Context) {
        if (registered) return
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        context.applicationContext.registerReceiver(this, filter)
        lastDate = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
        registered = true
    }

    fun unregister(context: Context) {
        if (!registered) return
        try {
            context.applicationContext.unregisterReceiver(this)
        } catch (_: IllegalArgumentException) {}
        registered = false
    }

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        val result = goAsync()
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val needsRefresh = when (action) {
                    Intent.ACTION_DATE_CHANGED, Intent.ACTION_TIMEZONE_CHANGED -> true
                    Intent.ACTION_TIME_TICK -> {
                        // Detect date rollover (midnight) even if ACTION_DATE_CHANGED is delayed
                        val today = java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)
                        if (today != lastDate) {
                            lastDate = today
                            true
                        } else {
                            false
                        }
                    }
                    else -> false
                }

                val manager = GlanceAppWidgetManager(context)
                val ids = manager.getGlanceIds(CalendarWidget::class.java)

                // Skip background updates while a user-triggered refresh is
                // running — updateAll would race with RefreshActionCallback
                // and overwrite the loading-dots animation.
                if (isAnyWidgetRefreshing(context, ids)) {
                    return@launch
                }

                if (needsRefresh) {
                    for (id in ids) {
                        CalendarWidget.refreshEventsIntoState(context, id)
                    }
                    CalendarWidget().updateAll(context)
                } else if (hasEventsInUrgencyWindow(context, ids)) {
                    CalendarWidget().updateAll(context)
                }
            } finally {
                result.finish()
            }
        }
    }

    private suspend fun isAnyWidgetRefreshing(
        context: Context,
        ids: List<GlanceId>,
    ): Boolean {
        for (id in ids) {
            val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
            if (prefs[CalendarWidget.IsRefreshingKey] == true) return true
        }
        return false
    }

    private suspend fun hasEventsInUrgencyWindow(
        context: Context,
        ids: List<GlanceId>,
    ): Boolean {
        val now = System.currentTimeMillis()
        val windowEnd = now + 31 * 60_000L // 31 min to cover the NONE→SUBTLE transition
        for (id in ids) {
            val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
            val events = CalendarEvent.fromJsonArray(prefs[CalendarWidget.CachedEventsKey] ?: "[]")
            if (events.any { !it.isAllDay && it.beginTime in now..windowEnd }) return true
            if (events.any { !it.isAllDay && it.beginTime < now && it.endTime > now }) return true
        }
        return false
    }
}
