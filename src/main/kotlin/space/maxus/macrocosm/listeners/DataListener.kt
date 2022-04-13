package space.maxus.macrocosm.listeners

import net.axay.kspigot.event.listen
import net.axay.kspigot.runnables.async
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.db.Database
import space.maxus.macrocosm.exceptions.DatabaseException
import space.maxus.macrocosm.players.MacrocosmPlayer

object DataListener {
    fun joinLeave() {
        listen<PlayerJoinEvent> { e ->
            if(Macrocosm.playersLazy.contains(e.player.uniqueId)) {
                val player = MacrocosmPlayer.readPlayer(e.player.uniqueId) ?: throw DatabaseException("Expected to find player with UUID ${e.player.uniqueId}!")
                Macrocosm.onlinePlayers[e.player.uniqueId] = player
            } else {
                val player = MacrocosmPlayer(e.player.uniqueId)
                Macrocosm.playersLazy.add(e.player.uniqueId)
                Macrocosm.onlinePlayers[e.player.uniqueId] = player
                async {
                    player.storeSelf(Database.statement)
                }
            }
        }

        listen<PlayerQuitEvent> { e ->
            val id = e.player.uniqueId
            Macrocosm.onlinePlayers.remove(id)?.storeSelf(Database.statement)
        }
    }
}
