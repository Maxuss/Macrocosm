package space.maxus.macrocosm.listeners

import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.bukkit.toComponent
import net.axay.kspigot.runnables.async
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.db.Database
import space.maxus.macrocosm.exceptions.DatabaseException
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.players.macrocosm

object DataListener {
    fun joinLeave() {
        listen<PlayerJoinEvent> { e ->
            val player = if (Macrocosm.playersLazy.contains(e.player.uniqueId)) {
                val player = MacrocosmPlayer.readPlayer(e.player.uniqueId)
                    ?: throw DatabaseException("Expected to find player with UUID ${e.player.uniqueId}!")
                Macrocosm.onlinePlayers[e.player.uniqueId] = player
                player
            } else {
                val player = MacrocosmPlayer(e.player.uniqueId)
                Macrocosm.playersLazy.add(e.player.uniqueId)
                Macrocosm.onlinePlayers[e.player.uniqueId] = player
                async {
                    player.storeSelf(Database.statement)
                }
                player
            }
            val displayName = e.player.macrocosm?.rank?.playerName(e.player.name) ?: e.player.displayName()
            e.player.playerListName(e.player.name.toComponent().color(e.player.macrocosm?.rank?.color))
            e.player.displayName(displayName)
            val active = player.activePet
            active?.prototype?.spawn(player, active.hashKey)
        }

        listen<PlayerQuitEvent> { e ->
            val id = e.player.uniqueId
            val player = Macrocosm.onlinePlayers.remove(id)
            player?.activePet?.despawn(player, false)
            player?.storeSelf(Database.statement)
        }
    }
}
