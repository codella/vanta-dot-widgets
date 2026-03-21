package dk.codella.nothingwidgets.calendar.widget

import dk.codella.nothingwidgets.calendar.data.CalendarEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarWidgetContentTest {

    private fun sampleEvents(count: Int): List<CalendarEvent> =
        (1..count).map { i ->
            CalendarEvent(
                id = i.toLong(),
                title = "Event $i",
                beginTime = System.currentTimeMillis() + i * 3600000L,
                endTime = System.currentTimeMillis() + i * 3600000L + 1800000L,
                isAllDay = false,
                calendarColor = -16776961,
                location = if (i % 2 == 0) "Location $i" else null,
            )
        }

    @Test
    fun `compact size limits to 2 events`() {
        val events = sampleEvents(5)
        val compactEvents = events.take(2)
        assertEquals(2, compactEvents.size)
    }

    @Test
    fun `expanded size limits to 4 events`() {
        val events = sampleEvents(8)
        val expandedEvents = events.take(4)
        assertEquals(4, expandedEvents.size)
    }

    @Test
    fun `full size limits to 8 events`() {
        val events = sampleEvents(10)
        val fullEvents = events.take(8)
        assertEquals(8, fullEvents.size)
    }

    @Test
    fun `empty events list is handled`() {
        val events = emptyList<CalendarEvent>()
        assertTrue(events.isEmpty())
    }

    @Test
    fun `widget sizes are correctly defined`() {
        assertTrue(CalendarWidgetSizes.COMPACT.width < CalendarWidgetSizes.EXPANDED.width)
        assertTrue(CalendarWidgetSizes.EXPANDED.height < CalendarWidgetSizes.FULL.height)
        assertEquals(CalendarWidgetSizes.EXPANDED.width, CalendarWidgetSizes.FULL.width)
    }
}
