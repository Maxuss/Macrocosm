package space.maxus.macrocosm.ability

import net.axay.kspigot.extensions.bukkit.toComponent
import net.kyori.adventure.text.Component
import org.bukkit.event.Listener
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.events.PlayerLeftClick
import space.maxus.macrocosm.events.PlayerRightClick
import space.maxus.macrocosm.events.PlayerSneak
import space.maxus.macrocosm.item.AbilityType
import space.maxus.macrocosm.text.comp

@Suppress("unused")
interface ItemAbility : Listener {
    val name: String
    val description: String
    val type: AbilityType

    fun rightClick(ctx: PlayerRightClick) {

    }

    fun leftClick(ctx: PlayerLeftClick) {

    }

    fun sneak(ctx: PlayerSneak) {

    }

    fun buildLore(): List<Component> {
        val lore = mutableListOf<Component>()
        lore.add(type.format(name))
        for (desc in description.reduceToList()) {
            lore.add(comp("<gray>$desc</gray>"))
        }
        lore.add("".toComponent())
        return lore
    }
}
