package space.maxus.macrocosm.fishing

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_18_R2.CraftWorld
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftItem
import org.bukkit.entity.EntityType
import org.bukkit.entity.FishHook
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.text.str
import space.maxus.macrocosm.util.Chance
import space.maxus.macrocosm.util.Pools
import space.maxus.macrocosm.util.summon
import kotlin.random.Random

data class FishingDrop<T>(
    val item: T,
    override val chance: Double
): Chance

data class TrophyTier(val name: String, val modifier: String, override val chance: Double): Chance

val DEFAULT_TROPHY_TIERS = listOf(
    TrophyTier("<#CD7F32>BRONZE", "bronze", .5),
    TrophyTier("<#C0C0C0>SILVER", "silver", .3),
    TrophyTier("<gold>GOLD", "gold", .15),
    TrophyTier("<aqua>DIAMOND", "diamond", .05)
)


class FishingPool(private val creatures: List<SeaCreature>, private val treasures: List<FishingTreasure>, private val trophies: List<TrophyFish>) {
    companion object {
        private val trash: List<Material> = listOf(
            Material.ROTTEN_FLESH, Material.LEATHER,
            Material.BONE, Material.COD,
            Material.SALMON, Material.TROPICAL_FISH,
            Material.PUFFERFISH, Material.STRING
            )
    }

    fun summonSeaCreature(creature: SeaCreature, player: MacrocosmPlayer, hook: FishHook) {
        player.sendMessage("<green>${creature.greeting}")
        val entity = Registry.ENTITY.find(creature.entity).spawn(hook.location)
        val diff = entity.location.toVector().subtract(player.paper!!.location.toVector()).normalize()
        entity.velocity = diff
    }

    fun announceTreasure(treasure: FishingTreasure, player: MacrocosmPlayer, hook: FishHook) {
        val item = Registry.ITEM.find(treasure.item).build(player)!!
        player.sendMessage("<bold><gold>TREASURE!</bold><green> You've caught ${item.displayName().str()}!")
        if(hook.hookedEntity == null) {
            val eI = createItem(item, hook.location)
            hook.hookedEntity = eI
            (hook.location.world as CraftWorld).handle.addFreshEntity((eI as CraftItem).handle)
            hook.pullHookedEntity()
        } else {
            val hooked = (hook.hookedEntity as Item)
            hooked.itemStack = item
        }
    }

    fun announceTrophy(trophy: TrophyFish, player: MacrocosmPlayer, hook: FishHook) {
        val item = trophy.build(player)!!
        player.sendMessage("<bold><gold>TROPHY!</bold><green> You've caught ${trophy.name.str()}!")
        if(hook.hookedEntity == null) {
            val eI = createItem(item, hook.location)
            hook.hookedEntity = eI
            (hook.location.world as CraftWorld).handle.addFreshEntity((eI as CraftItem).handle)
            hook.pullHookedEntity()
        } else {
            val hooked = (hook.hookedEntity as Item)
            hooked.itemStack = item
        }
    }

    fun roll(player: MacrocosmPlayer, hook: FishHook) {
        val stats = player.stats()!!

        println("ROLLING")
        val shouldCatchEntity = Random.nextFloat() <= stats.seaCreatureChance / 100f
        println("ENTITY: $shouldCatchEntity")

        if(shouldCatchEntity) {
            val creature = Pools.roll(creatures, stats.magicFind).randomOrNull()
            if(creature != null) {
                summonSeaCreature(creature, player, hook)
                return
            }
        }
        val shouldCatchTreasure = Random.nextFloat() <= stats.treasureChance / 100f
        println("TREASURE: $shouldCatchTreasure")
        if(shouldCatchTreasure) {
            val treasure = Pools.roll(treasures, stats.magicFind).randomOrNull()
            if(treasure != null) {
                announceTreasure(treasure, player, hook)
                return
            }
        }
        // constant chance to catch trophies: 0.34
        val shouldCatchTrophy = Random.nextFloat() <= .34f
        println("TROPHY: $shouldCatchTreasure")
        if(shouldCatchTrophy) {
            val trophy = Pools.roll(trophies, stats.magicFind).randomOrNull()
            if(trophy != null) {
                val tier = Pools.roll(DEFAULT_TROPHY_TIERS, stats.magicFind).lastOrNull() ?: DEFAULT_TROPHY_TIERS[0]
                trophy.tier = tier
                announceTrophy(trophy, player, hook)
                return
            }
        }

        // rolling trash instead
        if(hook.hookedEntity == null) {
            val eI = createItem(ItemStack(trash.random(), (1..3).random()).macrocosm!!.build(player)!!, hook.location)
            hook.hookedEntity = eI
            (hook.location.world as CraftWorld).handle.addFreshEntity((eI as CraftItem).handle)
            hook.pullHookedEntity()
        }
    }
}

private fun createItem(item: ItemStack, pos: Location): Item {
    val i = EntityType.DROPPED_ITEM.summon(pos) as Item
    i.itemStack = item
    return i
}
