package space.maxus.macrocosm.item.runes

import net.axay.kspigot.extensions.bukkit.toComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import space.maxus.macrocosm.item.buffs.BuffRegistry
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.stats.stats
import space.maxus.macrocosm.text.comp
import space.maxus.macrocosm.util.id

interface ApplicableRune {
    val id: Identifier

    fun locked(): Component
    fun unlocked(): Component
    fun full(level: Int): Component
    fun stats(level: Int): Statistics
}

data class RuneState(val open: Boolean = false, val tier: Int = 0) {
    companion object {
        val ZERO: RuneState = RuneState(false, 0)
    }
}

private fun colorFromTier(tier: Int): TextColor {
    return when (tier) {
        1 -> NamedTextColor.WHITE
        2 -> NamedTextColor.GREEN
        3 -> NamedTextColor.BLUE
        4 -> NamedTextColor.DARK_PURPLE
        5 -> NamedTextColor.GOLD
        else -> NamedTextColor.AQUA
    }
}

enum class DefaultRune(private val char: String, private val color: TextColor, private val baseStats: Statistics): ApplicableRune {
    EMERALD("◎", TextColor.color(0x20FB58), stats { critDamage = 5f }),
    DIAMOND("◆", TextColor.color(0x50B6CC), stats { intelligence = 10f }),
    REDSTONE("☄", TextColor.color(0xCC6350), stats { health = 10f }),
    AMETHYST("▒", TextColor.color(0x9950CC), stats { defense = 15f }),
    SILVER("★", TextColor.color(0xF6FDFA), stats { critChance = 2f }),
    MITHRIL("⚑", TextColor.color(0x70EFBC), stats { miningSpeed = 10f; miningFortune = 5f }),
    ADAMANTITE("\uD83D\uDD25", TextColor.color(0xFB7420), stats { strength = 6f }),
    CRYING_PEARL("∞", TextColor.color(0xFDFC6B), stats { attackSpeed = 2.5f }),
    MOONSTONE("☽", TextColor.color(0xFFEEB8), stats { intelligence = 5f; abilityDamage = 2.5f }),
    PRISMARITE("⚓", TextColor.color(0x15C38B), stats { seaCreatureChance = .5f }),
    CRYSTALLITE("\uD83C\uDFA3", TextColor.color(0x2485EC), stats { treasureChance = .5f })
    ;

    override val id: Identifier = id(name.lowercase())

    override fun full(level: Int): Component {
        val tierColor = colorFromTier(level)
        return "[".toComponent().color(tierColor).append(comp(char).color(color))
            .append("]".toComponent().color(tierColor))
    }

    override fun locked(): Component {
        return comp("[$char]").color(NamedTextColor.DARK_GRAY)
    }

    override fun unlocked(): Component {
        return comp("[$char]").color(NamedTextColor.GRAY)
    }

    override fun stats(level: Int): Statistics {
        val clone = baseStats.clone()
        clone.multiply(level.toFloat())
        return clone
    }

    companion object {
        fun init() {
            for (v in values()) {
                BuffRegistry.registerRune(v.id, v)
            }
        }
    }
}
