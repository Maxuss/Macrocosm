package space.maxus.macrocosm.util

interface LevelingTable {
    fun expForLevel(lvl: Int): Double
    fun shouldLevelUp(lvl: Int, current: Double, next: Double): Boolean

    fun totalExpForLevel(lvl: Int): Double {
        return (0..lvl).toList().sumOf { expForLevel(it) }
    }
}
