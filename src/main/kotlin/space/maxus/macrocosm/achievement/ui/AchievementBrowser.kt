package space.maxus.macrocosm.achievement.ui

import net.axay.kspigot.gui.GUIType
import net.axay.kspigot.gui.Slots
import net.axay.kspigot.gui.kSpigotGUI
import net.axay.kspigot.gui.rectTo
import org.bukkit.Material
import space.maxus.macrocosm.achievement.Achievement
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.text.text

fun achievementBrowser(player: MacrocosmPlayer) = kSpigotGUI(GUIType.FIVE_BY_NINE) {
    title = text("Achievement Browser")
    defaultPage = 0

    page(0) {
        placeholder(Slots.All, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))
        button(Slots.RowOneSlotFive, ItemValue.placeholder(Material.BARRIER, "<red>Close")) {
            it.bukkitEvent.isCancelled = true
            it.player.closeInventory()
        }

        val cmp = createCompound<Achievement>({ ach ->
            ach.buildItem(player)
        }) { e, _ ->
            e.bukkitEvent.isCancelled = true
        }
        compoundSpace(Slots.RowTwoSlotTwo rectTo Slots.RowFourSlotEight, cmp)

        cmp.addContent(Registry.ACHIEVEMENT.iter().values)
        cmp.sortContentBy { it.rarity.ordinal }

        compoundScroll(Slots.RowOneSlotEight, ItemValue.placeholder(Material.ARROW, "<green>Back"), cmp, reverse = true)
        compoundScroll(Slots.RowOneSlotNine, ItemValue.placeholder(Material.ARROW, "<green>Forward"), cmp)
    }
}
