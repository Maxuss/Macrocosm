package space.maxus.macrocosm.accessory.ui

import net.axay.kspigot.gui.*
import org.bukkit.Material
import space.maxus.macrocosm.accessory.power.StoneAccessoryPower
import space.maxus.macrocosm.item.ItemValue
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.text.text

fun powerStonesGuide(player: MacrocosmPlayer): GUI<ForInventorySixByNine> = kSpigotGUI(GUIType.SIX_BY_NINE) {
    defaultPage = 0
    title = text("Power Stones Guide")

    page(0) {
        placeholder(Slots.Border, ItemValue.placeholder(Material.GRAY_STAINED_GLASS_PANE, ""))

        button(
            Slots.RowOneSlotFive,
            ItemValue.placeholderDescripted(Material.ARROW, "<green>Go Back", "To Learn Power From Stones")
        ) { e ->
            e.bukkitEvent.isCancelled = true
            e.player.openGUI(learnPowerUi(player, mutableListOf()))
        }

        val compound = createCompound<StoneAccessoryPower>({ it.guideItem(player) }) { e, _ ->
            e.bukkitEvent.isCancelled = true
        }

        compoundSpace(Slots.RowTwoSlotTwo rectTo Slots.RowFiveSlotEight, compound)
        compound.addContent(
            Registry.ACCESSORY_POWER.iter().values.filterIsInstance<StoneAccessoryPower>()
                .sortedByDescending { it.combatLevel })
    }
}
