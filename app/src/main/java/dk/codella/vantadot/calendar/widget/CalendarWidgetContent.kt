package dk.codella.vantadot.calendar.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
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
import dk.codella.vantadot.R
import dk.codella.vantadot.calendar.data.CalendarEvent
import dk.codella.vantadot.common.GlanceText
import dk.codella.vantadot.common.VantaDotWidgetTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private enum class Urgency { NONE, SUBTLE, LOW, MEDIUM, HIGH, IN_PROGRESS }

private fun calcUrgency(event: CalendarEvent): Urgency {
    val now = System.currentTimeMillis()
    if (now >= event.beginTime && now < event.endTime) return Urgency.IN_PROGRESS
    val minutesUntil = (event.beginTime - now) / 60_000
    return when {
        minutesUntil > 30 -> Urgency.NONE
        minutesUntil > 10 -> Urgency.SUBTLE
        minutesUntil > 5 -> Urgency.LOW
        minutesUntil > 2 -> Urgency.MEDIUM
        else -> Urgency.HIGH
    }
}

private fun urgencyBackground(urgency: Urgency): Color = when (urgency) {
    Urgency.NONE -> Color.Transparent
    Urgency.SUBTLE -> VantaDotWidgetTheme.HighlightSubtle
    Urgency.LOW -> VantaDotWidgetTheme.HighlightLow
    Urgency.MEDIUM -> VantaDotWidgetTheme.HighlightMedium
    Urgency.HIGH -> VantaDotWidgetTheme.HighlightHigh
    Urgency.IN_PROGRESS -> VantaDotWidgetTheme.HighlightInProgress
}

@Composable
fun CalendarWidgetContent(
    events: List<CalendarEvent>,
    hasPermission: Boolean,
    isRefreshing: Boolean = false,
    refreshPhase: Int = 0,
) {
    val size = LocalSize.current
    val isCompact = size.width < CalendarWidgetSizes.EXPANDED.width
    val isFull = size.height >= CalendarWidgetSizes.FULL.height
    val allDayEvents = events.filter { it.isAllDay }
    val timedEvents = events.filter { !it.isAllDay }.sortedBy { it.beginTime }

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(VantaDotWidgetTheme.CornerRadius)
            .background(VantaDotWidgetTheme.GreyDark)
            .padding(VantaDotWidgetTheme.Padding),
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            SectionHeader("Upcoming events", isRefreshing, refreshPhase)
            Spacer(modifier = GlanceModifier.height(12.dp))
            when {
                !hasPermission -> PermissionMessage()
                events.isEmpty() -> EmptyMessage()
                isCompact -> {
                    if (allDayEvents.isNotEmpty()) {
                        AllDaySection(allDayEvents)
                        if (timedEvents.isNotEmpty()) Spacer(modifier = GlanceModifier.height(8.dp))
                    }
                    EventList(timedEvents.take(2))
                }
                isFull -> {
                    if (allDayEvents.isNotEmpty()) {
                        AllDaySection(allDayEvents)
                        Spacer(modifier = GlanceModifier.height(8.dp))
                    }
                    ScrollableEventList(timedEvents.take(20), modifier = GlanceModifier.defaultWeight().fillMaxWidth(), showTime = true, showLocation = true, verticalPadding = 6.dp)
                }
                else -> {
                    if (allDayEvents.isNotEmpty()) {
                        AllDaySection(allDayEvents)
                        if (timedEvents.isNotEmpty()) Spacer(modifier = GlanceModifier.height(8.dp))
                    }
                    ScrollableEventList(timedEvents.take(4), modifier = GlanceModifier.defaultWeight().fillMaxWidth(), showTime = true, verticalPadding = 6.dp)
                }
            }
        }
    }
}


@Composable
private fun SectionHeader(text: String, isRefreshing: Boolean = false, refreshPhase: Int = 0) {
    val context = LocalContext.current
    val bitmap = GlanceText.renderDotoText(
        context = context,
        text = text.uppercase(),
        textSizeSp = 18f,
    )
    Row(
        modifier = GlanceModifier.fillMaxWidth().clickable(actionRunCallback<RefreshActionCallback>()),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Image(
            provider = ImageProvider(bitmap),
            contentDescription = text,
        )
        if (isRefreshing) {
            Spacer(modifier = GlanceModifier.defaultWeight())
            Image(
                provider = ImageProvider(
                    GlanceText.renderLoadingDots(context, activeIndex = refreshPhase)
                ),
                contentDescription = "Loading",
                modifier = GlanceModifier.size(width = 20.dp, height = 8.dp),
            )
        }
    }
}

