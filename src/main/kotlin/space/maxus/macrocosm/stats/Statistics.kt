package space.maxus.macrocosm.stats

import net.axay.kspigot.extensions.bukkit.toComponent
import net.kyori.adventure.text.Component
import net.minecraft.nbt.CompoundTag
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.item.buffs.MinorItemBuff
import space.maxus.macrocosm.item.runes.StatRune
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.annotations.PreviewFeature
import java.io.Serializable
import java.util.*
import kotlin.math.ceil
import kotlin.math.roundToInt

inline fun defaultStats(builder: Statistics.() -> Unit) = Statistics.default().apply(builder)
inline fun stats(builder: Statistics.() -> Unit) = Statistics.zero().apply(builder)

data class Statistics(private val self: TreeMap<Statistic, Float>) : Serializable {
    companion object {
        @JvmStatic
        fun zero(): Statistics {
            val map = TreeMap<Statistic, Float>()
            for (stat in Statistic.values()) {
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

    }

    var farmingFortune: Float
        get() = self[Statistic.FARMING_FORTUNE]!!
        set(value) {
            self[Statistic.FARMING_FORTUNE] = value
        }
    var foragingFortune: Float
        get() = self[Statistic.FORAGING_FORTUNE]!!
        set(value) {
            self[Statistic.FORAGING_FORTUNE] = value
        }
    var excavatingFortune: Float
        get() = self[Statistic.EXCAVATING_FORTUNE]!!
        set(value) {
            self[Statistic.EXCAVATING_FORTUNE] = value
        }
    var miningFortune: Float
        get() = self[Statistic.MINING_FORTUNE]!!
        set(value) {
            self[Statistic.MINING_FORTUNE] = value
        }
    var miningSpeed: Float
        get() = self[Statistic.MINING_SPEED]!!
        set(value) {
            self[Statistic.MINING_SPEED] = value
        }
    var treasureChance: Float
        get() = self[Statistic.TREASURE_CHANCE]!!
        set(value) {
            self[Statistic.TREASURE_CHANCE] = value
        }

    var trueDamage: Float
        get() = self[Statistic.TRUE_DAMAGE]!!
        set(value) {
            self[Statistic.TRUE_DAMAGE] = value
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

    var vitality: Float
        get() = self[Statistic.VITALITY]!!
        set(value) {
            self[Statistic.VITALITY] = value
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

    var vigor: Float
        get() = self[Statistic.VIGOR]!!
        set(value) {
            self[Statistic.VIGOR] = value
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

    var summoningPower: Int
        get() = self[Statistic.SUMMONING_POWER]!!.roundToInt()
        set(value) {
            self[Statistic.SUMMONING_POWER] = value.toFloat()
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
        for ((stat, value) in self) {
            if (value == 0f)
                continue
            cmp.putFloat(stat.name, value)
        }
        return cmp
    }

    @OptIn(PreviewFeature::class)
    fun formatSimple(item: MacrocosmItem? = null, baseReforge: Reforge? = item?.reforge): List<Component> {
        val runes = item?.runes
        val buffs = item?.buffs
        val reforge = baseReforge?.stats(item?.rarity ?: Rarity.COMMON)

        val base = mutableListOf<Component>()
        var prev: Statistic? = null
        val dissolvedRunes: Statistics = zero()
        val dissolvedBuffs: HashMap<MinorItemBuff, Statistics> = hashMapOf()

        if (runes != null && runes.size() > 0) {
            for ((_, state) in runes.entries()) {
                val (contained, tier) = state
                if (tier <= 0 || contained == null)
                    continue
                val runeType = Registry.RUNE.find(contained)
                if (runeType is StatRune)
                    dissolvedRunes.increase(runeType.baseStats.clone().apply { multiply(tier.toFloat()) })
            }
        }

        if (!buffs.isNullOrEmpty()) {
            for ((buff, lvl) in buffs) {
                if (lvl <= 0)
                    continue
                val tmp = zero()
                tmp.increase(buff.stats(item, lvl))
                dissolvedBuffs[buff] = tmp
            }
        }

        for ((stat, value) in self) {
            var formatted = stat.formatSimple(value) ?: continue
            if (prev != null && prev.type != stat.type)
                base.add(" ".toComponent())

            // reforges
            if (reforge != null && reforge[stat] != 0f) {
                val amount = reforge[stat]
                var reforgeComp = " <blue>("
                val fmt = Formatting.stats(amount.toBigDecimal(), false)
                reforgeComp += if (amount < 0) fmt else "+$fmt"
                if (stat.percents)
                    reforgeComp += "%"
                formatted = formatted.append(text("$reforgeComp)</blue>"))
            }

            // gemstones
            if (dissolvedRunes[stat] != 0f) {
                val amount = dissolvedRunes[stat]
                var gemComp = " <light_purple>["
                val fmt = Formatting.stats(amount.toBigDecimal(), false)
                gemComp += if (amount < 0) fmt else "+$fmt"
                if (stat.percents)
                    gemComp += "%"
                formatted = formatted.append(text("$gemComp]</light_purple>"))
            }

            // buffs
            for ((buff, stats) in dissolvedBuffs) {
                if (stats[stat] != 0f) {
                    val amount = stats[stat]
                    formatted = formatted.append(" ".toComponent().append(buff.buildFancy(amount.toInt())))
                }
            }

            if (item?.isDungeonised == true) {
                formatted = formatted.append(
                    text(
                        " <dark_gray>(${
                            stat.type.formatSigned(value, false)!!.str()
                        }${if (stat.percents) "%" else ""}<dark_gray>)"
                    )
                )
            }

            base.add(formatted)

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

    fun increase(other: Statistics?) {
        if (other == null)
            return

        for ((stat, _) in self) {
            val otherStat = other.self[stat]!!
            if (otherStat == 0f)
                continue
            self[stat] = self[stat]!! + otherStat
        }
    }

    fun decrease(other: Statistics) {
        for ((stat, _) in self) {
            val otherStat = other.self[stat]!!
            if (otherStat == 0f)
                continue
            self[stat] = self[stat]!! - otherStat
        }
    }

    fun multiply(multiplier: Float) {
        for ((stat, _) in self) {
            val s = self[stat]!!
            if (s == 0f)
                continue
            self[stat] = s * multiplier
        }
    }

    fun round() {
        for ((stat, _) in self) {
            if (self[stat]!! == 0f)
                continue
            self[stat] = ceil(self[stat]!!).roundToInt().toFloat()
        }
    }

    fun ceil() {
        for ((stat, _) in self) {
            if (self[stat]!! == 0f)
                continue
            self[stat] = ceil(self[stat]!!)
        }
    }

    fun floor() {
        for ((stat, _) in self) {
            if (self[stat]!! == 0f)
                continue
            self[stat] = kotlin.math.floor(self[stat]!!)
        }
    }

    fun clone(): Statistics {
        val clone = TreeMap<Statistic, Float>()
        clone.putAll(self)
        return Statistics(clone)
    }
}
