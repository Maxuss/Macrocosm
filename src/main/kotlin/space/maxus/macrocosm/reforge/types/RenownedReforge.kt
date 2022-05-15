package space.maxus.macrocosm.reforge.types

import org.bukkit.event.EventHandler
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.reforge.ReforgeBase
import space.maxus.macrocosm.stats.stats

object RenownedReforge: ReforgeBase(
    "Renowned",
    "Stat Boost",
    "Boosts <blue>all<gray> your stats by <yellow>3%<gray>.",
    ItemType.armor(),
    stats {
        intelligence = 10f
        strength = 5f
        critChance = 3f
        critDamage = 5f
        defense = 10f
        health = 15f
        magicFind = 1f
    }
) {
    @EventHandler
    fun onStatCalculate(e: PlayerCalculateStatsEvent) {
        if(!ensureRequirements(e.player, EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.CHEST, EquipmentSlot.FEET))
            return
        e.stats.multiply(1.03f)
    }

    override fun clone(): Reforge {
        return RenownedReforge
    }
}
