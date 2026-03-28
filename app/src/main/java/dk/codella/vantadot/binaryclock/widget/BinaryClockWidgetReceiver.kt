package dk.codella.vantadot.binaryclock.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.state.PreferencesGlanceStateDefinition
import dk.codella.vantadot.settings.WidgetSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar

class BinaryClockWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BinaryClockWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        ClockMinuteTickReceiver.register(context)
        BinaryClockSecondTickHandler.checkAndStart(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        ClockMinuteTickReceiver.unregister(context)
        BinaryClockSecondTickHandler.stop()
    }
}

private object ClockMinuteTickReceiver : android.content.BroadcastReceiver() {
    private var registered = false

    fun register(context: Context) {
        if (registered) return
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_TIME_TICK)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        context.applicationContext.registerReceiver(this, filter)
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
        val result = goAsync()
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                writeTimeToAllWidgets(context)
                BinaryClockWidget().updateAll(context)
                BinaryClockSecondTickHandler.checkAndStart(context)
            } finally {
                result.finish()
            }
        }
    }
}

object BinaryClockSecondTickHandler {
    private var job: Job? = null

    val HourKey = intPreferencesKey("binary_clock_hour")
    val MinuteKey = intPreferencesKey("binary_clock_minute")
    val SecondKey = intPreferencesKey("binary_clock_second")

    fun start(context: Context) {
        if (job?.isActive == true) return
        val ctx = context.applicationContext
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            while (isActive) {
                delay(1000)
                try {
                    val manager = GlanceAppWidgetManager(ctx)
                    var anyShowSeconds = false
                    for (id in manager.getGlanceIds(BinaryClockWidget::class.java)) {
                        updateAppWidgetState(ctx, id) { prefs ->
                            val cal = Calendar.getInstance()
                            prefs[HourKey] = cal.get(Calendar.HOUR_OF_DAY)
                            prefs[MinuteKey] = cal.get(Calendar.MINUTE)
                            prefs[SecondKey] = cal.get(Calendar.SECOND)
                            val settings = WidgetSettings.fromPreferences(prefs)
                            if (settings.binaryClockShowSeconds) anyShowSeconds = true
                        }
                    }
                    BinaryClockWidget().updateAll(ctx)
                    if (!anyShowSeconds) break
                } catch (_: Exception) {}
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    fun checkAndStart(context: Context) {
        val ctx = context.applicationContext
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val manager = GlanceAppWidgetManager(ctx)
                for (id in manager.getGlanceIds(BinaryClockWidget::class.java)) {
                    val prefs = getAppWidgetState(ctx, PreferencesGlanceStateDefinition, id)
                    val settings = WidgetSettings.fromPreferences(prefs)
                    if (settings.binaryClockShowSeconds) {
                        start(ctx)
                        return@launch
                    }
                }
            } catch (_: Exception) {}
        }
    }
}

private suspend fun writeTimeToAllWidgets(context: Context) {
    val cal = Calendar.getInstance()
    val h = cal.get(Calendar.HOUR_OF_DAY)
    val m = cal.get(Calendar.MINUTE)
    val s = cal.get(Calendar.SECOND)
    val manager = GlanceAppWidgetManager(context)
    for (id in manager.getGlanceIds(BinaryClockWidget::class.java)) {
        updateAppWidgetState(context, id) { prefs ->
            prefs[BinaryClockSecondTickHandler.HourKey] = h
            prefs[BinaryClockSecondTickHandler.MinuteKey] = m
            prefs[BinaryClockSecondTickHandler.SecondKey] = s
        }
    }
}
