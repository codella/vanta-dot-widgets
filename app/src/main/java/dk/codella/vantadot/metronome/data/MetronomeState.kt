package dk.codella.vantadot.metronome.data

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

enum class MetronomeStatus { IDLE, PLAYING }

data class MetronomeWidgetState(
    val status: MetronomeStatus = MetronomeStatus.IDLE,
    val bpm: Int = DEFAULT_BPM,
    val currentBeat: Int = 0,
    val beatsPerBar: Int = 4,
) {
    companion object {
        const val DEFAULT_BPM = 120
        const val MIN_BPM = 30
        const val MAX_BPM = 300

        val StatusKey = stringPreferencesKey("metronome_status")
        val BpmKey = intPreferencesKey("metronome_bpm")
        val CurrentBeatKey = intPreferencesKey("metronome_current_beat")
        val BeatsPerBarKey = intPreferencesKey("metronome_beats_per_bar")

        fun fromPreferences(prefs: Preferences) = MetronomeWidgetState(
            status = prefs[StatusKey]?.let { runCatching { MetronomeStatus.valueOf(it) }.getOrNull() }
                ?: MetronomeStatus.IDLE,
            bpm = prefs[BpmKey] ?: DEFAULT_BPM,
            currentBeat = prefs[CurrentBeatKey] ?: 0,
            beatsPerBar = prefs[BeatsPerBarKey] ?: 4,
        )

        fun writeTo(prefs: MutablePreferences, state: MetronomeWidgetState) {
            prefs[StatusKey] = state.status.name
            prefs[BpmKey] = state.bpm
            prefs[CurrentBeatKey] = state.currentBeat
            prefs[BeatsPerBarKey] = state.beatsPerBar
        }
    }
}
