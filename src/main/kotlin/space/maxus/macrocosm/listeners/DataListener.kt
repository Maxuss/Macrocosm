package space.maxus.macrocosm.listeners

import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.bukkit.toComponent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.players.macrocosm

object DataListener {
    fun joinLeave() {
        listen<PlayerJoinEvent> { e ->
            if (Macrocosm.playersLazy.contains(e.player.uniqueId)) {
                val player = MacrocosmPlayer.loadOrInit(e.player.uniqueId)
                Macrocosm.loadedPlayers[e.player.uniqueId] = player
            } else {
                val player = MacrocosmPlayer(e.player.uniqueId)
                Macrocosm.playersLazy.add(e.player.uniqueId)
                Macrocosm.loadedPlayers[e.player.uniqueId] = player
                player.store()
            }
            val displayName = e.player.macrocosm?.rank?.playerName(e.player.name) ?: e.player.displayName()
            e.player.playerListName(e.player.name.toComponent().color(e.player.macrocosm?.rank?.color))
            e.player.displayName(displayName)
        }

        listen<PlayerQuitEvent> { e ->
            val id = e.player.uniqueId
            val player = Macrocosm.loadedPlayers.remove(id)
            player?.store()
            player?.activePet?.despawn(player)
            player?.summons?.forEach { summon ->
                e.player.world.getEntity(summon)?.remove()
            }
        }
    }
}
