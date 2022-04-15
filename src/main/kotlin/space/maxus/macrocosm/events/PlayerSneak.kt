package space.maxus.macrocosm.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import space.maxus.macrocosm.players.MacrocosmPlayer

data class PlayerSneak(val player: MacrocosmPlayer): Event() {
    companion object {
        internal val handlers = HandlerList()
    }

    override fun getHandlers() = Companion.handlers
}
