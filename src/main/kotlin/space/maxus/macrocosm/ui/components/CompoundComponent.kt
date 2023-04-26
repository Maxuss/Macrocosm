package space.maxus.macrocosm.ui.components

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIClickData
import space.maxus.macrocosm.ui.UIDimensions
import space.maxus.macrocosm.util.unreachable

/**
 * CompoundComponent is a component that can be used to display a list of items.
 *
 * @param space The space that this component will be in.
 * @param values The values to display.
 * @param map The function to map a value to an itemstack.
 * @param clickHandler The function to call when a value is clicked.
 * @param transparent Whether this component is transparent.
 */
open class CompoundComponent<M>(
    space: ComponentSpace,
    var values: List<M>,
    val map: (M) -> ItemStack,
    val clickHandler: (UIClickData, M) -> Unit,
    val transparent: Boolean = false
) : SpacedComponent(space) {
    private val slotToValue: MutableList<Int> = mutableListOf()
    private var scrollProgress: Int = 0
    private var slicedContent: List<M> = listOf()
    private var lastDim: UIDimensions = UIDimensions.SIX_X_NINE

    protected open fun initContentsIfNull(dim: UIDimensions) {
        lastDim = dim
        if (slotToValue.isNotEmpty())
            return
        for (slot in space.enumerate(dim)) {
            if (!slotToValue.contains(slot))
                slotToValue.add(slot)
        }
        slotToValue.sort()
        recalculateSlicedContent()
    }

    /**
     * Scrolls the component by the given [amount].
     */
    fun scroll(amount: Int) {
        val value = scrollProgress + amount

        val doScroll = if (slotToValue.size + value <= values.size) true
        else if (space is RectComponentSpace) {
            space.initContentsIfNull(lastDim)
            (slotToValue.size + value <= values.size + (space.width!! - (values.size % space.width!!)))
        } else false
        if (doScroll) {
            scrollProgress = value
            recalculateSlicedContent()
        }
    }

    /**
     * Recalculates the sliced content based on the current scroll progress.
     */
    open fun recalculateSlicedContent() {
        if (scrollProgress > values.size)
            scrollProgress = values.size
        else if (scrollProgress < 0)
            scrollProgress = 0

        var sliceUntil = slotToValue.size + scrollProgress
        if (sliceUntil > values.lastIndex)
            sliceUntil = values.size
        else if (sliceUntil < 0)
            sliceUntil = 0

        slicedContent = values.slice(scrollProgress until sliceUntil)
    }

    override fun handleClick(click: UIClickData) {
        initContentsIfNull(click.instance.dimensions)
        click.bukkit.isCancelled = true
        val index = slotToValue.indexOf(click.bukkit.slot)
        if (slicedContent.size <= index)
            return
        clickHandler(click, slicedContent[index])
    }

    override fun render(inv: Inventory, ui: MacrocosmUI) {
        initContentsIfNull(ui.dimensions)
        for (slot in space.enumerate(ui.dimensions)) {
            val index = slotToValue.indexOf(slot)
            if (slicedContent.size <= index) {
                if (!transparent)
                    inv.setItem(slot, null)
            } else {
                inv.setItem(slot, map(slicedContent[index]))
            }
        }
    }

    override fun render(inv: Inventory): ItemStack {
        unreachable()
    }
}

/**
 * LazyCompoundComponent is a compound component that only calculates its content when it is rendered.
 */
class LazyCompoundComponent<M>(
    space: ComponentSpace,
    val valueSupplier: () -> List<M>,
    map: (M) -> ItemStack,
    clickHandler: (UIClickData, M) -> Unit,
    transparent: Boolean = false
) : CompoundComponent<M>(
    space,
    listOf(),
    map,
    clickHandler,
    transparent
) {
    override fun recalculateSlicedContent() {
        this.values = valueSupplier()
        super.recalculateSlicedContent()
    }

    override fun render(inv: Inventory, ui: MacrocosmUI) {
        this.recalculateSlicedContent()
        super.render(inv, ui)
    }
}
