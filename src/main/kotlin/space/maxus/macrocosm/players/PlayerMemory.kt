package space.maxus.macrocosm.players

import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.util.general.id
import java.io.Serializable

data class PlayerMemory(
    val tier6Slayers: MutableList<Identifier>,
    val knownPowers: MutableList<Identifier>
) : Serializable {
    companion object {
        fun nullMemory() = PlayerMemory(mutableListOf(), mutableListOf(id("test_power"), id("test_power_2")))
    }
}
