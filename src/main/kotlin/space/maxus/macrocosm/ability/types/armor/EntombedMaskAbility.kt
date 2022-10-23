package space.maxus.macrocosm.ability.types.armor

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import net.axay.kspigot.event.listen
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.OwnableEntity
import net.minecraft.world.entity.ai.goal.*
import net.minecraft.world.entity.monster.Zombie
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.event.player.PlayerQuitEvent
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.entity.EntityValue
import space.maxus.macrocosm.item.macrocosm
import space.maxus.macrocosm.nms.AbsFollowOwnerGoal
import space.maxus.macrocosm.nms.AttackOwnerHurtGoal
import space.maxus.macrocosm.nms.MimicOwnerAttackGoal
import space.maxus.macrocosm.nms.NativeMacrocosmSummon
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.anyPoints
import space.maxus.macrocosm.util.general.id
import java.util.*

object EntombedMaskAbility :
    AbilityBase(AbilityType.PASSIVE, "Forever in Tomb", "Summons <gold>3 Revenant Spirits<gray> to assist you.") {
    private val summoned: HashMap<UUID, Triple<UUID, UUID, UUID>> = hashMapOf()

    override fun registerListeners() {
        listen<PlayerArmorChangeEvent> { e ->
            if (e.newItem == e.oldItem)
                return@listen
            val player = e.player.uniqueId
            if (e.newItem?.macrocosm?.abilities?.anyPoints(this) == true && !summoned.containsKey(player)) {
                val world = e.player.world
                val loc = e.player.location

                val arr = arrayOfNulls<UUID>(3)
                for (i in 0..2) {
                    val entity = SupportZombie((world as CraftWorld).handle, e.player.uniqueId)
                    entity.setPos(Vec3(loc.x, loc.y, loc.z))
                    EntityValue.ENTOMBED_MASK_ZOMBIE.entity.loadChanges(entity.bukkitLivingEntity)
                    world.handle.addFreshEntity(entity)
                    arr[i] = entity.uuid
                }
                summoned[e.player.uniqueId] = Triple(arr[0]!!, arr[1]!!, arr[2]!!)
            } else if (e.oldItem?.macrocosm?.abilities?.anyPoints(this) == true) {
                val triple = summoned[e.player.uniqueId]?.toList() ?: return@listen
                for (i in 0..2) {
                    val entity =
                        Bukkit.getEntity(triple[i]) ?: run { summoned.remove(e.player.uniqueId); return@listen }
                    entity.remove()
                }
                summoned.remove(e.player.uniqueId)
            }
        }
        listen<PlayerQuitEvent> { e ->
            if (summoned.containsKey(e.player.uniqueId)) {
                val triple = summoned[e.player.uniqueId]?.toList() ?: return@listen
                for (i in 0..2) {
                    val entity = Bukkit.getEntity(triple[i]) ?: return@listen
                    entity.remove()
                }
                summoned.remove(e.player.uniqueId)
            }
        }
    }

    class SupportZombie(level: Level, override val owner: UUID) : Zombie(level), OwnableEntity, NativeMacrocosmSummon {
        override val delegateId: Identifier = id("entombed_mask_zombie")

        override fun registerGoals() {
            goalSelector.addGoal(1, FloatGoal(this))
            goalSelector.addGoal(1, AttackOwnerHurtGoal(this))
            goalSelector.addGoal(1, MimicOwnerAttackGoal(this))
            goalSelector.addGoal(3, ZombieAttackGoal(this, 2.0, true))
            goalSelector.addGoal(4, LeapAtTargetGoal(this, 0.6f))
            goalSelector.addGoal(4, AbsFollowOwnerGoal(this, 1.4, 6f, 3f, false))
            goalSelector.addGoal(10, LookAtPlayerGoal(this, Player::class.java, 3f))
            goalSelector.addGoal(10, RandomLookAroundGoal(this))
        }

        override fun getOwnerUUID(): UUID {
            return owner
        }

        override fun getOwner(): Entity? {
            return (Bukkit.getPlayer(owner) as? CraftPlayer)?.handle
        }
    }
}
