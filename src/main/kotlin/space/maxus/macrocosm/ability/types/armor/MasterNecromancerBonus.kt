package space.maxus.macrocosm.ability.types.armor

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import net.axay.kspigot.event.listen
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.goal.*
import net.minecraft.world.entity.monster.Zombie
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import org.bukkit.Bukkit
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerQuitEvent
import space.maxus.macrocosm.ability.TieredSetBonus
import space.maxus.macrocosm.entity.EntityValue
import space.maxus.macrocosm.nms.AbsFollowOwnerGoal
import space.maxus.macrocosm.nms.AttackOwnerHurtGoal
import space.maxus.macrocosm.nms.MimicOwnerAttackGoal
import space.maxus.macrocosm.nms.NativeMacrocosmSummon
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.util.general.id
import java.util.*

object MasterNecromancerBonus : TieredSetBonus(
    "Master Necromancer",
    "Summons a <red>Zombie Youngling<gray>, <red>Golden Ghoul<gray> or a <red>Corpse Giant<gray>, depending on amount of pieces worn."
) {
    private val baby = hashMapOf<UUID, UUID>()
    private val golden = hashMapOf<UUID, UUID>()
    private val giant = hashMapOf<UUID, UUID>()

    override fun registerListeners() {
        listen<PlayerArmorChangeEvent>(priority = EventPriority.LOWEST) { e ->
            if (e.newItem == e.oldItem)
                return@listen
            val player = e.player.uniqueId
            val (ok, tier) = getArmorTier(e.player.macrocosm ?: return@listen)
            if (tier == 1) {
                sanitizeRemove(player, baby)
                sanitizeRemove(player, golden)
                sanitizeRemove(player, giant)
            }
            if (!ok)
                return@listen

            val world = e.player.world as CraftWorld
            val pos = e.player.location
            val level = world.handle
            when (tier) {
                2 -> {
                    // baby zombie
                    sanitizeRemove(player, golden)
                    sanitizeRemove(player, giant)

                    if (baby.containsKey(player))
                        return@listen
                    val entity = Youngling(level, player)
                    NativeMacrocosmSummon.summon(entity, pos, EntityValue.ZOMBIE_YOUNGLING.entity)
                    baby[e.player.uniqueId] = entity.uuid
                }

                3 -> {
                    // golden ghoul
                    sanitizeRemove(player, baby)
                    sanitizeRemove(player, giant)

                    if (golden.containsKey(player))
                        return@listen

                    val entity = Golden(level, player)
                    NativeMacrocosmSummon.summon(entity, pos, EntityValue.ZOMBIE_GOLDEN.entity)
                    golden[e.player.uniqueId] = entity.uuid
                }

                4 -> {
                    // giant
                    sanitizeRemove(player, baby)
                    sanitizeRemove(player, golden)

                    if (giant.containsKey(player))
                        return@listen

                    val entity = Giant(level, player)
                    NativeMacrocosmSummon.summon(entity, pos, EntityValue.ZOMBIE_GIANT.entity)
                    giant[e.player.uniqueId] = entity.uuid
                }
            }
        }

        listen<PlayerQuitEvent>(priority = EventPriority.LOWEST) { e ->
            val player = e.player.uniqueId
            sanitizeRemove(player, baby)
            sanitizeRemove(player, golden)
            sanitizeRemove(player, giant)
        }
    }

    private fun sanitizeRemove(player: UUID, map: HashMap<UUID, UUID>) {
        if (map.containsKey(player)) {
            val entity = Bukkit.getEntity(map[player]!!) ?: run { map.remove(player); return }
            entity.remove()
            map.remove(player)
        }
    }

    class Youngling(level: Level, override val owner: UUID) : Zombie(level), NativeMacrocosmSummon {
        override val delegateId: Identifier = id("zombie_youngling")

        init {
            isBaby = true
        }

        override fun registerGoals() {
            goalSelector.addGoal(1, FloatGoal(this))
            goalSelector.addGoal(1, AttackOwnerHurtGoal(this))
            goalSelector.addGoal(1, MimicOwnerAttackGoal(this))
            goalSelector.addGoal(3, ZombieAttackGoal(this, 2.0, true))
            goalSelector.addGoal(4, LeapAtTargetGoal(this, 0.6f))
            goalSelector.addGoal(4, AbsFollowOwnerGoal(this, 1.4, 10f, 3f, false))
            goalSelector.addGoal(10, LookAtPlayerGoal(this, Player::class.java, 3f))
            goalSelector.addGoal(10, RandomLookAroundGoal(this))
        }
    }

    class Golden(level: Level, override val owner: UUID) : Zombie(level), NativeMacrocosmSummon {
        override val delegateId: Identifier = id("zombie_golden")

        override fun registerGoals() {
            goalSelector.addGoal(1, FloatGoal(this))
            goalSelector.addGoal(1, AttackOwnerHurtGoal(this))
            goalSelector.addGoal(1, MimicOwnerAttackGoal(this))
            goalSelector.addGoal(3, ZombieAttackGoal(this, 2.0, true))
            goalSelector.addGoal(4, LeapAtTargetGoal(this, 0.6f))
            goalSelector.addGoal(4, AbsFollowOwnerGoal(this, 0.7, 12f, 3f, false))
            goalSelector.addGoal(10, LookAtPlayerGoal(this, Player::class.java, 3f))
            goalSelector.addGoal(10, RandomLookAroundGoal(this))
        }
    }

    class Giant(level: Level, override val owner: UUID) :
        net.minecraft.world.entity.monster.Giant(EntityType.GIANT, level), NativeMacrocosmSummon {
        override val delegateId: Identifier = id("zombie_giant")

        override fun registerGoals() {
            goalSelector.addGoal(1, FloatGoal(this))
            goalSelector.addGoal(1, AttackOwnerHurtGoal(this))
            goalSelector.addGoal(1, MimicOwnerAttackGoal(this))
            goalSelector.addGoal(3, MeleeAttackGoal(this, 1.0, true))
            goalSelector.addGoal(4, LeapAtTargetGoal(this, 0.6f))
            goalSelector.addGoal(4, AbsFollowOwnerGoal(this, 0.9, 25f, 6f, false))
            goalSelector.addGoal(10, LookAtPlayerGoal(this, Player::class.java, 3f))
            goalSelector.addGoal(10, RandomLookAroundGoal(this))
        }
    }
}
