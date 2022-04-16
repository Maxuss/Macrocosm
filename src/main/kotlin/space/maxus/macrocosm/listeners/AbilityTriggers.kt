package space.maxus.macrocosm.listeners

import net.axay.kspigot.extensions.events.interactItem
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import space.maxus.macrocosm.events.PlayerLeftClickEvent
import space.maxus.macrocosm.events.PlayerRightClickEvent
import space.maxus.macrocosm.events.PlayerSneakEvent
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.macrocosm

object AbilityTriggers : Listener {
    @EventHandler
    fun click(e: PlayerInteractEvent) {
        if (e.item == null || e.item?.type == Material.AIR)
            return
        if (e.action.isLeftClick) {
            val event = PlayerLeftClickEvent(e.player.macrocosm!!, e.item!!.macrocosm)
            event.callEvent()
        } else if (e.action.isRightClick) {
            val event = PlayerRightClickEvent(e.player.macrocosm!!, e.item!!.macrocosm)
            event.callEvent()
        }
    }

    @EventHandler
    fun clickEntity(e: PlayerInteractEntityEvent) {
        if (e.interactItem == null || e.interactItem?.type == Material.AIR)
            return
        val event = PlayerRightClickEvent(e.player.macrocosm!!, e.interactItem!!.macrocosm)
        event.callEvent()
    }

    @EventHandler
    fun sneak(e: PlayerToggleSneakEvent) {
        val event = PlayerSneakEvent(e.player.macrocosm!!)
        event.callEvent()
    }
}
