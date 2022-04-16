package space.maxus.macrocosm.ability

import net.axay.kspigot.extensions.bukkit.toComponent
import net.kyori.adventure.text.Component
import org.bukkit.event.Listener
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.text.comp

@Suppress("unused")
interface ItemAbility : Listener {
    val name: String
    val description: String
    val type: AbilityType
    val cost: AbilityCost?

    fun buildLore(lore: MutableList<Component>) {
        lore.add(type.format(name).noitalic())
        for (desc in description.reduceToList()) {
            lore.add(comp("<gray>$desc</gray>").noitalic())
        }
        cost?.buildLore(lore)
        lore.add("".toComponent())
    }
}
