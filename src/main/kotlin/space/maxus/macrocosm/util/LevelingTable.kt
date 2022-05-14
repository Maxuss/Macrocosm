package space.maxus.macrocosm.util

import kotlin.math.max

interface LevelingTable {
    fun expForLevel(lvl: Int): Double
    fun shouldLevelUp(lvl: Int, overflow: Double, next: Double): Boolean

    fun totalExpForLevel(lvl: Int): Double {
        return (1..max(lvl, 1)).toList().sumOf { expForLevel(it) }
    }
}
