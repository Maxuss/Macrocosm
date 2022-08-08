package space.maxus.macrocosm.ability.types.item

import net.axay.kspigot.event.listen
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.util.containsAny

internal fun Entity.isUndead() = type.name.containsAny("SKELETON", "ZO") || when (type) {
    EntityType.STRAY, EntityType.HUSK -> true
    else -> false
}

object BlackshardAbility : AbilityBase(
    AbilityType.PASSIVE,
    "Call from the Grave",
    "Deal <red>200% ${Statistic.DAMAGE.display}<gray> against <blue>non-undead<gray> mobs.<br>Heal for <red>0.1%<gray> of your maximum ${Statistic.HEALTH.display}<gray> per hit on <blue>undead<gray> mob."
) {
    override fun registerListeners() {
        listen<PlayerDealDamageEvent> { e ->
            if (!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen

            if (e.damaged.isUndead()) {
                e.player.heal(e.player.stats()!!.health * .001f)
            } else {
                e.damage *= 2f
            }
        }
    }
}
