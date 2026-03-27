package dk.codella.vantadot.timer.data

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

enum class TimerStatus { IDLE, RUNNING, PAUSED }

data class TimerWidgetState(
    val status: TimerStatus = TimerStatus.IDLE,
    val durationMillis: Long = DEFAULT_DURATION_MILLIS,
    val endTimeMillis: Long = 0L,
    val remainingMillis: Long = DEFAULT_DURATION_MILLIS,
) {
    companion object {
        const val DEFAULT_DURATION_MILLIS = 5 * 60 * 1000L // 5 minutes

        val StatusKey = stringPreferencesKey("timer_status")
        val DurationMillisKey = longPreferencesKey("timer_duration_millis")
        val EndTimeMillisKey = longPreferencesKey("timer_end_time_millis")
        val RemainingMillisKey = longPreferencesKey("timer_remaining_millis")

        fun fromPreferences(prefs: Preferences) = TimerWidgetState(
            status = prefs[StatusKey]?.let { runCatching { TimerStatus.valueOf(it) }.getOrNull() }
                ?: TimerStatus.IDLE,
            durationMillis = prefs[DurationMillisKey] ?: DEFAULT_DURATION_MILLIS,
            endTimeMillis = prefs[EndTimeMillisKey] ?: 0L,
            remainingMillis = prefs[RemainingMillisKey] ?: DEFAULT_DURATION_MILLIS,
        )

        fun writeTo(prefs: MutablePreferences, state: TimerWidgetState) {
            prefs[StatusKey] = state.status.name
            prefs[DurationMillisKey] = state.durationMillis
            prefs[EndTimeMillisKey] = state.endTimeMillis
            prefs[RemainingMillisKey] = state.remainingMillis
        }
    }
}
