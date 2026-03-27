package dk.codella.vantadot.timer.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.state.PreferencesGlanceStateDefinition
import dk.codella.vantadot.R
import dk.codella.vantadot.settings.WidgetSettings
import dk.codella.vantadot.timer.data.TimerStatus
import dk.codella.vantadot.timer.data.TimerWidgetState
import dk.codella.vantadot.timer.widget.TimerWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TimerAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val result = goAsync()
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val manager = GlanceAppWidgetManager(context)
                for (id in manager.getGlanceIds(TimerWidget::class.java)) {
                    updateAppWidgetState(context, id) { prefs ->
                        val state = TimerWidgetState.fromPreferences(prefs)
                        if (state.status == TimerStatus.RUNNING && state.endTimeMillis <= System.currentTimeMillis()) {
                            TimerWidgetState.writeTo(prefs, state.copy(
                                status = TimerStatus.IDLE,
                                remainingMillis = state.durationMillis,
                                endTimeMillis = 0L,
                            ))
                        }
                    }
                }
                TimerWidget().updateAll(context)

                var soundEnabled = true
                var vibrationEnabled = true
                val ids = manager.getGlanceIds(TimerWidget::class.java)
                if (ids.isNotEmpty()) {
                    val prefs = getAppWidgetState(context, PreferencesGlanceStateDefinition, ids.first())
                    val settings = WidgetSettings.fromPreferences(prefs)
                    soundEnabled = settings.timerSound
                    vibrationEnabled = settings.timerVibration
                }
                fireCompletionNotification(context, soundEnabled, vibrationEnabled)
                dk.codella.vantadot.timer.widget.SecondTickHandler.stop()
            } finally {
                result.finish()
            }
        }
    }

    private fun fireCompletionNotification(context: Context, soundEnabled: Boolean, vibrationEnabled: Boolean) {
        val nm = context.getSystemService(NotificationManager::class.java)

        val channelId = when {
            soundEnabled && vibrationEnabled -> CHANNEL_COMPLETE
            soundEnabled -> CHANNEL_COMPLETE_SOUND
            vibrationEnabled -> CHANNEL_COMPLETE_VIBRATE
            else -> CHANNEL_COMPLETE_QUIET
        }
        val importance = when {
            soundEnabled -> NotificationManager.IMPORTANCE_HIGH
            vibrationEnabled -> NotificationManager.IMPORTANCE_DEFAULT
            else -> NotificationManager.IMPORTANCE_LOW
        }

        nm.createNotificationChannel(
            NotificationChannel(channelId, "Timer Complete", importance).apply {
                enableVibration(vibrationEnabled)
                if (!soundEnabled) setSound(null, null)
            }
        )
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Timer")
            .setContentText("Time's up!")
            .setAutoCancel(true)
            .build()
        nm.notify(COMPLETE_NOTIFICATION_ID, notification)
    }

    companion object {
        private const val CHANNEL_COMPLETE = "timer_complete"
        private const val CHANNEL_COMPLETE_SOUND = "timer_complete_sound"
        private const val CHANNEL_COMPLETE_VIBRATE = "timer_complete_vibrate"
        private const val CHANNEL_COMPLETE_QUIET = "timer_complete_quiet"
        private const val COMPLETE_NOTIFICATION_ID = 1003
        private const val REQUEST_CODE = 2001

        fun schedule(context: Context, triggerAtMillis: Long) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent(context),
            )
        }

        fun cancel(context: Context) {
            val alarmManager = context.getSystemService(AlarmManager::class.java)
            alarmManager.cancel(pendingIntent(context))
        }

        private fun pendingIntent(context: Context): PendingIntent {
            return PendingIntent.getBroadcast(
                context,
                REQUEST_CODE,
                Intent(context, TimerAlarmReceiver::class.java),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }
    }
}
