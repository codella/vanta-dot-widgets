package dk.codella.vantadot.calendar.data

import android.graphics.Color

object StubCalendarData {

    private val COLORS = intArrayOf(
        Color.parseColor("#4285F4"), // blue
        Color.parseColor("#0F9D58"), // green
        Color.parseColor("#DB4437"), // red
        Color.parseColor("#F4B400"), // yellow
        Color.parseColor("#AB47BC"), // purple
        Color.parseColor("#00BCD4"), // cyan
    )

    private val TITLES = listOf(
        "Sprint planning", "1:1 with manager", "Design review", "Lunch with Anna",
        "Dentist appointment", "Yoga class", "Read chapter 5", "Team standup",
        "Code review", "Product sync", "Coffee chat", "Architecture deep-dive",
        "Retrospective", "Client call", "Interview prep", "Budget review",
        "Onboarding session", "Hack day kickoff", "Lightning talks", "Board meeting",
        "Strategy workshop", "Demo day", "Release planning", "Incident postmortem",
        "Pair programming", "Mentoring session", "Knowledge share", "Offsite planning",
        "Security review", "Perf review prep", "Roadmap alignment", "UX research debrief",
        "Data pipeline review", "API design session", "Ops handover", "Feature kickoff",
        "Stakeholder update", "Cross-team sync", "Tech debt triage", "Customer feedback",
        "Hiring committee", "OKR check-in", "Platform migration", "Infra planning",
        "Sprint review", "Backlog grooming", "Capacity planning", "Vendor call",
        "Analytics deep-dive", "Launch readiness",
    )

    private val LOCATIONS = listOf(
        "https://meet.google.com/abc-defg-hij",
        "https://zoom.us/j/123456789",
        "Café Norden",
        null,
        "Room 4B",
        "https://teams.microsoft.com/l/meetup/xyz",
        null,
        "Fitness World",
        "Tandlægen, Østerbro",
        null,
    )

    private val DESCRIPTIONS = listOf(
        "Agenda: https://docs.google.com/document/d/abc123",
        "Notes: https://docs.google.com/document/d/xyz789",
        null,
        "Slides: https://slides.google.com/presentation/d/ppt456",
        null,
        null,
        "Recording: https://drive.google.com/file/d/rec789",
        null,
    )

    fun getEvents(): List<CalendarEvent> {
        val nowMs = System.currentTimeMillis()
        val min = 60_000L
        val events = mutableListOf<CalendarEvent>()

        // 1 in-progress event
        events.add(CalendarEvent(
            id = 1, title = TITLES[0],
            beginTime = nowMs - 5 * min, endTime = nowMs + 25 * min,
            isAllDay = false, calendarColor = COLORS[0],
            location = LOCATIONS[0], description = DESCRIPTIONS[0],
        ))

        // 2 all-day events
        events.add(CalendarEvent(
            id = 100, title = "Birthday party prep",
            beginTime = nowMs - 12 * 60 * min, endTime = nowMs + 12 * 60 * min,
            isAllDay = true, calendarColor = COLORS[4], location = null,
        ))
        events.add(CalendarEvent(
            id = 101, title = "Company holiday",
            beginTime = nowMs - 12 * 60 * min, endTime = nowMs + 12 * 60 * min,
            isAllDay = true, calendarColor = COLORS[2], location = null,
        ))

        // 47 timed events spread across the next 8 hours
        for (i in 1..47) {
            val offsetMin = i * 10L // every 10 minutes
            val status = when {
                i % 7 == 0 -> CalendarEvent.ATTENDEE_STATUS_TENTATIVE
                i % 11 == 0 -> CalendarEvent.ATTENDEE_STATUS_INVITED
                else -> CalendarEvent.ATTENDEE_STATUS_ACCEPTED
            }
            events.add(CalendarEvent(
                id = (i + 1).toLong(),
                title = TITLES[i % TITLES.size],
                beginTime = nowMs + offsetMin * min,
                endTime = nowMs + (offsetMin + 30) * min,
                isAllDay = false,
                calendarColor = COLORS[i % COLORS.size],
                location = LOCATIONS[i % LOCATIONS.size],
                description = DESCRIPTIONS[i % DESCRIPTIONS.size],
                selfAttendeeStatus = status,
            ))
        }

        return events
    }
}
