package space.maxus.macrocosm.ui.components

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIClickData
import space.maxus.macrocosm.ui.UIComponent

data class CompoundScrollComponent(
    val space: ComponentSpace,
    val compound: SpacedCompoundComponent<*>,
    val amount: Int,
    val item: ItemStack,
): UIComponent {
    override fun handleClick(click: UIClickData) {
        click.bukkit.isCancelled = true
        compound.scroll(amount)
        compound.render(click.inventory, click.instance.base)
        click.paper.updateInventory()
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
