package dk.codella.phosphor.calendar.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.compose.ui.graphics.toArgb
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
import dk.codella.phosphor.MainActivity
import dk.codella.phosphor.R
import dk.codella.phosphor.calendar.data.CalendarEvent
import dk.codella.phosphor.common.GlanceText
import dk.codella.phosphor.common.PhosphorWidgetTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CalendarWidgetContent(
    events: List<CalendarEvent>,
    hasPermission: Boolean,
    isRefreshing: Boolean = false,
) {
    val size = LocalSize.current
    val isCompact = size.width < CalendarWidgetSizes.EXPANDED.width
    val isFull = size.height >= CalendarWidgetSizes.FULL.height
    val allDayEvents = events.filter { it.isAllDay }
    val timedEvents = events.filter { !it.isAllDay }.sortedBy { it.beginTime }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(PhosphorWidgetTheme.CornerRadius)
            .background(PhosphorWidgetTheme.GreyDark)
            .clickable(actionStartActivity<MainActivity>())
            .padding(PhosphorWidgetTheme.Padding),
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            SectionHeader("Upcoming events", isRefreshing)
            Spacer(modifier = GlanceModifier.height(12.dp))
            when {
                !hasPermission -> PermissionMessage()
                events.isEmpty() -> EmptyMessage()
                isCompact -> {
                    if (allDayEvents.isNotEmpty()) {
                        AllDaySection(allDayEvents)
                        if (timedEvents.isNotEmpty()) Spacer(modifier = GlanceModifier.height(8.dp))
                    }
                    CompactEventList(timedEvents.take(2))
                }
                isFull -> {
                    if (allDayEvents.isNotEmpty()) {
                        AllDaySection(allDayEvents)
                        Spacer(modifier = GlanceModifier.height(8.dp))
                    }
                    FullEventList(timedEvents.take(20))
                }
                else -> {
                    if (allDayEvents.isNotEmpty()) {
                        AllDaySection(allDayEvents)
                        if (timedEvents.isNotEmpty()) Spacer(modifier = GlanceModifier.height(8.dp))
                    }
                    ExpandedEventList(timedEvents.take(4))
                }
            }
        }
    }
}


@Composable
private fun SectionHeader(text: String, isRefreshing: Boolean = false) {
    val context = LocalContext.current
    val label = if (isRefreshing) "$text · · ·" else text
    val bitmap = GlanceText.renderDotoText(
        context = context,
        text = label.uppercase(),
        textSizeSp = 16f,
    )
    Image(
        provider = ImageProvider(bitmap),
        contentDescription = label,
        modifier = GlanceModifier.clickable(actionRunCallback<RefreshActionCallback>()),
    )
}

@Composable
private fun PermissionMessage() {
    val context = LocalContext.current
    Image(
        provider = ImageProvider(
            GlanceText.renderDotoText(context, "Calendar permission required", 14f, PhosphorWidgetTheme.GreyLight.toArgb())
        ),
        contentDescription = "Calendar permission required",
    )
}

private val INSPIRATIONAL_QUOTES = listOf(
    "Your day is wide open",
    "Time to do something great",
    "A blank canvas awaits",
    "Freedom looks good on you",
    "Make today count",
    "The best plans are no plans",
    "Go where the wind takes you",
    "Breathe. You have space today",
    "Nothing scheduled, everything possible",
    "Today belongs to you",
    "Less meetings, more meaning",
    "Room to think, room to create",
    "Enjoy the white space",
    "An empty calendar is a gift",
    "Adventure has no agenda",
)

