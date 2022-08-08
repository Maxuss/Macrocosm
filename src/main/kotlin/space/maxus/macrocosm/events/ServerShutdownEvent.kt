package space.maxus.macrocosm.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class ServerShutdownEvent : Event() {
    companion object {
        internal val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }

    override fun getHandlers() = HANDLERS
}
