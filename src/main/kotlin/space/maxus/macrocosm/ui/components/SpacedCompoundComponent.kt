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

    init {
        for(slot in space.enumerate()) {
            if(!slotToValue.contains(slot))
                slotToValue.add(slot)
        }
        slotToValue.sort()
    }

    override fun handleClick(click: UIClickData) {
        val index = slotToValue.indexOf(click.bukkit.slot)
        clickHandler(click, values[index])
    }

    override fun render(inv: Inventory, ui: MacrocosmUI) {
        for(slot in space.enumerate()) {
            val index = slotToValue.indexOf(slot)
            if(values.size <= index) {
                inv.setItem(slot, null)
            } else {
                inv.setItem(slot, map(values[index]))
            }
        }
    }

    override fun wasClicked(slot: Int): Boolean {
        return space.contains(slot)
    }
}
