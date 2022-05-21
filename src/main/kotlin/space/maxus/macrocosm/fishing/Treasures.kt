package space.maxus.macrocosm.fishing

import com.google.common.base.Predicates
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.util.id
import java.util.concurrent.TimeUnit

enum class Treasures(val treasure: FishingTreasure) {
    TEST_TREASURE(
        FishingTreasure(
            id("minecraft", "diamond"),
            0,
            Predicates.alwaysTrue(),
            1.0
        )
    )
    ;

    companion object {
        fun init() {
            Threading.start("Fishing Treasure Daemon") {
                info("Starting fishing treasure daemon")

                val pool = Threading.pool()
                for(treasure in values()) {
                    pool.execute {
                        val id = id(treasure.name.lowercase())
                        FishingRegistry.register(id, treasure.treasure)
                    }
                }

                pool.shutdown()
                val success = pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
                if (!success)
                    throw IllegalStateException("Could not execute all tasks in the thread pool!")

                info("Successfully registered ${values().size} treasures")
            }
        }
    }

}
