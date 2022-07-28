package space.maxus.macrocosm.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import space.maxus.macrocosm.entity.MacrocosmEntity
import space.maxus.macrocosm.stats.Statistics

class EntityCalculateStatsEvent(
    val self: MacrocosmEntity,
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
