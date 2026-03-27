package dk.codella.vantadot.calendar.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalendarWidgetContentTest {

    @Test
    fun `widget sizes are correctly defined`() {
        assertTrue(CalendarWidgetSizes.EXPANDED.height < CalendarWidgetSizes.FULL.height)
        assertEquals(CalendarWidgetSizes.EXPANDED.width, CalendarWidgetSizes.FULL.width)
    }
}
