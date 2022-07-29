package space.maxus.macrocosm.ability.types.item

import net.axay.kspigot.event.listen
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.util.metrics.report

object AssassinsDaggerAbility: AbilityBase(AbilityType.PASSIVE, "Assassin's Anticipation", "Deal <red>+100% ${Statistic.DAMAGE.display}<gray> when hitting entities on their backs.") {
    override fun registerListeners() {
        listen<PlayerDealDamageEvent> { e ->
            if(!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen
            val damagedYaw = e.damaged.location.yaw
            val playerYaw = e.player.paper?.location?.yaw ?: report("Player in DealDamageEvent was null!") { return@listen }
            val diffYaw = damagedYaw - playerYaw
            if(diffYaw in -50.0..50.0) {
                e.damage *= 2f
            }
        }
    }
}
