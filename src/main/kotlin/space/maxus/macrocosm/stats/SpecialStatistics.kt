package space.maxus.macrocosm.stats

import net.minecraft.nbt.CompoundTag
import kotlin.math.roundToInt

fun specialStats(builder: SpecialStatistics.() -> Unit) = SpecialStatistics().apply(builder)

class SpecialStatistics {
    private var self: HashMap<SpecialStatistic, Float> =
        hashMapOf(*SpecialStatistic.values().map { it to 0f }.toTypedArray())

    var knockbackBoost: Float
        get() = self[SpecialStatistic.KB_BOOST]!!
        set(value) {
            self[SpecialStatistic.KB_BOOST] = value
        }

    var knockbackResistance: Float
        get() = self[SpecialStatistic.KB_RESISTANCE]!!
        set(value) {
            self[SpecialStatistic.KB_RESISTANCE] = value
        }

    var fireResistance: Float
        get() = self[SpecialStatistic.FIRE_RESISTANCE]!!
        set(value) {
            self[SpecialStatistic.FIRE_RESISTANCE] = value
        }

    var blastResistance: Float
        get() = self[SpecialStatistic.BLAST_RESISTANCE]!!
        set(value) {
            self[SpecialStatistic.BLAST_RESISTANCE] = value
        }

    var fallResistance: Float
        get() = self[SpecialStatistic.FALL_RESISTANCE]!!
        set(value) {
            self[SpecialStatistic.FALL_RESISTANCE] = value
        }

    var statBoost: Float
        get() = self[SpecialStatistic.STAT_BOOST]!!
        set(value) {
            self[SpecialStatistic.STAT_BOOST] = value
        }

    var extraRegen: Float
        get() = self[SpecialStatistic.EXTRA_REGEN]!!
        set(value) {
            self[SpecialStatistic.EXTRA_REGEN] = value
        }

    var speedCapBoost: Float
        get() = self[SpecialStatistic.SPEED_CAP_BOOST]!!
        set(value) {
            self[SpecialStatistic.SPEED_CAP_BOOST] = value
        }

    operator fun set(stat: SpecialStatistic, value: Float) = this.self.put(stat, value)

    fun clone(): SpecialStatistics {
        val clone = SpecialStatistics()
        for ((stat, value) in self) {
            clone.self[stat] = value
        }
        return clone
    }

    fun multiply(multiplier: Float) {
        for ((stat, _) in self) {
            self[stat] = self[stat]!! * multiplier
        }
    }

    fun iter() = self

    fun compound(): CompoundTag {
        val cmp = CompoundTag()
        for ((stat, value) in self) {
            if (value == 0f)
                continue
            cmp.putFloat(stat.name, value)
        }
        return cmp
    }

    fun increase(other: SpecialStatistics) {
        for ((stat, value) in other.self) {
            if (other.self[stat]!! == 0f)
                continue
            self[stat] = self[stat]!! + value
        }
    }


    fun round() {
        for ((stat, _) in self) {
            if (self[stat]!! == 0f)
                continue
            self[stat] = self[stat]!!.roundToInt().toFloat()
        }
    }

    fun ceil() {
        for ((stat, _) in self) {
            if (self[stat]!! == 0f)
                continue
            self[stat] = kotlin.math.ceil(self[stat]!!)
        }
    }

    fun floor() {
        for ((stat, _) in self) {
            if (self[stat]!! == 0f)
                continue
            self[stat] = kotlin.math.floor(self[stat]!!)
        }
    }
}
