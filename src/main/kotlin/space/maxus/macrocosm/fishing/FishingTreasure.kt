package space.maxus.macrocosm.fishing

import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.util.math.Chance
import space.maxus.macrocosm.area.Area
import java.util.function.Predicate

data class FishingTreasure(
    val item: Identifier,
    val requiredLevel: Int,
    val predicate: Predicate<Pair<MacrocosmPlayer, Area>>,
    override val chance: Double
) : Chance
