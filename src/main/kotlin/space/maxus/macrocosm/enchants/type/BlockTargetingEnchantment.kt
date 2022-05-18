package space.maxus.macrocosm.enchants.type

import net.axay.kspigot.extensions.bukkit.toLegacyString
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.chat.isBlankOrEmpty
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.enchants.EnchantmentBase
import space.maxus.macrocosm.events.BlockDropItemsEvent
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.loot.Drop
import space.maxus.macrocosm.loot.LootPool
import space.maxus.macrocosm.loot.MacrocosmDrop
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.text.comp
import kotlin.math.roundToInt
import kotlin.random.Random

class BlockTargetingEnchantment(
    name: String,
    targetItem: ItemType,
    private val blockName: String,
    private val stat: Statistic,
    private val base: Int,
    vararg vaBlockTypes: Material
) : EnchantmentBase(name, "", 1..5, listOf(ItemType.GAUNTLET, targetItem)) {
    private val blockTypes = vaBlockTypes.toList()
    override fun description(level: Int): List<Component> {
        val str = "Grants <gold>+${level * base} ${stat.display}<gray> when ${
            stat.name.split("_").first().lowercase()
        } $blockName."
        val reduced = str.reduceToList(30).map { comp("<gray>$it").noitalic() }.toMutableList()
        reduced.removeIf { it.toLegacyString().isBlankOrEmpty() }
        return reduced
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onDropItems(e: BlockDropItemsEvent) {
        if (!blockTypes.contains(e.block.type))
            return
        val (ok, lvl) = ensureRequirements(e.player, EquipmentSlot.HAND)
        if (!ok)
            return
        val add =
            ((lvl * base) / 100f).roundToInt() + (if (Random.nextFloat() < (((lvl * base) % 100) / 100f)) 1 else 0)
        val pool = mutableListOf<Drop>()
        for (drop in e.pool.drops) {
            val first = drop.amount.first + add
            val last = drop.amount.last + add
            pool.add(MacrocosmDrop(drop.item, drop.rarity, drop.chance, first..last))
        }
        e.pool = LootPool.of(*pool.toTypedArray())
    }
}
