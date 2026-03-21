package dk.codella.phosphor.calendar.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarRepositoryTest {

    @Test
    fun `CalendarEvent stores all fields correctly`() {
        val event = CalendarEvent(
            id = 1L,
            title = "Meeting",
            beginTime = 1000L,
            endTime = 2000L,
            isAllDay = false,
            calendarColor = -16776961,
            location = "Room A",
        )

        assertEquals(1L, event.id)
        assertEquals("Meeting", event.title)
        assertEquals(1000L, event.beginTime)
        assertEquals(2000L, event.endTime)
        assertEquals(false, event.isAllDay)
        assertEquals(-16776961, event.calendarColor)
        assertEquals("Room A", event.location)
    }

    @Test
    fun `CalendarEvent handles all-day events`() {
        val event = CalendarEvent(
            id = 3L,
            title = "Holiday",
            beginTime = 5000L,
            endTime = 86400000L + 5000L,
            isAllDay = true,
            calendarColor = -1,
            location = null,
        )

        assertTrue(event.isAllDay)
        assertNull(event.location)
    }

    @Test
    fun `CalendarEvent handles empty title`() {
        val event = CalendarEvent(
            id = 4L,
            title = "",
            beginTime = 0L,
            endTime = 1000L,
            isAllDay = false,
            calendarColor = 0,
            location = null,
        )

        assertEquals("", event.title)
    }

    @Test
    fun `CalendarEvent equality works correctly`() {
        val event1 = CalendarEvent(1L, "Test", 100L, 200L, false, 0, null)
        val event2 = CalendarEvent(1L, "Test", 100L, 200L, false, 0, null)
        val event3 = CalendarEvent(2L, "Test", 100L, 200L, false, 0, null)

        assertEquals(event1, event2)
        assertTrue(event1 != event3)
    }

    @Test
    fun `CalendarEvent copy works correctly`() {
        val original = CalendarEvent(1L, "Original", 100L, 200L, false, -1, "Here")
        val modified = original.copy(title = "Modified")

        assertEquals("Modified", modified.title)
        assertEquals(original.id, modified.id)
        assertEquals(original.beginTime, modified.beginTime)
    }

    @Test
    fun `events list can be sorted by begin time`() {
        val events = listOf(
            CalendarEvent(3L, "Third", 3000L, 4000L, false, 0, null),
            CalendarEvent(1L, "First", 1000L, 2000L, false, 0, null),
            CalendarEvent(2L, "Second", 2000L, 3000L, false, 0, null),
        )

        val sorted = events.sortedBy { it.beginTime }

        assertEquals("First", sorted[0].title)
        assertEquals("Second", sorted[1].title)
        assertEquals("Third", sorted[2].title)
    }

    @Test
    fun `events list take limits correctly`() {
        val events = (1..10).map {
            CalendarEvent(it.toLong(), "Event $it", it * 1000L, it * 1000L + 500L, false, 0, null)
        }

        assertEquals(8, events.take(8).size)
        assertEquals(4, events.take(4).size)
        assertEquals(2, events.take(2).size)
    }
}
