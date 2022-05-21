package space.maxus.macrocosm.listeners

import net.axay.kspigot.extensions.geometry.blockLoc
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent
import space.maxus.macrocosm.players.macrocosm

object MovementListener : Listener {
    @EventHandler
    fun onMove(e: PlayerMoveEvent) {
        if (e.from.blockLoc == e.to.blockLoc)
            return
        val mc = e.player.macrocosm ?: return

        val pet = mc.activePet ?: return
        pet.teleport(mc)
    }
}
