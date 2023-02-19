package space.maxus.macrocosm.ui.components

data class SingleComponentSpace(val slot: Int): ComponentSpace {
    override fun contains(slot: Int): Boolean {
        return this.slot == slot
    }

    override fun enumerate(): List<Int> {
        return listOf(slot)
    }
}
