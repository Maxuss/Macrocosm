package space.maxus.macrocosm.util.math

abstract class FixedLevelingTable(val levels: List<Double>) : LevelingTable {
    override fun expForLevel(lvl: Int): Double {
        return try {
            levels[lvl - 1]
        } catch (ignored: ArrayIndexOutOfBoundsException) {
            .0
        }
    }

    override fun shouldLevelUp(lvl: Int, overflow: Double): Boolean {
        if (levels.size < lvl)
            return false
        val required = levels[lvl]
        return required - overflow <= .0
    }
}
