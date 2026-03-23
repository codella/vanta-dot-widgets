package dk.codella.vantadot.calendar.widget

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.currentState
import dk.codella.vantadot.calendar.data.CalendarRepository
import dk.codella.vantadot.calendar.data.StubCalendarData
import dk.codella.vantadot.settings.WidgetSettings

class CalendarWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Responsive(
        setOf(
            CalendarWidgetSizes.COMPACT,
            CalendarWidgetSizes.EXPANDED,
            CalendarWidgetSizes.FULL,
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val useStub = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(USE_STUB_KEY, false)
        val repository = CalendarRepository(context)
        val hasPermission = useStub || repository.hasCalendarPermission()

        val allEvents = if (useStub) StubCalendarData.getEvents()
            else if (hasPermission) repository.getUpcomingEvents()
            else emptyList()

        provideContent {
            val prefs = currentState<Preferences>()
            val isRefreshing = prefs[IsRefreshingKey] ?: false
            val refreshPhase = prefs[RefreshPhaseKey] ?: 0
            val settings = WidgetSettings.fromPreferences(prefs)

            val events = allEvents
                .let { list -> if (!settings.showAllDayEvents) list.filter { !it.isAllDay } else list }
                .let { list -> if (!settings.showTentativeEvents) list.filter { !it.isTentative } else list }
                .take(settings.maxEvents)

            CalendarWidgetContent(
                events = events,
                hasPermission = hasPermission,
                isRefreshing = isRefreshing,
                refreshPhase = refreshPhase,
                showHeader = settings.showSectionHeader,
                showLocation = settings.showEventLocation,
                accentColorIndex = settings.accentColorIndex,
            )
        }
    }

    companion object {
        val IsRefreshingKey = booleanPreferencesKey("is_refreshing")
        val RefreshPhaseKey = intPreferencesKey("refresh_phase")
        const val PREFS_NAME = "vantadot_debug"
        const val USE_STUB_KEY = "use_stub_calendar"
    }
}
