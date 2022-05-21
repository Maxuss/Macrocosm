package space.maxus.macrocosm.fishing

import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.item.ItemRegistry
import space.maxus.macrocosm.util.id
import java.util.concurrent.TimeUnit

enum class TrophyFishes(val fish: TrophyFish) {
//    TESTFIN(TrophyFish(
//        "Testfin",
//        "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGM0ZDQ0YTBmZmUwMjM0OWU5OWRhMDYyOTIxMzA2MzExM2U2YmIzYWZjMjU5ZjQ2NjE4YzkwZWRjZTgzMDc4NiJ9fX0=",
//        CatchConditions("Catched by doing pretty <red>sussy<gray> stuff.", Predicates.alwaysTrue(), 1.0f),
//        Rarity.SPECIAL
//    ))

    ;
    companion object {
        fun init() {
            Threading.start("Trophy Fish Daemon") {
                info("Starting trophy fish daemon")

                val pool = Threading.pool()
                for(fish in values()) {
                    pool.execute {
                        val id = id(fish.name.lowercase())
                        FishingRegistry.register(id, fish.fish)
                        ItemRegistry.register(id, fish.fish)
                    }
                }

                pool.shutdown()
                val success = pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS)
                if (!success)
                    throw IllegalStateException("Could not execute all tasks in the thread pool!")

                info("Successfully registered ${values().size} trophy fishes")
            }
        }
    }

}
