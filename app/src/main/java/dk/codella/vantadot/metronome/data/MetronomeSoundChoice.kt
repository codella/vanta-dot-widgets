package dk.codella.vantadot.metronome.data

import dk.codella.vantadot.R

enum class MetronomeSoundChoice(
    val displayName: String,
    val normalResId: Int,
    val accentResId: Int,
) {
    CLICK("Click", R.raw.click_normal, R.raw.click_accent),
    WOODBLOCK("Wood Block", R.raw.wood_normal, R.raw.wood_accent),
    BEEP("Digital Beep", R.raw.beep_normal, R.raw.beep_accent);

    companion object {
        fun fromIndex(index: Int): MetronomeSoundChoice =
            entries.getOrElse(index) { CLICK }
    }
}
