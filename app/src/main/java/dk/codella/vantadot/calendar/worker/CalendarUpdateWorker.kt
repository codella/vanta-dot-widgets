package dk.codella.vantadot.calendar.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dk.codella.vantadot.calendar.widget.CalendarWidget

class CalendarUpdateWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        CalendarWidget.refreshAllAndUpdate(applicationContext)
        return Result.success()
    }

    companion object {
        const val WORK_NAME = "calendar_widget_update"
    }
}
