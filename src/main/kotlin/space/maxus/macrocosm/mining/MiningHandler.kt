package space.maxus.macrocosm.mining

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import net.axay.kspigot.extensions.events.clickedBlockExceptAir
import net.axay.kspigot.runnables.task
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Barrel
import org.bukkit.block.Block
import org.bukkit.block.Chest
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.metadata.LazyMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.events.*
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.loot.LootPool
import space.maxus.macrocosm.loot.vanilla
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.skills.SkillType
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.floor
import kotlin.math.roundToInt

private fun skillExpFromBlock(block: Block): Pair<Int, SkillType>? {
    val meta = block.getMetadata("SKILL_EXP").firstOrNull()?.asInt()
    val exp = meta ?: when (block.type) {
        // mining
        Material.STONE, Material.COBBLESTONE -> return Pair(5, SkillType.MINING)
        Material.COPPER_ORE, Material.COAL_ORE, Material.DEEPSLATE -> return Pair(15, SkillType.MINING)
        Material.IRON_ORE, Material.NETHER_GOLD_ORE, Material.REDSTONE_ORE, Material.DEEPSLATE_COPPER_ORE, Material.DEEPSLATE_COAL_ORE -> return Pair(
            25,
            SkillType.MINING
        )
        Material.GOLD_ORE, Material.DEEPSLATE_IRON_ORE, Material.DEEPSLATE_REDSTONE_ORE -> return Pair(
            35,
            SkillType.MINING
        )
        Material.DIAMOND_ORE, Material.EMERALD_ORE, Material.DEEPSLATE_GOLD_ORE -> return Pair(50, SkillType.MINING)
        Material.DEEPSLATE_DIAMOND_ORE, Material.DEEPSLATE_EMERALD_ORE -> return Pair(80, SkillType.MINING)
        Material.OBSIDIAN -> return Pair(95, SkillType.MINING)
        Material.ANCIENT_DEBRIS -> return Pair(250, SkillType.MINING)

        // foraging
        Material.BIRCH_LOG, Material.OAK_LOG, Material.SPRUCE_LOG -> return Pair(30, SkillType.FORAGING)
        Material.ACACIA_LOG -> return Pair(40, SkillType.FORAGING)
        Material.DARK_OAK_LOG, Material.JUNGLE_LOG -> return Pair(50, SkillType.FORAGING)

        // farming
        Material.POTATOES, Material.CARROTS -> return Pair(10, SkillType.FARMING)
        Material.SUGAR_CANE, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM -> return Pair(25, SkillType.FARMING)
        Material.MELON -> return Pair(40, SkillType.FARMING)
        Material.PUMPKIN -> return Pair(50, SkillType.FARMING)
        Material.NETHER_WART -> return Pair(60, SkillType.FARMING)

        // excavating
        Material.DIRT, Material.PODZOL, Material.NETHERRACK -> return Pair(5, SkillType.EXCAVATING)
        Material.SAND, Material.GRAVEL, Material.CLAY -> return Pair(10, SkillType.EXCAVATING)
        Material.SOUL_SAND -> return Pair(25, SkillType.EXCAVATING)
        else -> return null
    }
    return Pair(
        exp, SkillType.valueOf(
            block.getMetadata("SKILL_TYPE").firstOrNull()?.asString() ?: when (block.type) {
                // mining
                Material.STONE, Material.COBBLESTONE -> return Pair(exp, SkillType.MINING)
                Material.COPPER_ORE, Material.COAL_ORE, Material.DEEPSLATE -> return Pair(exp, SkillType.MINING)
                Material.IRON_ORE, Material.NETHER_GOLD_ORE, Material.REDSTONE_ORE, Material.DEEPSLATE_COPPER_ORE, Material.DEEPSLATE_COAL_ORE -> return Pair(
                    exp,
                    SkillType.MINING
                )
                Material.GOLD_ORE, Material.DEEPSLATE_IRON_ORE, Material.DEEPSLATE_REDSTONE_ORE -> return Pair(
                    exp,
                    SkillType.MINING
                )
                Material.DIAMOND_ORE, Material.EMERALD_ORE, Material.DEEPSLATE_GOLD_ORE -> return Pair(
                    exp,
                    SkillType.MINING
                )
                Material.DEEPSLATE_DIAMOND_ORE, Material.DEEPSLATE_EMERALD_ORE -> return Pair(exp, SkillType.MINING)
                Material.OBSIDIAN -> return Pair(exp, SkillType.MINING)
                Material.ANCIENT_DEBRIS -> return Pair(exp, SkillType.MINING)

                // foraging
                Material.BIRCH_LOG, Material.OAK_LOG, Material.SPRUCE_LOG -> return Pair(exp, SkillType.FORAGING)
                Material.ACACIA_LOG -> return Pair(exp, SkillType.FORAGING)
                Material.DARK_OAK_LOG, Material.JUNGLE_LOG -> return Pair(exp, SkillType.FORAGING)

                // farming
                Material.POTATOES, Material.CARROTS -> return Pair(exp, SkillType.FARMING)
                Material.SUGAR_CANE, Material.BROWN_MUSHROOM, Material.RED_MUSHROOM -> return Pair(
                    exp,
                    SkillType.FARMING
                )
                Material.MELON -> return Pair(exp, SkillType.FARMING)
                Material.PUMPKIN -> return Pair(exp, SkillType.FARMING)
                Material.NETHER_WART -> return Pair(exp, SkillType.FARMING)

                // excavating
                Material.DIRT, Material.PODZOL, Material.NETHERRACK -> return Pair(exp, SkillType.EXCAVATING)
                Material.SAND, Material.GRAVEL, Material.CLAY -> return Pair(exp, SkillType.EXCAVATING)
                Material.SOUL_SAND -> return Pair(exp, SkillType.EXCAVATING)
                else -> return null
            }
        )
    )
}

