package space.maxus.macrocosm.ui.components

import org.bukkit.inventory.Inventory
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIClickData
import space.maxus.macrocosm.ui.UIComponent

data class ButtonComponent(
    val space: ComponentSpace,
    val item: ItemComponentRepr,
    val handler: (UIClickData) -> Unit
): UIComponent {
    override fun handleClick(click: UIClickData) {
        click.bukkit.isCancelled = true
        handler(click)
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
