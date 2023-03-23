package space.maxus.macrocosm.npc

import com.comphenix.protocol.wrappers.WrappedGameProfile
import net.axay.kspigot.extensions.geometry.vec
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.craftbukkit.v1_19_R3.CraftWorld
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.persistence.PersistentDataType
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.entity.EntityBase
import space.maxus.macrocosm.entity.MacrocosmEntity
import space.maxus.macrocosm.item.MacrocosmItem
import space.maxus.macrocosm.loot.LootPool
import space.maxus.macrocosm.nms.NativeMacrocosmEntity
import space.maxus.macrocosm.npc.nms.NPCEntity
import space.maxus.macrocosm.npc.ops.NPCOp
import space.maxus.macrocosm.npc.ops.NPCOperationData
import space.maxus.macrocosm.registry.AutoRegister
import space.maxus.macrocosm.registry.Identified
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.stats.SpecialStatistics
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.text.text

/**
 * A Macrocosm NPC with a player skin
 */
open class MacrocosmNPC(
    /**
     * Name of this NPC
     */
    val name: String,
    /**
     * ID of this NPC
     */
    id: String,
    /**
     * Profile texture for this NPC
     */
    private val profile: WrappedGameProfile,
    /**
     * Dialogue operations for this NPC
     */
    private val operations: List<NPCOp>,
    private var mainHand: MacrocosmItem? = null,
    private var offHand: MacrocosmItem? = null,
    private var helmet: MacrocosmItem? = null,
    private var chestplate: MacrocosmItem? = null,
    private var leggings: MacrocosmItem? = null,
    private var boots: MacrocosmItem? = null,
    /**
     * Whether this NPC should not be saved in LevelDB
     */
    val isTemporary: Boolean = false
) : Identified, AutoRegister<MacrocosmEntity> {
    override val id: Identifier = Identifier.parse(id)

    /**
     * Executes all dialogue operations for this entity
     */
    fun executeOperations(data: NPCOperationData) {
        for (op in operations) {
            // Waiting for completion
            op.operate(data).get()
        }
    }

    /**
     * Summons this NPC at provided location
     */
    fun summon(at: Location): NPCEntity {
        val stand = at.world.spawnEntity(at.clone().add(vec(y = 1.1)), EntityType.ARMOR_STAND) as ArmorStand
        stand.isInvulnerable = true
        stand.isVisible = false
        stand.isSmall = true
        stand.setGravity(false)
        stand.isCustomNameVisible = true
        stand.customName(text(name))
        stand.persistentDataContainer.set(NamespacedKey(Macrocosm, "ignore_damage"), PersistentDataType.BYTE, 0)
        val native = NPCEntity((at.world as CraftWorld).handle, id, stand.uniqueId)
        NativeMacrocosmEntity.summon(native, at, Registry.ENTITY.find(id))
        return native
    }

    override fun register(registry: Registry<MacrocosmEntity>) {
        registry.register(id, MacrocosmNPCEntity(mainHand, offHand, helmet, chestplate, leggings, boots, profile))
        Registry.DISGUISE.register(id, "")
    }

    private class MacrocosmNPCEntity(
        mainHand: MacrocosmItem?,
        offHand: MacrocosmItem?,
        helmet: MacrocosmItem?,
        chestplate: MacrocosmItem?,
        leggings: MacrocosmItem?,
        boots: MacrocosmItem?,
        profile: WrappedGameProfile,
    ) : EntityBase(
        text("<yellow><bold>CLICK"),
        EntityType.ZOMBIE,
        LootPool.of(),
        .0,
        Statistics.zero(),
        SpecialStatistics(),
        mainHand,
        offHand,
        helmet,
        chestplate,
        leggings,
        boots,
        playerFriendly = true,
        disguiseProfile = profile
    ) {
        override fun buildName(): Component {
            return text("<yellow><bold>CLICK")
        }
    }
}
