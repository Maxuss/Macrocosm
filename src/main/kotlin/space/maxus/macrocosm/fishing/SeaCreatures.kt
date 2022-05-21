package space.maxus.macrocosm.fishing

import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.util.id

enum class SeaCreatures(val creature: SeaCreature) {
//    EXAMPLE_CREATURE(SeaCreature("An example creature emerges from the water, lmao.", id("test_entity"), 0, Predicates.alwaysTrue(), 0.9))

    ;

    companion object {
        fun init() {
            Registry.SEA_CREATURE.delegateRegistration(values().map { id(it.name.lowercase()) to it.creature })
        }
    }
}
