package space.maxus.macrocosm.enchants

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.event.Listener
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.comp

internal fun colorFromLevel(lvl: Int, max: Int): TextColor {
    return if (lvl == max)
        NamedTextColor.RED
    else if (lvl >= max - 1)
        NamedTextColor.GOLD
    else
        NamedTextColor.BLUE
}

interface Enchantment : Listener {
    val name: String
    val levels: IntRange
    val applicable: List<ItemType>
    val conflicts: List<String>

    fun description(level: Int): List<Component>

    fun stats(level: Int): Statistics = Statistics.zero()
    fun special(level: Int): SpecialStatistics = SpecialStatistics()

    fun displayFancy(lore: MutableList<Component>, level: Int) {
        lore.add(displaySimple(level))
        lore.addAll(description(level))
    }

    fun displaySimple(level: Int): Component {
        val mm = MiniMessage.miniMessage()
        val color = colorFromLevel(level, levels.last)
        val name = name.split(" ").joinToString(" ") { mm.serialize(comp(it).color(color)) }
        return comp("$name ").append(comp(roman(level)).color(color)).noitalic()
    }
}
