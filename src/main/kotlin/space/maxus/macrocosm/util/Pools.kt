package space.maxus.macrocosm.util

import kotlin.random.Random

object Pools {
    fun <T : Chance> roll(values: List<T>, modifier: Float) = values.filter {
        val mod = 1 + (modifier / 100.0)
        Random.nextFloat() <= mod * it.chance
    }
}
