package space.maxus.macrocosm.util

class Ticker(val range: IntRange) {
    private var cursor = range.first
    private var direction: Direction = Direction.FORWARD
    fun position(): Int = cursor
    fun tick(): Int {
        when(direction) {
            Direction.FORWARD -> {
                cursor++
                if(cursor >= range.last)
                    direction = Direction.BACKWARDS
            }
            Direction.BACKWARDS -> {
                cursor--
                if(cursor <= range.first) {
                    direction = Direction.FORWARD
                }
            }
        }
        return cursor
    }

    enum class Direction {
        FORWARD,
        BACKWARDS
    }
}
