package dk.codella.phosphor.calendar.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.glance.appwidget.GlanceAppWidget
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
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        MinuteTickReceiver.unregister(context)
    }
}

private object MinuteTickReceiver : android.content.BroadcastReceiver() {
    private var registered = false

    fun register(context: Context) {
        if (registered) return
        context.applicationContext.registerReceiver(this, IntentFilter(Intent.ACTION_TIME_TICK))
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
        if (intent?.action != Intent.ACTION_TIME_TICK) return
        val result = goAsync()
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                CalendarWidget().updateAll(context)
            } finally {
                result.finish()
            }
        }
    }
}
