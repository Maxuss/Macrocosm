package space.maxus.macrocosm.listeners

import net.axay.kspigot.extensions.events.interactItem
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import space.maxus.macrocosm.events.PlayerLeftClick
import space.maxus.macrocosm.events.PlayerRightClick
import space.maxus.macrocosm.events.PlayerSneak
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.macrocosm

object AbilityTriggers : Listener {
    @EventHandler
    fun click(e: PlayerInteractEvent) {
        if (e.item == null || e.item?.type == Material.AIR)
            return
        if (e.action.isLeftClick) {
            val event = PlayerLeftClick(e.player.macrocosm!!, e.item!!.macrocosm)
            event.callEvent()
        } else if (e.action.isRightClick) {
            val event = PlayerRightClick(e.player.macrocosm!!, e.item!!.macrocosm)
            event.callEvent()
        }
    }

    @EventHandler
    fun clickEntity(e: PlayerInteractEntityEvent) {
        if (e.interactItem == null || e.interactItem?.type == Material.AIR)
            return
        val event = PlayerRightClick(e.player.macrocosm!!, e.interactItem!!.macrocosm)
        event.callEvent()
    }

    @EventHandler
    fun sneak(e: PlayerToggleSneakEvent) {
        val event = PlayerSneak(e.player.macrocosm!!)
        event.callEvent()
    }
}
