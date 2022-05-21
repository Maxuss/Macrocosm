package space.maxus.macrocosm.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.stats.Statistics

class ItemCalculateStatsEvent(
    val item: MacrocosmItem,
    var stats: Statistics,
) : Event() {
    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }

    }

    override fun getHandlers() = HANDLERS
}
