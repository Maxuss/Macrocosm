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

interface AccessoryPower {
    val name: String
    val tier: String
    val id: Identifier
    val stats: Statistics
    val displayItem: Material

    fun registerListeners()

    fun ensureRequirements(player: Player): Boolean {
        return player.macrocosm?.accessoryBag?.power == this.id
    }

    fun thaumaturgyPlaceholder(player: MacrocosmPlayer, mp: Int, selected: Boolean = false): ItemStack {
        val statsForMp = stats.clone()
        statsForMp.multiply(AccessoryBag.statModifier(mp).toFloat())
        val method: (Material, String, Array<out String>) -> ItemStack = if(selected) ItemValue::placeholderDescriptedGlow else ItemValue::placeholderDescripted
        return method(
            if(selected) Material.LIME_STAINED_GLASS_PANE else displayItem,
            "<green>$name",
            arrayOf(
            "<dark_gray>$tier Power",
            "",
            "Stats:",
            *statsForMp.iter().filter { it.value != 0f }.map { (stat, value) ->
                val mod = if(value < 0f) "-" else "+"
                val sValue = ceil(value).roundToInt()
                "<${stat.color.asHexString()}>$mod$sValue${stat.display}"
            }.toTypedArray(),
            "",
            "You have: <gold>$mp Magic Power",
            "",
            if(selected) "<green>Power is selected!" else "<yellow>Click to select power!"
            )
        )
    }
}
