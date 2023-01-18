package space.maxus.macrocosm.accessory.power

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.accessory.AccessoryBag
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.AutoRegister
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.util.general.id
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * An accessory power that is unlocked by combining power stones
 */
abstract class StoneAccessoryPower(
    id: String,
    override val name: String,
    private val stoneName: String,
    private val rarity: Rarity,
    override val tier: String,
    /**
     * Required combat level to use this accessory power
     */
    val combatLevel: Int,
    /**
     * Special bonus description for this accessory power
     */
    val specialBonus: String,
    /**
     * The way to obtain this accessory power
     */
    val obtaining: String,
    override val stats: Statistics,
    private val stoneTexture: String,
) : AccessoryPower, AutoRegister<MacrocosmItem> {
    override val id: Identifier = id(id)
    override val displayItem: Material = Material.PLAYER_HEAD

    final override fun register(registry: Registry<MacrocosmItem>) {
        val id = Identifier.macro(stoneName.lowercase().replace(" ", "_"))
        registry.register(id, PowerStone(id, stoneName, this.id, rarity, stoneTexture))
    }

    fun guideItem(player: MacrocosmPlayer): ItemStack {
        return ItemValue.placeholderHeadDesc(
            stoneTexture,
            "<green>$name",
            "<dark_gray>$tier Stone Power",
            "",
            "Power Stone:",
            "<${rarity.color.asHexString()}>$stoneName",
            "",
            "Source:",
            *obtaining.split("<br>").toTypedArray(),
            "",
            "Combat level required: <green>$combatLevel",
            "",
            "Learned: ${if (player.memory.knownPowers.contains(id)) "<green>Yes ✓" else "<red>Not Yet ❌"}"
        )
    }

    override fun thaumaturgyPlaceholder(player: MacrocosmPlayer, mp: Int, selected: Boolean): ItemStack {
        val statsForMp = stats.clone()
        statsForMp.multiply(AccessoryBag.statModifier(mp).toFloat())
        val lore = arrayOf(
            "<dark_gray>$tier Stone Power",
            "",
            "Stats:",
            *statsForMp.iter().filter { it.value != 0f }.map { (stat, value) ->
                val mod = if (value < 0f) "" else "+"
                val sValue = ceil(value).roundToInt()
                "<${stat.color.asHexString()}>$mod$sValue${stat.display}"
            }.toTypedArray(),
            "",
            "Unique Power Bonus:",
            *specialBonus.split("<br>").toTypedArray(),
            "",
            "You have: <gold>$mp Magic Power",
            "",
            if (selected) "<green>Power is selected!" else "<yellow>Click to select power!"
        )
        return if (selected)
            ItemValue.placeholderDescriptedGlow(Material.LIME_STAINED_GLASS_PANE, "<green>$name", *lore)
        else
            ItemValue.placeholderHeadDesc(stoneTexture, "<green>$name", *lore)
    }
}

