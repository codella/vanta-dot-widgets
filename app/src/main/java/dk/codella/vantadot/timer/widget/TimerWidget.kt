package dk.codella.vantadot.timer.widget

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.state.PreferencesGlanceStateDefinition
import dk.codella.vantadot.settings.WidgetSettings
import dk.codella.vantadot.timer.data.TimerWidgetState

class TimerWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val timerState = TimerWidgetState.fromPreferences(prefs)
            val settings = WidgetSettings.fromPreferences(prefs)

            TimerWidgetContent(
                timerState = timerState,
                fontSizePreset = settings.fontSizePreset,
                accentColorIndex = settings.accentColorIndex,
                presets = settings.timerPresets,
            )
        }
    }
}
