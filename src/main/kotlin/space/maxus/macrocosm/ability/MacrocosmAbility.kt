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

/**
 * Represents an item ability, that can execute various actions.
 *
 * Instead of inheriting this interface explicitly, loot at implementations:
 * - [AbilityBase]
 * - [TieredSetBonus]
 * - [FullSetBonus]
 */
interface MacrocosmAbility {
    /**
     * Name of this ability, supports MiniMessage tags
     */
    val name: String

    /**
     * Description of this ability. Will be later partitioned to 25 chars per line, not including MM tags
     */
    val description: String

    /**
     * Type of this ability, used only for visual purpose
     */
    val type: AbilityType

    /**
     * Cost of this ability (mana, health and cooldown)
     */
    val cost: AbilityCost?

    /**
     * You **have to** override this function, and register listeners there ([net.axay.kspigot.event.listen]))
     *
     */
    fun registerListeners() {

    }

    /**
     * Builds and inserts the ability name and descriptions in provided [lore] list
     *
     * @param lore List to be used for lore storage
     * @param player Player to be used for lore building. Used so that some values inside can be updated for players stats, etc.
     */
    fun buildLore(lore: MutableList<Component>, player: MacrocosmPlayer?) {
        val tmp = mutableListOf<Component>()
        tmp.add(type.format(name).noitalic())
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

    /**
     * Small interceptor class used for updating ability lore
     *
     * @param abil Inner ability
     */
    class Interceptor(
        abil: MacrocosmAbility,
        override val name: String = abil.name,
        override val description: String = abil.description,
        override val type: AbilityType = abil.type,
        override val cost: AbilityCost? = abil.cost,
    ) : MacrocosmAbility
}