@Composable
private fun PermissionMessage() {
    val context = LocalContext.current
    Image(
        provider = ImageProvider(
            GlanceText.renderDotoText(context, "Calendar permission required", 14f, VantaDotWidgetTheme.GreyLight.toArgb())
        ),
        contentDescription = "Calendar permission required",
    )
}

private val INSPIRATIONAL_QUOTES = listOf(
    "Zero meetings. Maximum vibes",
    "Calendar's empty. You win",
    "Plot twist: free time",
    "No meetings were harmed today",
    "Professional schedule dodger",
    "Achievement unlocked: free day",
    "Nothing to see here. Go play",
    "The calendar has left the chat",
    "Free as in free time",
    "Your schedule called in sick",
    "Meetings? Never heard of them",
    "Today's agenda: absolutely nothing",
    "Error 404: events not found",
    "Ctrl+Z the whole workday",
    "Gone fishing. Metaphorically",
    "You've been promoted to free",
    "Schedule status: on vacation",
    "Inbox zero. Calendar zero. Hero",
    "No plans is the new plan",
    "Task failed successfully: relax",
    "Calendar bankruptcy declared",
    "The void stares back. It's chill",
    "Deadlines? What deadlines",
    "Your future self thanks you",
    "Permission to do nothing granted",
    "Loading productivity... just kidding",
    "Out of office. In your pajamas",
    "Today's forecast: 100% free",
    "This is what winning looks like",
    "All dressed up, nowhere to Zoom",
    "Buffering... nope, just free",
    "You have mass. Meetings don't",
    "The schedule is a suggestion",
    "Peak performance: doing nothing",
    "Currently accepting zero invites",
    "Spontaneity mode: activated",
    "Autopilot engaged. Destination: couch",
    "No syncs needed. Ever",
    "Declined all. Felt great",
    "Productivity is overrated anyway",
    "Your calendar sends its regards",
    "Temporarily out of commitments",
    "This space intentionally left blank",
    "Free-range human detected",
    "The best meeting is no meeting",
    "Unscheduled and unbothered",
    "Today's vibe: unavailable",
    "Do not disturb. By events",
    "Running on free time and coffee",
    "You've reached the end of busy",
)

