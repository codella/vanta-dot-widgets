package dk.codella.vantadot.calendar.data

import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

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
            CalendarContract.Instances.DESCRIPTION,
            CalendarContract.Instances.SELF_ATTENDEE_STATUS,
            CalendarContract.Instances.CALENDAR_ID,
        )

        private const val COL_EVENT_ID = 0
        private const val COL_TITLE = 1
        private const val COL_BEGIN = 2
        private const val COL_END = 3
        private const val COL_ALL_DAY = 4
        private const val COL_COLOR = 5
        private const val COL_LOCATION = 6
        private const val COL_DESCRIPTION = 7
        private const val COL_SELF_ATTENDEE_STATUS = 8
        private const val COL_CALENDAR_ID = 9

        private fun endOfToday(): Long = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
            set(Calendar.MILLISECOND, 999)
        }.timeInMillis
    }

    suspend fun getUpcomingEvents(
        maxCount: Int = 20,
    ): List<CalendarEvent> =
        withContext(Dispatchers.IO) {
            if (!hasCalendarPermission()) return@withContext emptyList()

            val now = System.currentTimeMillis()
            val end = endOfToday()

            val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
            ContentUris.appendId(builder, now)
            ContentUris.appendId(builder, end)

            val selection = "${CalendarContract.Instances.SELF_ATTENDEE_STATUS} != ?"
            val selectionArgs = arrayOf("${CalendarEvent.ATTENDEE_STATUS_DECLINED}")

            val events = mutableListOf<CalendarEvent>()

            context.contentResolver.query(
                builder.build(),
                PROJECTION,
                selection,
                selectionArgs,
                "${CalendarContract.Instances.BEGIN} ASC",
            )?.use { cursor ->
                while (cursor.moveToNext() && events.size < maxCount) {
                    val endTime = cursor.getLong(COL_END)
                    val isAllDay = cursor.getInt(COL_ALL_DAY) == 1
                    if (!isAllDay && endTime <= now) continue
                    events.add(
                        CalendarEvent(
                            id = cursor.getLong(COL_EVENT_ID),
                            title = cursor.getString(COL_TITLE) ?: "",
                            beginTime = cursor.getLong(COL_BEGIN),
                            endTime = endTime,
                            isAllDay = isAllDay,
                            calendarColor = cursor.getInt(COL_COLOR),
                            location = cursor.getString(COL_LOCATION),
                            description = cursor.getString(COL_DESCRIPTION),
                            selfAttendeeStatus = cursor.getInt(COL_SELF_ATTENDEE_STATUS),
                        )
                    )
                }
            }

            events
        }

    suspend fun getCalendars(): List<CalendarInfo> = withContext(Dispatchers.IO) {
        if (!hasCalendarPermission()) return@withContext emptyList()

        val calendars = mutableListOf<CalendarInfo>()
        context.contentResolver.query(
            CalendarContract.Calendars.CONTENT_URI,
            arrayOf(
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                CalendarContract.Calendars.CALENDAR_COLOR,
                CalendarContract.Calendars.ACCOUNT_NAME,
            ),
            "${CalendarContract.Calendars.VISIBLE} = 1",
            null,
            "${CalendarContract.Calendars.ACCOUNT_NAME} ASC, ${CalendarContract.Calendars.CALENDAR_DISPLAY_NAME} ASC",
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                calendars.add(
                    CalendarInfo(
                        id = cursor.getLong(0),
                        displayName = cursor.getString(1) ?: "",
                        color = cursor.getInt(2),
                        accountName = cursor.getString(3) ?: "",
                    )
                )
            }
        }
        calendars
    }

    fun hasCalendarPermission(): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CALENDAR,
        ) == PackageManager.PERMISSION_GRANTED
}
