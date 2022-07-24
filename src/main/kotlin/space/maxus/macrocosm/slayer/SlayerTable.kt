package space.maxus.macrocosm.slayer

import space.maxus.macrocosm.util.math.FixedLevelingTable

object SlayerTable : FixedLevelingTable(
    listOf(
        10.0,
        25.0,
        150.0,
        1000.0,
        4000.0,
        20_000.0,
        50_000.0,
        150_000.0,
        300_000.0
    )
)
