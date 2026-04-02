package dk.codella.vantadot.calendar.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
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

                if (needsRefresh) {
                    val manager = GlanceAppWidgetManager(context)
                    for (id in manager.getGlanceIds(CalendarWidget::class.java)) {
                        CalendarWidget.refreshEventsIntoState(context, id)
                    }
                }
                CalendarWidget().updateAll(context)
            } finally {
                result.finish()
            }
        }
    }
}
