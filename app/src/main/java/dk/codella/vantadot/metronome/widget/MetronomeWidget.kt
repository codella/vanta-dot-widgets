package dk.codella.vantadot.metronome.widget

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.state.PreferencesGlanceStateDefinition
import dk.codella.vantadot.metronome.data.MetronomeWidgetState
import dk.codella.vantadot.settings.WidgetSettings

class MetronomeWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val metronomeState = MetronomeWidgetState.fromPreferences(prefs)
            val settings = WidgetSettings.fromPreferences(prefs)

            MetronomeWidgetContent(
                metronomeState = metronomeState,
                fontSizePreset = settings.fontSizePreset,
                accentColorIndex = settings.accentColorIndex,
                presets = settings.metronomePresets,
            )
        }
    }
}
