package space.maxus.macrocosm.ui.components

import space.maxus.macrocosm.ui.UIDimensions

/**
 * ComponentSpace is an abstract space that can be used to store components.
 */
interface ComponentSpace {
    /**
     * Returns true if the slot exists in the space.
     *
     * @param slot The slot to check.
     * @param dim The dimensions of the UI, this space is being used in.
     * @return  True if the slot exists, false otherwise.
     */
    fun contains(slot: Int, dim: UIDimensions): Boolean

    /**
     * Enumerates all slots in the space.
     *
     * @param dim The dimensions of the UI, this space is being used in.
     * @return  A list of all slots in the space.
     */
    fun enumerate(dim: UIDimensions): List<Int>
}
