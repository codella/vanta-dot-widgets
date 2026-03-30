package dk.codella.vantadot.banner.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.PowerManager
import android.os.SystemClock
import android.widget.RemoteViews
import dk.codella.vantadot.R
import dk.codella.vantadot.banner.data.BannerScrollDirection
import dk.codella.vantadot.common.GlanceText
import dk.codella.vantadot.settings.AccentColorPreset
import dk.codella.vantadot.settings.FontSizePreset
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class BannerWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        for (id in appWidgetIds) {
            setupWidget(context, appWidgetManager, id)
        }
        BannerAnimator.startIfNotRunning(context)
        scheduleKeepalive(context)
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: android.os.Bundle,
    ) {
        setupWidget(context, appWidgetManager, appWidgetId)
        BannerAnimator.startIfNotRunning(context)
    }

    override fun onDisabled(context: Context) {
        BannerAnimator.stop()
        cancelKeepalive(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_TAP -> {
                val id = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                if (id != -1) BannerAnimator.togglePause(id)
                return
            }
            ACTION_KEEPALIVE -> {
                BannerAnimator.startIfNotRunning(context)
                return
            }
        }
        super.onReceive(context, intent)
    }

    companion object {
        const val ACTION_TAP = "dk.codella.vantadot.BANNER_TAP"
        const val ACTION_KEEPALIVE = "dk.codella.vantadot.BANNER_KEEPALIVE"
        private const val KEEPALIVE_INTERVAL_MS = 60_000L

        private fun keepalivePendingIntent(context: Context): PendingIntent {
            val intent = Intent(context, BannerWidgetProvider::class.java).apply {
                action = ACTION_KEEPALIVE
            }
            return PendingIntent.getBroadcast(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
        }

        fun scheduleKeepalive(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + KEEPALIVE_INTERVAL_MS,
                KEEPALIVE_INTERVAL_MS,
                keepalivePendingIntent(context),
            )
        }

        private fun cancelKeepalive(context: Context) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(keepalivePendingIntent(context))
        }

        /** Full update — sets layout, click handler, and bitmap. Used on setup/resize. */
        fun setupWidget(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
            val bitmap = renderBitmap(context, manager, appWidgetId)

            val views = RemoteViews(context.packageName, R.layout.banner_widget_layout)
            views.setImageViewBitmap(R.id.banner_image, bitmap)

            val tapIntent = Intent(context, BannerWidgetProvider::class.java).apply {
                action = ACTION_TAP
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val tapPending = PendingIntent.getBroadcast(
                context, appWidgetId, tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            views.setOnClickPendingIntent(R.id.banner_root, tapPending)

            manager.updateAppWidget(appWidgetId, views)
        }

        /** Lightweight partial update — just the bitmap. Used for animation frames. */
        fun pushBitmap(context: Context, manager: AppWidgetManager, appWidgetId: Int) {
            val bitmap = renderBitmap(context, manager, appWidgetId)
            val views = RemoteViews(context.packageName, R.layout.banner_widget_layout)
            views.setImageViewBitmap(R.id.banner_image, bitmap)
            manager.partiallyUpdateAppWidget(appWidgetId, views)
        }

        private fun renderBitmap(
            context: Context,
            manager: AppWidgetManager,
            appWidgetId: Int,
        ): android.graphics.Bitmap {
            val state = BannerAnimator.getState(appWidgetId)
            val settings = BannerPrefs.load(context, appWidgetId)
            val fontScale = FontSizePreset.fromIndex(settings.fontSizePreset).scaleFactor
            val accent = AccentColorPreset.fromIndex(settings.accentColorIndex)
            val textSizeSp = 18f * fontScale

            val options = manager.getAppWidgetOptions(appWidgetId)
            val widthDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 120)
            val heightDp = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 55)
            val paddingDp = 12f
            val viewportW = (widthDp - 2 * paddingDp).coerceAtLeast(1f)
            val viewportH = (heightDp - 2 * paddingDp).coerceAtLeast(1f)

            val message = state.currentMessage(settings)
            val text = if (state.inGap) "" else message

            return GlanceText.renderMarqueeFrame(
                context, text, viewportW, viewportH, state.offset, textSizeSp,
                accent.swatchColor.toArgb(),
            )
        }

        private fun androidx.compose.ui.graphics.Color.toArgb(): Int {
            return android.graphics.Color.argb(
                (alpha * 255).toInt(),
                (red * 255).toInt(),
                (green * 255).toInt(),
                (blue * 255).toInt(),
            )
        }
    }
}

