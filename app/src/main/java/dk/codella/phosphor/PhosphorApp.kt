package dk.codella.phosphor

import android.app.Application
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dk.codella.phosphor.calendar.worker.CalendarUpdateWorker
import java.util.concurrent.TimeUnit

class PhosphorApp : Application() {

    override fun onCreate() {
        super.onCreate()
        enqueueCalendarUpdates()
    }

    private fun enqueueCalendarUpdates() {
        val constraints = Constraints.Builder()
            .setRequiresBatteryNotLow(true)
            .build()

        val workRequest = PeriodicWorkRequestBuilder<CalendarUpdateWorker>(
            15, TimeUnit.MINUTES,
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            CalendarUpdateWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest,
        )
    }
}
