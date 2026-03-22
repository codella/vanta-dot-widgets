package dk.codella.phosphor.calendar.data

import android.graphics.Color
import java.util.Calendar

object StubCalendarData {

    fun getEvents(): List<CalendarEvent> {
        val now = Calendar.getInstance()
        val base = Calendar.getInstance().apply {
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        fun todayAt(hour: Int, minute: Int = 0): Long = (base.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }.timeInMillis

        val nowMs = System.currentTimeMillis()
        val min = 60_000L

        return listOf(
            // IN PROGRESS — started 5 min ago, ends in 25 min
            CalendarEvent(
                id = 1,
                title = "Sprint planning",
                beginTime = nowMs - 5 * min,
                endTime = nowMs + 25 * min,
                isAllDay = false,
                calendarColor = Color.parseColor("#4285F4"),
                location = "https://meet.google.com/abc-defg-hij",
                description = "Agenda: https://docs.google.com/document/d/abc123",
            ),
            // HIGH — starts in 1 min
            CalendarEvent(
                id = 2,
                title = "1:1 with manager",
                beginTime = nowMs + 1 * min,
                endTime = nowMs + 31 * min,
                isAllDay = false,
                calendarColor = Color.parseColor("#0F9D58"),
                location = null,
                description = "Notes: https://docs.google.com/document/d/xyz789",
            ),
            // MEDIUM — starts in 4 min
            CalendarEvent(
                id = 3,
                title = "Design review",
                beginTime = nowMs + 4 * min,
                endTime = nowMs + 64 * min,
                isAllDay = false,
                calendarColor = Color.parseColor("#DB4437"),
                location = "https://zoom.us/j/123456789",
            ),
            // LOW — starts in 7 min
            CalendarEvent(
                id = 4,
                title = "Lunch with Anna",
                beginTime = nowMs + 7 * min,
                endTime = nowMs + 67 * min,
                isAllDay = false,
                calendarColor = Color.parseColor("#0F9D58"),
                location = "Café Norden",
            ),
            // SUBTLE — starts in 20 min
            CalendarEvent(
                id = 5,
                title = "Dentist appointment",
                beginTime = nowMs + 20 * min,
                endTime = nowMs + 50 * min,
                isAllDay = false,
                calendarColor = Color.parseColor("#F4B400"),
                location = "Tandlægen, Østerbro",
            ),
            // NONE — starts in 2 hours
            CalendarEvent(
                id = 6,
                title = "Yoga class",
                beginTime = nowMs + 120 * min,
                endTime = nowMs + 180 * min,
                isAllDay = false,
                calendarColor = Color.parseColor("#0F9D58"),
                location = "Fitness World",
            ),
            CalendarEvent(
                id = 7,
                title = "Read chapter 5",
                beginTime = nowMs + 180 * min,
                endTime = nowMs + 240 * min,
                isAllDay = false,
                calendarColor = Color.parseColor("#F4B400"),
                location = null,
            ),
            CalendarEvent(
                id = 11,
                title = "Birthday party prep",
                beginTime = todayAt(0),
                endTime = todayAt(23, 59),
                isAllDay = true,
                calendarColor = Color.parseColor("#AB47BC"),
                location = null,
            ),
        )
    }
}
