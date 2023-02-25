package space.maxus.macrocosm.ui.components

import space.maxus.macrocosm.ui.UIDimensions

interface ComponentSpace {
    fun contains(slot: Int, dim: UIDimensions): Boolean

    fun enumerate(dim: UIDimensions): List<Int>
}
