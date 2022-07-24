package space.maxus.macrocosm.ability.types.armor

import net.axay.kspigot.event.listen
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.events.PlayerReceiveDamageEvent
import space.maxus.macrocosm.stats.Statistic

class ZombieHeartHealing(name: String, val amount: Float) : AbilityBase(
    AbilityType.PASSIVE,
    name,
    "Heal for <red>${Formatting.stats(amount.toBigDecimal())} ${Statistic.HEALTH.display}<gray> each time you get hit."
) {
    override fun registerListeners() {
        listen<PlayerReceiveDamageEvent> { e ->
            if (!ensureRequirements(e.player, EquipmentSlot.HEAD))
                return@listen

            e.player.heal(amount)
        }
    }
}
