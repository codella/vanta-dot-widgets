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

        return listOf(
            CalendarEvent(
                id = 1,
                title = "Sprint planning",
                beginTime = todayAt(10),
                endTime = todayAt(11, 30),
                isAllDay = false,
                calendarColor = Color.parseColor("#4285F4"),
                location = "https://meet.google.com/abc-defg-hij",
                description = "Agenda: https://docs.google.com/document/d/abc123",
            ),
            CalendarEvent(
                id = 2,
                title = "1:1 with manager",
                beginTime = todayAt(14),
                endTime = todayAt(14, 30),
                isAllDay = false,
                calendarColor = Color.parseColor("#0F9D58"),
                location = null,
                description = "Notes: https://docs.google.com/document/d/xyz789",
            ),
            CalendarEvent(
                id = 3,
                title = "Design review",
                beginTime = todayAt(15),
                endTime = todayAt(16),
                isAllDay = false,
                calendarColor = Color.parseColor("#DB4437"),
                location = "https://zoom.us/j/123456789",
            ),
            CalendarEvent(
                id = 4,
                title = "Lunch with Anna",
                beginTime = todayAt(12),
                endTime = todayAt(13),
                isAllDay = false,
                calendarColor = Color.parseColor("#0F9D58"),
                location = "Café Norden",
            ),
            CalendarEvent(
                id = 5,
                title = "Dentist appointment",
                beginTime = todayAt(16, 30),
                endTime = todayAt(17),
                isAllDay = false,
                calendarColor = Color.parseColor("#F4B400"),
                location = "Tandlægen, Østerbro",
            ),
            CalendarEvent(
                id = 6,
                title = "Yoga class",
                beginTime = todayAt(18),
                endTime = todayAt(19),
                isAllDay = false,
                calendarColor = Color.parseColor("#0F9D58"),
                location = "Fitness World",
            ),
            CalendarEvent(
                id = 7,
                title = "Grocery shopping",
                beginTime = todayAt(17, 15),
                endTime = todayAt(18),
                isAllDay = false,
                calendarColor = Color.parseColor("#AB47BC"),
                location = "Irma, Frederiksberg",
            ),
            CalendarEvent(
                id = 8,
                title = "Call with client",
                beginTime = todayAt(11, 30),
                endTime = todayAt(12),
                isAllDay = false,
                calendarColor = Color.parseColor("#4285F4"),
                location = "https://teams.microsoft.com/l/meetup/abc",
            ),
            CalendarEvent(
                id = 9,
                title = "Pick up dry cleaning",
                beginTime = todayAt(13, 30),
                endTime = todayAt(14),
                isAllDay = false,
                calendarColor = Color.parseColor("#AB47BC"),
                location = null,
            ),
            CalendarEvent(
                id = 10,
                title = "Team retro",
                beginTime = todayAt(16),
                endTime = todayAt(16, 30),
                isAllDay = false,
                calendarColor = Color.parseColor("#4285F4"),
                location = "https://meet.google.com/xyz-abcd-efg",
                description = "Retro board: https://notion.so/retro-q1",
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
            CalendarEvent(
                id = 12,
                title = "Read chapter 5",
                beginTime = todayAt(20),
                endTime = todayAt(21),
                isAllDay = false,
                calendarColor = Color.parseColor("#F4B400"),
                location = null,
            ),
            CalendarEvent(
                id = 13,
                title = "Water the plants",
                beginTime = todayAt(8),
                endTime = todayAt(8, 15),
                isAllDay = false,
                calendarColor = Color.parseColor("#0F9D58"),
                location = null,
            ),
        )
    }
}