/** In-memory scroll state per widget — no DataStore writes per frame. */
data class BannerScrollState(
    var offset: Int = 0,
    var ltr: Boolean = false,
    var paused: Boolean = false,
    var inGap: Boolean = false,
    var gapEndMs: Long = 0L,
    var messageIndex: Int = 0,
    var initialized: Boolean = false,
) {
    fun currentMessage(settings: BannerSettings): String {
        val msgs = settings.messages.ifEmpty { listOf("EDIT ME IN WIDGET SETTINGS") }
        return msgs[messageIndex % msgs.size]
    }
}

/** Lightweight settings read from SharedPreferences (written by settings activity). */
data class BannerSettings(
    val messages: List<String> = listOf("EDIT ME IN WIDGET SETTINGS"),
    val vibe: Int = 0,
    val scrollSpeed: Int = 5,
    val scrollDirection: Int = 0,
    val gapSeconds: Int = 1,
    val accentColorIndex: Int = 0,
    val fontSizePreset: Int = 1,
)

object BannerPrefs {
    private const val PREFS_PREFIX = "banner_widget_"

    fun load(context: Context, appWidgetId: Int): BannerSettings {
        val prefs = context.getSharedPreferences("${PREFS_PREFIX}$appWidgetId", Context.MODE_PRIVATE)
        val messagesJson = prefs.getString("messages", null)
        val messages = if (messagesJson != null) {
            try {
                val arr = org.json.JSONArray(messagesJson)
                (0 until arr.length()).map { arr.getString(it) }.filter { it.isNotEmpty() }
            } catch (_: Exception) { emptyList() }
        } else emptyList()

        return BannerSettings(
            messages = messages,
            vibe = prefs.getInt("vibe", 0),
            scrollSpeed = prefs.getInt("scroll_speed", 5),
            scrollDirection = prefs.getInt("scroll_direction", 0),
            gapSeconds = prefs.getInt("gap_seconds", 1),
            accentColorIndex = prefs.getInt("accent_color_index", 0),
            fontSizePreset = prefs.getInt("font_size_preset", 1),
        )
    }

    fun save(context: Context, appWidgetId: Int, settings: BannerSettings) {
        val prefs = context.getSharedPreferences("${PREFS_PREFIX}$appWidgetId", Context.MODE_PRIVATE)
        val arr = org.json.JSONArray()
        settings.messages.forEach { arr.put(it) }
        prefs.edit()
            .putString("messages", arr.toString())
            .putInt("vibe", settings.vibe)
            .putInt("scroll_speed", settings.scrollSpeed)
            .putInt("scroll_direction", settings.scrollDirection)
            .putInt("gap_seconds", settings.gapSeconds)
            .putInt("accent_color_index", settings.accentColorIndex)
            .putInt("font_size_preset", settings.fontSizePreset)
            .apply()
    }
}

object BannerAnimator {
    @Volatile
    private var job: Job? = null
    private val states = mutableMapOf<Int, BannerScrollState>()

    private const val TICK_MS = 40L // 25 FPS — fast and smooth, no DataStore writes

    fun getState(appWidgetId: Int): BannerScrollState =
        states.getOrPut(appWidgetId) { BannerScrollState() }

    fun togglePause(appWidgetId: Int) {
        val state = getState(appWidgetId)
        state.paused = !state.paused
    }

    fun resetState(appWidgetId: Int) {
        states.remove(appWidgetId)
    }

