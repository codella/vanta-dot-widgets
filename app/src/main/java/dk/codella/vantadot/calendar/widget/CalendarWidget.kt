package dk.codella.vantadot.calendar.widget

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.LocalContext
import androidx.glance.currentState
import dk.codella.vantadot.BuildConfig
import dk.codella.vantadot.calendar.data.CalendarEvent
import dk.codella.vantadot.calendar.data.CalendarRepository
import dk.codella.vantadot.calendar.data.StubCalendarData
import dk.codella.vantadot.settings.WidgetSettings
import dk.codella.vantadot.settings.WidgetSettings.Companion.UseStubDataKey

class CalendarWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val isRefreshing = prefs[IsRefreshingKey] ?: false
            val refreshPhase = prefs[RefreshPhaseKey] ?: 0
            val settings = WidgetSettings.fromPreferences(prefs)
            val hasPermission = CalendarRepository(LocalContext.current).hasCalendarPermission() ||
                (prefs[HasPermissionKey] ?: false)
            val now = System.currentTimeMillis()

            val events = try {
                CalendarEvent.fromJsonArray(prefs[CachedEventsKey] ?: "[]")
                    .filter { it.isAllDay || it.endTime > now }
                    .let { list -> if (!settings.showAllDayEvents) list.filter { !it.isAllDay } else list }
                    .let { list -> if (!settings.showTentativeEvents) list.filter { !it.isTentative } else list }
                    .take(settings.maxEvents)
            } catch (e: Throwable) {
                Log.e(TAG, "Error parsing events", e)
                emptyList()
            }

            val size = LocalSize.current
            Log.d(TAG, "provideContent: size=${size.width}x${size.height}, events=${events.size}")

            CalendarWidgetContent(
                events = events,
                hasPermission = hasPermission,
                isRefreshing = isRefreshing,
                refreshPhase = refreshPhase,
                showHeader = settings.showSectionHeader,
                showLocation = settings.showEventLocation,
                accentColorIndex = settings.accentColorIndex,
                use24HourFormat = settings.use24HourFormat,
                showCompactTime = settings.showCompactTime,
                fontSizePreset = settings.fontSizePreset,
                wrapText = settings.wrapText,
                showEmptyQuote = settings.showEmptyQuote,
            )
        }
    }

    companion object {
        private const val TAG = "CalendarWidget"
        val IsRefreshingKey = booleanPreferencesKey("is_refreshing")
        val RefreshStartedAtKey = longPreferencesKey("refresh_started_at")
        val RefreshPhaseKey = intPreferencesKey("refresh_phase")
        val CachedEventsKey = stringPreferencesKey("cached_events")
        val HasPermissionKey = booleanPreferencesKey("has_permission")

        suspend fun refreshAllAndUpdate(context: Context) {
            val manager = GlanceAppWidgetManager(context)
            for (id in manager.getGlanceIds(CalendarWidget::class.java)) {
                refreshEventsIntoState(context, id)
            }
            CalendarWidget().updateAll(context)
        }

        suspend fun refreshEventsIntoState(context: Context, id: GlanceId, useStubOverride: Boolean? = null) {
            val useStub = if (useStubOverride != null) useStubOverride else {
                val state = getAppWidgetState(context, PreferencesGlanceStateDefinition, id)
                BuildConfig.DEBUG && (state[UseStubDataKey] ?: false)
            }
            val repository = CalendarRepository(context)
            val hasPermission = useStub || repository.hasCalendarPermission()

            val events = if (useStub) StubCalendarData.getEvents()
                else if (hasPermission) repository.getUpcomingEvents()
                else emptyList()

            updateAppWidgetState(context, id) { prefs ->
                prefs[CachedEventsKey] = CalendarEvent.toJsonArray(events)
                prefs[HasPermissionKey] = hasPermission
            }
        }
    }
}
