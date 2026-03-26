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
    val useStubData: Boolean = false,
) {
    companion object {
        const val DEFAULT_MAX_EVENTS = 8

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
        val UseStubDataKey = booleanPreferencesKey("use_stub_data")

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
            useStubData = prefs[UseStubDataKey] ?: false,
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
            prefs[UseStubDataKey] = settings.useStubData
        }
    }
}
