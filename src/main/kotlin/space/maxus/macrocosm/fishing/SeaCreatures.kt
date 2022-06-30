package space.maxus.macrocosm.fishing

import space.maxus.macrocosm.fishing.predicates.FishPredicates
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.util.generic.id

enum class SeaCreatures(val creature: SeaCreature) {
    SQUID(
        SeaCreature(
            "A squid emerges from the water.",
            id("minecraft", "squid"),
            1,
            FishPredicates.IN_WATER.build(),
            .6
        )
    ),

    LAVA_SQUID(
        SeaCreature(
            "A lava squid rises from the lava!",
            id("lava_squid"),
            17,
            FishPredicates.IN_LAVA.build(),
            .4
        )
    ),

    RADIOACTIVE_SQUID(
        SeaCreature(
            "A melting <dark_green>Radioactive Kraken<green> rises from the depths!",
            id("radioactive_squid"),
            23,
            FishPredicates.IN_LAVA.or { p, _, _ -> java.util.concurrent.TimeUnit.MILLISECONDS.toHours(p.playtimeMillis()) >= 12 }.build(),
            .05
        )
    ),

    SEA_WALKER(
        SeaCreature(
            "A sea walker appears from the water.",
            id("sea_walker"),
            2,
            FishPredicates.IN_WATER.build(),
            .6
        )
    ),

    SEA_ARCHER(
        SeaCreature(
            "A sea archer appears from the water.",
            id("sea_archer"),
            3,
            FishPredicates.IN_WATER.build(),
            .8
        )
    )

    ;

    companion object {
        fun init() {
            Registry.SEA_CREATURE.delegateRegistration(values().map { id(it.name.lowercase()) to it.creature })
        }
    }
}
