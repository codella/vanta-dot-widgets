package dk.codella.vantadot.timer.widget

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey

data class TimerSettings(
    val accentColorIndex: Int = 0,
    val fontSizePreset: Int = 1,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val pomodoroWorkMinutes: Int = 25,
    val pomodoroBreakMinutes: Int = 5,
    val pomodoroTotalCycles: Int = 4,
) {
    companion object {
        val AccentColorIndexKey = intPreferencesKey("timer_accent_color_index")
        val FontSizePresetKey = intPreferencesKey("timer_font_size_preset")
        val SoundEnabledKey = booleanPreferencesKey("timer_sound_enabled")
        val VibrationEnabledKey = booleanPreferencesKey("timer_vibration_enabled")
        val PomodoroWorkMinutesKey = intPreferencesKey("timer_pomo_work_minutes")
        val PomodoroBreakMinutesKey = intPreferencesKey("timer_pomo_break_minutes")
        val PomodoroTotalCyclesKey = intPreferencesKey("timer_pomo_total_cycles")

        fun fromPreferences(prefs: Preferences) = TimerSettings(
            accentColorIndex = prefs[AccentColorIndexKey] ?: 0,
            fontSizePreset = prefs[FontSizePresetKey] ?: 1,
            soundEnabled = prefs[SoundEnabledKey] ?: true,
            vibrationEnabled = prefs[VibrationEnabledKey] ?: true,
            pomodoroWorkMinutes = prefs[PomodoroWorkMinutesKey] ?: 25,
            pomodoroBreakMinutes = prefs[PomodoroBreakMinutesKey] ?: 5,
            pomodoroTotalCycles = prefs[PomodoroTotalCyclesKey] ?: 4,
        )

        fun writeTo(prefs: MutablePreferences, settings: TimerSettings) {
            prefs[AccentColorIndexKey] = settings.accentColorIndex
            prefs[FontSizePresetKey] = settings.fontSizePreset
            prefs[SoundEnabledKey] = settings.soundEnabled
            prefs[VibrationEnabledKey] = settings.vibrationEnabled
            prefs[PomodoroWorkMinutesKey] = settings.pomodoroWorkMinutes
            prefs[PomodoroBreakMinutesKey] = settings.pomodoroBreakMinutes
            prefs[PomodoroTotalCyclesKey] = settings.pomodoroTotalCycles
        }
    }
}