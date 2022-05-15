package space.maxus.macrocosm.util

abstract class FixedLevelingTable(val levels: List<Double>) : LevelingTable {
    override fun expForLevel(lvl: Int): Double {
        return try {
            levels[lvl - 1]
        } catch (ignored: ArrayIndexOutOfBoundsException) {
            .0
        }
    }

    override fun shouldLevelUp(lvl: Int, overflow: Double, next: Double): Boolean {
        val nextLvl = expForLevel(lvl + 1)
        return nextLvl - overflow - next <= .0
    }
}
