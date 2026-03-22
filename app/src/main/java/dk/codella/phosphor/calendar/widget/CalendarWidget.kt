package dk.codella.phosphor.calendar.widget

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.currentState
import dk.codella.phosphor.calendar.data.CalendarRepository
import dk.codella.phosphor.calendar.data.StubCalendarData

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
        val events = if (useStub) StubCalendarData.getEvents()
            else if (hasPermission) repository.getUpcomingEvents()
            else emptyList()

        provideContent {
            val isRefreshing = currentState<Preferences>()[IsRefreshingKey] ?: false
            CalendarWidgetContent(
                events = events,
                hasPermission = hasPermission,
                isRefreshing = isRefreshing,
            )
        }
    }

    companion object {
        val IsRefreshingKey = booleanPreferencesKey("is_refreshing")
        const val PREFS_NAME = "phosphor_debug"
        const val USE_STUB_KEY = "use_stub_calendar"
    }
}
