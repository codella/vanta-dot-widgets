package dk.codella.phosphor.calendar.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import dk.codella.phosphor.MainActivity
import dk.codella.phosphor.calendar.data.CalendarEvent
import dk.codella.phosphor.common.GlanceText
import dk.codella.phosphor.common.PhosphorWidgetTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun CalendarWidgetContent(events: List<CalendarEvent>, hasPermission: Boolean) {
    val size = LocalSize.current
    val isCompact = size.width < CalendarWidgetSizes.EXPANDED.width
    val isFull = size.height >= CalendarWidgetSizes.FULL.height

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(PhosphorWidgetTheme.CornerRadius)
            .background(PhosphorWidgetTheme.Black)
            .clickable(actionStartActivity<MainActivity>())
            .padding(PhosphorWidgetTheme.Padding),
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            when {
                !hasPermission -> {
                    DateHeader()
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    PermissionMessage()
                }
                events.isEmpty() -> {
                    DateHeader()
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    EmptyMessage()
                }
                isCompact -> {
                    val compactEvents = events.take(2)
                    val grouped = groupEventsByDate(compactEvents)
                    DateSectionHeader(grouped.keys.first())
                    Spacer(modifier = GlanceModifier.height(8.dp))
                    CompactEventList(compactEvents)
                }
                isFull -> FullEventList(events.take(8))
                else -> ExpandedEventList(events.take(4))
            }
        }
    }
}

@Composable
private fun DateHeader() {
    val context = LocalContext.current
    val today = SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date())
    val bitmap = GlanceText.renderDotoText(
        context = context,
        text = today.uppercase(),
        textSizeSp = 20f,
    )
    Image(
        provider = ImageProvider(bitmap),
        contentDescription = today,
    )
}

@Composable
private fun DateSectionHeader(dateText: String) {
    val context = LocalContext.current
    val bitmap = GlanceText.renderDotoText(
        context = context,
        text = dateText.uppercase(),
        textSizeSp = 16f,
    )
    Image(
        provider = ImageProvider(bitmap),
        contentDescription = dateText,
    )
}

@Composable
private fun PermissionMessage() {
    Text(
        text = "Calendar permission required",
        style = TextStyle(
            color = ColorProvider(PhosphorWidgetTheme.GreyLight, PhosphorWidgetTheme.GreyLight),
            fontSize = PhosphorWidgetTheme.BodyTextSize,
        ),
    )
}

@Composable
private fun EmptyMessage() {
    Text(
        text = "No upcoming events",
        style = TextStyle(
            color = ColorProvider(PhosphorWidgetTheme.GreyLight, PhosphorWidgetTheme.GreyLight),
            fontSize = PhosphorWidgetTheme.BodyTextSize,
        ),
    )
}

@Composable
private fun CompactEventList(events: List<CalendarEvent>) {
    Column(modifier = GlanceModifier.fillMaxWidth()) {
        events.forEachIndexed { index, event ->
            if (index > 0) {
                DotSeparator()
            }
            CompactEventRow(event)
        }
    }
}

@Composable
private fun CompactEventRow(event: CalendarEvent) {
    Text(
        text = event.title,
        style = TextStyle(
            color = ColorProvider(PhosphorWidgetTheme.White, PhosphorWidgetTheme.White),
            fontSize = PhosphorWidgetTheme.BodyTextSize,
            fontWeight = FontWeight.Medium,
        ),
        maxLines = 1,
    )
}

@Composable
private fun ExpandedEventList(events: List<CalendarEvent>) {
    val grouped = groupEventsByDate(events)
    Column(modifier = GlanceModifier.fillMaxWidth()) {
        var firstGroup = true
        grouped.forEach { (dateLabel, groupEvents) ->
            if (!firstGroup) {
                Spacer(modifier = GlanceModifier.height(8.dp))
            }
            firstGroup = false
            DateSectionHeader(dateLabel)
            Spacer(modifier = GlanceModifier.height(4.dp))
            groupEvents.forEachIndexed { index, event ->
                if (index > 0) {
                    DotSeparator()
                }
                ExpandedEventRow(event)
            }
        }
    }
}

@Composable
private fun ExpandedEventRow(event: CalendarEvent) {
    Column(modifier = GlanceModifier.fillMaxWidth()) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CalendarColorDot(event.calendarColor)
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = event.title,
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(
                    color = ColorProvider(PhosphorWidgetTheme.White, PhosphorWidgetTheme.White),
                    fontSize = PhosphorWidgetTheme.BodyTextSize,
                    fontWeight = FontWeight.Medium,
                ),
                maxLines = 1,
            )
        }
        Text(
            text = formatEventTime(event),
            modifier = GlanceModifier.padding(start = 16.dp),
            style = TextStyle(
                color = ColorProvider(PhosphorWidgetTheme.GreyLight, PhosphorWidgetTheme.GreyLight),
                fontSize = PhosphorWidgetTheme.SmallTextSize,
            ),
            maxLines = 1,
        )
    }
}

