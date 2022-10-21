package space.maxus.macrocosm.players

import com.destroystokyo.paper.profile.ProfileProperty
import net.axay.kspigot.gui.GUIType
import net.axay.kspigot.gui.Slots
import net.axay.kspigot.gui.kSpigotGUI
import net.axay.kspigot.items.flags
import net.axay.kspigot.items.itemStack
import net.axay.kspigot.items.meta
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.chat.noitalic
import space.maxus.macrocosm.chat.reduceToList
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.text.text
import space.maxus.macrocosm.util.allNull
import space.maxus.macrocosm.util.padForward

fun statBreakdown(player: MacrocosmPlayer) = kSpigotGUI(GUIType.SIX_BY_NINE) {
    val glass = ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, "")
    val p = player.paper!!

    defaultPage = 0
    title = text("Your Stats Breakdown")

    val stats = player.stats()!!

    page(0) {
        placeholder(Slots.All, glass)

        val allStatsFancy = itemStack(Material.ANVIL) {
            meta {
                displayName(text("<light_purple>Your Stats Breakdown").noitalic())

                lore(stats.iter().entries.filter { (_, v) -> v != 0f }.map { (k, v) -> k.formatFancy(v) })
            }
        }
        placeholder(Slots.RowSixSlotFive, allStatsFancy)

        button(
            Slots.RowOneSlotFour,
            ItemValue.placeholderDescripted(Material.ARROW, "<green>Go Back", "To Equipment Screen")
        ) {
            EquipmentHandler.menu(player)
        }
        button(Slots.RowOneSlotFive, ItemValue.placeholder(Material.BARRIER, "<red>Close")) {
            p.closeInventory()
        }

        val compound = createRectCompound<Pair<Statistic, Float>>(Slots.RowTwoSlotTwo, Slots.RowFiveSlotEight,
            iconGenerator = {
                if (it.second == -1f)
                    return@createRectCompound glass

                val (stat, amount) = it

                val name = stat.formatFancy(amount) ?: return@createRectCompound glass
                if (allNull(stat.displayItem, stat.displaySkin))
                    return@createRectCompound glass

                val baseItem: ItemStack = if (stat.displayItem == null) {
                    val stack = ItemStack(Material.PLAYER_HEAD)
                    stack.meta<SkullMeta> {
                        val profile = Bukkit.createProfile(Macrocosm.constantProfileId)
                        profile.setProperty(ProfileProperty("textures", stat.displaySkin!!))
                        this.playerProfile = profile
                    }
                    stack
                } else ItemStack(stat.displayItem)

                baseItem.meta {
                    flags(*ItemFlag.values())
                    displayName(name)

                    val rawLore = stat.description
                    if (rawLore.isBlank())
                        return@createRectCompound glass

                    val lore = rawLore.reduceToList().filter { str -> !str.isBlank() }
                        .map { reduced -> text("<gray>$reduced").noitalic() }.toMutableList()

                    val buf = mutableListOf<String>()
                    stat.addExtraLore(buf, stats)

                    lore.addAll(buf.map { b -> text(b).noitalic() })

                    lore(lore)
                }

                baseItem
            },
            onClick = { e, _ ->
                e.bukkitEvent.isCancelled = true
            })

        compound.addContent(stats.iter().entries.filter { (k, v) -> v != 0f && !k.hidden && !k.hiddenFancy }
            .map { (k, v) -> Pair(k, v) }.padForward(28, Pair(Statistic.DAMAGE, -1f)))
    }
}
