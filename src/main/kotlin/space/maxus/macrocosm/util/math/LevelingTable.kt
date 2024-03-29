package space.maxus.macrocosm.util.math

import kotlin.math.max

interface LevelingTable {
    fun expForLevel(lvl: Int): Double
    fun shouldLevelUp(lvl: Int, overflow: Double): Boolean

    fun totalExpForLevel(lvl: Int): Double {
        return (1..max(lvl, 1)).toList().sumOf { expForLevel(it) }
    }
}