@Composable
private fun EmptyMessage() {
    val context = LocalContext.current
    val dayIndex = (System.currentTimeMillis() / 86_400_000).mod(INSPIRATIONAL_QUOTES.size)
    val quote = INSPIRATIONAL_QUOTES[dayIndex]
    Column {
        Image(
            provider = ImageProvider(
                GlanceText.renderDotoText(context, "No upcoming events", 14f, VantaDotWidgetTheme.GreyLight.toArgb())
            ),
            contentDescription = "No upcoming events",
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Image(
            provider = ImageProvider(
                GlanceText.renderDotoText(context, quote, 12f, VantaDotWidgetTheme.GreyLight.toArgb())
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
            .background(VantaDotWidgetTheme.GreyMedium)
            .padding(10.dp),
    ) {
        Column(modifier = GlanceModifier.fillMaxWidth()) {
            Image(
                provider = ImageProvider(
                    GlanceText.renderDotoText(context, "All day", 11f, VantaDotWidgetTheme.GreyLight.toArgb())
                ),
                contentDescription = "All day",
            )
            Spacer(modifier = GlanceModifier.height(6.dp))
            events.forEachIndexed { index, event ->
                if (index > 0) Spacer(modifier = GlanceModifier.height(6.dp))
                val allDayTitleColor = if (event.isTentative) VantaDotWidgetTheme.TentativeText.toArgb() else Color.White.toArgb()
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CalendarColorDot(event.calendarColor, hollow = true)
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Image(
                        provider = ImageProvider(GlanceText.renderDotoText(context, event.title, 14f, allDayTitleColor)),
                        contentDescription = event.title,
                    )
                    if (event.isTentative) {
                        Spacer(modifier = GlanceModifier.width(4.dp))
                        Image(
                            provider = ImageProvider(
                                GlanceText.renderDotoText(context, "?", 12f, VantaDotWidgetTheme.TentativeText.toArgb())
                            ),
                            contentDescription = "Tentative",
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EventHighlight(urgency: Urgency, content: @Composable () -> Unit) {
    if (urgency == Urgency.NONE) {
        content()
    } else {
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .cornerRadius(8.dp)
                .background(urgencyBackground(urgency)),
        ) {
            content()
        }
    }
}

@Composable
private fun EventRow(
    event: CalendarEvent,
    showTime: Boolean = false,
    showLocation: Boolean = false,
    hollowDot: Boolean = false,
    verticalPadding: Dp = 0.dp,
) {
    val context = LocalContext.current
    val titleColor = if (event.isTentative) VantaDotWidgetTheme.TentativeText.toArgb() else Color.White.toArgb()
    Column(modifier = GlanceModifier.fillMaxWidth().padding(start = 10.dp, top = verticalPadding, bottom = verticalPadding)) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CalendarColorDot(event.calendarColor, hollow = hollowDot || event.isTentative)
            Spacer(modifier = GlanceModifier.width(8.dp))
            Image(
                provider = ImageProvider(GlanceText.renderDotoText(context, event.title, 14f, titleColor)),
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
            if (event.isTentative) {
                Spacer(modifier = GlanceModifier.width(4.dp))
                Image(
                    provider = ImageProvider(
                        GlanceText.renderDotoText(context, "?", 12f, VantaDotWidgetTheme.TentativeText.toArgb())
                    ),
                    contentDescription = "Tentative",
                )
            }
        }
        if (showTime) {
            Image(
                provider = ImageProvider(
                    GlanceText.renderDotoText(context, formatEventTime(event), 12f, VantaDotWidgetTheme.GreyLight.toArgb())
                ),
                contentDescription = formatEventTime(event),
                modifier = GlanceModifier.padding(start = 16.dp),
            )
        }
        if (showLocation && !event.location.isNullOrBlank()) {
            Image(
                provider = ImageProvider(
                    GlanceText.renderDotoText(context, event.location, 12f, VantaDotWidgetTheme.GreyLight.toArgb())
                ),
                contentDescription = event.location,
                modifier = GlanceModifier.padding(start = 16.dp),
            )
        }
    }
}

@Composable
private fun EventList(events: List<CalendarEvent>, showTime: Boolean = false) {
    Column(modifier = GlanceModifier.fillMaxWidth()) {
        events.forEachIndexed { index, event ->
            if (index > 0) DotSeparator()
            EventHighlight(calcUrgency(event)) {
                EventRow(event, showTime = showTime, hollowDot = showTime && event.isAllDay)
            }
        }
    }
}

@Composable
private fun ScrollableEventList(
    events: List<CalendarEvent>,
    modifier: GlanceModifier = GlanceModifier.fillMaxSize(),
    showTime: Boolean = false,
    showLocation: Boolean = false,
    verticalPadding: Dp = 0.dp,
) {
    LazyColumn(modifier = modifier) {
        items(events, itemId = { it.id }) { event ->
            Column(modifier = GlanceModifier.fillMaxWidth()) {
                EventHighlight(calcUrgency(event)) {
                    EventRow(event, showTime = showTime, showLocation = showLocation, hollowDot = showTime && event.isAllDay, verticalPadding = verticalPadding)
                }
                Spacer(modifier = GlanceModifier.height(4.dp))
            }
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
    val context = LocalContext.current
    val bitmap = if (hollow) {
        GlanceText.renderHollowCircle(context, 8f, color)
    } else {
        GlanceText.renderFilledCircle(context, 8f, color)
    }
    Image(
        provider = ImageProvider(bitmap),
        contentDescription = null,
        modifier = GlanceModifier.size(8.dp),
    )
}

@Composable
private fun DotSeparator() {
    val context = LocalContext.current
    Box(modifier = GlanceModifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Image(
            provider = ImageProvider(
                GlanceText.renderDotoText(context, "· · ·", 10f, VantaDotWidgetTheme.GreyMedium.toArgb())
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
