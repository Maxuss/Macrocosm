package space.maxus.macrocosm.listeners

import io.papermc.paper.event.player.AsyncChatEvent
import net.axay.kspigot.extensions.bukkit.toLegacyString
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import space.maxus.macrocosm.players.macrocosm

/**
 * Listener for formatting player's rank and messages
 */
object ChatHandler : Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    fun formatRank(e: AsyncChatEvent) {
        e.renderer { source, _, message, _ ->
            source.macrocosm?.rank?.format(source.name, message.toLegacyString()) ?: message
        }
    }
}
