package space.maxus.macrocosm.stats

import net.axay.kspigot.extensions.bukkit.toComponent
import net.kyori.adventure.text.Component
import net.minecraft.nbt.CompoundTag
import java.sql.ResultSet
import java.util.TreeMap

inline fun stats(builder: Statistics.() -> Unit) = Statistics.zero().apply(builder)

@Suppress("unused")
@JvmInline
value class Statistics(private val self: TreeMap<Statistic, Float>) {
    companion object {
        @JvmStatic
        fun zero(): Statistics {
            val map = TreeMap<Statistic, Float>()
            for(stat in Statistic.values()) {
                map[stat] = 0f
            }
            return Statistics(map)
        }


        @JvmStatic
        fun default(): Statistics {
            val map = TreeMap<Statistic, Float>()
            for (stat in Statistic.values()) {
                map[stat] = stat.default
            }
            return Statistics(map)
        }

        @JvmStatic
        fun fromRes(res: ResultSet): Statistics {
            val map = TreeMap<Statistic, Float>()
            for (stat in Statistic.values()) {
                map[stat] = res.getFloat(stat.name)
            }
            return Statistics(map)
        }
    }

    var strength: Float
        get() = self[Statistic.STRENGTH]!!
        set(value) {
            self[Statistic.STRENGTH] = value
        }

    var damage: Float
        get() = self[Statistic.DAMAGE]!!
        set(value) {
            self[Statistic.DAMAGE] = value
        }

    var ferocity: Float
        get() = self[Statistic.FEROCITY]!!
        set(value) {
            self[Statistic.FEROCITY] = value
        }

    var critChance: Float
        get() = self[Statistic.CRIT_CHANCE]!!
        set(value) {
            self[Statistic.CRIT_CHANCE] = value
        }

    var critDamage: Float
        get() = self[Statistic.CRIT_DAMAGE]!!
        set(value) {
            self[Statistic.CRIT_DAMAGE] = value
        }

    var attackSpeed: Float
        get() = self[Statistic.BONUS_ATTACK_SPEED]!!
        set(value) {
            self[Statistic.BONUS_ATTACK_SPEED] = value
        }

    var seaCreatureChance: Float
        get() = self[Statistic.SEA_CREATURE_CHANCE]!!
        set(value) {
            self[Statistic.SEA_CREATURE_CHANCE] = value
        }

    var abilityDamage: Float
        get() = self[Statistic.ABILITY_DAMAGE]!!
        set(value) {
            self[Statistic.ABILITY_DAMAGE] = value
        }

    var health: Float
        get() = self[Statistic.HEALTH]!!
        set(value) {
            self[Statistic.HEALTH] = value
        }

    var defense: Float
        get() = self[Statistic.DEFENSE]!!
        set(value) {
            self[Statistic.DEFENSE] = value
        }

    var trueDefense: Float
        get() = self[Statistic.TRUE_DEFENSE]!!
        set(value) {
            self[Statistic.TRUE_DEFENSE] = value
        }

    var speed: Float
        get() = self[Statistic.SPEED]!!
        set(value) {
            self[Statistic.SPEED] = value
        }

    var intelligence: Float
        get() = self[Statistic.INTELLIGENCE]!!
        set(value) {
            self[Statistic.INTELLIGENCE] = value
        }

    var petLuck: Float
        get() = self[Statistic.PET_LUCK]!!
        set(value) {
            self[Statistic.PET_LUCK] = value
        }

    var magicFind: Float
        get() = self[Statistic.MAGIC_FIND]!!
        set(value) {
            self[Statistic.MAGIC_FIND] = value
        }

    var damageBoost: Float
        get() = self[Statistic.DAMAGE_BOOST]!!
        set(value) {
            self[Statistic.DAMAGE_BOOST] = value
        }

    var damageReduction: Float
        get() = self[Statistic.DAMAGE_REDUCTION]!!
        set(value) {
            self[Statistic.DAMAGE_REDUCTION] = value
        }

    operator fun get(stat: Statistic) = self[stat]!!
    operator fun set(stat: Statistic, value: Float) {
        self[stat] = value
    }

    fun iter() = self

    fun compound(): CompoundTag {
        val cmp = CompoundTag()
        for((stat, value) in self) {
            if(value == 0f)
                continue
            cmp.putFloat(stat.name, value)
        }
        return cmp
    }

    fun formatSimple(): List<Component> {
        val base = mutableListOf<Component>()
        var prev: Statistic? = null
        for ((stat, value) in self) {
            val formatted = stat.formatSimple(value) ?: continue
            if(prev != null) {
                if(prev.type != stat.type) {
                    base.add(" ".toComponent())
                }
            }
            base.add(formatted.append(" ".toComponent()))
            prev = stat
        }
        return base
    }

    fun formatFancy(): List<Component> {
        val base = mutableListOf<Component>()
        for ((stat, value) in self) {
            val formatted = stat.formatFancy(value) ?: continue
            base.add(formatted)
        }
        return base
    }

    fun merge(other: Statistics) {
        for((stat, _) in self) {
            val otherStat = other.self[stat]!!
            if(otherStat == 0f)
                continue
            self[stat] = self[stat]!! + otherStat
        }
    }

    fun clone(): Statistics {
        val clone = TreeMap<Statistic, Float>()
        for((stat, value) in self) {
            clone[stat] = value
        }
        return Statistics(clone)
    }
}
