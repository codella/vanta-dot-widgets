package dk.codella.vantadot.calendar.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
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
import dk.codella.vantadot.common.CircleStyle
import dk.codella.vantadot.common.GlanceText
import dk.codella.vantadot.common.VantaDotWidgetTheme
import dk.codella.vantadot.settings.AccentColorPreset
import dk.codella.vantadot.settings.FontSizePreset
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

private fun urgencyBackground(urgency: Urgency, accent: AccentColorPreset = AccentColorPreset.AMBER): Color = when (urgency) {
    Urgency.NONE -> Color.Transparent
    Urgency.SUBTLE -> VantaDotWidgetTheme.HighlightSubtle
    Urgency.LOW -> VantaDotWidgetTheme.HighlightLow
    Urgency.MEDIUM -> VantaDotWidgetTheme.HighlightMedium
    Urgency.HIGH -> VantaDotWidgetTheme.HighlightHigh
    Urgency.IN_PROGRESS -> accent.inProgressBg
}

@Composable
fun CalendarWidgetContent(
    events: List<CalendarEvent>,
    hasPermission: Boolean,
    isRefreshing: Boolean = false,
    refreshPhase: Int = 0,
    showHeader: Boolean = true,
    showLocation: Boolean = true,
    accentColorIndex: Int = 0,
    use24HourFormat: Boolean = true,
    showCompactTime: Boolean = false,
    fontSizePreset: Int = 1,
) {
    val size = LocalSize.current
    val isFull = size.height >= CalendarWidgetSizes.FULL.height
    val allDayEvents = events.filter { it.isAllDay }
    val timedEvents = events.filter { !it.isAllDay }.sortedBy { it.beginTime }
    val accent = AccentColorPreset.fromIndex(accentColorIndex)
    val fontScale = FontSizePreset.fromIndex(fontSizePreset).scaleFactor

    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .cornerRadius(VantaDotWidgetTheme.CornerRadius)
            .background(VantaDotWidgetTheme.GreyDark)
            .padding(VantaDotWidgetTheme.Padding),
    ) {
        Column(modifier = GlanceModifier.fillMaxSize()) {
            if (showHeader) {
                SectionHeader("Upcoming events", isRefreshing, refreshPhase, fontScale)
                Spacer(modifier = GlanceModifier.height(12.dp))
            }
            when {
                !hasPermission -> PermissionMessage(fontScale)
                events.isEmpty() -> EmptyMessage(fontScale)
                else -> {
                    if (allDayEvents.isNotEmpty()) {
                        AllDaySection(allDayEvents, fontScale)
                        Spacer(modifier = GlanceModifier.height(8.dp))
                    }
                    ScrollableEventList(timedEvents.take(20), modifier = GlanceModifier.defaultWeight().fillMaxWidth(), showTime = true, showLocation = if (isFull) showLocation else false, verticalPadding = 6.dp, accent = accent, fontScale = fontScale, use24HourFormat = use24HourFormat, showCompactTime = showCompactTime)
                }
            }
        }
    }
}


@Composable
private fun SectionHeader(text: String, isRefreshing: Boolean = false, refreshPhase: Int = 0, fontScale: Float = 1f) {
    val context = LocalContext.current
    val bitmap = GlanceText.renderDotoText(
        context = context,
        text = text.uppercase(),
        textSizeSp = 14f * fontScale,
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
private fun PermissionMessage(fontScale: Float = 1f) {
    val context = LocalContext.current
    Image(
        provider = ImageProvider(
            GlanceText.renderDotoText(context, "Calendar permission required", 14f * fontScale, VantaDotWidgetTheme.GreyLightArgb)
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
private fun EmptyMessage(fontScale: Float = 1f) {
    val context = LocalContext.current
    val dayIndex = (System.currentTimeMillis() / 86_400_000).mod(INSPIRATIONAL_QUOTES.size)
    val quote = INSPIRATIONAL_QUOTES[dayIndex]
    Column {
        Image(
            provider = ImageProvider(
                GlanceText.renderDotoText(context, "No upcoming events", 14f * fontScale, VantaDotWidgetTheme.GreyLightArgb)
            ),
            contentDescription = "No upcoming events",
        )
        Spacer(modifier = GlanceModifier.height(8.dp))
        Image(
            provider = ImageProvider(
                GlanceText.renderDotoText(context, quote, 12f * fontScale, VantaDotWidgetTheme.GreyLightArgb)
            ),
            contentDescription = quote,
        )
    }
}

@Composable
private fun AllDaySection(events: List<CalendarEvent>, fontScale: Float = 1f) {
    val context = LocalContext.current
    val widgetWidth = LocalSize.current.width
    // Available width: widget - outer padding(12*2) - inner box padding(10*2) - dot(8) - spacer(8) - right margin(12)
    val titleMaxWidth = (widgetWidth - 72.dp).value
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
                    GlanceText.renderDotoText(context, "All day", 11f * fontScale, VantaDotWidgetTheme.GreyLightArgb)
                ),
                contentDescription = "All day",
            )
            Spacer(modifier = GlanceModifier.height(6.dp))
            events.forEachIndexed { index, event ->
                if (index > 0) Spacer(modifier = GlanceModifier.height(6.dp))
                val allDayTitleColor = if (event.isTentative) VantaDotWidgetTheme.TentativeTextArgb else android.graphics.Color.WHITE
                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Image(
                        provider = ImageProvider(GlanceText.renderFilledSquare(context, 8f, event.calendarColor)),
                        contentDescription = null,
                        modifier = GlanceModifier.size(8.dp),
                    )
                    Spacer(modifier = GlanceModifier.width(8.dp))
                    Image(
                        provider = ImageProvider(GlanceText.renderDotoText(context, event.title, 14f * fontScale, allDayTitleColor, maxWidthDp = titleMaxWidth)),
                        contentDescription = event.title,
                    )
                }
            }
        }
    }
}

