package space.maxus.macrocosm.reforge.types

import net.axay.kspigot.runnables.taskRunLater
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.events.EntityActivateFerocityEvent
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.reforge.ReforgeBase
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.stats

object RelentlessReforge: ReforgeBase(
    "Relentless",
    "<red>No Mercy</red>",
    "Whenever you activate ${Statistic.FEROCITY.display}<gray>, gain <red>+20 ${Statistic.STRENGTH.display}<gray> for <green>2s<gray>.",
    ItemType.melee(),
    stats {
        ferocity = 4f
        strength = 4f
        critChance = 3f
        critDamage = 2f
        attackSpeed = 3f
    }
) {
    @EventHandler
    fun onFerocity(e: EntityActivateFerocityEvent) {
        val damager = e.damager
        if(damager !is Player || damager.macrocosm == null)
            return
        val mc = damager.macrocosm!!
        if(!ensureRequirements(mc, EquipmentSlot.HAND))
            return
        mc.tempStats.strength += 20
        taskRunLater(2 * 20L) {
            mc.tempStats.strength -= 20
        }
    }

    override fun clone(): Reforge {
        return RelentlessReforge
    }
}
