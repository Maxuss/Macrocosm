package space.maxus.macrocosm.fishing

import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.util.general.id

enum class Treasures(val treasure: FishingTreasure) {

    ;

    companion object {
        fun init() {
            Registry.FISHING_TREASURE.delegateRegistration(values().map { id(it.name.lowercase()) to it.treasure })
        }
    }

}
