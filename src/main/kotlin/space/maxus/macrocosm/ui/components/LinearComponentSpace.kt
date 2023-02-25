package space.maxus.macrocosm.ui.components

import space.maxus.macrocosm.ui.UIDimensions

data class LinearComponentSpace(val slots: List<Int>): ComponentSpace {
    override fun contains(slot: Int, dim: UIDimensions): Boolean {
        return slots.contains(slot)
    }

    override fun enumerate(dim: UIDimensions): List<Int> {
        return slots
    }
}
