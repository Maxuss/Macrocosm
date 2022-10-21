package space.maxus.macrocosm.ability

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.ChatColor
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.text.text
import kotlin.math.roundToInt

/**
 * Represents an item ability, that can execute various actions.
 *
 * Instead of inheriting this interface explicitly, loot at implementations:
 * - [AbilityBase]
 * - [TieredSetBonus]
 * - [FullSetBonus]
 */
interface MacrocosmAbility {
    companion object {
        private val DAMAGE_REGEX = "\\[[\\d.]+:[\\d.]+]".toRegex()

        /**
         * Formats dynamic damage numbers inside the provided string.
         *
         * The dynamic damage value is defined as [[&lt;base damage&gt;:&lt;scaling value&gt;]]
         *
         * @param str String to be formatted
         * @param player Player for which the values will be formatted
         *
         * @return Formatted string
         */
        fun formatDamageNumbers(str: String, player: MacrocosmPlayer?): String {
            val stats = player?.stats()
            return DAMAGE_REGEX.replace(str) { res ->
                val data = res.value.trim('[', ']').split(":")
                val dmg = java.lang.Double.valueOf(data[0])
                val scaling = java.lang.Double.valueOf(data[1])
                if (stats == null)
                    return@replace Formatting.withCommas(dmg.toBigDecimal(), true)
                return@replace Formatting.withCommas(
                    DamageCalculator.calculateMagicDamage(
                        dmg.roundToInt(),
                        scaling.toFloat(),
                        stats
                    ).toBigDecimal(), true
                )
            }
        }
    }

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
     * You **have to** override this function, and register listeners there ([net.axay.kspigot.event.listen])),
     * this is where all the ability logic is defined at
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
        for (part in formatDamageNumbers(description, player).split("<br>")) {
            for (desc in part.reduceToList()) {
                tmp.add(text("<gray>$desc</gray>").noitalic())
            }
        }
        tmp.removeIf {
            ChatColor.stripColor(LegacyComponentSerializer.legacySection().serialize(it))!!.isBlank()
        }
        lore.addAll(tmp)
    }


}
