package space.maxus.macrocosm.ui.components

import space.maxus.macrocosm.ui.UIDimensions

class InventoryBorderSlots(
    padding: Int,
): ComponentSpace {
    private val slots: MutableList<Int> = mutableListOf()

    init {
        for(cur in 0 until padding) {
            for(col in 1 + cur..UIDimensions.SIX_X_NINE.width - cur) {
                slots.add(Slot(1, col).value)
                slots.add(Slot(UIDimensions.SIX_X_NINE.height, col).value)
            }
            for(row in 2 + cur until UIDimensions.SIX_X_NINE.height - cur) {
                slots.add(Slot(row, 1).value)
                slots.add(Slot(row, UIDimensions.SIX_X_NINE.width).value)
            }
        }
    }

    override fun contains(slot: Int): Boolean {
        return slots.contains(slot)
    }

    override fun enumerate(): List<Int> {
        return slots
    }
}

