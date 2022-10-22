package space.maxus.macrocosm.display

/**
 * A priority/position for component rendering
 */
enum class RenderPriority(val priority: Int) {
    HIGHEST(0),
    HIGHER(1),
    HIGH(2),
    NEUTRAL(3),
    LOW(4),
    LOWER(5),
    LOWEST(6)
}
