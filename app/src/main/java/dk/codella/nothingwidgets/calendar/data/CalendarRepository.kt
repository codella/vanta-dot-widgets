package dk.codella.nothingwidgets.calendar.data

import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CalendarRepository(private val context: Context) {

    companion object {
        private val PROJECTION = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.CALENDAR_COLOR,
            CalendarContract.Instances.EVENT_LOCATION,
        )

        private const val COL_EVENT_ID = 0
        private const val COL_TITLE = 1
        private const val COL_BEGIN = 2
        private const val COL_END = 3
        private const val COL_ALL_DAY = 4
        private const val COL_COLOR = 5
        private const val COL_LOCATION = 6

        private const val SEVEN_DAYS_MS = 7L * 24 * 60 * 60 * 1000
    }

    suspend fun getUpcomingEvents(maxCount: Int = 8): List<CalendarEvent> =
        withContext(Dispatchers.IO) {
            if (!hasCalendarPermission()) return@withContext emptyList()

            val now = System.currentTimeMillis()
            val end = now + SEVEN_DAYS_MS

            val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
            ContentUris.appendId(builder, now)
            ContentUris.appendId(builder, end)

            val events = mutableListOf<CalendarEvent>()

            context.contentResolver.query(
                builder.build(),
                PROJECTION,
                null,
                null,
                "${CalendarContract.Instances.BEGIN} ASC",
            )?.use { cursor ->
                while (cursor.moveToNext() && events.size < maxCount) {
                    events.add(
                        CalendarEvent(
                            id = cursor.getLong(COL_EVENT_ID),
                            title = cursor.getString(COL_TITLE) ?: "",
                            beginTime = cursor.getLong(COL_BEGIN),
                            endTime = cursor.getLong(COL_END),
                            isAllDay = cursor.getInt(COL_ALL_DAY) == 1,
                            calendarColor = cursor.getInt(COL_COLOR),
                            location = cursor.getString(COL_LOCATION),
                        )
                    )
                }
            }

            events
        }

    fun hasCalendarPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CALENDAR,
        ) == PackageManager.PERMISSION_GRANTED
}