private fun blockHardness(block: Block): Int {
    val meta = block.getMetadata("BLOCK_HARDNESS").firstOrNull()?.asInt()
    if (meta != null) {
        return meta
    }
    val ty = block.type
    if (ty.name.contains("DEEPSLATE") && !ty.name.contains("ORE"))
        return 600
    if (ty.name.contains("LOG") || ty.name.contains("WOOD") || ty.name.contains("STEM"))
        return 250
    return when (ty) {
        Material.NETHERRACK, Material.WARPED_NYLIUM, Material.CRIMSON_NYLIUM -> 150
        Material.DIRT, Material.GRASS_BLOCK, Material.SAND, Material.GRAVEL, Material.CLAY, Material.PODZOL -> 250
        Material.COBBLESTONE, Material.ANDESITE, Material.DIORITE, Material.GRANITE, Material.MYCELIUM -> 260
        Material.STONE, Material.COAL_ORE, Material.NETHER_WART -> 300
        Material.IRON_ORE, Material.COPPER_ORE, Material.CRYING_OBSIDIAN, Material.SOUL_SAND -> 410
        Material.REDSTONE_ORE, Material.EMERALD_ORE -> 500
        Material.DIAMOND_ORE, Material.DEEPSLATE_COAL_ORE, Material.DEEPSLATE_COPPER_ORE -> 600
        Material.DEEPSLATE_IRON_ORE, Material.DEEPSLATE_EMERALD_ORE, Material.DEEPSLATE_REDSTONE_ORE -> 650
        Material.DEEPSLATE_DIAMOND_ORE, Material.OBSIDIAN, Material.ANCIENT_DEBRIS -> 700
        else -> 100
    }
}