    fun start(context: Context) {
        val ctx = context.applicationContext
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            while (isActive) {
                val pm = ctx.getSystemService(Context.POWER_SERVICE) as PowerManager
                if (pm.isInteractive) {
                    try {
                        tick(ctx)
                    } catch (e: CancellationException) {
                        throw e
                    } catch (_: Throwable) {}
                    delay(TICK_MS)
                } else {
                    delay(1000)
                }
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

    private fun speedPxPerSec(speed: Int): Float {
        val s = speed.coerceIn(1, 10)
        return (s * s / 3 + 1) * 25f
    }

    private fun getViewportWidthPx(context: Context, manager: AppWidgetManager, appWidgetId: Int): Int {
        val density = context.resources.displayMetrics.density
        val widthDp = manager.getAppWidgetOptions(appWidgetId)
            .getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 120)
        return ((widthDp - 24f) * density).toInt().coerceAtLeast(1)
    }

    private fun getTextWidthPx(context: Context, settings: BannerSettings, text: String): Int {
        val fontScale = FontSizePreset.fromIndex(settings.fontSizePreset).scaleFactor
        return GlanceText.measureTextWidth(context, text, 18f * fontScale)
    }

    private fun tick(context: Context) {
        val manager = AppWidgetManager.getInstance(context)
        val component = ComponentName(context, BannerWidgetProvider::class.java)
        val widgetIds = manager.getAppWidgetIds(component)
        if (widgetIds.isEmpty()) return

        for (appWidgetId in widgetIds) {
            val settings = BannerPrefs.load(context, appWidgetId)
            val state = getState(appWidgetId)
            val viewportWidthPx = getViewportWidthPx(context, manager, appWidgetId)
            val msg = state.currentMessage(settings)
            val textWidthPx = getTextWidthPx(context, settings, msg)

            // Initialize
            if (!state.initialized) {
                state.initialized = true
                state.ltr = resolveInitialLtr(settings.scrollDirection)
                state.offset = if (state.ltr) textWidthPx else -viewportWidthPx
            }

            if (state.paused) {
                BannerWidgetProvider.pushBitmap(context, manager, appWidgetId)
                continue
            }

            // In gap
            if (state.inGap) {
                if (System.currentTimeMillis() >= state.gapEndMs) {
                    state.inGap = false
                    val msgs = settings.messages.ifEmpty { listOf("EDIT ME IN WIDGET SETTINGS") }
                    state.messageIndex = (state.messageIndex + 1) % msgs.size
                    state.ltr = resolveNextLtr(settings.scrollDirection, state.ltr)
                    val nextMsg = state.currentMessage(settings)
                    val nextTextWidthPx = getTextWidthPx(context, settings, nextMsg)
                    state.offset = if (state.ltr) nextTextWidthPx else -viewportWidthPx
                }
                BannerWidgetProvider.pushBitmap(context, manager, appWidgetId)
                continue
            }

            // Advance
            val pxPerSec = speedPxPerSec(settings.scrollSpeed)
            val step = (TICK_MS * pxPerSec / 1000f).toInt().coerceAtLeast(1)

            state.offset += if (state.ltr) -step else step
            val completed = if (state.ltr) {
                state.offset < -viewportWidthPx
            } else {
                state.offset > textWidthPx
            }
            if (completed) {
                state.inGap = true
                state.gapEndMs = System.currentTimeMillis() + settings.gapSeconds * 1000L
            }

            BannerWidgetProvider.pushBitmap(context, manager, appWidgetId)
        }
    }

    private fun resolveInitialLtr(directionSetting: Int): Boolean {
        val dir = BannerScrollDirection.entries.getOrElse(directionSetting) {
            BannerScrollDirection.RIGHT_TO_LEFT
        }
        return when (dir) {
            BannerScrollDirection.RIGHT_TO_LEFT -> false
            BannerScrollDirection.LEFT_TO_RIGHT -> true
            BannerScrollDirection.ALTERNATE -> false
            BannerScrollDirection.RANDOM -> Math.random() > 0.5
        }
    }

    private fun resolveNextLtr(directionSetting: Int, currentLtr: Boolean): Boolean {
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
}
