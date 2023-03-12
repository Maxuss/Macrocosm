package space.maxus.macrocosm.listeners

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import space.maxus.macrocosm.players.macrocosm

object InventoryListeners : Listener {
    @EventHandler
    fun onCloseAll(e: InventoryCloseEvent) {
        val mc = (e.player as Player).macrocosm ?: return
        if(mc.openUi != null) {
            mc.openUi!!.close()
            mc.openUi = null
        }
    }
}
