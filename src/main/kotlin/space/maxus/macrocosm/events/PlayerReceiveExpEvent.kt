package space.maxus.macrocosm.events

import org.bukkit.event.Cancellable
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.skills.SkillType

class PlayerReceiveExpEvent(
    val player: MacrocosmPlayer,
    var type: SkillType,
    var amount: Float
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
