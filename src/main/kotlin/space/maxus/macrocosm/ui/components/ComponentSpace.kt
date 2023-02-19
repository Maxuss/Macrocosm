package space.maxus.macrocosm.ui.components

interface ComponentSpace {
    fun contains(slot: Int): Boolean

    fun enumerate(): List<Int>
}
