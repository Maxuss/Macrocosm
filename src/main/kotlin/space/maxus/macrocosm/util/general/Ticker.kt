package space.maxus.macrocosm.util.general

import java.util.concurrent.atomic.AtomicInteger

class Ticker(val range: IntRange) {
    private var cursor = AtomicInteger(range.first)
    private var direction: Direction = Direction.FORWARD
    fun position(): Int = cursor.get()
    fun tick(): Int {
        val c = when (direction) {
            Direction.FORWARD -> {
                val c = cursor.incrementAndGet()
                if (c >= range.last)
                    direction = Direction.BACKWARDS
                c
            }

            Direction.BACKWARDS -> {
                val c = cursor.decrementAndGet()
                if (c <= range.first)
                    direction = Direction.FORWARD
                c
            }
        }
        return c
    }

    enum class Direction {
        FORWARD,
        BACKWARDS
    }
}
