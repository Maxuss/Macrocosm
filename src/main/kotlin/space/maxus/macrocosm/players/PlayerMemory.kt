package space.maxus.macrocosm.players

import space.maxus.macrocosm.registry.Identifier
import java.io.Serializable

data class PlayerMemory(
    val tier6Slayers: MutableList<Identifier>
) : Serializable {
    companion object {
        fun nullMemory() = PlayerMemory(mutableListOf())
    }
}
