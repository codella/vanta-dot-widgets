package dk.codella.vantadot.timer.widget

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.Preferences
import androidx.glance.GlanceId
import androidx.glance.LocalSize
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import androidx.glance.currentState
import androidx.glance.state.PreferencesGlanceStateDefinition
import dk.codella.vantadot.timer.data.TimerState

class TimerWidget : GlanceAppWidget() {

    override val stateDefinition = PreferencesGlanceStateDefinition

    override val sizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            val prefs = currentState<Preferences>()
            val timerState = TimerState.fromPreferences(prefs)
            val settings = TimerSettings.fromPreferences(prefs)

            val size = LocalSize.current
            Log.d(TAG, "provideContent: size=${size.width}x${size.height}, mode=${timerState.mode}, running=${timerState.isRunning}")

            TimerWidgetContent(
                timerState = timerState,
                settings = settings,
            )
        }
    }

    companion object {
        private const val TAG = "TimerWidget"
    }
}