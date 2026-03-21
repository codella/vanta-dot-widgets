package dk.codella.nothingwidgets.calendar.data

data class CalendarEvent(
    val id: Long,
    val title: String,
    val beginTime: Long,
    val endTime: Long,
    val isAllDay: Boolean,
    val calendarColor: Int,
    val location: String?,
)
