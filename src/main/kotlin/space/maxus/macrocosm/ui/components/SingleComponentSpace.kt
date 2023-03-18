package space.maxus.macrocosm.ui.components

import space.maxus.macrocosm.ui.UIDimensions

data class SingleComponentSpace(val slot: Int) : ComponentSpace {
    override fun contains(slot: Int, dim: UIDimensions): Boolean {
        return this.slot == slot
    }

    override fun enumerate(dim: UIDimensions): List<Int> {
        return listOf(slot)
    }
}
