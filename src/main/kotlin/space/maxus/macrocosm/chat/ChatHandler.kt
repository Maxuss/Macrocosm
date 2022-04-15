package space.maxus.macrocosm.chat

import io.papermc.paper.event.player.AsyncChatEvent
import net.axay.kspigot.extensions.bukkit.toLegacyString
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.text.comp

object ChatHandler : Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    fun formatRank(e: AsyncChatEvent) {
        e.renderer { source, _, message, _ ->
            source.macrocosm?.rank?.format(source.name, message.toLegacyString()) ?: message
        }
    }

    @EventHandler
    fun formatEz(e: AsyncChatEvent) {
        val legacy = e.message().toLegacyString()
        if (!legacy.contains("ez"))
            return
        e.message(comp("Nice play!"))
    }
}
