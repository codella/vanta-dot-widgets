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
