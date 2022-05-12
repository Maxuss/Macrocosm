package space.maxus.macrocosm.ability.types

import net.axay.kspigot.event.listen
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.entity.raycast
import space.maxus.macrocosm.events.PlayerRightClickEvent

object InstantTransmission : AbilityBase(
    AbilityType.RIGHT_CLICK,
    "Instant Transmission",
    "Instantly teleport <green>5 blocks</green> forward!",
    AbilityCost(50)
) {
    override fun registerListeners() {
        listen<PlayerRightClickEvent> { ctx ->
            if (!ensureRequirements(ctx.player, EquipmentSlot.HAND))
                return@listen
            val player = ctx.player.paper!!
            player.teleport(raycast(player, 5))
        }
    }
}
