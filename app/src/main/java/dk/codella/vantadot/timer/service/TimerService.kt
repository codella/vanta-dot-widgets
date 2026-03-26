package dk.codella.vantadot.timer.service

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import dk.codella.vantadot.timer.data.PomodoroPhase
import dk.codella.vantadot.timer.data.TimerMode
import dk.codella.vantadot.timer.data.TimerState
import dk.codella.vantadot.timer.notification.TimerNotificationManager
import dk.codella.vantadot.timer.widget.TimerWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TimerService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var state = TimerState()

    private val tickRunnable = object : Runnable {
        override fun run() {
            tick()
            handler.postDelayed(this, 1000L)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        TimerNotificationManager.createChannels(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> handleStart()
            ACTION_PAUSE -> handlePause()
            ACTION_RESET -> handleReset()
            ACTION_PRESET -> {
                val durationMs = intent.getLongExtra(EXTRA_DURATION_MS, 5 * 60 * 1000L)
                handlePreset(durationMs)
            }
            else -> {
                // Service restarted after kill — recover state
                scope.launch { recoverState() }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(tickRunnable)
        super.onDestroy()
    }

    private fun handleStart() {
        val now = System.currentTimeMillis()
        state = if (state.isCompleted) {
            // Restart with same duration
            state.copy(
                isRunning = true,
                isCompleted = false,
                startedAtMs = now,
                pausedRemainingMs = state.totalDurationMs,
                remainingMs = state.totalDurationMs,
                elapsedMs = 0L,
                pausedElapsedMs = 0L,
            )
        } else {
            state.copy(
                isRunning = true,
                startedAtMs = now,
            )
        }

        startForeground(
            TimerNotificationManager.NOTIFICATION_ID_ACTIVE,
            TimerNotificationManager.buildActiveNotification(this, state)
        )
        handler.removeCallbacks(tickRunnable)
        handler.post(tickRunnable)
        updateWidgets()
    }

    private fun handlePause() {
        handler.removeCallbacks(tickRunnable)
        val now = System.currentTimeMillis()
        state = when (state.mode) {
            TimerMode.COUNTDOWN, TimerMode.POMODORO -> {
                val remaining = (state.pausedRemainingMs - (now - state.startedAtMs)).coerceAtLeast(0)
                state.copy(isRunning = false, pausedRemainingMs = remaining, remainingMs = remaining)
            }
            TimerMode.STOPWATCH -> {
                val elapsed = state.pausedElapsedMs + (now - state.startedAtMs)
                state.copy(isRunning = false, pausedElapsedMs = elapsed, elapsedMs = elapsed)
            }
        }
        updateNotification()
        updateWidgets()
    }

    private fun handleReset() {
        handler.removeCallbacks(tickRunnable)
        state = TimerState(mode = state.mode)
        updateWidgets()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun handlePreset(durationMs: Long) {
        handler.removeCallbacks(tickRunnable)
        val now = System.currentTimeMillis()
        state = TimerState(
            mode = TimerMode.COUNTDOWN,
            isRunning = true,
            totalDurationMs = durationMs,
            remainingMs = durationMs,
            pausedRemainingMs = durationMs,
            startedAtMs = now,
        )
        startForeground(
            TimerNotificationManager.NOTIFICATION_ID_ACTIVE,
            TimerNotificationManager.buildActiveNotification(this, state)
        )
        handler.post(tickRunnable)
        updateWidgets()
    }

    private fun tick() {
        val now = System.currentTimeMillis()
        state = when (state.mode) {
            TimerMode.COUNTDOWN, TimerMode.POMODORO -> {
                val remaining = (state.pausedRemainingMs - (now - state.startedAtMs)).coerceAtLeast(0)
                state.copy(remainingMs = remaining)
            }
            TimerMode.STOPWATCH -> {
                val elapsed = state.pausedElapsedMs + (now - state.startedAtMs)
                state.copy(elapsedMs = elapsed)
            }
        }

        if ((state.mode == TimerMode.COUNTDOWN || state.mode == TimerMode.POMODORO) && state.remainingMs <= 0) {
            onCompletion()
            return
        }

        updateNotification()
        updateWidgets()
    }

    private fun onCompletion() {
        handler.removeCallbacks(tickRunnable)

        if (state.mode == TimerMode.POMODORO) {
            handlePomodoroCycle()
            return
        }

        state = state.copy(isRunning = false, isCompleted = true, remainingMs = 0)
        updateWidgets()

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(
            TimerNotificationManager.NOTIFICATION_ID_COMPLETION,
            TimerNotificationManager.buildCompletionNotification(this, state)
        )

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun handlePomodoroCycle() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(
            TimerNotificationManager.NOTIFICATION_ID_COMPLETION,
            TimerNotificationManager.buildCompletionNotification(this, state)
        )

        val now = System.currentTimeMillis()
        state = if (state.pomodoroPhase == PomodoroPhase.WORK) {
            // Switch to break
            state.copy(
                pomodoroPhase = PomodoroPhase.BREAK,
                totalDurationMs = state.pomodoroBreakDurationMs,
                remainingMs = state.pomodoroBreakDurationMs,
                pausedRemainingMs = state.pomodoroBreakDurationMs,
                startedAtMs = now,
                isRunning = true,
                isCompleted = false,
            )
        } else {
            // Switch to next work cycle
            val nextCycle = state.pomodoroCurrentCycle + 1
            if (nextCycle > state.pomodoroTotalCycles) {
                // All cycles done
                state.copy(isRunning = false, isCompleted = true, remainingMs = 0)
            } else {
                state.copy(
                    pomodoroPhase = PomodoroPhase.WORK,
                    pomodoroCurrentCycle = nextCycle,
                    totalDurationMs = state.pomodoroWorkDurationMs,
                    remainingMs = state.pomodoroWorkDurationMs,
                    pausedRemainingMs = state.pomodoroWorkDurationMs,
                    startedAtMs = now,
                    isRunning = true,
                    isCompleted = false,
                )
            }
        }

        if (state.isRunning) {
            updateNotification()
            handler.post(tickRunnable)
        } else {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
        updateWidgets()
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(
            TimerNotificationManager.NOTIFICATION_ID_ACTIVE,
            TimerNotificationManager.buildActiveNotification(this, state)
        )
    }

    private fun updateWidgets() {
        scope.launch {
            try {
                val manager = GlanceAppWidgetManager(this@TimerService)
                for (id in manager.getGlanceIds(TimerWidget::class.java)) {
                    updateAppWidgetState(this@TimerService, id) { prefs ->
                        TimerState.writeTo(prefs, state)
                    }
                }
                TimerWidget().updateAll(this@TimerService)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update widgets", e)
            }
        }
    }

    private suspend fun recoverState() {
        try {
            val manager = GlanceAppWidgetManager(this@TimerService)
            val ids = manager.getGlanceIds(TimerWidget::class.java)
            if (ids.isEmpty()) {
                stopSelf()
                return
            }
            val prefs = androidx.glance.appwidget.state.getAppWidgetState(
                this@TimerService,
                androidx.glance.state.PreferencesGlanceStateDefinition,
                ids.first()
            )
            state = TimerState.fromPreferences(prefs)
            if (state.isRunning) {
                startForeground(
                    TimerNotificationManager.NOTIFICATION_ID_ACTIVE,
                    TimerNotificationManager.buildActiveNotification(this@TimerService, state)
                )
                handler.post(tickRunnable)
            } else {
                stopSelf()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to recover state", e)
            stopSelf()
        }
    }

    companion object {
        private const val TAG = "TimerService"
        const val ACTION_START = "dk.codella.vantadot.timer.START"
        const val ACTION_PAUSE = "dk.codella.vantadot.timer.PAUSE"
        const val ACTION_RESET = "dk.codella.vantadot.timer.RESET"
        const val ACTION_PRESET = "dk.codella.vantadot.timer.PRESET"
        const val EXTRA_DURATION_MS = "duration_ms"

        fun start(context: Context) {
            val intent = Intent(context, TimerService::class.java).apply { action = ACTION_START }
            context.startForegroundService(intent)
        }

        fun pause(context: Context) {
            val intent = Intent(context, TimerService::class.java).apply { action = ACTION_PAUSE }
            context.startForegroundService(intent)
        }

        fun reset(context: Context) {
            val intent = Intent(context, TimerService::class.java).apply { action = ACTION_RESET }
            context.startForegroundService(intent)
        }

        fun startPreset(context: Context, durationMs: Long) {
            val intent = Intent(context, TimerService::class.java).apply {
                action = ACTION_PRESET
                putExtra(EXTRA_DURATION_MS, durationMs)
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, TimerService::class.java))
        }
    }
}