package space.maxus.macrocosm.enchants

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.chat.isBlankOrEmpty
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.events.EnchantCalculateStatsEvent
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import java.math.BigDecimal

abstract class EnchantmentBase(
    override val name: String,
    private val description: String,
    override val levels: IntRange,
    override val applicable: List<ItemType>,
    private val baseStats: Statistics = Statistics.zero(),
    private val baseSpecials: SpecialStatistics = SpecialStatistics(),
    private val multiplier: Float = 1f,
    conflicts: List<String> = listOf()
) : Enchantment {
    override val conflicts: List<Identifier> = conflicts.map { Identifier.macro(it.lowercase()) }
    protected fun ensureRequirements(player: MacrocosmPlayer, vararg slots: EquipmentSlot): Pair<Boolean, Int> {
        val filtered = slots.map {
            val item = player.paper!!.inventory.getItem(it)
            if (item.type == Material.AIR)
                return@map Pair(false, -1)
            val enchants = item.macrocosm?.enchantments
            if (enchants?.contains(this) != true)
                return@map Pair(false, -1)
            Pair(true, enchants[this]!!)
        }.filter { (success, _) -> success }
        return filtered.maxByOrNull { (_, lvl) -> lvl } ?: filtered.firstOrNull() ?: Pair(false, -1)
    }

    protected fun ensureRequirementsStacking(player: MacrocosmPlayer, vararg slots: EquipmentSlot): Pair<Boolean, Int> {
        val filtered = slots.map {
            val item = player.paper!!.inventory.getItem(it)
            if (item.type == Material.AIR)
                return@map Pair(false, -1)
            val enchants = item.macrocosm?.enchantments
            if (enchants?.contains(this) != true)
                return@map Pair(false, -1)
            Pair(true, enchants[this]!!)
        }.filter { (success, _) -> success }
        if (filtered.isEmpty())
            return Pair(false, -1)
        return Pair(true, filtered.sumOf { (_, lvl) -> lvl })
    }

    protected fun ensureRequirements(item: MacrocosmItem): Pair<Boolean, Int> {
        val enchants = item.enchantments
        if (!enchants.contains(this))
            return Pair(false, -1)
        return Pair(true, enchants[this]!!)
    }


    override fun stats(level: Int, player: MacrocosmPlayer?): Statistics {
        val clone = baseStats.clone()
        clone.multiply(multiplier * level)
        val event = EnchantCalculateStatsEvent(player, this, clone)
        event.callEvent()
        return event.stats
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
        val regulatedDescription = "\\[\\d.]+".toRegex().replace(description) {
            Formatting.stats(BigDecimal.valueOf(java.lang.Double.parseDouble(it.value.removeSurrounding("[", "]")) * level * multiplier))
        }
        val reduced = regulatedDescription.reduceToList(25).map { mm.deserialize("<gray>$it").noitalic() }.toMutableList()
        reduced.removeIf {
            ChatColor.stripColor(LegacyComponentSerializer.legacySection().serialize(it))!!.isBlankOrEmpty()
        }
        return reduced
    }
}
