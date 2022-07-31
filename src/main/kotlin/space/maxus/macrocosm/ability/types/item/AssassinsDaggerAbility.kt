package space.maxus.macrocosm.ability.types.item

import net.axay.kspigot.event.listen
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.util.game.Fmt
import space.maxus.macrocosm.util.metrics.report
import space.maxus.macrocosm.util.superCritMod

object AssassinsDaggerAbility: AbilityBase(AbilityType.PASSIVE, "Assassin's Anticipation", "Attack on the enemies' backs to deal<br>${Fmt.SUPER_CRIT} damage!") {
    override fun registerListeners() {
        listen<PlayerDealDamageEvent> { e ->
            if(!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen
            val damagedYaw = e.damaged.location.yaw
            val playerYaw = e.player.paper?.location?.yaw ?: report("Player in DealDamageEvent was null!") { return@listen }
            val diffYaw = damagedYaw - playerYaw
            if(diffYaw in -50.0..50.0) {
                e.damage *= superCritMod(e.player)
                e.isSuperCrit = true
            }
        }
    }
}