private fun breakingPowerRequired(block: Block): Int {
    val meta = block.getMetadata("BP_REQUIREMENT").firstOrNull()?.asInt()
    if (meta != null) {
        return meta
    }
    val ty = block.type
    if (ty.name.contains("DEEPSLATE") && !ty.name.contains("ORE"))
        return 3
    return when (ty) {
        Material.STONE, Material.COBBLESTONE, Material.NETHERRACK, Material.ANDESITE, Material.GRANITE, Material.DIORITE, Material.ACACIA_LOG -> 1
        Material.COAL_ORE, Material.COPPER_ORE, Material.IRON_ORE, Material.DARK_OAK_LOG, Material.JUNGLE_LOG, Material.SOUL_SAND, Material.NETHER_WART -> 2
        Material.REDSTONE_ORE, Material.DIAMOND_ORE, Material.DEEPSLATE_COPPER_ORE, Material.DEEPSLATE_COAL_ORE, Material.CRIMSON_STEM, Material.WARPED_STEM -> 3
        Material.EMERALD_ORE, Material.DEEPSLATE_REDSTONE_ORE -> 4
        Material.ANCIENT_DEBRIS, Material.DEEPSLATE_EMERALD_ORE, Material.DEEPSLATE_DIAMOND_ORE -> 5
        else -> 0
    }
}

private val stones = listOf("ANDESITE", "DIORITE", "GRANITE", "NETHERRACK")
private fun suitingTypes(block: Block): List<ItemType> {
    val ty = block.type
    if (ty.name.contains("LOG") || ty.name.contains("WOOD") || ty.name.contains("STEM"))
        return listOf(ItemType.AXE, ItemType.GAUNTLET)
    if (ty.name.contains("STONE") || ty.name.contains("DEBRIS") || ty.name.contains("DEEP") || ty.name.contains("ORE") || stones.any {
            ty.name.contains(
                it
            )
        })
        return listOf(ItemType.PICKAXE, ItemType.GAUNTLET, ItemType.DRILL)
    if (ty.name.contains("WART") || ty.name.contains("HAY"))
        return listOf(ItemType.HOE, ItemType.GAUNTLET)
    if (ty.name.contains("LEAVES"))
        return listOf(ItemType.SWORD, ItemType.GAUNTLET, ItemType.OTHER)
    return listOf(ItemType.SHOVEL, ItemType.GAUNTLET, ItemType.OTHER)
}

