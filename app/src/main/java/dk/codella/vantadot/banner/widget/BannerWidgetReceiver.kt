package dk.codella.vantadot.banner.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import dk.codella.vantadot.banner.data.BannerScrollDirection
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
    val CurrentLtrKey = booleanPreferencesKey("banner_current_ltr")

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
        return s * s / 3 + 1
    }

    private fun getViewportWidthPx(context: Context, appWidgetId: Int): Int {
        val density = context.resources.displayMetrics.density
        val padding = 12f
        val options = AppWidgetManager.getInstance(context).getAppWidgetOptions(appWidgetId)
        val widthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 120)
        return ((widthDp - 2 * padding) * density).toInt().coerceAtLeast(1)
    }

    private fun initialLtr(directionSetting: Int): Boolean {
        val dir = BannerScrollDirection.entries.getOrElse(directionSetting) {
            BannerScrollDirection.RIGHT_TO_LEFT
        }
        return when (dir) {
            BannerScrollDirection.RIGHT_TO_LEFT -> false
            BannerScrollDirection.LEFT_TO_RIGHT -> true
            BannerScrollDirection.ALTERNATE -> false // start RTL, then flip
            BannerScrollDirection.RANDOM -> Math.random() > 0.5
        }
    }

    private fun nextLtr(directionSetting: Int, currentLtr: Boolean): Boolean {
        val dir = BannerScrollDirection.entries.getOrElse(directionSetting) {
            BannerScrollDirection.RIGHT_TO_LEFT
        }
        return when (dir) {
            BannerScrollDirection.RIGHT_TO_LEFT -> false
            BannerScrollDirection.LEFT_TO_RIGHT -> true
            BannerScrollDirection.ALTERNATE -> !currentLtr
            BannerScrollDirection.RANDOM -> Math.random() > 0.5
        }
    }

    private fun setStartOffset(
        prefs: MutablePreferences,
        ltr: Boolean,
        vibe: BannerVibe,
        textWidthPx: Int,
        viewportWidthPx: Int,
    ) {
        prefs[CurrentLtrKey] = ltr
        prefs[ScrollOffsetKey] = when {
            vibe == BannerVibe.BOUNCE -> 0
            ltr -> textWidthPx
            else -> -viewportWidthPx
        }
    }

    private suspend fun advanceAllWidgets(context: Context) {
        val manager = GlanceAppWidgetManager(context)
        for (id in manager.getGlanceIds(BannerWidget::class.java)) {
            val appWidgetId = manager.getAppWidgetId(id)
            val viewportWidthPx = getViewportWidthPx(context, appWidgetId)
            updateAppWidgetState(context, id) { prefs ->
                val paused = prefs[PausedKey] ?: false
                if (paused) return@updateAppWidgetState

                val settings = WidgetSettings.fromPreferences(prefs)
                val messages = settings.bannerMessages.ifEmpty {
                    WidgetSettings.DEFAULT_BANNER_MESSAGES
                }
                val messageIndex = prefs[MessageIndexKey] ?: 0
                val currentMsg = messages.getOrElse(messageIndex % messages.size) { messages[0] }
                val vibe = BannerVibe.entries.getOrElse(settings.bannerVibe) { BannerVibe.SCROLL }
                val fontScale = FontSizePreset.fromIndex(settings.bannerFontSizePreset).scaleFactor
                val textSizeSp = 18f * fontScale
                val textWidthPx = GlanceText.measureTextWidth(context, currentMsg.text, textSizeSp)

                // First tick or reset: set initial direction and offset
                if (prefs[CurrentLtrKey] == null) {
                    val ltr = initialLtr(settings.bannerScrollDirection)
                    setStartOffset(prefs, ltr, vibe, textWidthPx, viewportWidthPx)
                    return@updateAppWidgetState
                }

                // Blank gap between messages
                val gapUntil = prefs[GapUntilKey] ?: 0L
                if (gapUntil > 0L) {
                    if (System.currentTimeMillis() < gapUntil) return@updateAppWidgetState
                    // Gap ended — resolve next direction, advance message, set offset
                    prefs[GapUntilKey] = 0L
                    val currentLtr = prefs[CurrentLtrKey] ?: false
                    val nextLtr = nextLtr(settings.bannerScrollDirection, currentLtr)
                    prefs[MessageIndexKey] = (messageIndex + 1) % messages.size
                    val nextIndex = prefs[MessageIndexKey] ?: 0
                    val nextMsg = messages.getOrElse(nextIndex % messages.size) { messages[0] }
                    val nextTextWidthPx = GlanceText.measureTextWidth(context, nextMsg.text, textSizeSp)
                    setStartOffset(prefs, nextLtr, vibe, nextTextWidthPx, viewportWidthPx)
                    return@updateAppWidgetState
                }

                // Normal scrolling
                val step = scrollStepForSpeed(settings.bannerScrollSpeed)
                val offset = prefs[ScrollOffsetKey] ?: 0
                val ltr = prefs[CurrentLtrKey] ?: false
                val gapMs = settings.bannerGapSeconds * 1000L

                fun enterGap() {
                    prefs[ScrollOffsetKey] = 0
                    prefs[GapUntilKey] = System.currentTimeMillis() + gapMs
                }

                when (vibe) {
                    BannerVibe.SCROLL -> {
                        if (ltr) {
                            val newOffset = offset - step
                            if (newOffset < -viewportWidthPx) enterGap()
                            else prefs[ScrollOffsetKey] = newOffset
                        } else {
                            val newOffset = offset + step
                            if (newOffset > textWidthPx) enterGap()
                            else prefs[ScrollOffsetKey] = newOffset
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
                            if (!forward && messages.size > 1) enterGap()
                        } else {
                            prefs[ScrollOffsetKey] = newOffset
                        }
                    }
                }
            }
        }
    }
}
