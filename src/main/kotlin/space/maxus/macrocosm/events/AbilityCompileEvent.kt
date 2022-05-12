package space.maxus.macrocosm.events

import net.kyori.adventure.text.Component
import org.bukkit.event.Event
import org.bukkit.event.HandlerList
import space.maxus.macrocosm.ability.ItemAbility
import space.maxus.macrocosm.item.MacrocosmItem

class AbilityCompileEvent(
    val item: MacrocosmItem,
    val ability: ItemAbility,
    var lore: MutableList<Component>
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
