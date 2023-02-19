package space.maxus.macrocosm.ui.components

data class LinearComponentSpace(val slots: List<Int>): ComponentSpace {
    override fun contains(slot: Int): Boolean {
        return slots.contains(slot)
    }

    override fun enumerate(): List<Int> {
        return slots
    }
}
