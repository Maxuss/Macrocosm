package space.maxus.macrocosm.ability

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.ChatColor
import space.maxus.macrocosm.chat.isBlankOrEmpty
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.text.comp

/**
 * Represents a tiered set bonus, that increases buffs based on amount of armor pieces worn
 *
 * @constructor Constructs a new tiered set bonus
 *
 * @param name Name of this ability, supports MM tags
 * @param description Description of this ability, will later be partitioned to 25 chars per line
 */
open class TieredSetBonus(name: String, description: String) : AbilityBase(AbilityType.PASSIVE, name, description) {
    /**
     * Gets amount of armor pieces worn by player, and whether the player meets the requirements
     *
     * @param player Player to be used for checks
     * @return Pair of (True if ability should be executed, Amount of armor pieces worn)
     */
    protected fun getArmorTier(player: MacrocosmPlayer): Pair<Boolean, Int> {
        val tier = listOf(
            player.helmet,
            player.chestplate,
            player.leggings,
            player.boots
        ).map { it != null && it.abilities.contains(this) }.filter { it }.size
        return Pair(tier > 1, tier)
    }

    override fun buildLore(lore: MutableList<Component>, player: MacrocosmPlayer?) {
        val tmp = mutableListOf<Component>()
        val (_, tier) = if (player != null) getArmorTier(player) else Pair(false, 0)
        val name = if (tier <= 1) "<dark_gray>Tiered Bonus: $name ($tier/4)" else "<gold>Tiered Bonus: $name ($tier/4)"
        tmp.add(comp(name).noitalic())
        for (desc in description.reduceToList()) {
            tmp.add(comp("<gray>$desc</gray>").noitalic())
        }
        tmp.removeIf {
            ChatColor.stripColor(LegacyComponentSerializer.legacySection().serialize(it))!!.isBlankOrEmpty()
        }
        lore.addAll(tmp)
    }
}
