package space.maxus.macrocosm.listeners

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.macrocosm

object PickupListener : Listener {
    @EventHandler
    fun onPickup(e: EntityPickupItemEvent) {
        if (e.entity !is Player)
            return
        e.item.itemStack = e.item.itemStack.macrocosm?.build((e.entity as Player).macrocosm)!!
    }
}
