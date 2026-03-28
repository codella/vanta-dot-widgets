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
) {
    companion object {
        const val DEFAULT_MAX_EVENTS = 20

        val DEFAULT_TIMER_PRESETS = listOf(
            TimerPreset("Timer 1", 60),
            TimerPreset("Timer 2", 300),
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
        }
    }
}
