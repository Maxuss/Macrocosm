package space.maxus.macrocosm.pets

import net.minecraft.Util
import space.maxus.macrocosm.util.math.LevelingTable
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min

class ProgressivePetTable(val modifier: Float) : LevelingTable {
    companion object {
        val BASE: List<Double> = Util.make(mutableListOf()) {
            for (i in 0..199) {
                val lvl = i + 1
                // https://www.desmos.com/calculator/2jrlu04ezy
                val cumulative = (lvl * lvl) * ln(lvl.toDouble()) * min(max(100.0, lvl * 2.0), 198.0)
                it.add(cumulative)
            }
        }
    }

    override fun expForLevel(lvl: Int): Double {
        if (lvl <= 0)
            return .1
        else if(lvl == 1)
            return BASE[1] * modifier
        return (BASE[lvl - 1] - BASE[lvl - 2]) * modifier
    }

    override fun shouldLevelUp(lvl: Int, overflow: Double): Boolean {
        val nextLvl = expForLevel(lvl + 1)
        return nextLvl - overflow <= .0
    }
}
