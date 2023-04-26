package space.maxus.macrocosm.ui.components

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.ui.UIClickData

/**
 * A component that represents a clickable button.
 *
 * @param space The space this component is in.
 * @param item The item to render.
 * @param handler The handler to call when this button is clicked.
 */
class ButtonComponent(
    space: ComponentSpace,
    val item: ItemComponentRepr,
    val handler: (UIClickData) -> Unit
) : SpacedComponent(space) {
    override fun handleClick(click: UIClickData) {
        click.bukkit.isCancelled = true
        handler(click)
    }

    override fun render(inv: Inventory): ItemStack = item.item
}
