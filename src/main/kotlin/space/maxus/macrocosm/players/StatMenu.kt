package space.maxus.macrocosm.players

import com.destroystokyo.paper.profile.ProfileProperty
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
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIDimensions
import space.maxus.macrocosm.ui.components.Slot
import space.maxus.macrocosm.ui.dsl.macrocosmUi
import space.maxus.macrocosm.util.allNull
import space.maxus.macrocosm.util.padForward

fun statBreakdown(player: MacrocosmPlayer): MacrocosmUI = macrocosmUi("stat_breakdown", UIDimensions.SIX_X_NINE) {
    val glass = ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, "")

    title = "Your Stats Breakdown"

    val stats = player.stats()!!

    page {
        background()

        placeholder(Slot.RowOneSlotFive, itemStack(Material.ANVIL) {
            meta {
                displayName(text("<light_purple>Your Stats Breakdown").noitalic())

                lore(stats.iter().entries.filter { (_, v) -> v != 0f }.map { (k, v) -> k.formatFancy(v) })
            }
        })

        button(
            Slot.RowSixSlotFour,
            ItemValue.placeholderDescripted(Material.ARROW, "<green>Go Back", "To Equipment Screen")
        ) {
            EquipmentHandler.menu(player)
        }

        close()

        compound(Slot.RowTwoSlotTwo rect Slot.RowFiveSlotEight,
            { stats.iter().entries.filter { (k, v) -> v != 0f && !k.hidden && !k.hiddenFancy }
                .map { (k, v) -> Pair(k, v) }.padForward(28, Pair(Statistic.DAMAGE, -1f)).toList() },
            {
                if (it.second == -1f)
                    return@compound glass

                val (stat, amount) = it

                val name = stat.formatFancy(amount) ?: return@compound glass
                if (allNull(stat.displayItem, stat.displaySkin))
                    return@compound glass

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
                        return@compound glass

                    val lore = rawLore.reduceToList().filter { str -> str.isNotBlank() }
                        .map { reduced -> text("<gray>$reduced").noitalic() }.toMutableList()

                    val buf = mutableListOf<String>()
                    stat.addExtraLore(buf, stats)

                    lore.addAll(buf.map { b -> text(b).noitalic() })

                    lore(lore)
                }
                baseItem
            },
            { _, _ -> }
        )
    }
}
