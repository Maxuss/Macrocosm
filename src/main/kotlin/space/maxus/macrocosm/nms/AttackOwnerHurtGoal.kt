package space.maxus.macrocosm.nms

import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.OwnableEntity
import net.minecraft.world.entity.ai.goal.target.TargetGoal
import net.minecraft.world.entity.ai.targeting.TargetingConditions
import org.bukkit.event.entity.EntityTargetEvent
import java.util.*

class AttackOwnerHurtGoal(mob: Mob): TargetGoal(mob, true, true) {
    init {
        if(mob !is OwnableEntity)
            throw IllegalStateException("${this.javaClass.canonicalName} expects an OwnableEntity, got ${mob.javaClass.canonicalName}!")
        setFlags(EnumSet.of(Flag.TARGET))
    }

    private var ownerLastHurt: LivingEntity? = null
    private var timestamp: Int = -1

    override fun canUse(): Boolean {
        val entityliving = (this.mob as OwnableEntity).owner as? LivingEntity
        return if (entityliving == null) {
            false
        } else {
            if(this.mob.javaClass.isInstance(entityliving.lastHurtMob ?: return false))
                return false
            this.ownerLastHurt = entityliving.lastHurtMob
            val i = entityliving.lastHurtMobTimestamp
            i != this.timestamp && canAttack(
                this.ownerLastHurt,
                TargetingConditions.DEFAULT
            )
        }
    }

    override fun start() {
        mob.setTarget(ownerLastHurt ?: return, EntityTargetEvent.TargetReason.OWNER_ATTACKED_TARGET, true) // CraftBukkit - reason

        val entityliving: LivingEntity? = (this.mob as OwnableEntity).owner as? LivingEntity

        if (entityliving != null) {
            timestamp = entityliving.lastHurtMobTimestamp
        }

        super.start()
    }
}
