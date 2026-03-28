package dk.codella.vantadot.binaryclock.widget

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
import dk.codella.vantadot.ui.screens.BinaryClockSettingsScreen
import dk.codella.vantadot.ui.theme.VantaDotTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BinaryClockSettingsActivity : ComponentActivity() {

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
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                val manager = GlanceAppWidgetManager(applicationContext)
                val glanceId = manager.getGlanceIdBy(appWidgetId)
                val prefs = getAppWidgetState(applicationContext, PreferencesGlanceStateDefinition, glanceId)
                initialSettings = WidgetSettings.fromPreferences(prefs)
            }
            settingsLoaded = true
        }

        setContent {
            VantaDotTheme {
                if (settingsLoaded) {
                    BinaryClockSettingsScreen(
                        initialSettings = initialSettings,
                        onBack = { finish() },
                        onSettingsChanged = { settings -> saveSettings(settings) },
                    )
                }
            }
        }
    }

    private fun saveSettings(settings: WidgetSettings) {
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) return
        saveJob?.cancel()
        val context = applicationContext
        saveJob = updateScope.launch {
            delay(80)
            val manager = GlanceAppWidgetManager(context)
            val glanceId = manager.getGlanceIdBy(appWidgetId)
            updateAppWidgetState(context, glanceId) { prefs ->
                WidgetSettings.writeTo(prefs, settings)
            }
            BinaryClockWidget().update(context, glanceId)
            if (settings.binaryClockShowSeconds) {
                BinaryClockSecondTickHandler.start(context)
            } else {
                BinaryClockSecondTickHandler.stop()
            }
        }
    }
}
