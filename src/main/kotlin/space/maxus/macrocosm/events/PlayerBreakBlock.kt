package space.maxus.macrocosm.events

import org.bukkit.block.Block
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import space.maxus.macrocosm.players.MacrocosmPlayer

class PlayerBreakBlock(
    val player: MacrocosmPlayer,
    val block: Block
) : Event(), Cancellable {
    private var eventCancelled: Boolean = false
    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }

    override fun getHandlers() = HANDLERS
    override fun isCancelled(): Boolean {
        return eventCancelled
    }

    override fun setCancelled(cancel: Boolean) {
        eventCancelled = cancel
    }
}
