package space.maxus.macrocosm.listeners

import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChangedWorldEvent
import space.maxus.macrocosm.players.macrocosm

object MovementListener : Listener {
    @EventHandler
    fun onDimensionChange(e: PlayerChangedWorldEvent) {
        val pet = e.player.macrocosm!!.activePet
        pet?.respawn(e.player.macrocosm!!)
    }
}
