package space.maxus.macrocosm.fishing.predicates

import org.bukkit.World
import org.bukkit.entity.FishHook
import space.maxus.macrocosm.area.Area
import space.maxus.macrocosm.players.MacrocosmPlayer
import java.util.function.Predicate

object FishPredicates {
    val NONE = FishPredicateBuilder { _, _, _ -> true }

    val IN_WATER = FishPredicateBuilder { _, _, h -> h.isInWater }
    val IN_LAVA = FishPredicateBuilder { _, _, h -> h.isInLava }
    val NETHER = FishPredicateBuilder { _, _, h -> h.world.environment == World.Environment.NETHER }
    val END = FishPredicateBuilder { _, _, h -> h.world.environment == World.Environment.THE_END }

    fun not(predicate: FishPredicate): Predicate<Triple<MacrocosmPlayer, Area, FishHook>> = predicate.negate()
}
