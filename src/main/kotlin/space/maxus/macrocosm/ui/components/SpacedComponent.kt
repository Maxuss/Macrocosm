package space.maxus.macrocosm.ui.components

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIComponent
import space.maxus.macrocosm.ui.UIDimensions

abstract class SpacedComponent(val space: ComponentSpace) : UIComponent {
    abstract fun render(inv: Inventory): ItemStack

    override fun render(inv: Inventory, ui: MacrocosmUI) {
        val item = render(inv)
        for (slot in space.enumerate(ui.dimensions)) {
            inv.setItem(slot, item)
        }
    }

    override fun wasClicked(slot: Int, dim: UIDimensions): Boolean {
        return space.contains(slot, dim)
    }
}
