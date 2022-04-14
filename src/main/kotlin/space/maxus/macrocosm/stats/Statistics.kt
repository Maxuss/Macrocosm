package space.maxus.macrocosm.stats

import net.axay.kspigot.extensions.bukkit.toComponent
import net.kyori.adventure.text.Component
import java.sql.ResultSet

@Suppress("unused")
@JvmInline
value class Statistics(private val self: HashMap<Statistic, Float>) {
    companion object {
        @JvmStatic
        fun default(): Statistics {
            val map = hashMapOf<Statistic, Float>()
            for(stat in Statistic.values()) {
                map[stat] = stat.default
            }
            return Statistics(map)
        }

        @JvmStatic
        fun fromRes(res: ResultSet): Statistics {
            val map = hashMapOf<Statistic, Float>()
            for(stat in Statistic.values()) {
                map[stat] = res.getFloat(stat.name)
            }
            return Statistics(map)
        }
    }

    var strength: Float
        get() = self[Statistic.STRENGTH]!!
        set(value) { self[Statistic.STRENGTH] = value }

    var damage: Float
        get() = self[Statistic.DAMAGE]!!
        set(value) { self[Statistic.DAMAGE] = value }

    var ferocity: Float
        get() = self[Statistic.FEROCITY]!!
        set(value) { self[Statistic.FEROCITY] = value }

    var critChance: Float
        get() = self[Statistic.CRIT_CHANCE]!!
        set(value) { self[Statistic.CRIT_CHANCE] = value }

    var critDamage: Float
        get() = self[Statistic.CRIT_DAMAGE]!!
        set(value) { self[Statistic.CRIT_DAMAGE] = value }

    var attackSpeed: Float
        get() = self[Statistic.BONUS_ATTACK_SPEED]!!
        set(value) { self[Statistic.BONUS_ATTACK_SPEED] = value }

    var seaCreatureChance: Float
        get() = self[Statistic.SEA_CREATURE_CHANCE]!!
        set(value) { self[Statistic.SEA_CREATURE_CHANCE] = value }

    var abilityDamage: Float
        get() = self[Statistic.ABILITY_DAMAGE]!!
        set(value) { self[Statistic.ABILITY_DAMAGE] = value }

    var health: Float
        get() = self[Statistic.HEALTH]!!
        set(value) { self[Statistic.HEALTH] = value }

    var defense: Float
        get() = self[Statistic.DEFENSE]!!
        set(value) { self[Statistic.DEFENSE] = value }

    var trueDefense: Float
        get() = self[Statistic.TRUE_DEFENSE]!!
        set(value) { self[Statistic.TRUE_DEFENSE] = value }

    var speed: Float
        get() = self[Statistic.SPEED]!!
        set(value) { self[Statistic.SPEED] = value }

    var intelligence: Float
        get() = self[Statistic.INTELLIGENCE]!!
        set(value) { self[Statistic.INTELLIGENCE] = value }

    var petLuck: Float
        get() = self[Statistic.PET_LUCK]!!
        set(value) { self[Statistic.PET_LUCK] = value }

    var magicFind: Float
        get() = self[Statistic.MAGIC_FIND]!!
        set(value) { self[Statistic.MAGIC_FIND] = value }

    var damageBoost: Float
        get() = self[Statistic.DAMAGE_BOOST]!!
        set(value) { self[Statistic.DAMAGE_BOOST] = value }

    var damageReduction: Float
        get() = self[Statistic.DAMAGE_REDUCTION]!!
        set(value) { self[Statistic.DAMAGE_REDUCTION] = value }

    operator fun get(stat: Statistic) = self[stat]!!
    operator fun set(stat: Statistic, value: Float) {
        self[stat] = value
    }

    fun iter() = self

    fun formatSimple(): Component {
        val base = Component.empty()
        for((stat, value) in self) {
            val formatted = stat.formatSimple(value) ?: continue
            base.append(formatted).append("\n".toComponent())
        }
        return base
    }

    fun formatFancy(): Component {
        val base = Component.empty()
        for((stat, value) in self) {
            val formatted = stat.formatFancy(value) ?: continue
            base.append(formatted).append("\n".toComponent())
        }
        return base
    }
}
