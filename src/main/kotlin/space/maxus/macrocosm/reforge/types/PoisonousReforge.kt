package space.maxus.macrocosm.reforge.types

import org.bukkit.event.EventHandler
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.reforge.Reforge
import space.maxus.macrocosm.reforge.ReforgeBase
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.stats.stats

object PoisonousReforge : ReforgeBase(
    "Poisonous",
    "Toxicity",
    "Grants you <blue>+10% ${Statistic.CRIT_CHANCE.display}<gray> on hit", ItemType.melee(), stats {
        critDamage = 10f
        damage = 2f
    }) {

    @EventHandler
    fun ability(e: PlayerDealDamageEvent) {
        if(!ensureRequirements(e.player, EquipmentSlot.HAND))
            return
        if (e.crit)
            return
        val stats = e.player.stats()!!
        stats.increase(stats { critChance = 10f })
        val (dmg, crit) = DamageCalculator.calculateStandardDealt(stats.damage, stats)
        e.damage = dmg
        e.crit = crit
    }

    override fun clone(): Reforge {
        return PoisonousReforge
    }
}
