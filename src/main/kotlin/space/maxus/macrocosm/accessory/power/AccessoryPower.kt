package space.maxus.macrocosm.accessory.power

import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.accessory.AccessoryBag
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.stats.Statistics
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * An accessory power interface
 */
interface AccessoryPower {
    /**
     * Name of this accessory power
     */
    val name: String

    /**
     * Tier of this accessory power. E.g. "Starter" or "Grandiose"
     */
    val tier: String

    /**
     * ID of this accessory power
     */
    val id: Identifier

    /**
     * Basic stats of this accessory power
     */
    val stats: Statistics

    /**
     * Item that this accessory power shows
     */
    val displayItem: Material

    /**
     * Registers specific listeners for this power
     */
    fun registerListeners()

    /**
     * Ensures that player has this accessory power
     */
    fun ensureRequirements(player: Player): Boolean {
        return player.macrocosm?.accessoryBag != null && player.macrocosm?.accessoryBag?.power == this.id
    }

    /**
     * Ensures that player has this accessory power
     */
    fun ensureRequirements(player: MacrocosmPlayer): Boolean {
        @Suppress("SENSELESS_COMPARISON") // Can be NPE here
        return player.accessoryBag != null && player.accessoryBag.power == this.id
    }

    /**
     * Builds a placeholder item for Thaumaturgy UI
     */
    fun thaumaturgyPlaceholder(player: MacrocosmPlayer, mp: Int, selected: Boolean = false): ItemStack {
        val statsForMp = stats.clone()
        statsForMp.multiply(AccessoryBag.statModifier(mp).toFloat())
        val method: (Material, String, Array<out String>) -> ItemStack =
            if (selected) ItemValue::placeholderDescriptedGlow else ItemValue::placeholderDescripted
        return method(
            if (selected) Material.LIME_STAINED_GLASS_PANE else displayItem,
            "<green>$name",
            arrayOf(
                "<dark_gray>$tier Power",
                "",
                "Stats:",
                *statsForMp.iter().filter { it.value != 0f }.map { (stat, value) ->
                    val mod = if (value < 0f) "" else "+"
                    val sValue = ceil(value).roundToInt()
                    "<${stat.color.asHexString()}>$mod$sValue${stat.display}"
                }.toTypedArray(),
                "",
                "You have: <gold>$mp Magic Power",
                "",
                if (selected) "<green>Power is selected!" else "<yellow>Click to select power!"
            )
        )
    }
}
