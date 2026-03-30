package dk.codella.vantadot.banner.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import dk.codella.vantadot.banner.data.BannerVibe
import dk.codella.vantadot.common.GlanceText
import dk.codella.vantadot.settings.FontSizePreset
import dk.codella.vantadot.settings.WidgetSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BannerWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = BannerWidget()

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)
        BannerScreenReceiver.register(context)
        BannerScrollTickHandler.startIfNotRunning(context)
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        BannerScreenReceiver.unregister(context)
        BannerScrollTickHandler.stop()
    }
}

internal object BannerScreenReceiver : android.content.BroadcastReceiver() {
    @Volatile
    private var registered = false

    fun register(context: Context) {
        if (registered) return
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        context.applicationContext.registerReceiver(this, filter)
        registered = true
    }

    fun unregister(context: Context) {
        if (!registered) return
        try {
            context.applicationContext.unregisterReceiver(this)
        } catch (_: IllegalArgumentException) {}
        registered = false
    }

    override fun onReceive(context: Context, intent: Intent?) {
        when (intent?.action) {
            Intent.ACTION_SCREEN_OFF -> BannerScrollTickHandler.stop()
            Intent.ACTION_SCREEN_ON -> BannerScrollTickHandler.start(context)
        }
    }
}

object BannerScrollTickHandler {
    @Volatile
    private var job: Job? = null

    val ScrollOffsetKey = intPreferencesKey("banner_scroll_offset")
    val MessageIndexKey = intPreferencesKey("banner_message_index")
    val PausedKey = booleanPreferencesKey("banner_paused")
    val GapUntilKey = longPreferencesKey("banner_gap_until")
    private val InitializedKey = booleanPreferencesKey("banner_initialized")

    private const val TICK_INTERVAL_MS = 40L

