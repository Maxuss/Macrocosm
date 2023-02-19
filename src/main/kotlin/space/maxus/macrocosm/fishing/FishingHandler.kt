package space.maxus.macrocosm.fishing

import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.FishHook
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerFishEvent
import space.maxus.macrocosm.area.Areas
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.skills.SkillType

object FishingHandler : Listener {
    @EventHandler
    fun onPullHook(e: PlayerFishEvent) {
        if (e.state != PlayerFishEvent.State.CAUGHT_FISH)
            return
        possibleRewards(e.player.location, e.player.macrocosm!!, e.hook).roll(e.player.macrocosm!!, e.hook)
    }

    @EventHandler
    fun onHookInLava(e: EntityDeathEvent) {
        if (e.entityType != EntityType.FISHING_HOOK)
            return
        e.isCancelled = true
    }

    private fun possibleRewards(location: Location, player: MacrocosmPlayer, hook: FishHook): FishingPool {
        val zones = Areas.matching(location).values
        val level = player.skills.level(SkillType.FISHING)
        val tuple: Triple<List<SeaCreature>, List<TrophyFish>, List<FishingTreasure>> = Triple(
            Registry.SEA_CREATURE.iter().values.filter { sc ->
                zones.any { zone -> sc.requiredLevel <= level && sc.predicate.test(Triple(player, zone, hook)) }
            },
            Registry.TROPHY_FISH.iter().values.filter { trophy ->
                zones.any { zone -> trophy.conditions.predicate.test(Pair(player, zone)) }
            },
            Registry.FISHING_TREASURE.iter().values.filter { treasure ->
                zones.any { zone -> treasure.predicate.test(Pair(player, zone)) }
            }
        )
        return FishingPool(tuple.first, tuple.third, tuple.second)
    }
}
