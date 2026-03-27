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
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import dk.codella.vantadot.R
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
                fireCompletionNotification(context)
                dk.codella.vantadot.timer.widget.SecondTickHandler.stop()
            } finally {
                result.finish()
            }
        }
    }

    private fun fireCompletionNotification(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(
                CHANNEL_COMPLETE,
                "Timer Complete",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply { enableVibration(true) }
        )
        val notification = NotificationCompat.Builder(context, CHANNEL_COMPLETE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Timer")
            .setContentText("Time's up!")
            .setAutoCancel(true)
            .build()
        nm.notify(COMPLETE_NOTIFICATION_ID, notification)
    }

    companion object {
        private const val CHANNEL_COMPLETE = "timer_complete"
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
