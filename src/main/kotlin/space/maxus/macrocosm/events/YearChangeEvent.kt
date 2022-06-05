package space.maxus.macrocosm.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

data class YearChangeEvent(val new: Int, val previous: Int) : Event() {
    companion object {
        internal val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }

    override fun getHandlers() = HANDLERS
}
