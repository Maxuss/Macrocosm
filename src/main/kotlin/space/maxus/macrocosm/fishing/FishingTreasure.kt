package space.maxus.macrocosm.fishing

import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.util.Chance
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.zone.Zone
import java.util.function.Predicate

data class FishingTreasure(val item: Identifier, val requiredLevel: Int, val predicate: Predicate<Pair<MacrocosmPlayer, Zone>>, override val chance: Double): Chance
