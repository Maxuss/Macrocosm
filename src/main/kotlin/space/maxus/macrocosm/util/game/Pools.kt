package space.maxus.macrocosm.util.game

import space.maxus.macrocosm.util.math.Chance
import kotlin.random.Random

object Pools {
    fun <T : Chance> roll(values: List<T>, modifier: Float) = values.filter {
        val mod = 1 + (modifier / 100.0)
        Random.nextFloat() <= mod * it.chance
    }
}
