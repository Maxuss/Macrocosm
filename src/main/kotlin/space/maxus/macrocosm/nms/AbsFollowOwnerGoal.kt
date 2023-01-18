package space.maxus.macrocosm.nms

import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.Mob
import net.minecraft.world.entity.OwnableEntity
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.ai.navigation.PathNavigation
import net.minecraft.world.level.LevelReader
import net.minecraft.world.level.block.LeavesBlock
import net.minecraft.world.level.pathfinder.BlockPathTypes
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftEntity
import org.bukkit.event.entity.EntityTeleportEvent
import java.util.*
import kotlin.math.abs

@Suppress("PrivatePropertyName")
class AbsFollowOwnerGoal(
    private val entity: Mob,
    private val speedModifier: Double,
    private val startDistance: Float,
    private val stopDistance: Float,
    private val canFly: Boolean
) : Goal() {
    private val MIN_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 2
    private val MAX_HORIZONTAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 3
    private val MAX_VERTICAL_DISTANCE_FROM_PLAYER_WHEN_TELEPORTING = 1
    private var owner: LivingEntity? = null
    private val level: LevelReader = entity.level
    private val navigation: PathNavigation = entity.navigation
    private var timeToRecalcPath = 0
    private var oldWaterCost = 0f
    private val owned: OwnableEntity get() = entity as OwnableEntity

    init {
        if (entity !is OwnableEntity)
            throw IllegalStateException("${this.javaClass.canonicalName} expects an OwnableEntity, got ${entity.javaClass.canonicalName}!")
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK))
    }

    override fun canUse(): Boolean {
        val entityliving = this.owned.owner as? LivingEntity

        return if (entityliving == null) {
            false
        } else if (entityliving.isSpectator) {
            false
        } else if (this.entity.distanceToSqr(entityliving) < (startDistance * startDistance).toDouble()) {
            false
        } else {
            owner = entityliving
            true
        }
    }

    override fun canContinueToUse(): Boolean {
        return navigation.isDone && this.entity.distanceToSqr(
            owner ?: return false
        ) > (stopDistance * stopDistance).toDouble()
    }

    override fun start() {
        timeToRecalcPath = 0
        oldWaterCost = this.entity.getPathfindingMalus(BlockPathTypes.WATER)
        this.entity.setPathfindingMalus(BlockPathTypes.WATER, 0.0f)
    }

    override fun stop() {
        owner = null
        navigation.stop()
        this.entity.setPathfindingMalus(BlockPathTypes.WATER, oldWaterCost)
    }

    val MAX_DISTANCE_SQR = Mth.square(startDistance + (startDistance / 5f))
    override fun tick() {
        this.entity.lookControl.setLookAt(owner ?: return, 10.0f, this.entity.maxHeadXRot.toFloat())
        if (--timeToRecalcPath <= 0) {
            timeToRecalcPath = adjustedTickDelay(10)
            if (!this.entity.isLeashed && !this.entity.isPassenger) {
                // 12^2
                if (this.entity.distanceToSqr(owner ?: return) >= MAX_DISTANCE_SQR) {
                    this.teleportToOwner()
                } else {
                    navigation.moveTo(owner ?: return, speedModifier)
                }
            }
        }
    }

    private fun teleportToOwner() {
        val blockposition = owner!!.blockPosition()
        for (i in 0..9) {
            val j = randomIntInclusive(-3, 3)
            val k = randomIntInclusive(-1, 1)
            val l = randomIntInclusive(-3, 3)
            val flag = maybeTeleportTo(blockposition.x + j, blockposition.y + k, blockposition.z + l)
            if (flag) {
                return
            }
        }
    }

    private fun maybeTeleportTo(x: Int, y: Int, z: Int): Boolean {
        return if (abs(x.toDouble() - owner!!.x) < 2.0 && abs(z.toDouble() - owner!!.z) < 2.0) {
            false
        } else if (!canTeleportTo(BlockPos(x, y, z))) {
            false
        } else {
            val entity: CraftEntity = this.entity.bukkitEntity
            var to = Location(
                entity.world,
                x.toDouble() + 0.5,
                y.toDouble(),
                z.toDouble() + 0.5,
                this.entity.yRot,
                this.entity.xRot
            )
            val event = EntityTeleportEvent(entity, entity.location, to)
            this.entity.level.craftServer.pluginManager.callEvent(event)
            if (event.isCancelled) {
                return false
            }
            to = event.to ?: return false
            this.entity.moveTo(to.x, to.y, to.z, to.yaw, to.pitch)
            navigation.stop()
            true
        }
    }

    private fun canTeleportTo(pos: BlockPos): Boolean {
        val pathtype = WalkNodeEvaluator.getBlockPathTypeStatic(level, pos.mutable())
        return if (pathtype != BlockPathTypes.WALKABLE) {
            false
        } else {
            val iblockdata = level.getBlockState(pos.below())
            if (!canFly && iblockdata.block is LeavesBlock) {
                false
            } else {
                val blockposition1 = pos.subtract(this.entity.blockPosition())
                level.noCollision(this.entity, this.entity.boundingBox.move(blockposition1))
            }
        }
    }

    private fun randomIntInclusive(min: Int, max: Int): Int {
        return this.entity.random.nextInt(max - min + 1) + min
    }
}
