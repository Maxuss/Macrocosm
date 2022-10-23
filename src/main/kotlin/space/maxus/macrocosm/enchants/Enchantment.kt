package space.maxus.macrocosm.enchants

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.event.Listener
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.text

internal fun colorFromLevel(lvl: Int, max: Int): TextColor {
    return if (lvl == max)
        NamedTextColor.RED
    else if (lvl >= max - 1)
        NamedTextColor.GOLD
    else
        NamedTextColor.BLUE
}

/**
 * An interface that defines an enchantment
 */
interface Enchantment : Listener {
    /**
     * Display name of the enchantment
     */
    val name: String

    /**
     * Possible levels that this enchantment may be applicable to
     */
    val levels: IntRange

    /**
     * Items this enchantment can be applicable to
     */
    val applicable: List<ItemType>

    /**
     * IDs of enchantments this enchantment conflicts with
     */
    val conflicts: List<Identifier>

    /**
     * Provides description for this enchantment
     */
    fun description(level: Int): List<Component>

    /**
     * Stats this enchantment modifies
     */
    fun stats(level: Int, player: MacrocosmPlayer? = null): Statistics = Statistics.zero()

    /**
     * Special stats this enchantment modifies
     */
    fun special(level: Int): SpecialStatistics = SpecialStatistics()

    /**
     * Adds a fancy display of this enchantment to [lore]
     *
     * Example:
     * ```
     * Enchantment VI
     * Does some stuff
     * And more stuff
     * ```
     */
    fun displayFancy(lore: MutableList<Component>, level: Int) {
        lore.add(displaySimple(level))
        lore.addAll(description(level))
    }

    /**
     * Construts a simple display of this enchantment
     *
     * Example:
     *
     * `Enchantment VI`
     */
    fun displaySimple(level: Int): Component {
        val mm = MiniMessage.miniMessage()
        val color = colorFromLevel(level, levels.last)
        val name = name.split(" ").joinToString(" ") { mm.serialize(text(it).color(color)) }
        return text("$name ").append(text(roman(level)).color(color)).noitalic()
    }
}
