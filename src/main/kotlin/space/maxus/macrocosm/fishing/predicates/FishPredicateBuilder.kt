package space.maxus.macrocosm.fishing.predicates

import org.bukkit.World.Environment
import org.bukkit.entity.FishHook
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.area.Area

class FishPredicateBuilder(val baseConditions: (MacrocosmPlayer, Area, FishHook) -> Boolean) {
    inline fun and(crossinline v: (MacrocosmPlayer, Area, FishHook) -> Boolean) =
        FishPredicateBuilder { p, z, h -> baseConditions(p, z, h) && v(p, z, h) }

    inline fun or(crossinline v: (MacrocosmPlayer, Area, FishHook) -> Boolean) =
        FishPredicateBuilder { p, z, h -> baseConditions(p, z, h) || v(p, z, h) }

    fun below(y: Int) = and { _, _, h -> h.location.y <= y }
    fun inDimension(d: Environment) = and { _, _, h -> h.world.environment == d }
    fun inLava() = and { _, _, h -> h.isInLava }

    fun build() = FishPredicate(baseConditions)
}
