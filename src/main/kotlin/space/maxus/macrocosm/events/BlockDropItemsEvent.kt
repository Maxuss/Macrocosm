package space.maxus.macrocosm.events

import org.bukkit.block.Block
import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import space.maxus.macrocosm.entity.loot.LootPool
import space.maxus.macrocosm.players.MacrocosmPlayer

class BlockDropItemsEvent(
    val player: MacrocosmPlayer,
    val block: Block,
    var pool: LootPool
) : Event(), Cancellable {
    private var eventCancelled = false

    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }

    }

    override fun getHandlers() = HANDLERS
    override fun isCancelled() = eventCancelled

    override fun setCancelled(cancel: Boolean) {
        eventCancelled = cancel
    }
}
