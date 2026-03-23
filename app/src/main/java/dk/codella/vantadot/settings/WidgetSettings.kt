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
        )

        fun writeTo(prefs: MutablePreferences, settings: WidgetSettings) {
            prefs[ShowSectionHeaderKey] = settings.showSectionHeader
            prefs[ShowAllDayEventsKey] = settings.showAllDayEvents
            prefs[ShowEventLocationKey] = settings.showEventLocation
            prefs[ShowTentativeEventsKey] = settings.showTentativeEvents
            prefs[MaxEventsKey] = settings.maxEvents
            prefs[AccentColorIndexKey] = settings.accentColorIndex
            prefs[IncludedCalendarIdsKey] = settings.includedCalendarIds.map { it.toString() }.toSet()
        }
    }
}
