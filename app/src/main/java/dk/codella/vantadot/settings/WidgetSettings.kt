package dk.codella.vantadot.settings

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import org.json.JSONArray
import org.json.JSONObject

data class TimerPreset(val name: String, val seconds: Int)

data class MetronomePreset(val name: String, val bpm: Int)

data class WidgetSettings(
    val showSectionHeader: Boolean = true,
    val showAllDayEvents: Boolean = true,
    val showEventLocation: Boolean = true,
    val showTentativeEvents: Boolean = true,
    val maxEvents: Int = DEFAULT_MAX_EVENTS,
    val accentColorIndex: Int = 0,
    val includedCalendarIds: Set<Long> = emptySet(),
    val use24HourFormat: Boolean = true,
    val showCompactTime: Boolean = false,
    val fontSizePreset: Int = 1,
    val wrapText: Boolean = false,
    val useStubData: Boolean = false,
    // Timer settings
    val timerPresets: List<TimerPreset> = DEFAULT_TIMER_PRESETS,
    val timerVibration: Boolean = true,
    val timerSound: Boolean = true,
    // Metronome settings
    val metronomePresets: List<MetronomePreset> = DEFAULT_METRONOME_PRESETS,
    val metronomeBeatsPerBar: Int = 4,
    val metronomeAccentFirstBeat: Boolean = true,
    val metronomeVibration: Boolean = false,
    val metronomeSoundChoice: Int = 0,
    // Binary clock settings
    val binaryClockShowSeconds: Boolean = true,
    val binaryClockUse24Hour: Boolean = false,
    val binaryClockShowDigitalTime: Boolean = false,
    val binaryClockShowBitLabels: Boolean = false,
    val binaryClockShowColumnLabels: Boolean = false,
    val binaryClockDotShape: Int = 1,
    val binaryClockAccentColorIndex: Int = 6,
) {
    companion object {
        const val DEFAULT_MAX_EVENTS = 20

        val DEFAULT_TIMER_PRESETS = listOf(
            TimerPreset("Timer 1", 60),
            TimerPreset("Timer 2", 300),
        )

        val DEFAULT_METRONOME_PRESETS = listOf(
            MetronomePreset("Adagio", 72),
            MetronomePreset("Moderato", 108),
            MetronomePreset("Allegro", 132),
        )

        val ShowSectionHeaderKey = booleanPreferencesKey("show_section_header")
        val ShowAllDayEventsKey = booleanPreferencesKey("show_all_day_events")
        val ShowEventLocationKey = booleanPreferencesKey("show_event_location")
        val ShowTentativeEventsKey = booleanPreferencesKey("show_tentative_events")
        val MaxEventsKey = intPreferencesKey("max_events")
        val AccentColorIndexKey = intPreferencesKey("accent_color_index")
        val IncludedCalendarIdsKey = stringSetPreferencesKey("included_calendar_ids")
        val Use24HourFormatKey = booleanPreferencesKey("use_24_hour_format")
        val ShowCompactTimeKey = booleanPreferencesKey("show_compact_time")
        val FontSizePresetKey = intPreferencesKey("font_size_preset")
        val WrapTextKey = booleanPreferencesKey("wrap_text")
        val UseStubDataKey = booleanPreferencesKey("use_stub_data")
        val TimerPresetsKey = stringPreferencesKey("timer_presets_json")
        val TimerVibrationKey = booleanPreferencesKey("timer_vibration")
        val TimerSoundKey = booleanPreferencesKey("timer_sound")
        val MetronomePresetsKey = stringPreferencesKey("metronome_presets_json")
        val MetronomeBeatsPerBarKey = intPreferencesKey("metronome_beats_per_bar")
        val MetronomeAccentFirstBeatKey = booleanPreferencesKey("metronome_accent_first_beat")
        val MetronomeVibrationKey = booleanPreferencesKey("metronome_vibration")
        val MetronomeSoundChoiceKey = intPreferencesKey("metronome_sound_choice")
        val BinaryClockShowSecondsKey = booleanPreferencesKey("binary_clock_show_seconds")
        val BinaryClockUse24HourKey = booleanPreferencesKey("binary_clock_use_24_hour")
        val BinaryClockShowDigitalTimeKey = booleanPreferencesKey("binary_clock_show_digital_time")
        val BinaryClockShowBitLabelsKey = booleanPreferencesKey("binary_clock_show_bit_labels")
        val BinaryClockShowColumnLabelsKey = booleanPreferencesKey("binary_clock_show_column_labels")
        val BinaryClockDotShapeKey = intPreferencesKey("binary_clock_dot_shape")
        val BinaryClockAccentColorIndexKey = intPreferencesKey("binary_clock_accent_color_index")

        private fun parsePresets(json: String): List<TimerPreset> {
            return try {
                val arr = JSONArray(json)
                (0 until arr.length()).map { i ->
                    val obj = arr.getJSONObject(i)
                    TimerPreset(obj.getString("name"), obj.getInt("seconds"))
                }
            } catch (_: Exception) {
                DEFAULT_TIMER_PRESETS
            }
        }

        private fun serializePresets(presets: List<TimerPreset>): String {
            val arr = JSONArray()
            presets.forEach { p ->
                arr.put(JSONObject().apply {
                    put("name", p.name)
                    put("seconds", p.seconds)
                })
            }
            return arr.toString()
        }

        private fun parseMetronomePresets(json: String): List<MetronomePreset> {
            return try {
                val arr = JSONArray(json)
                (0 until arr.length()).map { i ->
                    val obj = arr.getJSONObject(i)
                    MetronomePreset(obj.getString("name"), obj.getInt("bpm"))
                }
            } catch (_: Exception) {
                DEFAULT_METRONOME_PRESETS
            }
        }

        private fun serializeMetronomePresets(presets: List<MetronomePreset>): String {
            val arr = JSONArray()
            presets.forEach { p ->
                arr.put(JSONObject().apply {
                    put("name", p.name)
                    put("bpm", p.bpm)
                })
            }
            return arr.toString()
        }

        fun fromPreferences(prefs: Preferences) = WidgetSettings(
            showSectionHeader = prefs[ShowSectionHeaderKey] ?: true,
            showAllDayEvents = prefs[ShowAllDayEventsKey] ?: true,
            showEventLocation = prefs[ShowEventLocationKey] ?: true,
            showTentativeEvents = prefs[ShowTentativeEventsKey] ?: true,
            maxEvents = prefs[MaxEventsKey] ?: DEFAULT_MAX_EVENTS,
            accentColorIndex = prefs[AccentColorIndexKey] ?: 0,
            includedCalendarIds = prefs[IncludedCalendarIdsKey]
                ?.mapNotNull { it.toLongOrNull() }
                ?.toSet()
                ?: emptySet(),
            use24HourFormat = prefs[Use24HourFormatKey] ?: true,
            showCompactTime = prefs[ShowCompactTimeKey] ?: false,
            fontSizePreset = prefs[FontSizePresetKey] ?: 1,
            wrapText = prefs[WrapTextKey] ?: false,
            useStubData = prefs[UseStubDataKey] ?: false,
            timerPresets = prefs[TimerPresetsKey]?.let { parsePresets(it) } ?: DEFAULT_TIMER_PRESETS,
            timerVibration = prefs[TimerVibrationKey] ?: true,
            timerSound = prefs[TimerSoundKey] ?: true,
            metronomePresets = prefs[MetronomePresetsKey]?.let { parseMetronomePresets(it) } ?: DEFAULT_METRONOME_PRESETS,
            metronomeBeatsPerBar = prefs[MetronomeBeatsPerBarKey] ?: 4,
            metronomeAccentFirstBeat = prefs[MetronomeAccentFirstBeatKey] ?: true,
            metronomeVibration = prefs[MetronomeVibrationKey] ?: false,
            metronomeSoundChoice = prefs[MetronomeSoundChoiceKey] ?: 0,
            binaryClockShowSeconds = prefs[BinaryClockShowSecondsKey] ?: true,
            binaryClockUse24Hour = prefs[BinaryClockUse24HourKey] ?: false,
            binaryClockShowDigitalTime = prefs[BinaryClockShowDigitalTimeKey] ?: false,
            binaryClockShowBitLabels = prefs[BinaryClockShowBitLabelsKey] ?: false,
            binaryClockShowColumnLabels = prefs[BinaryClockShowColumnLabelsKey] ?: false,
            binaryClockDotShape = prefs[BinaryClockDotShapeKey] ?: 1,
            binaryClockAccentColorIndex = prefs[BinaryClockAccentColorIndexKey] ?: 6,
        )

        fun writeTo(prefs: MutablePreferences, settings: WidgetSettings) {
            prefs[ShowSectionHeaderKey] = settings.showSectionHeader
            prefs[ShowAllDayEventsKey] = settings.showAllDayEvents
            prefs[ShowEventLocationKey] = settings.showEventLocation
            prefs[ShowTentativeEventsKey] = settings.showTentativeEvents
            prefs[MaxEventsKey] = settings.maxEvents
            prefs[AccentColorIndexKey] = settings.accentColorIndex
            prefs[IncludedCalendarIdsKey] = settings.includedCalendarIds.map { it.toString() }.toSet()
            prefs[Use24HourFormatKey] = settings.use24HourFormat
            prefs[ShowCompactTimeKey] = settings.showCompactTime
            prefs[FontSizePresetKey] = settings.fontSizePreset
            prefs[WrapTextKey] = settings.wrapText
            prefs[UseStubDataKey] = settings.useStubData
            prefs[TimerPresetsKey] = serializePresets(settings.timerPresets)
            prefs[TimerVibrationKey] = settings.timerVibration
            prefs[TimerSoundKey] = settings.timerSound
            prefs[MetronomePresetsKey] = serializeMetronomePresets(settings.metronomePresets)
            prefs[MetronomeBeatsPerBarKey] = settings.metronomeBeatsPerBar
            prefs[MetronomeAccentFirstBeatKey] = settings.metronomeAccentFirstBeat
            prefs[MetronomeVibrationKey] = settings.metronomeVibration
            prefs[MetronomeSoundChoiceKey] = settings.metronomeSoundChoice
            prefs[BinaryClockShowSecondsKey] = settings.binaryClockShowSeconds
            prefs[BinaryClockUse24HourKey] = settings.binaryClockUse24Hour
            prefs[BinaryClockShowDigitalTimeKey] = settings.binaryClockShowDigitalTime
            prefs[BinaryClockShowBitLabelsKey] = settings.binaryClockShowBitLabels
            prefs[BinaryClockShowColumnLabelsKey] = settings.binaryClockShowColumnLabels
            prefs[BinaryClockDotShapeKey] = settings.binaryClockDotShape
            prefs[BinaryClockAccentColorIndexKey] = settings.binaryClockAccentColorIndex
        }
    }
}
