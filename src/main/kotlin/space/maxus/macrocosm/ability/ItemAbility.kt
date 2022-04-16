package space.maxus.macrocosm.ability

import net.axay.kspigot.extensions.bukkit.toComponent
import net.kyori.adventure.text.Component
import org.bukkit.event.Listener
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.events.PlayerLeftClickEvent
import space.maxus.macrocosm.events.PlayerRightClickEvent
import space.maxus.macrocosm.events.PlayerSneakEvent
import space.maxus.macrocosm.text.comp

@Suppress("unused")
interface ItemAbility : Listener {
    val name: String
    val description: String
    val type: AbilityType

    fun rightClick(ctx: PlayerRightClickEvent) {

    }

    fun leftClick(ctx: PlayerLeftClickEvent) {

    }

    fun sneak(ctx: PlayerSneakEvent) {

    }

    fun buildLore(lore: MutableList<Component>) {
        lore.add(type.format(name))
        for (desc in description.reduceToList()) {
            lore.add(comp("<gray>$desc</gray>"))
        }
        lore.add("".toComponent())
    }
}