    fun start(context: Context) {
        val ctx = context.applicationContext
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            while (isActive) {
                try {
                    advanceAllWidgets(ctx)
                    BannerWidget().updateAll(ctx)
                } catch (_: Exception) {}
                delay(TICK_INTERVAL_MS)
            }
        }
    }

    fun startIfNotRunning(context: Context) {
        if (job?.isActive == true) return
        start(context)
    }

    fun stop() {
        job?.cancel()
        job = null
    }

    private fun scrollStepForSpeed(speed: Int): Int {
        val s = speed.coerceIn(1, 10)
        return s * s / 3 + 1 // 1→1, 3→4, 5→9, 7→17, 10→34
    }

    private fun getViewportWidthPx(context: Context, appWidgetId: Int): Int {
        val density = context.resources.displayMetrics.density
        val padding = 12f // VantaDotWidgetTheme.Padding in dp
        val options = AppWidgetManager.getInstance(context).getAppWidgetOptions(appWidgetId)
        val widthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 120)
        return ((widthDp - 2 * padding) * density).toInt().coerceAtLeast(1)
    }

    private suspend fun advanceAllWidgets(context: Context) {
        val manager = GlanceAppWidgetManager(context)
        for (id in manager.getGlanceIds(BannerWidget::class.java)) {
            val appWidgetId = manager.getAppWidgetId(id)
            val viewportWidthPx = getViewportWidthPx(context, appWidgetId)
            updateAppWidgetState(context, id) { prefs ->
                val paused = prefs[PausedKey] ?: false
                if (paused) return@updateAppWidgetState

                // First-load: position text off-screen
                if (prefs[InitializedKey] != true) {
                    prefs[InitializedKey] = true
                    val settings = WidgetSettings.fromPreferences(prefs)
                    val vibe = BannerVibe.entries.getOrElse(settings.bannerVibe) { BannerVibe.SCROLL }
                    if (vibe == BannerVibe.SCROLL) {

                        if (settings.bannerScrollFromLeft) {
                            val fontScale = FontSizePreset.fromIndex(settings.bannerFontSizePreset).scaleFactor
                            val textSizeSp = 18f * fontScale
                            val messages = settings.bannerMessages.ifEmpty { WidgetSettings.DEFAULT_BANNER_MESSAGES }
                            val textWidthPx = GlanceText.measureTextWidth(context, messages[0].text, textSizeSp)
                            prefs[ScrollOffsetKey] = textWidthPx
                        } else {
                            prefs[ScrollOffsetKey] = -viewportWidthPx
                        }
                    }
                    return@updateAppWidgetState
                }

                // Check if we're in a blank gap between messages
                val gapUntil = prefs[GapUntilKey] ?: 0L
                if (gapUntil > 0L) {
                    if (System.currentTimeMillis() < gapUntil) {
                        // Still in gap — stay blank
                        return@updateAppWidgetState
                    }
                    // Gap ended — advance to next message and start scrolling
                    prefs[GapUntilKey] = 0L
                    val settings = WidgetSettings.fromPreferences(prefs)
                    val messages = settings.bannerMessages.ifEmpty {
                        WidgetSettings.DEFAULT_BANNER_MESSAGES
                    }
                    val messageIndex = prefs[MessageIndexKey] ?: 0
                    prefs[MessageIndexKey] = (messageIndex + 1) % messages.size
                    val vibe = BannerVibe.entries.getOrElse(settings.bannerVibe) { BannerVibe.SCROLL }
                    if (vibe == BannerVibe.SCROLL) {
                        val fontScale = FontSizePreset.fromIndex(settings.bannerFontSizePreset).scaleFactor
                        val textSizeSp = 18f * fontScale
                        val nextMsg = settings.bannerMessages.ifEmpty { WidgetSettings.DEFAULT_BANNER_MESSAGES }
                        val nextIndex = prefs[MessageIndexKey] ?: 0
                        val nextText = nextMsg.getOrElse(nextIndex % nextMsg.size) { nextMsg[0] }
                        val nextTextWidth = GlanceText.measureTextWidth(context, nextText.text, textSizeSp)
                        prefs[ScrollOffsetKey] = if (settings.bannerScrollFromLeft) {
                            nextTextWidth
                        } else {
                            -viewportWidthPx
                        }
                    } else {
                        prefs[ScrollOffsetKey] = 0
                    }
                    return@updateAppWidgetState
                }

                val settings = WidgetSettings.fromPreferences(prefs)
                val messages = settings.bannerMessages.ifEmpty {
                    WidgetSettings.DEFAULT_BANNER_MESSAGES
                }
                val messageIndex = prefs[MessageIndexKey] ?: 0
                val currentMsg = messages.getOrElse(messageIndex % messages.size) { messages[0] }
                val vibe = BannerVibe.entries.getOrElse(settings.bannerVibe) { BannerVibe.SCROLL }
                val fontScale = FontSizePreset.fromIndex(settings.bannerFontSizePreset).scaleFactor
                val textSizeSp = 18f * fontScale
                val gapMs = settings.bannerGapSeconds * 1000L

                val step = scrollStepForSpeed(settings.bannerScrollSpeed)
                val textWidthPx = GlanceText.measureTextWidth(context, currentMsg.text, textSizeSp)

                val offset = prefs[ScrollOffsetKey] ?: 0

                fun enterGap() {
                    prefs[ScrollOffsetKey] = 0
                    prefs[GapUntilKey] = System.currentTimeMillis() + gapMs
                }

                val fromLeft = settings.bannerScrollFromLeft

                when (vibe) {
                    BannerVibe.SCROLL -> {
                        if (fromLeft) {
                            val newOffset = offset - step
                            if (newOffset < -viewportWidthPx) {
                                enterGap()
                            } else {
                                prefs[ScrollOffsetKey] = newOffset
                            }
                        } else {
                            val newOffset = offset + step
                            if (newOffset > textWidthPx) {
                                enterGap()
                            } else {
                                prefs[ScrollOffsetKey] = newOffset
                            }
                        }
                    }
                    BannerVibe.BOUNCE -> {
                        val bounceEnd = (textWidthPx - viewportWidthPx).coerceAtLeast(0)
                        val forward = prefs[intPreferencesKey("banner_bounce_dir")] != 1
                        val newOffset = if (forward) offset + step else offset - step
                        if (newOffset >= bounceEnd) {
                            prefs[ScrollOffsetKey] = bounceEnd
                            prefs[intPreferencesKey("banner_bounce_dir")] = 1
                        } else if (newOffset <= 0) {
                            prefs[ScrollOffsetKey] = 0
                            prefs[intPreferencesKey("banner_bounce_dir")] = 0
                            if (!forward && messages.size > 1) {
                                enterGap()
                            }
                        } else {
                            prefs[ScrollOffsetKey] = newOffset
                        }
                    }
                }
            }
        }
    }
}
