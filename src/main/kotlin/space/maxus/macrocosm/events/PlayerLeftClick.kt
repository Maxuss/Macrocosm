package space.maxus.macrocosm.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.players.MacrocosmPlayer

data class PlayerLeftClick(val player: MacrocosmPlayer, val item: MacrocosmItem?): Event() {
    companion object {
        internal val handlers = HandlerList()
    }

    override fun getHandlers() = Companion.handlers
}
