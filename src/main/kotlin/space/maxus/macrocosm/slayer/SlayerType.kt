package space.maxus.macrocosm.slayer

import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.slayer.wither.WitherSlayer
import space.maxus.macrocosm.slayer.zombie.ZombieSlayer
import space.maxus.macrocosm.util.general.id
import java.io.Serializable

enum class SlayerType(val slayer: Slayer) : Serializable {
    REVENANT_HORROR(ZombieSlayer),
    CINDERFLAME_SPIRIT(WitherSlayer),

    ;

    companion object {
        fun init() {
            Registry.SLAYER.delegateRegistration(values().map { id(it.slayer.id) to it.slayer })
        }
    }
}
