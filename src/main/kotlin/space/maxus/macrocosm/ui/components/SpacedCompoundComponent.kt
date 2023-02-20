package space.maxus.macrocosm.ui.components

import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.ui.MacrocosmUI
import space.maxus.macrocosm.ui.UIClickData
import space.maxus.macrocosm.ui.UIComponent

class SpacedCompoundComponent<M>(
    val space: ComponentSpace,
    val values: List<M>,
    val map: (M) -> ItemStack,
    val clickHandler: (UIClickData, M) -> Unit): UIComponent {
    private val slotToValue: MutableList<Int> = mutableListOf()
    private var scrollProgress: Int = 0
    private var slicedContent: List<M> = listOf()

    init {
        for(slot in space.enumerate()) {
            if(!slotToValue.contains(slot))
                slotToValue.add(slot)
        }
        slotToValue.sort()
        recalculateSlicedContent()
    }

    fun scroll(amount: Int) {
        val value = scrollProgress + amount

        val doScroll = if(slotToValue.size + value <= values.size) true
            else if(space is RectComponentSpace)
                (slotToValue.size + value <= values.size + (space.width - (values.size % space.width)))
            else false
        if(doScroll) {
            scrollProgress = value
            recalculateSlicedContent()
        }
    }

    private fun recalculateSlicedContent() {
        if (scrollProgress > values.size)
            scrollProgress = values.size
        else if(scrollProgress < 0)
            scrollProgress = 0

        var sliceUntil = slotToValue.size + scrollProgress
        if (sliceUntil > values.lastIndex)
            sliceUntil = values.size
        else if(sliceUntil < 0)
            sliceUntil = 0

        slicedContent = values.slice(scrollProgress until sliceUntil)
    }

    override fun handleClick(click: UIClickData) {
        click.bukkit.isCancelled = true
        val index = slotToValue.indexOf(click.bukkit.slot)
        if(slicedContent.size <= index)
            return
        clickHandler(click, slicedContent[index])
    }

    override fun render(inv: Inventory, ui: MacrocosmUI) {
        for(slot in space.enumerate()) {
            val index = slotToValue.indexOf(slot)
            if(slicedContent.size <= index) {
                inv.setItem(slot, null)
            } else {
                inv.setItem(slot, map(slicedContent[index]))
            }
        }
    }

    override fun wasClicked(slot: Int): Boolean {
        return space.contains(slot)
    }
}
