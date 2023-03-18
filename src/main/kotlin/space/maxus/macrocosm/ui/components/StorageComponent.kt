package space.maxus.macrocosm.ui.components

import org.bukkit.Material
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.players.isAirOrNull
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIClickData
import space.maxus.macrocosm.util.unreachable


open class StorageComponent(
    space: ComponentSpace,
    val fits: (ItemStack) -> Boolean = { true },
    val onPut: (UIClickData, ItemStack) -> Unit = { _, _ -> },
    val onTake: (UIClickData, ItemStack) -> Unit = { _, _ -> }
) : SpacedComponent(space) {
    var stored: ItemStack = ItemStack(Material.AIR)

    override fun render(inv: Inventory): ItemStack {
        unreachable()
    }

    override fun render(inv: Inventory, ui: MacrocosmUI) {
        for (slot in space.enumerate(ui.dimensions)) {
            inv.setItem(slot, stored)
        }
    }

    @Suppress("DEPRECATION")
    override fun handleClick(click: UIClickData) {
        click.bukkit.isCancelled = true
        val e = click.bukkit
        val cursor = e.cursor
        if (cursor.isAirOrNull() && e.action.name.contains("PICKUP")) {
            // taking items
            e.cursor = stored
            onTake(click, stored)
            stored = ItemStack(Material.AIR)
        } else if (!cursor.isAirOrNull() && e.action.name.contains("PLACE") && fits(cursor!!)) {
            // putting items
            if (cursor.amount > 1) {
                stored = cursor.clone()
                onPut(click, stored)
                stored.amount = 1
                e.cursor!!.amount -= 1
            } else {
                stored = cursor.clone()
                onPut(click, stored)
                e.cursor = null
            }
        }
        click.instance.reload()
    }
}
