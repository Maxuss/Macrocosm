package space.maxus.macrocosm.ui.components

import space.maxus.macrocosm.ui.UIDimensions

data class RectComponentSpace(
    private val min: ComponentSpace,
    private val max: ComponentSpace,
): ComponentSpace {
    private val contents: MutableList<Int> = mutableListOf()
    var width: Int? = null

    fun initContentsIfNull(dim: UIDimensions) {
        if(this.width == null) {
            val slotMin = Slot.fromRaw(min.enumerate(dim).first())
            val slotMax = Slot.fromRaw(max.enumerate(dim).first())
            width = slotMax.column - slotMin.column + 1
            for(row in slotMin.row..slotMax.row) {
                for(col in slotMin.column..slotMax.column) {
                    contents.add(Slot(row, col).value)
                }
            }
        }
    }

    override fun contains(slot: Int, dim: UIDimensions): Boolean {
        this.initContentsIfNull(dim)
        return contents.contains(slot)
    }

    override fun enumerate(dim: UIDimensions): List<Int> {
        this.initContentsIfNull(dim)
        return contents
    }
}
