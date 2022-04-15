package space.maxus.macrocosm.stats

import net.axay.kspigot.extensions.bukkit.toComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.text.comp
import java.util.*

enum class Statistic(
    val type: StatisticType,
    private val color: TextColor,
    private val specialChar: Char,
    val default: Float = 0f,
    val percents: Boolean = false,
    private val hidden: Boolean = false,
    private val hiddenFancy: Boolean = false
) {
    DAMAGE(StatisticType.OFFENSIVE, NamedTextColor.RED, '❁', 10f, hiddenFancy = true),
    STRENGTH(StatisticType.OFFENSIVE, NamedTextColor.RED, '❁', 10f),
    FEROCITY(StatisticType.OFFENSIVE, NamedTextColor.RED, '⫽', percents = true),
    CRIT_CHANCE(StatisticType.OFFENSIVE, NamedTextColor.BLUE, '☣', 10f, percents = true),
    CRIT_DAMAGE(StatisticType.OFFENSIVE, NamedTextColor.BLUE, '☠', 100f, true),
    BONUS_ATTACK_SPEED(StatisticType.OFFENSIVE, NamedTextColor.YELLOW, '⚔', percents = true),
    SEA_CREATURE_CHANCE(StatisticType.OFFENSIVE, NamedTextColor.DARK_AQUA, 'α', 5f, true),
    ABILITY_DAMAGE(StatisticType.OFFENSIVE, NamedTextColor.RED, '๑', 5f, percents = true),
    TRUE_DAMAGE(StatisticType.OFFENSIVE, NamedTextColor.WHITE, 'Ϟ'),

    HEALTH(StatisticType.DEFENSIVE, NamedTextColor.RED, '❤', 100f),
    DEFENSE(StatisticType.DEFENSIVE, NamedTextColor.GREEN, '❈', 50f),
    TRUE_DEFENSE(StatisticType.DEFENSIVE, NamedTextColor.WHITE, '❂'),
    SPEED(StatisticType.DEFENSIVE, NamedTextColor.WHITE, '✦', 100f),
    INTELLIGENCE(StatisticType.DEFENSIVE, NamedTextColor.AQUA, '✎', 100f),

    PET_LUCK(StatisticType.DEFENSIVE, NamedTextColor.LIGHT_PURPLE, '♣', 5f),
    MAGIC_FIND(StatisticType.DEFENSIVE, NamedTextColor.AQUA, '✯', 15f),

    DAMAGE_BOOST(StatisticType.OFFENSIVE, NamedTextColor.GOLD, '⚠', 0f, true, true),
    DAMAGE_REDUCTION(StatisticType.DEFENSIVE, NamedTextColor.GOLD, '⓪', 0f, true, true),
    ;

    override fun toString() =
        name.lowercase().split("_")
            .joinToString(separator = " ") { str -> str.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }

    fun formatSimple(num: Float): Component? {
        if (hidden) return null
        return explicitFormatSimple(num)
    }

    fun formatFancy(num: Float): Component? {
        if (hidden || hiddenFancy) return null
        return explicitFormatFancy(num)
    }

    fun explicitFormatSimple(num: Float): Component? {
        val number = type.formatSigned(num) ?: return null
        val comp = comp("<gray>$this: </gray>").append(number)
        return (if (percents) comp.append("%".toComponent().color(type.color)) else comp).noitalic()
    }

    fun explicitFormatFancy(num: Float): Component {
        val comp = comp("$specialChar $this ").color(color).append(type.format(num))
        return (if (percents) comp.append("%".toComponent()) else comp).noitalic()
    }
}
