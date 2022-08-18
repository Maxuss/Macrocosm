package space.maxus.macrocosm.fishing

import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.util.general.id

enum class Treasures(val treasure: FishingTreasure) {
//    TEST_TREASURE(
//        FishingTreasure(
//            id("minecraft", "diamond"),
//            0,
//            Predicates.alwaysTrue(),
//            1.0
//        )
//    )
    ;

    companion object {
        fun init() {
            Registry.FISHING_TREASURE.delegateRegistration(values().map { id(it.name.lowercase()) to it.treasure })
        }
    }

}
