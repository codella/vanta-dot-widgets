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
            DateHeader()
            Spacer(modifier = GlanceModifier.height(8.dp))

            when {
                !hasPermission -> PermissionMessage()
                events.isEmpty() -> EmptyMessage()
                isCompact -> CompactEventList(events.take(2))
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
    Column(modifier = GlanceModifier.fillMaxWidth()) {
        events.forEachIndexed { index, event ->
            if (index > 0) {
                DotSeparator()
            }
            ExpandedEventRow(event)
        }
    }
}

@Composable
private fun ExpandedEventRow(event: CalendarEvent) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CalendarColorDot(event.calendarColor)
        Spacer(modifier = GlanceModifier.width(8.dp))
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = event.title,
                style = TextStyle(
                    color = ColorProvider(PhosphorWidgetTheme.White, PhosphorWidgetTheme.White),
                    fontSize = PhosphorWidgetTheme.BodyTextSize,
                    fontWeight = FontWeight.Medium,
                ),
                maxLines = 1,
            )
            Text(
                text = formatEventTime(event),
                style = TextStyle(
                    color = ColorProvider(PhosphorWidgetTheme.GreyLight, PhosphorWidgetTheme.GreyLight),
                    fontSize = PhosphorWidgetTheme.SmallTextSize,
                ),
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun FullEventList(events: List<CalendarEvent>) {
    LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
        items(events, itemId = { it.id }) { event ->
            Column(modifier = GlanceModifier.fillMaxWidth()) {
                FullEventRow(event)
                Spacer(modifier = GlanceModifier.height(PhosphorWidgetTheme.EventSpacing))
            }
        }
    }
}

@Composable
private fun FullEventRow(event: CalendarEvent) {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
    ) {
        CalendarColorDot(event.calendarColor)
        Spacer(modifier = GlanceModifier.width(8.dp))
        Column(modifier = GlanceModifier.defaultWeight()) {
            Text(
                text = event.title,
                style = TextStyle(
                    color = ColorProvider(PhosphorWidgetTheme.White, PhosphorWidgetTheme.White),
                    fontSize = PhosphorWidgetTheme.BodyTextSize,
                    fontWeight = FontWeight.Medium,
                ),
                maxLines = 1,
            )
            Text(
                text = formatEventTime(event),
                style = TextStyle(
                    color = ColorProvider(PhosphorWidgetTheme.GreyLight, PhosphorWidgetTheme.GreyLight),
                    fontSize = PhosphorWidgetTheme.SmallTextSize,
                ),
                maxLines = 1,
            )
            if (!event.location.isNullOrBlank()) {
                Text(
                    text = event.location,
                    style = TextStyle(
                        color = ColorProvider(PhosphorWidgetTheme.GreyMedium, PhosphorWidgetTheme.GreyMedium),
                        fontSize = PhosphorWidgetTheme.SmallTextSize,
                    ),
                    maxLines = 1,
                )
            }
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
