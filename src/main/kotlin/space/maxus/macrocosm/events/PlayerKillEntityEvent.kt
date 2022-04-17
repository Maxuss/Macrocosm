package space.maxus.macrocosm.events

import org.bukkit.entity.LivingEntity
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import space.maxus.macrocosm.players.MacrocosmPlayer

class PlayerKillEntityEvent(
    val player: MacrocosmPlayer,
    val killed: LivingEntity
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
