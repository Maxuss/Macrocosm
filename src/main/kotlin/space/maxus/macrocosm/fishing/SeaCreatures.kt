package space.maxus.macrocosm.fishing

import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.util.id
import java.util.concurrent.TimeUnit

enum class SeaCreatures(val creature: SeaCreature) {
//    EXAMPLE_CREATURE(SeaCreature("An example creature emerges from the water, lmao.", id("test_entity"), 0, Predicates.alwaysTrue(), 0.9))

    ;

    companion object {
        fun init() {
            Threading.start("Sea Creature Daemon") {
                info("Starting sea creature daemon")

                val pool = Threading.pool()
                for(creature in values()) {
                    pool.execute {
                        val id = id(creature.name.lowercase())
                        FishingRegistry.register(id, creature.creature)
                    }
                }

                pool.shutdown()
                val success = pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
                if (!success)
                    throw IllegalStateException("Could not execute all tasks in the thread pool!")

                info("Successfully registered ${values().size} sea creatures")
            }
        }
    }
}
