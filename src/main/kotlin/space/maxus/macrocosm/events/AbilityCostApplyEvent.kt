package space.maxus.macrocosm.events

import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import space.maxus.macrocosm.players.MacrocosmPlayer

class AbilityCostApplyEvent(
    val player: MacrocosmPlayer,
    var mana: Number,
    var health: Int,
    var cooldown: Float,
    var summonDifficulty: Int
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
