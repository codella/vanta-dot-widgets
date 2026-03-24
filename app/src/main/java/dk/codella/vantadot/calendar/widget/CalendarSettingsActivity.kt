package dk.codella.vantadot.calendar.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.appwidget.state.updateAppWidgetState
import dk.codella.vantadot.BuildConfig
import dk.codella.vantadot.VantaDotApp
import dk.codella.vantadot.calendar.data.CalendarRepository
import dk.codella.vantadot.settings.WidgetSettings
import dk.codella.vantadot.ui.screens.SettingsScreen
import dk.codella.vantadot.ui.theme.VantaDotTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class CalendarSettingsActivity : ComponentActivity() {

    private var hasCalendarPermission by mutableStateOf(false)
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var initialSettings by mutableStateOf(WidgetSettings())
    private var settingsLoaded by mutableStateOf(false)
    private val updateScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var saveJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
            setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
        }

        hasCalendarPermission = CalendarRepository(this).hasCalendarPermission()

        // Load current settings from Glance state
        updateScope.launch {
            val manager = GlanceAppWidgetManager(applicationContext)
            val ids = manager.getGlanceIds(CalendarWidget::class.java)
            if (ids.isNotEmpty()) {
                val prefs = getAppWidgetState(applicationContext, PreferencesGlanceStateDefinition, ids.first())
                initialSettings = WidgetSettings.fromPreferences(prefs)
            }
            settingsLoaded = true
        }

        setContent {
            VantaDotTheme {
                if (settingsLoaded) {
                    SettingsScreen(
                        hasCalendarPermission = hasCalendarPermission,
                        initialSettings = initialSettings,
                        onBack = { finish() },
                        onSettingsChanged = { settings -> saveSettings(settings) },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        hasCalendarPermission = CalendarRepository(this).hasCalendarPermission()
    }

    private fun saveSettings(settings: WidgetSettings) {
        saveJob?.cancel()
        val context = applicationContext
        saveJob = updateScope.launch {
            delay(80) // debounce rapid changes (e.g. slider dragging)
            val manager = GlanceAppWidgetManager(context)
            val ids = manager.getGlanceIds(CalendarWidget::class.java)
            ids.forEach { id ->
                updateAppWidgetState(context, id) { prefs ->
                    WidgetSettings.writeTo(prefs, settings)
                }
                CalendarWidget.refreshEventsIntoState(context, id,
                    useStubOverride = BuildConfig.DEBUG && settings.useStubData)
                CalendarWidget().update(context, id)
            }
            VantaDotApp.enqueuePeriodicCalendarUpdates(context, settings.refreshIntervalMinutes.toLong())
        }
    }
}
