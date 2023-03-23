package space.maxus.macrocosm.npc

import me.libraryaddict.disguise.DisguiseAPI
import me.libraryaddict.disguise.disguisetypes.PlayerDisguise
import net.axay.kspigot.extensions.bukkit.kill
import net.axay.kspigot.runnables.task
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_19_R3.entity.CraftEntity
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.async.Threading
import space.maxus.macrocosm.data.level.LevelDbAdapter
import space.maxus.macrocosm.entity.EntityBase
import space.maxus.macrocosm.entity.nameMm
import space.maxus.macrocosm.npc.nms.NPCEntity
import space.maxus.macrocosm.npc.ops.NPCOperationData
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.util.general.getId
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * A LevelDB adapter for NPC storage
 */
object NPCLevelDbAdapter : LevelDbAdapter("NPC"), Listener {
    private val npcs: ConcurrentHashMap<UUID, NPCInstance> = ConcurrentHashMap()
    private val playerInDialogue: ConcurrentLinkedQueue<UUID> = ConcurrentLinkedQueue()

    override fun save(to: CompoundTag) {
        val list = ListTag()
        for (npc in npcs) {
            val base = Registry.NPC.find(npc.value.kind)
            if (base.isTemporary)
                continue
            list.add(npc.value.save())
        }
        to.put("value", list)
    }

    override fun load(from: CompoundTag) {
        task(delay = 1L) {
            // Need this to be synchronous environment
            val values = from.getList("value", CompoundTag.TAG_COMPOUND.toInt())
            for (tag in values) {
                if (!Registry.NPC.has((tag as CompoundTag).getId("Kind")))
                    continue
                val instance = NPCInstance.read(tag)
                val uuid = instance.summon()
                npcs[uuid] = instance
            }
        }
    }

    /**
     * Adds an NPC to world
     */
    fun addNpc(instance: NPCInstance, id: UUID) {
        this.npcs[id] = instance
    }

    /**
     * Saves all NPCs
     */
    fun close() {
        for (npc in this.npcs) {
            val entity = Bukkit.getEntity(npc.key) as? LivingEntity ?: continue
            entity.kill()
            val npcEntity = (entity as CraftEntity).handle as NPCEntity
            (Bukkit.getEntity(npcEntity.standId) as? ArmorStand)?.kill()
        }
    }

    @EventHandler
    fun playerWorldChange(e: PlayerTeleportEvent) {
        if (e.from.world.uid == e.to.world.uid)
            return
        for (npc in npcs) {
            val entity = Registry.ENTITY.find(npc.value.kind) as EntityBase
            val skin = Registry.DISGUISE.find(npc.value.kind)
            val disguise = PlayerDisguise(nameMm((entity.buildName())), skin)
            if (entity.disguiseProfile != null) {
                disguise.gameProfile = entity.disguiseProfile
            }

            DisguiseAPI.disguiseEntity(e.player, Bukkit.getEntity(npc.key), disguise)
        }
    }

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        for (npc in npcs) {
            val entity = Registry.ENTITY.find(npc.value.kind) as EntityBase
            val skin = Registry.DISGUISE.find(npc.value.kind)
            val disguise = PlayerDisguise(nameMm((entity.buildName())), skin)
            if (entity.disguiseProfile != null) {
                disguise.gameProfile = entity.disguiseProfile
            }

            DisguiseAPI.disguiseEntity(e.player, Bukkit.getEntity(npc.key), disguise)
        }
    }

    private val dialogueThread = Threading.newFixedPool(8)

    @EventHandler
    fun onPlayerEntityInteract(e: PlayerInteractEntityEvent) {
        if (e.hand != EquipmentSlot.HAND)
            return
        val handle = (e.rightClicked as CraftEntity).handle
        if (handle !is NPCEntity || playerInDialogue.contains(e.player.uniqueId))
            return
        playerInDialogue.add(e.player.uniqueId)
        val id = handle.id
        val npc = Registry.NPC.find(id)
        dialogueThread.execute {
            npc.executeOperations(NPCOperationData(e.player.macrocosm ?: return@execute, e.player, npc, e.rightClicked))
            playerInDialogue.remove(e.player.uniqueId)
        }
    }
}
