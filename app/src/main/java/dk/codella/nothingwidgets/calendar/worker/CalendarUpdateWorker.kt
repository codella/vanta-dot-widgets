package dk.codella.nothingwidgets.calendar.worker

import android.content.Context
import androidx.glance.appwidget.updateAll
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dk.codella.nothingwidgets.calendar.widget.CalendarWidget

class CalendarUpdateWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        CalendarWidget().updateAll(applicationContext)
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "calendar_widget_update"
    }
}
