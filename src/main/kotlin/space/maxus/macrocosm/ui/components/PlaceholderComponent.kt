package space.maxus.macrocosm.ui.components

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.ui.UIClickData

class PlaceholderComponent(
    space: ComponentSpace,
    val item: ItemStack
) : SpacedComponent(space) {
    override fun handleClick(click: UIClickData) {
        click.bukkit.isCancelled = true
    }

    override fun render(inv: Inventory): ItemStack = item
}

class AdvancedPlaceholderComponent(
    space: ComponentSpace,
    val item: ItemComponentRepr
) : SpacedComponent(space) {
    override fun handleClick(click: UIClickData) {
        click.bukkit.isCancelled = true
    }

    override fun render(inv: Inventory): ItemStack = item.item
}
