package dk.codella.vantadot.timer.data

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

enum class TimerMode { COUNTDOWN, STOPWATCH, POMODORO }
enum class PomodoroPhase { WORK, BREAK }

data class TimerState(
    val mode: TimerMode = TimerMode.COUNTDOWN,
    val isRunning: Boolean = false,
    val isCompleted: Boolean = false,

    // Countdown / Pomodoro
    val totalDurationMs: Long = 0L,
    val remainingMs: Long = 0L,

    // Stopwatch
    val elapsedMs: Long = 0L,

    // Timing anchors (drift-free calculation)
    val startedAtMs: Long = 0L,
    val pausedRemainingMs: Long = 0L,
    val pausedElapsedMs: Long = 0L,

    // Pomodoro
    val pomodoroPhase: PomodoroPhase = PomodoroPhase.WORK,
    val pomodoroWorkDurationMs: Long = 25 * 60 * 1000L,
    val pomodoroBreakDurationMs: Long = 5 * 60 * 1000L,
    val pomodoroCurrentCycle: Int = 1,
    val pomodoroTotalCycles: Int = 4,
) {
    fun displayTime(): String {
        val ms = when (mode) {
            TimerMode.COUNTDOWN, TimerMode.POMODORO -> remainingMs.coerceAtLeast(0)
            TimerMode.STOPWATCH -> elapsedMs
        }
        val totalSeconds = (ms / 1000).toInt()
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds)
        else "%02d:%02d".format(minutes, seconds)
    }

    fun progress(): Float = when (mode) {
        TimerMode.COUNTDOWN, TimerMode.POMODORO -> {
            if (totalDurationMs <= 0) 0f
            else (1f - remainingMs.toFloat() / totalDurationMs).coerceIn(0f, 1f)
        }
        TimerMode.STOPWATCH -> 0f
    }

    companion object {
        val ModeKey = stringPreferencesKey("timer_mode")
        val IsRunningKey = booleanPreferencesKey("timer_is_running")
        val IsCompletedKey = booleanPreferencesKey("timer_is_completed")
        val TotalDurationMsKey = longPreferencesKey("timer_total_duration_ms")
        val RemainingMsKey = longPreferencesKey("timer_remaining_ms")
        val ElapsedMsKey = longPreferencesKey("timer_elapsed_ms")
        val StartedAtMsKey = longPreferencesKey("timer_started_at_ms")
        val PausedRemainingMsKey = longPreferencesKey("timer_paused_remaining_ms")
        val PausedElapsedMsKey = longPreferencesKey("timer_paused_elapsed_ms")
        val PomodoroPhaseKey = stringPreferencesKey("timer_pomodoro_phase")
        val PomodoroWorkDurationKey = longPreferencesKey("timer_pomodoro_work_duration")
        val PomodoroBreakDurationKey = longPreferencesKey("timer_pomodoro_break_duration")
        val PomodoroCurrentCycleKey = intPreferencesKey("timer_pomodoro_current_cycle")
        val PomodoroTotalCyclesKey = intPreferencesKey("timer_pomodoro_total_cycles")

        fun fromPreferences(prefs: Preferences) = TimerState(
            mode = prefs[ModeKey]?.let { runCatching { TimerMode.valueOf(it) }.getOrNull() } ?: TimerMode.COUNTDOWN,
            isRunning = prefs[IsRunningKey] ?: false,
            isCompleted = prefs[IsCompletedKey] ?: false,
            totalDurationMs = prefs[TotalDurationMsKey] ?: 0L,
            remainingMs = prefs[RemainingMsKey] ?: 0L,
            elapsedMs = prefs[ElapsedMsKey] ?: 0L,
            startedAtMs = prefs[StartedAtMsKey] ?: 0L,
            pausedRemainingMs = prefs[PausedRemainingMsKey] ?: 0L,
            pausedElapsedMs = prefs[PausedElapsedMsKey] ?: 0L,
            pomodoroPhase = prefs[PomodoroPhaseKey]?.let { runCatching { PomodoroPhase.valueOf(it) }.getOrNull() } ?: PomodoroPhase.WORK,
            pomodoroWorkDurationMs = prefs[PomodoroWorkDurationKey] ?: 25 * 60 * 1000L,
            pomodoroBreakDurationMs = prefs[PomodoroBreakDurationKey] ?: 5 * 60 * 1000L,
            pomodoroCurrentCycle = prefs[PomodoroCurrentCycleKey] ?: 1,
            pomodoroTotalCycles = prefs[PomodoroTotalCyclesKey] ?: 4,
        )

        fun writeTo(prefs: MutablePreferences, state: TimerState) {
            prefs[ModeKey] = state.mode.name
            prefs[IsRunningKey] = state.isRunning
            prefs[IsCompletedKey] = state.isCompleted
            prefs[TotalDurationMsKey] = state.totalDurationMs
            prefs[RemainingMsKey] = state.remainingMs
            prefs[ElapsedMsKey] = state.elapsedMs
            prefs[StartedAtMsKey] = state.startedAtMs
            prefs[PausedRemainingMsKey] = state.pausedRemainingMs
            prefs[PausedElapsedMsKey] = state.pausedElapsedMs
            prefs[PomodoroPhaseKey] = state.pomodoroPhase.name
            prefs[PomodoroWorkDurationKey] = state.pomodoroWorkDurationMs
            prefs[PomodoroBreakDurationKey] = state.pomodoroBreakDurationMs
            prefs[PomodoroCurrentCycleKey] = state.pomodoroCurrentCycle
            prefs[PomodoroTotalCyclesKey] = state.pomodoroTotalCycles
        }
    }
}