private sealed class CalendarListItem {
    data class Header(val dateText: String, val id: Long) : CalendarListItem()
    data class Event(val event: CalendarEvent) : CalendarListItem()
}

private fun buildListItems(events: List<CalendarEvent>): List<CalendarListItem> {
    val items = mutableListOf<CalendarListItem>()
    val grouped = groupEventsByDate(events)
    var headerId = -1L
    grouped.forEach { (dateLabel, groupEvents) ->
        items.add(CalendarListItem.Header(dateLabel, headerId--))
        groupEvents.forEach { event ->
            items.add(CalendarListItem.Event(event))
        }
    }
    return items
}

@Composable
private fun FullEventList(events: List<CalendarEvent>) {
    val listItems = buildListItems(events)
    LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
        items(listItems, itemId = { item ->
            when (item) {
                is CalendarListItem.Header -> item.id
                is CalendarListItem.Event -> item.event.id
            }
        }) { item ->
            when (item) {
                is CalendarListItem.Header -> {
                    Column(modifier = GlanceModifier.fillMaxWidth()) {
                        Spacer(modifier = GlanceModifier.height(4.dp))
                        DateSectionHeader(item.dateText)
                        Spacer(modifier = GlanceModifier.height(4.dp))
                    }
                }
                is CalendarListItem.Event -> {
                    Column(modifier = GlanceModifier.fillMaxWidth()) {
                        FullEventRow(item.event)
                        Spacer(modifier = GlanceModifier.height(PhosphorWidgetTheme.EventSpacing))
                    }
                }
            }
        }
    }
}

@Composable
private fun FullEventRow(event: CalendarEvent) {
    Column(modifier = GlanceModifier.fillMaxWidth()) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CalendarColorDot(event.calendarColor)
            Spacer(modifier = GlanceModifier.width(8.dp))
            Text(
                text = event.title,
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(
                    color = ColorProvider(PhosphorWidgetTheme.White, PhosphorWidgetTheme.White),
                    fontSize = PhosphorWidgetTheme.BodyTextSize,
                    fontWeight = FontWeight.Medium,
                ),
                maxLines = 1,
            )
        }
        Text(
            text = formatEventTime(event),
            modifier = GlanceModifier.padding(start = 16.dp),
            style = TextStyle(
                color = ColorProvider(PhosphorWidgetTheme.GreyLight, PhosphorWidgetTheme.GreyLight),
                fontSize = PhosphorWidgetTheme.SmallTextSize,
            ),
            maxLines = 1,
        )
        if (!event.location.isNullOrBlank()) {
            Text(
                text = event.location,
                modifier = GlanceModifier.padding(start = 16.dp),
                style = TextStyle(
                    color = ColorProvider(PhosphorWidgetTheme.GreyMedium, PhosphorWidgetTheme.GreyMedium),
                    fontSize = PhosphorWidgetTheme.SmallTextSize,
                ),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun CalendarColorDot(color: Int) {
    Box(
        modifier = GlanceModifier
            .size(8.dp)
            .cornerRadius(4.dp)
            .background(androidx.compose.ui.graphics.Color(color)),
    ) {}
}

@Composable
private fun DotSeparator() {
    Box(modifier = GlanceModifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = "· · ·",
            style = TextStyle(
                color = ColorProvider(PhosphorWidgetTheme.GreyMedium, PhosphorWidgetTheme.GreyMedium),
                fontSize = 10.sp,
            ),
        )
    }
}

private fun formatEventTime(event: CalendarEvent): String {
    if (event.isAllDay) return "All day"
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val begin = timeFormat.format(Date(event.beginTime))
    val end = timeFormat.format(Date(event.endTime))
    return "$begin – $end"
}

private fun groupEventsByDate(events: List<CalendarEvent>): LinkedHashMap<String, List<CalendarEvent>> {
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val tomorrow = (today.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
    val dateFormat = SimpleDateFormat("EEE, MMM d", Locale.getDefault())

    val grouped = LinkedHashMap<String, MutableList<CalendarEvent>>()
    for (event in events) {
        val eventCal = Calendar.getInstance().apply { timeInMillis = event.beginTime }
        val label = when {
            eventCal.before(tomorrow) -> "Today"
            eventCal.before((tomorrow.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }) -> "Tomorrow"
            else -> dateFormat.format(Date(event.beginTime))
        }
        grouped.getOrPut(label) { mutableListOf() }.add(event)
    }
    return LinkedHashMap(grouped.mapValues { it.value.toList() })
}
