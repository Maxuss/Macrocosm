package space.maxus.macrocosm.enchants

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.chat.isBlankOrEmpty
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.comp

abstract class EnchantmentBase(
    override val name: String,
    private val description: String,
    override val levels: IntRange,
    override val applicable: List<ItemType>,
    private val baseStats: Statistics = Statistics.zero(),
    private val baseSpecials: SpecialStatistics = SpecialStatistics(),
    private val multiplier: Float = 1f,
    override val conflicts: List<String> = listOf()
) : Enchantment {
    protected fun ensureRequirements(player: MacrocosmPlayer, slot: EquipmentSlot): Pair<Boolean, Int> {
        val item = player.paper!!.inventory.getItem(slot)
        if(item.type == Material.AIR)
            return Pair(false, -1)
        val enchants = item.macrocosm?.enchantments
        if(enchants?.contains(this) != true)
            return Pair(false, -1)
        return Pair(true, enchants[this]!!)
    }

    override fun stats(level: Int): Statistics {
        val clone = baseStats.clone()
        clone.multiply(multiplier * level)
        return clone
    }

    override fun special(level: Int): SpecialStatistics {
        val clone = baseSpecials.clone()
        clone.multiply(multiplier * level)
        return clone
    }

    override fun description(level: Int): List<Component> {
        val base = baseStats.clone()
        val special = baseSpecials.clone()
        base.multiply(multiplier * level)
        special.multiply(multiplier * level)
        val mm = MiniMessage.miniMessage()
        val basePlaceholders = base.iter().map { (k, v) ->
            Placeholder.parsed(k.name.lowercase(), mm.serialize(
                if(!k.hidden) { k.type.formatSigned(v) ?: comp("0").color(k.type.color) } else { k.type.formatSigned(v * 100, true) ?: comp("0").color(k.type.color) }))
        }.toMutableList()
        val specPlaceholders = special.map().map { (k, v) ->
            Placeholder.unparsed(k.name.lowercase(), Formatting.stats(v.toBigDecimal(), false))
        }
        basePlaceholders.addAll(specPlaceholders)
        val arr = basePlaceholders.toTypedArray()
        val reduced = description.reduceToList(25).map { mm.deserialize("<gray>$it", *arr).noitalic() }.toMutableList()
        reduced.removeIf { ChatColor.stripColor(LegacyComponentSerializer.legacySection().serialize(it))!!.isBlankOrEmpty() }
        return reduced
    }
}
