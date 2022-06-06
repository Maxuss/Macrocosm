package space.maxus.macrocosm.slayer.ui

import net.axay.kspigot.gui.*
import net.axay.kspigot.items.meta
import org.bukkit.Material
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.enchants.roman
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.loot.Drop
import space.maxus.macrocosm.loot.DropRarity
import space.maxus.macrocosm.loot.vanilla
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.slayer.SlayerType
import space.maxus.macrocosm.slayer.colorFromTier
import space.maxus.macrocosm.text.comp
import space.maxus.macrocosm.util.id
import space.maxus.macrocosm.util.pad
import space.maxus.macrocosm.util.stripTags

private fun formatChance(chance: Float): String {
    return if(chance >= 0.01 && chance < 1)
        "<gray>(" + Formatting.stats((chance * 100).toBigDecimal()) + "%)"
    else ""
}

fun dropsMenu(player: MacrocosmPlayer, ty: SlayerType) = kSpigotGUI(GUIType.FOUR_BY_NINE) {
    val slayer = ty.slayer
    val playerLevel = player.slayers[ty]!!
    title = comp("${slayer.name.stripTags()} Drops")
    defaultPage = 0

    page(0) {
        placeholder(Slots.All, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))

        val cmp = createCompound<SlayerDrop>(iconGenerator = { drop ->
            if(drop is NullDrop) {
                ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, "")
            } else {
                if (drop.requiredLevel > playerLevel.level)
                    ItemValue.placeholderDescripted(
                        Material.COAL_BLOCK,
                        "<red>???",
                        "Required LVL: <yellow>${drop.requiredLevel}"
                    )
                val item = Registry.ITEM.find(drop.drop.item)
                val it = item.build(player)!!
                it.meta {
                    val bufferedLore = mutableListOf<String>()
                    drop.amounts.forEachIndexed { tier, amount ->
                        val str =
                            if (amount.first == amount.last) "${amount.first}" else "${amount.first} to ${amount.last}"
                        bufferedLore.add("Tier ${roman(tier + 1)} amount: <green>$str")
                    }
                    bufferedLore.add("")
                    bufferedLore.add("Minimum: <${colorFromTier(drop.minTier).asHexString()}>Tier ${roman(drop.minTier)}")
                    bufferedLore.add("Odds: ${drop.drop.rarity.odds} ${formatChance(drop.drop.chance.toFloat())}")
                    if (drop.requiredLevel > 0) {
                        bufferedLore.add("")
                        bufferedLore.add("Required LVL: <yellow>${drop.requiredLevel}")
                    }
                    lore(bufferedLore.map { l -> comp("<gray>$l").noitalic() })
                }
                it
            }
        }, onClick = { ev, _ ->
            ev.bukkitEvent.isCancelled = true
        })

        compoundSpace(Slots.RowTwoSlotTwo rectTo Slots.RowThreeSlotEight, cmp)
        val drops = slayer.drops.pad(16, NullDrop)
        cmp.addContent(drops)

        button(Slots.RowOneSlotOne, ItemValue.placeholder(Material.ARROW, "<red>Back")) { ev ->
            ev.bukkitEvent.isCancelled = true
            ev.player.openGUI(specificSlayerMenu(player, ty))
        }
    }
}

object NullDrop: SlayerDrop(vanilla(Material.AIR, -1.0), -1, -1, listOf())
open class SlayerDrop(val drop: Drop, val minTier: Int, val requiredLevel: Int, val amounts: List<IntRange>)

class VisualDrop internal constructor(rarity: DropRarity, item: Identifier, chance: Double): Drop(rarity, chance, item, 1..1)

fun visual(item: String, rarity: DropRarity, chance: Double): Drop = VisualDrop(rarity, id(item), chance)
