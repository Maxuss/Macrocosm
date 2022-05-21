package space.maxus.macrocosm.fishing

import org.bukkit.Location
import space.maxus.macrocosm.entity.EntityRegistry
import space.maxus.macrocosm.item.ItemRegistry
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.skills.SkillType
import space.maxus.macrocosm.util.Identifier
import space.maxus.macrocosm.zone.ZoneRegistry
import java.util.concurrent.ConcurrentHashMap

object FishingRegistry {
    private val creatures: ConcurrentHashMap<Identifier, SeaCreature> = ConcurrentHashMap()
    private val trophies: ConcurrentHashMap<Identifier, TrophyFish> = ConcurrentHashMap()
    private val treasures: ConcurrentHashMap<Identifier, FishingTreasure> = ConcurrentHashMap(hashMapOf())

    fun register(id: Identifier, creature: SeaCreature): SeaCreature {
        if(creatures.contains(id))
            return creature
        creatures[id] = creature
        return creature
    }

    fun register(id: Identifier, trophy: TrophyFish): MacrocosmItem {
        if(trophies.contains(id))
            return trophy
        trophies[id] = trophy
        return trophy
    }

    fun register(id: Identifier, treasure: FishingTreasure): FishingTreasure {
        if(treasures.contains(id))
            return treasure
        treasures[id] = treasure
        return treasure
    }

    fun findCreature(id: Identifier) = EntityRegistry.find(id)
    fun findTrophy(id: Identifier) = ItemRegistry.find(id)
    fun findTreasure(id: Identifier) = treasures[id]!!

    fun possibleRewards(location: Location, player: MacrocosmPlayer): FishingPool {
        val zones = ZoneRegistry.matching(location).values
        val level = player.skills.level(SkillType.FISHING)
        val tuple: Triple<List<SeaCreature>, List<TrophyFish>, List<FishingTreasure>> = Triple(
            creatures.values.filter { sc ->
                zones.any { zone -> sc.requiredLevel <= level && sc.predicate.test(Pair(player, zone)) }
            },
            trophies.values.filter { trophy ->
                zones.any { zone -> trophy.conditions.predicate.test(Pair(player, zone)) }
            },
            treasures.values.filter { treasure ->
                zones.any { zone -> treasure.predicate.test(Pair(player, zone)) }
            }
        )
        return FishingPool(tuple.first, tuple.third, tuple.second)
    }
}
