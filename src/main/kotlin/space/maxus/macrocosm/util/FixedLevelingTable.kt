package space.maxus.macrocosm.util

abstract class FixedLevelingTable(val levels: List<Double>) : LevelingTable {
    override fun expForLevel(lvl: Int): Double {
        return levels.subList(0, lvl - 1).sum()
    }

    override fun shouldLevelUp(lvl: Int, current: Double, next: Double): Boolean {
        val requiredExp = expForLevel(lvl + 1) - current
        return next > requiredExp
    }
}
