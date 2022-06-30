package space.maxus.macrocosm.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import space.maxus.macrocosm.util.game.Calendar

data class SeasonChangeEvent(val new: Calendar.Season, val old: Calendar.Season) : Event() {
    companion object {
        internal val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }

    override fun getHandlers() = HANDLERS
}
