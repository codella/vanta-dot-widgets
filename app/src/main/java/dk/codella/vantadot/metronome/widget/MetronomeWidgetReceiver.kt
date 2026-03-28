package dk.codella.vantadot.metronome.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import dk.codella.vantadot.metronome.service.MetronomeService

class MetronomeWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MetronomeWidget()

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        for (id in appWidgetIds) {
            try {
                context.startService(MetronomeService.stopIntent(context, id))
            } catch (_: Exception) {}
        }
    }
}
