package space.maxus.macrocosm.slayer.ui

import net.axay.kspigot.gui.*
import net.axay.kspigot.items.meta
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
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
import space.maxus.macrocosm.slayer.SlayerLevel
import space.maxus.macrocosm.slayer.SlayerType
import space.maxus.macrocosm.slayer.colorFromTier
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.general.id
import space.maxus.macrocosm.util.padForward
import space.maxus.macrocosm.util.stripTags

internal fun formatChance(chance: Float): String {
    return "<gray>(" + Formatting.stats((chance * 100).toBigDecimal(), scale = if(chance * 100 <= .2) 4 else 1) + "%)"
}

internal fun buildDropItem(player: MacrocosmPlayer, playerLevel: SlayerLevel, drop: SlayerDrop, newChance: Float = -1f): ItemStack {
    if (drop.requiredLevel > playerLevel.level)
        return ItemValue.placeholderDescripted(
            Material.COAL_BLOCK,
            "<red>???",
            "Required LVL: <yellow>${drop.requiredLevel}"
        )
    val item = Registry.ITEM.find(drop.drop.item)
    val it = item.build(player)!!
    it.meta {
        val bufferedLore = mutableListOf<String>()
        val f = drop.amounts.toList().first()
        if (drop.amounts.all { it.value == f.second }) {
            val amount = f.second
            val str =
                if (amount.first == amount.last) "${amount.first}" else "${amount.first} to ${amount.last}"
            bufferedLore.add("Tier ${roman(f.first)} amount: <green>$str")
        } else {
            drop.amounts.forEach { (tier, amount) ->
                if (amount.last <= 0)
                    return@forEach
                val str =
                    if (amount.first == amount.last) "${amount.first}" else "${amount.first} to ${amount.last}"
                bufferedLore.add("Tier ${roman(tier)} amount: <green>$str")
            }
        }
        bufferedLore.add("")
        bufferedLore.add("Minimum: <${colorFromTier(drop.minTier).asHexString()}>Tier ${roman(drop.minTier)}")
        if(newChance != -1f) {
            bufferedLore.add("Odds: ${drop.drop.rarity.odds} <strikethrough>${formatChance(drop.drop.chance.toFloat()).replace("<gray>", "<dark_gray>")}</strikethrough> ${formatChance(newChance)}")
        } else {
            bufferedLore.add("Odds: ${drop.drop.rarity.odds} ${formatChance(drop.drop.chance.toFloat())}")
        }
        if (drop.requiredLevel > 0) {
            bufferedLore.add("")
            bufferedLore.add("Required LVL: <yellow>${drop.requiredLevel}")
        }
        lore(bufferedLore.map { l -> text("<gray>$l").noitalic() })
    }
    return it
}

fun dropsMenu(player: MacrocosmPlayer, ty: SlayerType) = kSpigotGUI(GUIType.FIVE_BY_NINE) {
    val slayer = ty.slayer
    val playerLevel = player.slayers[ty]!!
    title = text("${slayer.name.stripTags()} Drops")
    defaultPage = 0

    page(0) {
        placeholder(Slots.All, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))

        val cmp = createCompound<SlayerDrop>(iconGenerator = { drop ->
            if (drop is NullDrop) {
                ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, "")
            } else {
                buildDropItem(player, playerLevel, drop)
            }
        }, onClick = { ev, _ ->
            ev.bukkitEvent.isCancelled = true
        })

        compoundSpace(Slots.RowTwoSlotTwo rectTo Slots.RowFourSlotEight, cmp)
        val drops = slayer.drops.padForward(21, NullDrop)
        cmp.addContent(drops)
        cmp.sortContentBy { it.requiredLevel }
        cmp.sortContentBy { it.minTier }

        button(Slots.RowOneSlotOne, ItemValue.placeholder(Material.ARROW, "<red>Back")) { ev ->
            ev.bukkitEvent.isCancelled = true
            ev.player.openGUI(specificSlayerMenu(player, ty))
        }
    }
}

object NullDrop : SlayerDrop(vanilla(Material.AIR, -1.0), -1, -1, hashMapOf())
open class SlayerDrop(val drop: Drop, val minTier: Int, val requiredLevel: Int, val amounts: HashMap<Int, IntRange>)

class VisualDrop internal constructor(rarity: DropRarity, item: Identifier, chance: Double) :
    Drop(rarity, chance, item, 1..1)

fun visual(item: String, rarity: DropRarity, chance: Double): Drop = VisualDrop(rarity, id(item), chance)
