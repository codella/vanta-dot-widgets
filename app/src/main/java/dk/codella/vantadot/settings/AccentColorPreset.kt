package dk.codella.vantadot.settings

import androidx.compose.ui.graphics.Color

enum class AccentColorPreset(
    val displayName: String,
    val swatchColor: Color,
    val inProgressBg: Color,
    val inProgressBorder: Color,
) {
    AMBER("Amber", Color(0xFFD4A017), Color(0xFF302818), Color(0xFF6B5928)),
    RED("Red", Color(0xFFE8212A), Color(0xFF301818), Color(0xFF6B2828)),
    BLUE("Blue", Color(0xFF4285F4), Color(0xFF182030), Color(0xFF28456B)),
    GREEN("Green", Color(0xFF0F9D58), Color(0xFF183018), Color(0xFF286B28)),
    PURPLE("Purple", Color(0xFFAB47BC), Color(0xFF281830), Color(0xFF5B286B)),
    CYAN("Cyan", Color(0xFF00BCD4), Color(0xFF183030), Color(0xFF286B6B)),
    WHITE("White", Color(0xFFFFFFFF), Color(0xFF2A2A2A), Color(0xFF555555));

    companion object {
        fun fromIndex(index: Int): AccentColorPreset =
            entries.getOrElse(index) { AMBER }
    }
}
