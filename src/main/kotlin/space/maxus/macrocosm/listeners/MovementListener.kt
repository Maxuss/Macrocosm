package space.maxus.macrocosm.listeners

import net.axay.kspigot.extensions.geometry.blockLoc
import net.axay.kspigot.runnables.task
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerMoveEvent
import space.maxus.macrocosm.players.macrocosm

object MovementListener : Listener {
    @EventHandler
    fun onMove(e: PlayerMoveEvent) {
        if (e.from.blockLoc == e.to.blockLoc)
            return
        val mc = e.player.macrocosm ?: return

        val pet = mc.activePet ?: return
        pet.floatingPaused
        pet.teleport(mc)
        task(delay = 10L) {
            pet.floatingPaused = false
        }
    }

    @EventHandler
    fun onDimensionChange(e: PlayerChangedWorldEvent) {
        val pet = e.player.macrocosm!!.activePet
        pet?.respawn(e.player.macrocosm!!)
    }
}
