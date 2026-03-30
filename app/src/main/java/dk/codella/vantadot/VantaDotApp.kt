package dk.codella.vantadot

import android.app.Application
import android.appwidget.AppWidgetManager
import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dk.codella.vantadot.banner.widget.BannerAnimator
import dk.codella.vantadot.banner.widget.BannerWidgetProvider
import dk.codella.vantadot.binaryclock.widget.BinaryClockSecondTickHandler
import dk.codella.vantadot.binaryclock.widget.BinaryClockWidget
import dk.codella.vantadot.binaryclock.widget.ClockMinuteTickReceiver
import dk.codella.vantadot.calendar.worker.CalendarContentChangeWorker
import dk.codella.vantadot.calendar.worker.CalendarUpdateWorker
import dk.codella.vantadot.settings.WidgetSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class VantaDotApp : Application() {

    override fun onCreate() {
        super.onCreate()
        enqueuePeriodicCalendarUpdates(this)
        CalendarContentChangeWorker.enqueue(this)
        recoverBinaryClockTick(this)
        recoverBannerTick(this)
    }

    companion object {
        fun enqueuePeriodicCalendarUpdates(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<CalendarUpdateWorker>(
                15, TimeUnit.MINUTES,
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                CalendarUpdateWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest,
            )
        }

        fun recoverBannerTick(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                android.content.ComponentName(context, BannerWidgetProvider::class.java)
            )
            if (ids.isNotEmpty()) {
                BannerAnimator.startIfNotRunning(context)
                BannerWidgetProvider.scheduleKeepalive(context)
            }
        }

        fun recoverBinaryClockTick(context: Context) {
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                try {
                    val manager = GlanceAppWidgetManager(context)
                    val ids = manager.getGlanceIds(BinaryClockWidget::class.java)
                    if (ids.isEmpty()) return@launch
                    ClockMinuteTickReceiver.register(context)
                    for (id in ids) {
                        val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
                        val settings = WidgetSettings.fromPreferences(prefs)
                        if (settings.binaryClockShowSeconds) {
                            BinaryClockSecondTickHandler.startIfNotRunning(context)
                            return@launch
                        }
                    }
                } catch (_: Exception) {}
            }
        }
    }
}
