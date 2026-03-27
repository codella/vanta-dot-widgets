package dk.codella.vantadot.timer.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import dk.codella.vantadot.settings.WidgetSettings
import dk.codella.vantadot.ui.screens.TimerSettingsScreen
import dk.codella.vantadot.ui.theme.VantaDotTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TimerSettingsActivity : ComponentActivity() {

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

        updateScope.launch {
            val manager = GlanceAppWidgetManager(applicationContext)
            val ids = manager.getGlanceIds(TimerWidget::class.java)
            if (ids.isNotEmpty()) {
                val prefs = getAppWidgetState(applicationContext, PreferencesGlanceStateDefinition, ids.first())
                initialSettings = WidgetSettings.fromPreferences(prefs)
            }
            settingsLoaded = true
        }

        setContent {
            VantaDotTheme {
                if (settingsLoaded) {
                    TimerSettingsScreen(
                        initialSettings = initialSettings,
                        onBack = { finish() },
                        onSettingsChanged = { settings -> saveSettings(settings) },
                    )
                }
            }
        }
    }

    private fun saveSettings(settings: WidgetSettings) {
        saveJob?.cancel()
        val context = applicationContext
        saveJob = updateScope.launch {
            delay(80)
            val manager = GlanceAppWidgetManager(context)
            val ids = manager.getGlanceIds(TimerWidget::class.java)
            ids.forEach { id ->
                updateAppWidgetState(context, id) { prefs ->
                    WidgetSettings.writeTo(prefs, settings)
                }
                TimerWidget().update(context, id)
            }
        }
    }
}