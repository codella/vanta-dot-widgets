package dk.codella.vantadot.settings

import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

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
    val timerPreset1Minutes: Int = DEFAULT_TIMER_PRESET_1,
    val timerPreset2Minutes: Int = DEFAULT_TIMER_PRESET_2,
    val timerPreset3Minutes: Int = DEFAULT_TIMER_PRESET_3,
    val timerPreset4Minutes: Int = DEFAULT_TIMER_PRESET_4,
    val timerVibration: Boolean = true,
    val timerSound: Boolean = true,
) {
    val timerPresets: List<Int>
        get() = listOf(timerPreset1Minutes, timerPreset2Minutes, timerPreset3Minutes, timerPreset4Minutes)

    companion object {
        const val DEFAULT_MAX_EVENTS = 20
        const val DEFAULT_TIMER_PRESET_1 = 1
        const val DEFAULT_TIMER_PRESET_2 = 5
        const val DEFAULT_TIMER_PRESET_3 = 15
        const val DEFAULT_TIMER_PRESET_4 = 30

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
        val TimerPreset1Key = intPreferencesKey("timer_preset_1_minutes")
        val TimerPreset2Key = intPreferencesKey("timer_preset_2_minutes")
        val TimerPreset3Key = intPreferencesKey("timer_preset_3_minutes")
        val TimerPreset4Key = intPreferencesKey("timer_preset_4_minutes")
        val TimerVibrationKey = booleanPreferencesKey("timer_vibration")
        val TimerSoundKey = booleanPreferencesKey("timer_sound")

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
            timerPreset1Minutes = prefs[TimerPreset1Key] ?: DEFAULT_TIMER_PRESET_1,
            timerPreset2Minutes = prefs[TimerPreset2Key] ?: DEFAULT_TIMER_PRESET_2,
            timerPreset3Minutes = prefs[TimerPreset3Key] ?: DEFAULT_TIMER_PRESET_3,
            timerPreset4Minutes = prefs[TimerPreset4Key] ?: DEFAULT_TIMER_PRESET_4,
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
            prefs[TimerPreset1Key] = settings.timerPreset1Minutes
            prefs[TimerPreset2Key] = settings.timerPreset2Minutes
            prefs[TimerPreset3Key] = settings.timerPreset3Minutes
            prefs[TimerPreset4Key] = settings.timerPreset4Minutes
            prefs[TimerVibrationKey] = settings.timerVibration
            prefs[TimerSoundKey] = settings.timerSound
        }
    }
}
