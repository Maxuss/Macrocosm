package space.maxus.macrocosm.mining

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.ListenerPriority
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketEvent
import net.axay.kspigot.extensions.events.clickedBlockExceptAir
import net.axay.kspigot.runnables.task
import net.minecraft.core.BlockPos
import net.minecraft.network.protocol.game.ClientboundBlockDestructionPacket
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.events.MineTickEvent
import space.maxus.macrocosm.events.PlayerBreakBlock
import space.maxus.macrocosm.item.ItemType
import space.maxus.macrocosm.players.macrocosm
import kotlin.math.floor
import kotlin.math.roundToInt

fun blockHardness(block: Block): Int {
    val meta = block.getMetadata("BLOCK_HARDNESS").firstOrNull()?.asInt()
    if(meta != null) {
        return meta
    }
    val ty = block.type
    if(ty.name.contains("DEEPSLATE") && !ty.name.contains("ORE"))
        return 600
    if(ty.name.contains("LOG") || ty.name.contains("WOOD") || ty.name.contains("STEM"))
        return 250
    return when(ty) {
        Material.NETHERRACK, Material.WARPED_NYLIUM, Material.CRIMSON_NYLIUM -> 150
        Material.DIRT, Material.GRASS_BLOCK, Material.SAND, Material.GRAVEL, Material.CLAY, Material.PODZOL -> 250
        Material.COBBLESTONE, Material.ANDESITE, Material.DIORITE, Material.GRANITE -> 260
        Material.STONE, Material.COAL_ORE -> 300
        Material.IRON_ORE, Material.COPPER_ORE, Material.CRYING_OBSIDIAN -> 410
        Material.REDSTONE_ORE, Material.EMERALD_ORE -> 500
        Material.DIAMOND_ORE, Material.DEEPSLATE_COAL_ORE, Material.DEEPSLATE_COPPER_ORE -> 600
        Material.DEEPSLATE_IRON_ORE, Material.DEEPSLATE_EMERALD_ORE, Material.DEEPSLATE_REDSTONE_ORE -> 650
        Material.DEEPSLATE_DIAMOND_ORE, Material.OBSIDIAN, Material.ANCIENT_DEBRIS -> 700
        else -> 100
    }
}

fun breakingPowerRequired(block: Block): Int {
    val meta = block.getMetadata("BP_REQUIREMENT").firstOrNull()?.asInt()
    if(meta != null) {
        return meta
    }
    val ty = block.type
    if(ty.name.contains("DEEPSLATE") && !ty.name.contains("ORE"))
        return 3
    return when(ty) {
        Material.STONE, Material.COBBLESTONE, Material.NETHERRACK, Material.ANDESITE, Material.GRANITE, Material.DIORITE -> 1
        Material.COAL_ORE, Material.COPPER_ORE, Material.IRON_ORE -> 2
        Material.REDSTONE_ORE, Material.DIAMOND_ORE, Material.DEEPSLATE_COPPER_ORE, Material.DEEPSLATE_COAL_ORE -> 3
        Material.EMERALD_ORE, Material.DEEPSLATE_REDSTONE_ORE -> 4
        Material.ANCIENT_DEBRIS, Material.DEEPSLATE_EMERALD_ORE, Material.DEEPSLATE_DIAMOND_ORE -> 5
        else -> 0
    }
}

private val stones = listOf("ANDESITE", "DIORITE", "GRANITE", "NETHERRACK")
fun suitingTypes(block: Block): List<ItemType> {
    val ty = block.type
    if(ty.name.contains("LOG") || ty.name.contains("WOOD"))
        return listOf(ItemType.AXE, ItemType.GAUNTLET)
    if(ty.name.contains("STONE") || ty.name.contains("DEBRIS") || ty.name.contains("ORE") || stones.any { ty.name.contains(it) })
        return listOf(ItemType.PICKAXE, ItemType.GAUNTLET)
    if(ty.name.contains("WART") || ty.name.contains("HAY"))
        return listOf(ItemType.HOE, ItemType.GAUNTLET)
    if(ty.name.contains("LEAVES"))
        return listOf(ItemType.SWORD, ItemType.GAUNTLET, ItemType.OTHER)
    return listOf(ItemType.SHOVEL, ItemType.GAUNTLET, ItemType.OTHER)
}

object MiningHandler: PacketAdapter(Macrocosm, ListenerPriority.NORMAL, PacketType.Play.Client.ARM_ANIMATION), Listener {
    @EventHandler
    fun onStartBreak(e: PlayerInteractEvent) {
        val block = e.clickedBlockExceptAir
        val rightClick = e.action == Action.LEFT_CLICK_BLOCK
        if(block == null && !rightClick)
            return

        block!!.setMetadata("BREAKING_STATE", FixedMetadataValue(Macrocosm, 0.0))
        e.player.addPotionEffect(PotionEffect(PotionEffectType.SLOW_DIGGING, Int.MAX_VALUE, -1))
    }

    @EventHandler
    fun onMineTick(e: MineTickEvent) {
        val breaking = e.block
        val hardness = blockHardness(breaking)
        val bp = breakingPowerRequired(breaking)
        val item = e.player.mainHand
        if(item == null && bp > 0)
            return
        val itemBp = item?.breakingPower ?: 0
        if(itemBp < bp)
            return
        val suiting = suitingTypes(breaking)
        if(item != null && !suiting.contains(item.type))
            return

        var state = breaking.getMetadata("BREAKING_STATE").firstOrNull()?.asFloat() ?: return
        val minePerTick = e.player.stats()!!.miningSpeed / (13f * hardness)
        state += minePerTick
        if(state >= 1f) {
            val event = PlayerBreakBlock(e.player, e.block)
            if(event.callEvent()) {
                breaking.breakNaturally()
                return
            } else {
                breaking.setMetadata("BREAKING_STATE", FixedMetadataValue(Macrocosm, .0f))
                val packet = ClientboundBlockDestructionPacket(kotlin.random.Random.nextInt(), BlockPos(breaking.x, breaking.y, breaking.z), 0)
                e.player.sendPacket(packet)
                return
            }
        } else {
            breaking.setMetadata("BREAKING_STATE", FixedMetadataValue(Macrocosm, state))
            val packet = ClientboundBlockDestructionPacket(kotlin.random.Random.nextInt(), BlockPos(breaking.x, breaking.y, breaking.z), floor(9 * state).roundToInt())
            e.player.sendPacket(packet)
        }
    }

    override fun onPacketReceiving(e: PacketEvent) {
        if (e.packetType == PacketType.Play.Client.ARM_ANIMATION) {
            val player = e.player
            val target = player.getTargetBlock(null, 4)

            if (target.type != Material.AIR) {
                // workaround to call event on main thread
                task {
                    val event = MineTickEvent(player.macrocosm!!, target)
                    event.callEvent()
                }
            }
        }
    }
}
