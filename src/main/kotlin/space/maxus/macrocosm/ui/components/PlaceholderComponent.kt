package space.maxus.macrocosm.ui.components

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIClickData
import space.maxus.macrocosm.ui.UIComponent

data class PlaceholderComponent(
    val space: ComponentSpace,
    val item: ItemStack
): UIComponent {
    override fun handleClick(click: UIClickData) {
        click.bukkit.isCancelled = true
    }

    override fun wasClicked(slot: Int): Boolean {
        return space.contains(slot)
    }

    override fun render(inv: Inventory, ui: MacrocosmUI) {
        for(slot in space.enumerate()) {
            inv.setItem(slot, item)
        }
    }
}

data class AdvancedPlaceholderComponent(
    val space: ComponentSpace,
    val item: ItemComponentRepr
): UIComponent {
    override fun handleClick(click: UIClickData) {
        click.bukkit.isCancelled = true
    }

    override fun wasClicked(slot: Int): Boolean {
        return space.contains(slot)
    }

    override fun render(inv: Inventory, ui: MacrocosmUI) {
        for(slot in space.enumerate()) {
            inv.setItem(slot, item.item)
        }
    }
}
