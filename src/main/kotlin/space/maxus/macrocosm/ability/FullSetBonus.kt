package space.maxus.macrocosm.ability

import net.axay.kspigot.extensions.bukkit.toComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.ChatColor
import space.maxus.macrocosm.chat.isBlankOrEmpty
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.text.comp

open class FullSetBonus(name: String, description: String): AbilityBase(AbilityType.PASSIVE, name, description) {
    fun ensureSetRequirement(player: MacrocosmPlayer): Boolean {
        return listOf(player.helmet, player.chestplate, player.leggings, player.boots).map { it != null && it.abilities.contains(this) }.all { it }
    }

    override fun buildLore(lore: MutableList<Component>, player: MacrocosmPlayer?) {
        val tmp = mutableListOf<Component>()
        tmp.add(comp("<gold>Full Set Bonus: $name").noitalic())
        for (desc in description.reduceToList()) {
            tmp.add(comp("<gray>$desc</gray>").noitalic())
        }
        tmp.removeIf {
            ChatColor.stripColor(LegacyComponentSerializer.legacySection().serialize(it))!!.isBlankOrEmpty()
        }
        lore.addAll(tmp)
        cost?.buildLore(lore)

        lore.add("".toComponent())
    }
}