@Composable
private fun EmptyMessage() {
    val context = LocalContext.current
    val quote = INSPIRATIONAL_QUOTES[System.currentTimeMillis().mod(INSPIRATIONAL_QUOTES.size)]
    Column {
        Image(
            provider = ImageProvider(
                GlanceText.renderDotoText(context, "No upcoming events", 14f, PhosphorWidgetTheme.GreyLight.toArgb())
            ),
            contentDescription = "No upcoming events",
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Image(
            provider = ImageProvider(
                GlanceText.renderDotoText(context, quote, 12f, PhosphorWidgetTheme.GreyLight.toArgb())
            ),
            contentDescription = quote,
        )
    }
}

@Composable
private fun AllDaySection(events: List<CalendarEvent>) {
    val context = LocalContext.current
    Box(
        modifier = GlanceModifier
            .fillMaxWidth()
            .cornerRadius(8.dp)
            .background(PhosphorWidgetTheme.GreyMedium)
            .padding(10.dp),
    ) {
        Column(modifier = GlanceModifier.fillMaxWidth()) {
            events.forEachIndexed { index, event ->
                if (index > 0) Spacer(modifier = GlanceModifier.height(6.dp))
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CalendarColorDot(event.calendarColor, hollow = true)
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Image(
                        provider = ImageProvider(GlanceText.renderDotoText(context, event.title, 14f)),
                        contentDescription = event.title,
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Image(
                        provider = ImageProvider(
                            GlanceText.renderDotoText(context, "All day", 11f, PhosphorWidgetTheme.GreyLight.toArgb())
                        ),
                        contentDescription = "All day",
                    )
                }
            }
        }
    }
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
    val context = LocalContext.current
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CalendarColorDot(event.calendarColor)
        Spacer(modifier = GlanceModifier.width(8.dp))
        Image(
            provider = ImageProvider(GlanceText.renderDotoText(context, event.title, 14f)),
            contentDescription = event.title,
        )
        if (event.hasVideoConference) {
            Spacer(modifier = GlanceModifier.width(4.dp))
            VideocamIcon()
        }
        if (event.hasAttachments) {
            Spacer(modifier = GlanceModifier.width(4.dp))
            AttachIcon()
        }
    }
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
    val context = LocalContext.current
    Column(modifier = GlanceModifier.fillMaxWidth()) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CalendarColorDot(event.calendarColor, hollow = event.isAllDay)
            Spacer(modifier = GlanceModifier.width(8.dp))
            Image(
                provider = ImageProvider(GlanceText.renderDotoText(context, event.title, 14f)),
                contentDescription = event.title,
            )
            if (event.hasVideoConference) {
                Spacer(modifier = GlanceModifier.width(4.dp))
                VideocamIcon()
            }
            if (event.hasAttachments) {
                Spacer(modifier = GlanceModifier.width(4.dp))
                AttachIcon()
            }
        }
        Image(
            provider = ImageProvider(
                GlanceText.renderDotoText(context, formatEventTime(event), 12f, PhosphorWidgetTheme.GreyLight.toArgb())
            ),
            contentDescription = formatEventTime(event),
            modifier = GlanceModifier.padding(start = 16.dp),
        )
    }
}

@Composable
private fun FullEventList(events: List<CalendarEvent>) {
    LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
        items(events, itemId = { it.id }) { event ->
            FullEventRow(event)
        }
    }
}

@Composable
private fun FullEventRow(event: CalendarEvent) {
    val context = LocalContext.current
    Column(modifier = GlanceModifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CalendarColorDot(event.calendarColor, hollow = event.isAllDay)
            Spacer(modifier = GlanceModifier.width(8.dp))
            Image(
                provider = ImageProvider(GlanceText.renderDotoText(context, event.title, 14f)),
                contentDescription = event.title,
            )
            if (event.hasVideoConference) {
                Spacer(modifier = GlanceModifier.width(4.dp))
                VideocamIcon()
            }
            if (event.hasAttachments) {
                Spacer(modifier = GlanceModifier.width(4.dp))
                AttachIcon()
            }
        }
        Image(
            provider = ImageProvider(
                GlanceText.renderDotoText(context, formatEventTime(event), 12f, PhosphorWidgetTheme.GreyLight.toArgb())
            ),
            contentDescription = formatEventTime(event),
            modifier = GlanceModifier.padding(start = 16.dp),
        )
        if (!event.location.isNullOrBlank()) {
            Image(
                provider = ImageProvider(
                    GlanceText.renderDotoText(context, event.location, 12f, PhosphorWidgetTheme.GreyLight.toArgb())
                ),
                contentDescription = event.location,
                modifier = GlanceModifier.padding(start = 16.dp),
            )
        }
    }
}

@Composable
private fun VideocamIcon() {
    Image(
        provider = ImageProvider(R.drawable.ic_videocam),
        contentDescription = "Video call",
        modifier = GlanceModifier.size(14.dp),
    )
}

@Composable
private fun AttachIcon() {
    Image(
        provider = ImageProvider(R.drawable.ic_attach),
        contentDescription = "Attachment",
        modifier = GlanceModifier.size(14.dp),
    )
}

@Composable
private fun CalendarColorDot(color: Int, hollow: Boolean = false) {
    if (hollow) {
        val context = LocalContext.current
        Image(
            provider = ImageProvider(GlanceText.renderHollowCircle(context, 8f, color)),
            contentDescription = null,
            modifier = GlanceModifier.size(8.dp),
        )
    } else {
        Box(
            modifier = GlanceModifier
                .size(8.dp)
                .cornerRadius(4.dp)
                .background(androidx.compose.ui.graphics.Color(color)),
        ) {}
    }
}

@Composable
private fun DotSeparator() {
    val context = LocalContext.current
    Box(modifier = GlanceModifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Image(
            provider = ImageProvider(
                GlanceText.renderDotoText(context, "· · ·", 10f, PhosphorWidgetTheme.GreyMedium.toArgb())
            ),
            contentDescription = null,
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

