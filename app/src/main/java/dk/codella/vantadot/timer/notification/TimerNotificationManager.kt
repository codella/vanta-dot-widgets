package dk.codella.vantadot.timer.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import dk.codella.vantadot.R
import dk.codella.vantadot.timer.data.PomodoroPhase
import dk.codella.vantadot.timer.data.TimerMode
import dk.codella.vantadot.timer.data.TimerState
import dk.codella.vantadot.timer.service.TimerService

object TimerNotificationManager {
    const val CHANNEL_ID_ACTIVE = "timer_active"
    const val CHANNEL_ID_COMPLETION = "timer_completion"
    const val NOTIFICATION_ID_ACTIVE = 1001
    const val NOTIFICATION_ID_COMPLETION = 1002

    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)

        val activeChannel = NotificationChannel(
            CHANNEL_ID_ACTIVE,
            "Active Timer",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Shows while a timer is running"
            setShowBadge(false)
        }

        val completionChannel = NotificationChannel(
            CHANNEL_ID_COMPLETION,
            "Timer Complete",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Alerts when a timer completes"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 200, 500)
        }

        manager.createNotificationChannel(activeChannel)
        manager.createNotificationChannel(completionChannel)
    }

    fun buildActiveNotification(context: Context, state: TimerState): Notification {
        val title = when (state.mode) {
            TimerMode.COUNTDOWN -> "Countdown"
            TimerMode.STOPWATCH -> "Stopwatch"
            TimerMode.POMODORO -> {
                val phase = if (state.pomodoroPhase == PomodoroPhase.WORK) "Work" else "Break"
                "Pomodoro - $phase ${state.pomodoroCurrentCycle}/${state.pomodoroTotalCycles}"
            }
        }
        val timeText = state.displayTime()
        val content = if (state.isRunning) timeText
        else "$timeText (paused)"

        val pauseIntent = Intent(context, TimerService::class.java).apply {
            action = if (state.isRunning) TimerService.ACTION_PAUSE else TimerService.ACTION_START
        }
        val pausePending = PendingIntent.getService(
            context, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val pauseLabel = if (state.isRunning) "Pause" else "Resume"

        val resetIntent = Intent(context, TimerService::class.java).apply {
            action = TimerService.ACTION_RESET
        }
        val resetPending = PendingIntent.getService(
            context, 1, resetIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID_ACTIVE)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(content)
            .setOngoing(true)
            .setSilent(true)
            .addAction(0, pauseLabel, pausePending)
            .addAction(0, "Reset", resetPending)
            .build()
    }

    fun buildCompletionNotification(context: Context, state: TimerState): Notification {
        val title = when {
            state.mode == TimerMode.POMODORO && state.pomodoroPhase == PomodoroPhase.WORK -> "Break time!"
            state.mode == TimerMode.POMODORO -> "Back to work!"
            else -> "Timer complete!"
        }

        return NotificationCompat.Builder(context, CHANNEL_ID_COMPLETION)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setAutoCancel(true)
            .build()
    }
}