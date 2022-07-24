package space.maxus.macrocosm.ability

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.ChatColor
import space.maxus.macrocosm.chat.isBlankOrEmpty
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.text.text

/**
 * A wrapper for abstract [AbilityBase], which provides some minor features. Can also be inherited for more abstraction.
 *
 * Note that this ability is also rendered as `Full Set Bonus: <name>`, and does not accept ability types, so make
 * sure to inherit it and override the [buildLore] method to inject your own type.
 *
 * @constructor Creates a new full set bonus ability
 *
 * @param name Name of the ability. Supports MM tags.
 * @param description Description of this ability. Will be partitioned to 25 chars per line, not including MM tags
 */
open class FullSetBonus(name: String, description: String, val threePiece: Boolean = false) :
    AbilityBase(AbilityType.PASSIVE, name, description) {
    /**
     * Ensures that the provided [player] has set with this ability
     *
     * @param player Player to be checked against
     * @return True if all checks passed, false otherwise
     */
    fun ensureSetRequirement(player: MacrocosmPlayer): Boolean {
        return (if (threePiece) listOf(
            player.chestplate,
            player.leggings,
            player.boots
        ) else listOf(
            player.helmet,
            player.chestplate,
            player.leggings,
            player.boots
        )).map { it != null && it.abilities.contains(this) }.all { it }
    }

    /**
     * Builds and inserts the ability name and descriptions in provided [lore] list
     *
     * @param lore List to be used for lore storage
     * @param player Player to be used for lore building. Used so that some values inside can be updated for players stats, etc.
     */
    override fun buildLore(lore: MutableList<Component>, player: MacrocosmPlayer?) {
        val tmp = mutableListOf<Component>()
        tmp.add(
            text(
                "<gold>Full Set Bonus: $name${
                    if (type != AbilityType.PASSIVE) " <yellow><bold>" + type.name.replace(
                        "_",
                        " "
                    ).uppercase() else ""
                }"
            ).noitalic()
        )
        for (part in description.split("<br>")) {
            for (desc in part.reduceToList(30)) {
                tmp.add(text("<gray>$desc</gray>").noitalic())
            }
        }
        tmp.removeIf {
            ChatColor.stripColor(LegacyComponentSerializer.legacySection().serialize(it))!!.isBlankOrEmpty()
        }
        lore.addAll(tmp)
    }
}
