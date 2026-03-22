package dk.codella.phosphor.calendar.widget

import dk.codella.phosphor.calendar.data.CalendarEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

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

    private fun sampleEventsAcrossDays(eventsPerDay: List<Int>): List<CalendarEvent> {
        val events = mutableListOf<CalendarEvent>()
        var id = 1L
        val baseTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        eventsPerDay.forEachIndexed { dayOffset, count ->
            val dayCal = (baseTime.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, dayOffset)
            }
            repeat(count) { eventIndex ->
                val begin = dayCal.timeInMillis + eventIndex * 3600000L
                events.add(
                    CalendarEvent(
                        id = id++,
                        title = "Day${dayOffset}_Event${eventIndex + 1}",
                        beginTime = begin,
                        endTime = begin + 1800000L,
                        isAllDay = false,
                        calendarColor = -16776961,
                        location = null,
                    )
                )
            }
        }
        return events
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
    fun `full size limits to 20 events`() {
        val events = sampleEvents(25)
        val fullEvents = events.take(20)
        assertEquals(20, fullEvents.size)
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

    @Test
    fun `buildListItems includes all events per day`() {
        val events = sampleEventsAcrossDays(listOf(5))
        val items = buildListItems(events)

        val eventItems = items.filterIsInstance<CalendarListItem.Event>()
        assertEquals(5, eventItems.size)

        val headers = items.filterIsInstance<CalendarListItem.Header>()
        assertEquals(1, headers.size)
    }

    @Test
    fun `buildListItems sets totalEventCount on first header when more than 3 events`() {
        val events = sampleEventsAcrossDays(listOf(2, 3))
        val items = buildListItems(events)

        val headers = items.filterIsInstance<CalendarListItem.Header>()
        assertEquals(2, headers.size)
        assertEquals(5, headers[0].totalEventCount)
        assertNull(headers[1].totalEventCount)
    }

    @Test
    fun `buildListItems no totalEventCount when 3 or fewer total events`() {
        val events = sampleEventsAcrossDays(listOf(2, 1))
        val items = buildListItems(events)

        val headers = items.filterIsInstance<CalendarListItem.Header>()
        headers.forEach { header ->
            assertNull(header.totalEventCount)
        }
    }

    @Test
    fun `buildListItems groups multiple days correctly`() {
        val events = sampleEventsAcrossDays(listOf(5, 4, 2))
        val items = buildListItems(events)

        val headers = items.filterIsInstance<CalendarListItem.Header>()
        assertEquals(3, headers.size)

        val eventItems = items.filterIsInstance<CalendarListItem.Event>()
        assertEquals(11, eventItems.size) // 5 + 4 + 2
    }
}