object MiningHandler : PacketAdapter(Macrocosm, ListenerPriority.NORMAL, PacketType.Play.Client.ARM_ANIMATION),
    Listener {
    /**
     * Blocks that were last broken by player
     */
    private val breakingNow: ConcurrentHashMap<UUID, Location> = ConcurrentHashMap(hashMapOf())

    @EventHandler
    fun onStartBreak(e: PlayerInteractEvent) {
        val block = e.clickedBlockExceptAir
        val rightClick = e.action == Action.LEFT_CLICK_BLOCK
        if (block == null && !rightClick)
            return

        // preparing block to be mined
        block!!.setMetadata("BREAKING_STATE", LazyMetadataValue(Macrocosm) { .0f })
        block.setMetadata("_BREAKING_ID", LazyMetadataValue(Macrocosm) { kotlin.random.Random.nextInt(2000) })

        // adding haste effect to player
        e.player.addPotionEffect(PotionEffect(PotionEffectType.SLOW_DIGGING, Int.MAX_VALUE, -1))
    }

    @EventHandler
    fun onBlockPlace(e: BlockPlaceEvent) {
        e.block.setMetadata("_PLAYER_PLACED", FixedMetadataValue(Macrocosm, 0))
    }

    @EventHandler
    fun onBreakBlock(e: PlayerBreakBlockEvent) {
        val mc = e.player
        var pool =
            LootPool.of(*e.block.drops.map { vanilla(it.type, 1.0, amount = it.amount..it.amount) }.toTypedArray())
        val items = if (!e.block.hasMetadata("_PLAYER_PLACED")) {
            val event = BlockDropItemsEvent(mc, e.block, pool)
            event.callEvent()
            pool = event.pool
            val pair = skillExpFromBlock(e.block) ?: return
            val (exp, type) = pair
            val expEvent = PlayerReceiveExpEvent(mc, type, exp)
            if (!expEvent.callEvent())
                pool.roll(mc, true)
            else {
                e.player.addSkillExperience(expEvent.type, expEvent.amount.toDouble())
                pool.roll(mc, true)
            }
        } else pool.roll(mc, false)

        for (item in items) {
            e.block.world.dropItemNaturally(e.block.location, item ?: continue)
        }
    }

    @EventHandler
    fun onMineTick(e: MineTickEvent) {
        val breaking = e.block
        val hardness = blockHardness(breaking)
        val bp = breakingPowerRequired(breaking)
        val item = e.player.mainHand
        if (item == null && bp > 0)
            return
        val itemBp = item?.breakingPower ?: 0
        if (itemBp < bp)
            return
        val suiting = suitingTypes(breaking)
        if (item != null && !suiting.contains(item.type))
            return

        val bId = breaking.getMetadata("_BREAKING_ID").firstOrNull()?.asInt() ?: kotlin.random.Random.nextInt()

        var state = breaking.getMetadata("BREAKING_STATE").firstOrNull()?.asFloat() ?: return
        // amount of damage applied to block on each tick
        val minePerTick = e.player.stats()!!.miningSpeed / (12f * hardness)
        state += minePerTick
        if (state >= 1f) {
            // checking if item is a container
            if (breaking is Chest || breaking is Barrel) {
                for (drop in breaking.drops) {
                    val loc = breaking.location
                    loc.world.dropItemNaturally(loc, drop)
                }
            }

            val event = PlayerBreakBlockEvent(e.player, e.block)
            if (event.callEvent()) {
                val pos = BlockPos(breaking.x, breaking.y, breaking.z)
                // resetting metadata, because its persistent even if the new block is placed for some reason
                breaking.removeMetadata("BREAKING_STATE", Macrocosm)
                breaking.removeMetadata("_BREAKING_ID", Macrocosm)
                // sending packet anyway, even if we destroyed block
                // because client caches them, and next block placed will have that weird texture
                e.player.sendPacket(ClientboundBlockDestructionPacket(bId, pos, -1))
                // setting block to air, without dropping items, because
                // handlers of PlayerBreakBlockEvent should drop them
                breaking.type = Material.AIR
            } else {
                breaking.removeMetadata("BREAKING_STATE", Macrocosm)
                val packet = ClientboundBlockDestructionPacket(bId, BlockPos(breaking.x, breaking.y, breaking.z), -1)
                e.player.sendPacket(packet)
            }

            return
        } else {
            breaking.setMetadata("BREAKING_STATE", LazyMetadataValue(Macrocosm) { state })
            val packet = ClientboundBlockDestructionPacket(
                bId,
                BlockPos(breaking.x, breaking.y, breaking.z),
                floor(9 * state).roundToInt()
            )
            e.player.sendPacket(packet)
        }
    }

    @EventHandler
    fun onStopBreaking(e: StopBreakingBlockEvent) {
        val breaking = e.block
        if (breaking.type.isAir)
            return
        e.block.removeMetadata("BREAKING_STATE", Macrocosm)
        val bId = e.block.getMetadata("_BREAKING_ID").firstOrNull()?.asInt() ?: kotlin.random.Random.nextInt(2000)
        e.block.removeMetadata("_BREAKING_ID", Macrocosm)
        val packet = ClientboundBlockDestructionPacket(bId, BlockPos(breaking.x, breaking.y, breaking.z), -1)
        e.player.sendPacket(packet)
    }

    override fun onPacketReceiving(e: PacketEvent) {
        if (e.packetType == PacketType.Play.Client.ARM_ANIMATION) {
            val player = e.player
            val target = player.getTargetBlock(null, 5)

            if (target.type != Material.AIR) {
                val targetLoc = target.location
                val cachedLoc = breakingNow[player.uniqueId]
                if (cachedLoc != null && targetLoc != cachedLoc) {
                    task {
                        val event = StopBreakingBlockEvent(player.macrocosm!!, cachedLoc.block)
                        event.callEvent()
                    }
                    breakingNow[player.uniqueId] = targetLoc
                    return
                }
                // workaround to call event on main thread
                task {
                    val event = MineTickEvent(player.macrocosm!!, target)
                    event.callEvent()
                }
                breakingNow[player.uniqueId] = targetLoc
            }
        }
    }
}
