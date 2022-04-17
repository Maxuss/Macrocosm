package space.maxus.macrocosm.stats

import net.minecraft.nbt.CompoundTag

fun specialStats(builder: SpecialStatistics.() -> Unit) = SpecialStatistics().apply(builder)

class SpecialStatistics {
    private var values: HashMap<SpecialStatistic, Float> =
        hashMapOf(*SpecialStatistic.values().map { it to 0f }.toTypedArray())

    var knockbackBoost: Float
        get() = values[SpecialStatistic.KB_BOOST]!!
        set(value) {
            values[SpecialStatistic.KB_BOOST] = value
        }

    var knockbackResistance: Float
        get() = values[SpecialStatistic.KB_RESISTANCE]!!
        set(value) {
            values[SpecialStatistic.KB_RESISTANCE] = value
        }

    var fireResistance: Float
        get() = values[SpecialStatistic.FIRE_RESISTANCE]!!
        set(value) {
            values[SpecialStatistic.FIRE_RESISTANCE] = value
        }

    var blastResistance: Float
        get() = values[SpecialStatistic.BLAST_RESISTANCE]!!
        set(value) {
            values[SpecialStatistic.BLAST_RESISTANCE] = value
        }

    var fallResistance: Float
        get() = values[SpecialStatistic.FALL_RESISTANCE]!!
        set(value) {
            values[SpecialStatistic.FALL_RESISTANCE] = value
        }

    var statBoost: Float
        get() = values[SpecialStatistic.STAT_BOOST]!!
        set(value) {
            values[SpecialStatistic.STAT_BOOST] = value
        }

    fun clone(): SpecialStatistics {
        val clone = SpecialStatistics()
        for ((stat, value) in values) {
            clone.values[stat] = value
        }
        return clone
    }

    fun multiply(multiplier: Float) {
        for ((stat, _) in values) {
            values[stat] = values[stat]!! * multiplier
        }
    }

    fun map() = values

    fun compound(): CompoundTag {
        val cmp = CompoundTag()
        for ((stat, value) in values) {
            if (value == 0f)
                continue
            cmp.putFloat(stat.name, value)
        }
        return cmp
    }

    fun increase(other: SpecialStatistics) {
        for ((stat, value) in other.values) {
            if (other.values[stat]!! == 0f)
                continue
            values[stat] = values[stat]!! + value
        }
    }
}
