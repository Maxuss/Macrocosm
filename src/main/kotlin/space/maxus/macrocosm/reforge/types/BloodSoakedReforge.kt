package space.maxus.macrocosm.reforge.types

import org.bukkit.event.EventHandler
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.reforge.ReforgeBase
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.stats

object BloodSoakedReforge : ReforgeBase(
    "Blood-Soaked",
    "Bloodlust",
    "Heal for <green>0.5%<gray> of your maximum ${Statistic.HEALTH.display}<gray> each hit.",
    ItemType.melee(),
    stats {
        strength = 6f
        ferocity = 5f
    }
) {
    @EventHandler
    fun onHit(e: PlayerDealDamageEvent) {
        if (!ensureRequirements(e.player, EquipmentSlot.HAND))
            return

        e.player.heal(25f)
    }
}
