package dk.codella.vantadot

import android.app.Application
import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dk.codella.vantadot.calendar.worker.CalendarContentChangeWorker
import dk.codella.vantadot.calendar.worker.CalendarUpdateWorker
import java.util.concurrent.TimeUnit

class VantaDotApp : Application() {

    override fun onCreate() {
        super.onCreate()
        enqueuePeriodicCalendarUpdates(this)
        CalendarContentChangeWorker.enqueue(this)
    }

    companion object {
        fun enqueuePeriodicCalendarUpdates(context: Context, intervalMinutes: Long = 15) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(true)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<CalendarUpdateWorker>(
                intervalMinutes, TimeUnit.MINUTES,
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                CalendarUpdateWorker.WORK_NAME,
                ExistingPeriodicWorkPolicy.REPLACE,
                workRequest,
            )
        }
    }
}
