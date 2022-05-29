package space.maxus.macrocosm.enchants.type

import org.bukkit.event.EventHandler
import space.maxus.macrocosm.enchants.UltimateEnchantment
import space.maxus.macrocosm.events.ItemCalculateStatsEvent
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.stats.Statistic

object OneForAllEnchantment : UltimateEnchantment(
    "One For All",
    "Removes <yellow>all<gray> enchantments, but boosts ${Statistic.DAMAGE.display}<gray> of this item by <red>500%<gray>.",
    1..1,
    ItemType.melee(),
    conflicts = listOf("ALL")
) {
    @EventHandler
    fun onItemStatCalculate(e: ItemCalculateStatsEvent) {
        val (ok, _) = ensureRequirements(e.item)
        if(!ok)
            return
        e.stats.damage *= 5f
    }
}
