package dk.codella.vantadot.calendar.data

data class CalendarEvent(
    val id: Long,
    val title: String,
    val beginTime: Long,
    val endTime: Long,
    val isAllDay: Boolean,
    val calendarColor: Int,
    val location: String?,
    val description: String? = null,
    val selfAttendeeStatus: Int = ATTENDEE_STATUS_ACCEPTED,
) {
    fun toJson(): org.json.JSONObject = org.json.JSONObject().apply {
        put("id", id)
        put("title", title)
        put("beginTime", beginTime)
        put("endTime", endTime)
        put("isAllDay", isAllDay)
        put("calendarColor", calendarColor)
        put("location", location ?: "")
        put("description", description ?: "")
        put("selfAttendeeStatus", selfAttendeeStatus)
    }
    val isTentative: Boolean
        get() = selfAttendeeStatus == ATTENDEE_STATUS_INVITED ||
                selfAttendeeStatus == ATTENDEE_STATUS_TENTATIVE

    val hasVideoConference: Boolean
        get() {
            val text = listOfNotNull(location, description).joinToString(" ").lowercase()
            return VIDEO_PATTERNS.any { it in text }
        }

    val hasAttachments: Boolean
        get() {
            val text = listOfNotNull(description).joinToString(" ").lowercase()
            return ATTACHMENT_PATTERNS.any { it in text }
        }

    companion object {
        const val ATTENDEE_STATUS_NONE = 0
        const val ATTENDEE_STATUS_ACCEPTED = 1
        const val ATTENDEE_STATUS_DECLINED = 2
        const val ATTENDEE_STATUS_INVITED = 3
        const val ATTENDEE_STATUS_TENTATIVE = 4

        fun fromJson(json: org.json.JSONObject) = CalendarEvent(
            id = json.getLong("id"),
            title = json.getString("title"),
            beginTime = json.getLong("beginTime"),
            endTime = json.getLong("endTime"),
            isAllDay = json.getBoolean("isAllDay"),
            calendarColor = json.getInt("calendarColor"),
            location = json.getString("location").ifEmpty { null },
            description = json.getString("description").ifEmpty { null },
            selfAttendeeStatus = json.optInt("selfAttendeeStatus", ATTENDEE_STATUS_ACCEPTED),
        )

        fun toJsonArray(events: List<CalendarEvent>): String =
            org.json.JSONArray(events.map { it.toJson() }).toString()

        fun fromJsonArray(json: String): List<CalendarEvent> {
            if (json.isBlank()) return emptyList()
            val array = org.json.JSONArray(json)
            return (0 until array.length()).map { fromJson(array.getJSONObject(it)) }
        }

        private val VIDEO_PATTERNS = listOf(
            "zoom.us", "zoom.com",
            "meet.google.com",
            "teams.microsoft.com", "teams.live.com",
            "webex.com",
            "whereby.com",
            "facetime",
        )

        private val ATTACHMENT_PATTERNS = listOf(
            "drive.google.com",
            "docs.google.com",
            "sheets.google.com",
            "slides.google.com",
            "dropbox.com",
            "onedrive.live.com",
            "sharepoint.com",
            "notion.so",
            "confluence",
            ".pdf", ".docx", ".xlsx", ".pptx",
        )
    }
}
