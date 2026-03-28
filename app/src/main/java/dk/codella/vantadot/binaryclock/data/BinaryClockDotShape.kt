package dk.codella.vantadot.binaryclock.data

enum class BinaryClockDotShape(val displayName: String) {
    CIRCLE("Circle"),
    SQUARE("Square"),
    DIAMOND("Diamond");

    companion object {
        fun fromIndex(index: Int) = entries.getOrElse(index) { CIRCLE }
    }
}
