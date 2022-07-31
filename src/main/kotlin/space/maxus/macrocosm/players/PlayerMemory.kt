package space.maxus.macrocosm.players

import space.maxus.macrocosm.registry.Identifier

data class PlayerMemory(
    val tier6Slayers: MutableList<Identifier>
) {
    companion object {
        fun nullMemory() = PlayerMemory(mutableListOf())
    }
}
