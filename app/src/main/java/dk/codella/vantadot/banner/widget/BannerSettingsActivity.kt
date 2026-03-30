package dk.codella.vantadot.banner.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dk.codella.vantadot.settings.BannerMessageEntry
import dk.codella.vantadot.settings.WidgetSettings
import dk.codella.vantadot.ui.screens.BannerSettingsScreen
import dk.codella.vantadot.ui.theme.VantaDotTheme

class BannerSettingsActivity : ComponentActivity() {

    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    private var initialSettings by mutableStateOf(WidgetSettings())
    private var settingsLoaded by mutableStateOf(false)

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

        // Load from SharedPreferences
        val bannerSettings = BannerPrefs.load(applicationContext, appWidgetId)
        initialSettings = WidgetSettings(
            bannerMessages = bannerSettings.messages.map { BannerMessageEntry(it) },
            bannerVibe = bannerSettings.vibe,
            bannerScrollSpeed = bannerSettings.scrollSpeed,
            bannerScrollDirection = bannerSettings.scrollDirection,
            bannerGapSeconds = bannerSettings.gapSeconds,
            bannerAccentColorIndex = bannerSettings.accentColorIndex,
            bannerFontSizePreset = bannerSettings.fontSizePreset,
        )
        settingsLoaded = true

        setContent {
            VantaDotTheme {
                if (settingsLoaded) {
                    BannerSettingsScreen(
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
        val bannerSettings = BannerSettings(
            messages = settings.bannerMessages.map { it.text },
            vibe = settings.bannerVibe,
            scrollSpeed = settings.bannerScrollSpeed,
            scrollDirection = settings.bannerScrollDirection,
            gapSeconds = settings.bannerGapSeconds,
            accentColorIndex = settings.bannerAccentColorIndex,
            fontSizePreset = settings.bannerFontSizePreset,
        )
        BannerPrefs.save(applicationContext, appWidgetId, bannerSettings)
        BannerAnimator.resetState(appWidgetId)
        BannerAnimator.startIfNotRunning(applicationContext)
    }
}
