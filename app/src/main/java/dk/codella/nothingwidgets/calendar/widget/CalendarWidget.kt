package dk.codella.nothingwidgets.calendar.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.provideContent
import dk.codella.nothingwidgets.calendar.data.CalendarRepository

class CalendarWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(
        setOf(
            CalendarWidgetSizes.COMPACT,
            CalendarWidgetSizes.EXPANDED,
            CalendarWidgetSizes.FULL,
        )
    )

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val repository = CalendarRepository(context)
        val hasPermission = repository.hasCalendarPermission()
        val events = if (hasPermission) repository.getUpcomingEvents() else emptyList()

        provideContent {
            CalendarWidgetContent(
                events = events,
                hasPermission = hasPermission,
            )
        }
    }
}
