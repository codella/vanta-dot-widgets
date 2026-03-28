package dk.codella.vantadot.metronome.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.VibratorManager
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.appwidget.updateAll
import dk.codella.vantadot.R
import dk.codella.vantadot.metronome.data.MetronomeSoundChoice
import dk.codella.vantadot.metronome.data.MetronomeStatus
import dk.codella.vantadot.metronome.data.MetronomeWidgetState
import dk.codella.vantadot.metronome.widget.MetronomeWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MetronomeService : Service() {

    companion object {
        const val EXTRA_APP_WIDGET_ID = "app_widget_id"
        const val EXTRA_BPM = "bpm"
        const val EXTRA_BEATS_PER_BAR = "beats_per_bar"
        const val EXTRA_ACCENT_FIRST_BEAT = "accent_first_beat"
        const val EXTRA_SOUND_CHOICE = "sound_choice"
        const val EXTRA_VIBRATION = "vibration"
        const val ACTION_START = "dk.codella.vantadot.metronome.START"
        const val ACTION_STOP = "dk.codella.vantadot.metronome.STOP"

        private const val NOTIFICATION_ID = 9001
        private const val CHANNEL_ID = "metronome_running"

        fun startIntent(context: Context, appWidgetId: Int, bpm: Int, beatsPerBar: Int,
                        accentFirstBeat: Boolean, soundChoice: Int, vibration: Boolean): Intent {
            return Intent(context, MetronomeService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_APP_WIDGET_ID, appWidgetId)
                putExtra(EXTRA_BPM, bpm)
                putExtra(EXTRA_BEATS_PER_BAR, beatsPerBar)
                putExtra(EXTRA_ACCENT_FIRST_BEAT, accentFirstBeat)
                putExtra(EXTRA_SOUND_CHOICE, soundChoice)
                putExtra(EXTRA_VIBRATION, vibration)
            }
        }

        fun stopIntent(context: Context, appWidgetId: Int): Intent {
            return Intent(context, MetronomeService::class.java).apply {
                action = ACTION_STOP
                putExtra(EXTRA_APP_WIDGET_ID, appWidgetId)
            }
        }
    }

    private data class MetronomeInstance(
        val appWidgetId: Int,
        val bpm: Int,
        val beatsPerBar: Int,
        val accentFirstBeat: Boolean,
        val vibrationEnabled: Boolean,
        val normalSoundId: Int,
        val accentSoundId: Int,
        var currentBeat: Int = 0,
        var tickRunnable: Runnable? = null,
    )

    private val instances = mutableMapOf<Int, MetronomeInstance>()
    private var soundPool: SoundPool? = null
    private var handler: Handler? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null) {
            stopSelf()
            return START_NOT_STICKY
        }

        val appWidgetId = intent.getIntExtra(EXTRA_APP_WIDGET_ID, -1)
        if (appWidgetId == -1) {
            if (instances.isEmpty()) stopSelf()
            return START_NOT_STICKY
        }

        when (intent.action) {
            ACTION_START -> handleStart(intent, appWidgetId)
            ACTION_STOP -> handleStop(appWidgetId)
            else -> {
                if (instances.isEmpty()) stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    private fun handleStart(intent: Intent, appWidgetId: Int) {
        // Stop existing instance for this widget if any
        stopInstance(appWidgetId)

        // Ensure shared resources
        if (soundPool == null) {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            soundPool = SoundPool.Builder()
                .setMaxStreams(4)
                .setAudioAttributes(audioAttributes)
                .build()
        }
        if (handler == null) {
            handler = Handler(Looper.getMainLooper())
        }

        val bpm = intent.getIntExtra(EXTRA_BPM, 120)
        val beatsPerBar = intent.getIntExtra(EXTRA_BEATS_PER_BAR, 4)
        val accentFirstBeat = intent.getBooleanExtra(EXTRA_ACCENT_FIRST_BEAT, true)
        val soundChoiceIndex = intent.getIntExtra(EXTRA_SOUND_CHOICE, 0)
        val vibrationEnabled = intent.getBooleanExtra(EXTRA_VIBRATION, false)

        val choice = MetronomeSoundChoice.fromIndex(soundChoiceIndex)
        val normalId = soundPool!!.load(this, choice.normalResId, 1)
        val accentId = soundPool!!.load(this, choice.accentResId, 1)

        val instance = MetronomeInstance(
            appWidgetId = appWidgetId,
            bpm = bpm,
            beatsPerBar = beatsPerBar,
            accentFirstBeat = accentFirstBeat,
            vibrationEnabled = vibrationEnabled,
            normalSoundId = normalId,
            accentSoundId = accentId,
        )
        instances[appWidgetId] = instance

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, buildNotification())
        startTicking(instance)
    }

    private fun handleStop(appWidgetId: Int) {
        stopInstance(appWidgetId)
        resetWidgetState(appWidgetId)

        if (instances.isEmpty()) {
            stopSelf()
        } else {
            // Update notification to reflect remaining instances
            val nm = getSystemService(NotificationManager::class.java)
            nm.notify(NOTIFICATION_ID, buildNotification())
        }
    }

    private fun stopInstance(appWidgetId: Int) {
        val instance = instances.remove(appWidgetId) ?: return
        instance.tickRunnable?.let { handler?.removeCallbacks(it) }
        instance.tickRunnable = null
    }

    private fun startTicking(instance: MetronomeInstance) {
        val intervalMs = 60_000L / instance.bpm

        val runnable = object : Runnable {
            override fun run() {
                val inst = instances[instance.appWidgetId] ?: return
                playBeat(inst)
                val beat = inst.currentBeat
                updateWidgetBeat(inst.appWidgetId, beat)
                inst.currentBeat = (inst.currentBeat + 1) % inst.beatsPerBar
                handler?.postDelayed(this, intervalMs)
            }
        }
        instance.tickRunnable = runnable
        handler?.post(runnable)
    }

    private fun playBeat(instance: MetronomeInstance) {
        val isAccent = instance.accentFirstBeat && instance.currentBeat == 0
        val soundId = if (isAccent) instance.accentSoundId else instance.normalSoundId
        soundPool?.play(soundId, 1f, 1f, 1, 0, 1f)

        if (instance.vibrationEnabled) {
            val vibrator = getSystemService(VibratorManager::class.java)?.defaultVibrator
            vibrator?.vibrate(
                VibrationEffect.createOneShot(
                    if (isAccent) 30L else 15L,
                    if (isAccent) VibrationEffect.DEFAULT_AMPLITUDE else 80,
                )
            )
        }
    }

    private fun updateWidgetBeat(appWidgetId: Int, beat: Int) {
        serviceScope.launch {
            try {
                val manager = GlanceAppWidgetManager(applicationContext)
                val glanceId = manager.getGlanceIdBy(appWidgetId)
                updateAppWidgetState(applicationContext, glanceId) { prefs ->
                    prefs[MetronomeWidgetState.CurrentBeatKey] = beat
                }
                MetronomeWidget().updateAll(applicationContext)
            } catch (_: Exception) {}
        }
    }

    private fun resetWidgetState(appWidgetId: Int) {
        serviceScope.launch {
            try {
                val manager = GlanceAppWidgetManager(applicationContext)
                val glanceId = manager.getGlanceIdBy(appWidgetId)
                updateAppWidgetState(applicationContext, glanceId) { prefs ->
                    val state = MetronomeWidgetState.fromPreferences(prefs)
                    MetronomeWidgetState.writeTo(prefs, state.copy(
                        status = MetronomeStatus.IDLE,
                        currentBeat = 0,
                    ))
                }
                MetronomeWidget().updateAll(applicationContext)
            } catch (_: Exception) {}
        }
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Metronome",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Shown while the metronome is running"
            setSound(null, null)
        }
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification {
        val text = instances.values.joinToString(", ") { "${it.bpm} BPM" }
        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Metronome")
            .setContentText(text.ifEmpty { "Running" })
            .setOngoing(true)
            .build()
    }

    override fun onDestroy() {
        // Stop all instances
        for (instance in instances.values) {
            instance.tickRunnable?.let { handler?.removeCallbacks(it) }
        }
        val widgetIds = instances.keys.toList()
        instances.clear()
        handler = null
        soundPool?.release()
        soundPool = null

        // Reset all widget states
        for (id in widgetIds) {
            resetWidgetState(id)
        }

        super.onDestroy()
    }
}
