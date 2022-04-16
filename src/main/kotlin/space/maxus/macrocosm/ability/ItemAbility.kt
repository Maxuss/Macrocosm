package space.maxus.macrocosm.ability

import net.axay.kspigot.extensions.bukkit.toComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.ChatColor
import org.bukkit.event.Listener
import space.maxus.macrocosm.chat.isBlankOrEmpty
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
        val tmp = mutableListOf<Component>()
        tmp.add(type.format(name).noitalic())
        for (desc in description.reduceToList()) {
            tmp.add(comp("<gray>$desc</gray>").noitalic())
        }
        tmp.removeIf { ChatColor.stripColor(LegacyComponentSerializer.legacySection().serialize(it))!!.isBlankOrEmpty() }
        lore.addAll(tmp)
        cost?.buildLore(lore)

        lore.add("".toComponent())
    }
}
