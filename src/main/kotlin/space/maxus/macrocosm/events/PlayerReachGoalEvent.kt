package space.maxus.macrocosm.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import space.maxus.macrocosm.players.MacrocosmPlayer

class PlayerReachGoalEvent(
    val player: MacrocosmPlayer,
    val goal: String,
) : Event(true) {
    companion object {
        private val HANDLERS = HandlerList()

        @JvmStatic
        fun getHandlerList(): HandlerList {
            return HANDLERS
        }
    }

    override fun getHandlers() = HANDLERS
}
