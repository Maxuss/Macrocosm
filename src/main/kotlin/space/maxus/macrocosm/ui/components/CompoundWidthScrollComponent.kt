package space.maxus.macrocosm.ui.components

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.ui.UIClickData
import space.maxus.macrocosm.util.general.Ensure

/**
 * A component that can be used to scroll a compound component by its  width.
 *
 * @param space The space of the compound component.
 * @param compound The compound component to scroll.
 * @param item The item to render.
 * @param reverse Whether to scroll in reverse.
 */
class CompoundWidthScrollComponent(
    space: ComponentSpace,
    val compound: CompoundComponent<*>,
    val item: ItemStack,
    private val reverse: Boolean = false
) : SpacedComponent(space) {
    /**
     * The amount to scroll by.
     */
    var amount: Int? = null

    init {
        // make sure the compound is rect
        Ensure.isTrue(compound.space is RectComponentSpace, "Can not do width-scroll for non-rect compounds")
    }

    override fun handleClick(click: UIClickData) {
        if (amount == null) {
            val rect = compound.space as RectComponentSpace
            rect.initContentsIfNull(click.instance.dimensions)
            amount = rect.width!!.let { if (reverse) -it else it }
        }
        click.bukkit.isCancelled = true
        compound.scroll(amount!!)
        compound.render(click.inventory, click.instance.base)
        click.paper.updateInventory()
    }

    override fun render(inv: Inventory): ItemStack = item
}
