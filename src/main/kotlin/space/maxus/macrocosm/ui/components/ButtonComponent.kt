package space.maxus.macrocosm.ui.components

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.ui.UIClickData

class ButtonComponent(
    space: ComponentSpace,
    val item: ItemComponentRepr,
    val handler: (UIClickData) -> Unit
): SpacedComponent(space) {
    override fun handleClick(click: UIClickData) {
        click.bukkit.isCancelled = true
        handler(click)
    }

    override fun render(inv: Inventory): ItemStack = item.item
}
