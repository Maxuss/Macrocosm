package space.maxus.macrocosm.util.data

import kotlin.math.roundToLong
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class Ticks private constructor(private val inner: Duration) {
    companion object {
        fun millis(ms: Long) = Ticks(ms.toDuration(DurationUnit.MILLISECONDS))
        fun secs(sec: Long) = Ticks(sec.toDuration(DurationUnit.SECONDS))
    }

    fun ticks(): Long {
        val millis = inner.inWholeMilliseconds
        // 1 tick -> 1/20 sec -> 1/(20 * 1000) ms
        return (millis / (20f * 1000f)).roundToLong()
    }
}
