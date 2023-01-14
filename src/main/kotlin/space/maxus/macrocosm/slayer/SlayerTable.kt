package space.maxus.macrocosm.slayer

import space.maxus.macrocosm.util.math.FixedLevelingTable

object SlayerTable : FixedLevelingTable(
    listOf(
        10.0,
        15.0,
        200.0,
        1000.0,
        5000.0,
        20_000.0,
        100_000.0,
        400_000.0,
        1_000_000.0
    )
)
