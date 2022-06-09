package space.maxus.macrocosm.ability.types.item

import net.axay.kspigot.event.listen
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.stats.Statistic

class LifeStealAbility(private val amount: Int): AbilityBase(AbilityType.PASSIVE, "Life Steal", "Heal for <red>$amount ${Statistic.HEALTH.display}<gray> each time you deal damage to enemy.") {
    override fun registerListeners() {
        listen<PlayerDealDamageEvent> { e ->
            if(ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen

            e.player.heal(amount.toFloat(), e.player.stats()!!)
        }
    }
}
