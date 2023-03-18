package space.maxus.macrocosm.listeners

import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.text.text

/**
 * Listener for formatting player's rank and messages
 */
object ChatHandler : Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    fun formatRank(e: AsyncChatEvent) {
        e.isCancelled = true
        val formatted = e.player.macrocosm?.rank?.format(e.player.name, e.originalMessage().str())
            ?: text("Something went very wrong")
        Bukkit.broadcast(formatted)
    }
}
