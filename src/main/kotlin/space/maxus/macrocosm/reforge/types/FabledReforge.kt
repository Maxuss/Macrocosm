package space.maxus.macrocosm.reforge.types

import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.reforge.ReforgeBase
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.stats
import kotlin.random.Random

object FabledReforge: ReforgeBase(
    "Fabled",
    "Cold-Blooded",
    "Grants a <blue>20%<gray> chance to deal <red>150% ${Statistic.DAMAGE.display}<gray> on critical hit",
    ItemType.melee(),
    baseStats = stats {
        strength = 8f
        critDamage = 6f
        critChance = 3f
    }
) {
    @EventHandler(priority = EventPriority.LOWEST)
    fun onDealDamage(e: PlayerDealDamageEvent) {
        if(!ensureRequirements(e.player, EquipmentSlot.HAND))
            return
        if(!e.crit)
            return
        if(Random.nextFloat() > .2f)
            return

        e.damage *= 1.5f
    }

    override fun clone(): Reforge {
        return FabledReforge
    }
}
