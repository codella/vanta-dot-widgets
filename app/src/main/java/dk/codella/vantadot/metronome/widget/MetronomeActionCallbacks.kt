package dk.codella.vantadot.metronome.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import dk.codella.vantadot.metronome.data.MetronomeStatus
import dk.codella.vantadot.metronome.data.MetronomeWidgetState
import dk.codella.vantadot.metronome.service.MetronomeService
import dk.codella.vantadot.settings.WidgetSettings

class PlayStopActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val appWidgetId = GlanceAppWidgetManager(context).getAppWidgetId(glanceId)

        updateAppWidgetState(context, glanceId) { prefs ->
            val state = MetronomeWidgetState.fromPreferences(prefs)
            val settings = WidgetSettings.fromPreferences(prefs)

            when (state.status) {
                MetronomeStatus.IDLE -> {
                    MetronomeWidgetState.writeTo(prefs, state.copy(
                        status = MetronomeStatus.PLAYING,
                        currentBeat = 0,
                        beatsPerBar = settings.metronomeBeatsPerBar,
                    ))

                    context.startForegroundService(
                        MetronomeService.startIntent(
                            context, appWidgetId, state.bpm,
                            settings.metronomeBeatsPerBar,
                            settings.metronomeAccentFirstBeat,
                            settings.metronomeSoundChoice,
                            settings.metronomeVibration,
                        )
                    )
                }
                MetronomeStatus.PLAYING -> {
                    MetronomeWidgetState.writeTo(prefs, state.copy(
                        status = MetronomeStatus.IDLE,
                        currentBeat = 0,
                    ))
                    context.startService(MetronomeService.stopIntent(context, appWidgetId))
                }
            }
        }

        MetronomeWidget().update(context, glanceId)
    }
}

val MetronomeDeltaParam = ActionParameters.Key<Int>("delta")

class AdjustBpmActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val delta = parameters[MetronomeDeltaParam] ?: 1

        updateAppWidgetState(context, glanceId) { prefs ->
            val state = MetronomeWidgetState.fromPreferences(prefs)
            if (state.status != MetronomeStatus.IDLE) return@updateAppWidgetState

            val newBpm = (state.bpm + delta).coerceIn(
                MetronomeWidgetState.MIN_BPM,
                MetronomeWidgetState.MAX_BPM,
            )
            MetronomeWidgetState.writeTo(prefs, state.copy(bpm = newBpm))
        }

        MetronomeWidget().update(context, glanceId)
    }
}

val MetronomeForwardParam = ActionParameters.Key<Boolean>("forward")

class CycleMetronomePresetActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        val forward = parameters[MetronomeForwardParam] ?: true

        updateAppWidgetState(context, glanceId) { prefs ->
            val state = MetronomeWidgetState.fromPreferences(prefs)
            if (state.status != MetronomeStatus.IDLE) return@updateAppWidgetState

            val settings = WidgetSettings.fromPreferences(prefs)
            val presetBpms = settings.metronomePresets.map { it.bpm }
            if (presetBpms.isEmpty()) return@updateAppWidgetState

            val currentIndex = presetBpms.indexOf(state.bpm)
            val size = presetBpms.size
            val nextIndex = if (currentIndex == -1) 0
            else if (forward) (currentIndex + 1) % size
            else (currentIndex - 1 + size) % size

            MetronomeWidgetState.writeTo(prefs, state.copy(bpm = presetBpms[nextIndex]))
        }

        MetronomeWidget().update(context, glanceId)
    }
}
