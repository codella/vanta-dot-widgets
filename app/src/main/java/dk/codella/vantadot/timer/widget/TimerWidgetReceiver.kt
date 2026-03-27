package dk.codella.vantadot.timer.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import dk.codella.vantadot.timer.data.TimerStatus
import dk.codella.vantadot.timer.data.TimerWidgetState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TimerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TimerWidget()
}

object SecondTickHandler {
    private var job: Job? = null

    fun start(context: Context) {
        val ctx = context.applicationContext
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            while (isActive) {
                delay(1000)
                try {
                    val manager = GlanceAppWidgetManager(ctx)
                    var anyRunning = false
                    for (id in manager.getGlanceIds(TimerWidget::class.java)) {
                        updateAppWidgetState(ctx, id) { prefs ->
                            val state = TimerWidgetState.fromPreferences(prefs)
                            if (state.status == TimerStatus.RUNNING) {
                                val remaining = (state.endTimeMillis - System.currentTimeMillis()).coerceAtLeast(0)
                                prefs[TimerWidgetState.RemainingMillisKey] = remaining
                                anyRunning = true
                            }
                        }
                    }
                    TimerWidget().updateAll(ctx)
                    if (!anyRunning) {
                        break
                    }
                } catch (_: Exception) {}
            }
        }
    }

    fun stop() {
        job?.cancel()
        job = null
    }
}
