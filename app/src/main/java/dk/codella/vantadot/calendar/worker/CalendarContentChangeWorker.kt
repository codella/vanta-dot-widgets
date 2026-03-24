package dk.codella.vantadot.calendar.worker

import android.content.Context
import android.provider.CalendarContract
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.updateAll
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dk.codella.vantadot.calendar.widget.CalendarWidget

class CalendarContentChangeWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val manager = GlanceAppWidgetManager(applicationContext)
        val ids = manager.getGlanceIds(CalendarWidget::class.java)
        ids.forEach { id ->
            CalendarWidget.refreshEventsIntoState(applicationContext, id)
        }
        CalendarWidget().updateAll(applicationContext)

        // Re-enqueue to keep listening for calendar changes
        enqueue(applicationContext)

        return Result.success()
    }

    companion object {
        const val WORK_NAME = "calendar_content_change"

        fun enqueue(context: Context) {
            val constraints = Constraints.Builder()
                .addContentUriTrigger(
                    CalendarContract.Events.CONTENT_URI,
                    triggerForDescendants = true,
                )
                .build()

            val request = OneTimeWorkRequestBuilder<CalendarContentChangeWorker>()
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request,
            )
        }
    }
}
