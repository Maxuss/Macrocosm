package space.maxus.macrocosm.ui.components

class RectComponentSpace(
    min: Slot,
    max: Slot,
): ComponentSpace {
    private val contents: MutableList<Int> = mutableListOf()

    init {
        for(row in min.row..max.row) {
            for(col in min.column..max.column) {
                contents.add(Slot(row, col).value)
            }
        }
    }

    override fun contains(slot: Int): Boolean {
        return contents.contains(slot)
    }

    override fun enumerate(): List<Int> {
        return contents
    }
}
