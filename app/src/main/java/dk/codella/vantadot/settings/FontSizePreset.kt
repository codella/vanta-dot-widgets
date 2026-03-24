package dk.codella.vantadot.settings

enum class FontSizePreset(val displayName: String, val scaleFactor: Float) {
    SMALL("Small", 0.85f),
    MEDIUM("Medium", 1.0f),
    LARGE("Large", 1.15f);

    companion object {
        fun fromIndex(index: Int) = entries.getOrElse(index) { MEDIUM }
    }
}
