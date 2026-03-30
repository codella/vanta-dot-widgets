package dk.codella.vantadot.banner.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import dk.codella.vantadot.settings.WidgetSettings

val TapParam = ActionParameters.Key<Boolean>("tap")

class TapActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters,
    ) {
        updateAppWidgetState(context, glanceId) { prefs ->
            val paused = prefs[BannerScrollTickHandler.PausedKey] ?: false
            if (paused) {
                // Resume scrolling
                prefs[BannerScrollTickHandler.PausedKey] = false
            } else {
                // Pause scrolling
                prefs[BannerScrollTickHandler.PausedKey] = true
            }
        }
        BannerWidget().update(context, glanceId)
    }
}
