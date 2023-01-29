package space.maxus.macrocosm.npc.nms

import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
import net.minecraft.world.entity.monster.Zombie
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import space.maxus.macrocosm.nms.NativeMacrocosmEntity
import space.maxus.macrocosm.registry.Identifier
import java.util.*

class NPCEntity(level: Level, override val id: Identifier, val standId: UUID): Zombie(EntityType.ZOMBIE, level), NativeMacrocosmEntity {
    init {
        this.setShouldBurnInDay(false)
        this.isSilent = true
        this.isInvulnerable = true
        this.collides = false
    }

    override fun addBehaviourGoals() {
        // Empty
    }

    override fun registerGoals() {
        goalSelector.addGoal(1, LookAtPlayerGoal(this, Player::class.java, 8.0f, .1f, true))
        goalSelector.addGoal(2, RandomLookAroundGoal(this))
    }
}