@Composable
private fun EventHighlight(urgency: Urgency, accent: AccentColorPreset = AccentColorPreset.AMBER, content: @Composable () -> Unit) {
    if (urgency == Urgency.NONE) {
        content()
    } else if (urgency == Urgency.IN_PROGRESS) {
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .cornerRadius(8.dp)
                .background(accent.inProgressBorder)
                .padding(1.dp),
        ) {
            Box(
                modifier = GlanceModifier
                    .fillMaxWidth()
                    .cornerRadius(7.dp)
                    .background(urgencyBackground(urgency, accent)),
            ) {
                content()
            }
        }
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
    verticalPadding: Dp = 0.dp,
    fontScale: Float = 1f,
    use24HourFormat: Boolean = true,
    showCompactTime: Boolean = false,
) {
    val context = LocalContext.current
    val widgetWidth = LocalSize.current.width
    // Account for icons in title width: each icon is 14dp + 4dp spacer
    var iconsWidth = 0.dp
    if (event.hasVideoConference) iconsWidth += 18.dp
    if (event.hasAttachments) iconsWidth += 18.dp
    // Available width for title: widget - outer padding(12*2) - column start(10) - dot(8) - spacer(8) - icons - right margin(12)
    val titleMaxWidth = (widgetWidth - 62.dp - iconsWidth).value
    // Available width for time/location: widget - outer padding(12*2) - column start(10) - inner start padding(16) - right margin(12)
    val detailMaxWidth = (widgetWidth - 62.dp).value
    val titleColor = if (event.isTentative) VantaDotWidgetTheme.TentativeTextArgb else android.graphics.Color.WHITE
    val dotStyle = when {
        event.isTentative -> CircleStyle.DASHED
        showTime && event.isAllDay -> CircleStyle.HOLLOW
        else -> CircleStyle.FILLED
    }
    Column(modifier = GlanceModifier.fillMaxWidth().padding(start = 10.dp, top = verticalPadding, bottom = verticalPadding)) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CalendarColorDot(event.calendarColor, style = dotStyle)
            Spacer(modifier = GlanceModifier.width(8.dp))
            Image(
                provider = ImageProvider(GlanceText.renderDotoText(context, event.title, 14f * fontScale, titleColor, maxWidthDp = titleMaxWidth)),
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
        if (showTime) {
            val timeText = formatEventTime(event, use24HourFormat, showCompactTime)
            Image(
                provider = ImageProvider(
                    GlanceText.renderDotoText(context, timeText, 12f * fontScale, VantaDotWidgetTheme.GreyLightArgb, maxWidthDp = detailMaxWidth)
                ),
                contentDescription = timeText,
                modifier = GlanceModifier.padding(start = 16.dp),
            )
        }
        val displayLocation = if (showLocation) event.location?.let { cleanLocationDisplay(it) } else null
        if (displayLocation != null) {
            Image(
                provider = ImageProvider(
                    GlanceText.renderDotoText(context, displayLocation, 12f * fontScale, VantaDotWidgetTheme.GreyLightArgb, maxWidthDp = detailMaxWidth)
                ),
                contentDescription = displayLocation,
                modifier = GlanceModifier.padding(start = 16.dp),
            )
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
    accent: AccentColorPreset = AccentColorPreset.AMBER,
    fontScale: Float = 1f,
    use24HourFormat: Boolean = true,
    showCompactTime: Boolean = false,
) {
    LazyColumn(modifier = modifier) {
        items(events, itemId = { it.id }) { event ->
            Column(modifier = GlanceModifier.fillMaxWidth()) {
                EventHighlight(calcUrgency(event), accent = accent) {
                    EventRow(event, showTime = showTime, showLocation = showLocation, verticalPadding = verticalPadding, fontScale = fontScale, use24HourFormat = use24HourFormat, showCompactTime = showCompactTime)
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
private fun CalendarColorDot(color: Int, style: CircleStyle = CircleStyle.FILLED) {
    val context = LocalContext.current
    Image(
        provider = ImageProvider(GlanceText.renderCircle(context, 8f, color, style)),
        contentDescription = null,
        modifier = GlanceModifier.size(8.dp),
    )
}

@Composable
private fun DotSeparator(fontScale: Float = 1f) {
    val context = LocalContext.current
    Box(modifier = GlanceModifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Image(
            provider = ImageProvider(
                GlanceText.renderDotoText(context, "· · ·", 10f * fontScale, VantaDotWidgetTheme.GreyMediumArgb)
            ),
            contentDescription = null,
        )
    }
}

private fun formatEventTime(event: CalendarEvent, use24HourFormat: Boolean = true, showCompactTime: Boolean = false): String {
    if (event.isAllDay) return "All day"
    val pattern = if (use24HourFormat) "HH:mm" else "h:mma"
    val timeFormat = SimpleDateFormat(pattern, Locale.getDefault())
    val begin = timeFormat.format(Date(event.beginTime))
    if (showCompactTime) return begin
    val end = timeFormat.format(Date(event.endTime))
    return "$begin – $end"
}

private val URL_REGEX = Regex("""https?://\S+""")

private fun cleanLocationDisplay(location: String): String? {
    val stripped = location.replace(URL_REGEX, "").trim()
    if (stripped.isNotEmpty()) return stripped
    val firstUrl = URL_REGEX.find(location)?.value ?: return null
    return try { java.net.URI(firstUrl).host } catch (_: Exception) { null }
}
