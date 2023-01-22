package space.maxus.macrocosm.enchants

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.events.EnchantCalculateStatsEvent
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import java.math.BigDecimal

/**
 * A base for enchantment. It is recommended to inherit
 * this class instead of implementing raw [Enchantment] interface
 */
abstract class EnchantmentBase(
    override val name: String,
    /**
     * Description for this enchantment
     */
    private val description: String,
    override val levels: IntRange,
    override val applicable: List<ItemType>,
    /**
     * Base statistics this enchantment modifies, zero by default
     */
    private val baseStats: Statistics = Statistics.zero(),
    /**
     * Base special statistics this enchantment modifies, zero by default
     */
    private val baseSpecials: SpecialStatistics = SpecialStatistics(),
    /**
     * Stat modifiers per each level
     */
    private val multiplier: Float = 1f,
    /**
     * Enchantment IDs that this enchantment conflicts with
     */
    conflicts: List<String> = listOf()
) : Enchantment {
    override val conflicts: List<Identifier> = conflicts.map { Identifier.macro(it.lowercase()) }

    /**
     * Ensures that the player has enchantments in any of the provided [slots]
     *
     * @return A pair of (whether the player has the enchantment, the level of enchantment)
     */
    protected fun ensureRequirements(player: MacrocosmPlayer, vararg slots: EquipmentSlot): Pair<Boolean, Int> {
        val filtered = slots.map {
            val item = player.paper!!.inventory.getItem(it)
            if (item.type == Material.AIR)
                return@map Pair(false, -1)
            val enchants = item.macrocosm?.enchantments
            if (enchants?.keys?.contains(Registry.ENCHANT.byValue(this)) != true)
                return@map Pair(false, -1)
            Pair(true, enchants[Registry.ENCHANT.byValue(this)!!]!!)
        }.filter { (success, _) -> success }
        return filtered.maxByOrNull { (_, lvl) -> lvl } ?: filtered.firstOrNull() ?: Pair(false, -1)
    }

    /**
     * Ensures that the player has enchantments in any of the provided [slots] but also stacks all the
     * levels.
     *
     * @return A pair of (whether the player has the enchantment, the combined level of enchantment)
     */
    protected fun ensureRequirementsStacking(player: MacrocosmPlayer, vararg slots: EquipmentSlot): Pair<Boolean, Int> {
        val filtered = slots.map {
            val item = player.paper!!.inventory.getItem(it)
            if (item.type == Material.AIR)
                return@map Pair(false, -1)
            val enchants = item.macrocosm?.enchantments
            if (enchants?.contains(Registry.ENCHANT.byValue(this)) != true)
                return@map Pair(false, -1)
            Pair(true, enchants[Registry.ENCHANT.byValue(this)]!!)
        }.filter { (success, _) -> success }
        if (filtered.isEmpty())
            return Pair(false, -1)
        return Pair(true, filtered.sumOf { (_, lvl) -> lvl })
    }

    /**
     * Checks whether the provided [item] has the enchantment
     *
     * @return A pair of (whether the item has the enchantment, the level of enchantment)
     */
    protected fun ensureRequirements(item: MacrocosmItem): Pair<Boolean, Int> {
        val enchants = item.enchantments
        if (!enchants.contains(Registry.ENCHANT.byValue(this)))
            return Pair(false, -1)
        return Pair(true, enchants[Registry.ENCHANT.byValue(this)]!!)
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
        val regulatedDescription = "\\[[\\d.]+]".toRegex().replace(description) {
            Formatting.stats(
                BigDecimal.valueOf(
                    java.lang.Double.parseDouble(
                        it.value.removeSurrounding(
                            "[",
                            "]"
                        )
                    ) * level * multiplier
                )
            )
        }
        val reduced =
            regulatedDescription.reduceToList(25).map { mm.deserialize("<gray>$it").noitalic() }.toMutableList()
        reduced.removeIf {
            ChatColor.stripColor(LegacyComponentSerializer.legacySection().serialize(it))!!.isBlank()
        }
        return reduced
    }
}
