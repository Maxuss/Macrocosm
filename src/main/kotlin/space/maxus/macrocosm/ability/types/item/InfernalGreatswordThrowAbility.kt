package space.maxus.macrocosm.ability.types.item

import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.geometry.vec
import net.axay.kspigot.particles.particle
import net.axay.kspigot.runnables.task
import net.axay.kspigot.sound.sound
import net.minecraft.util.Mth
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.damage.DamageType
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerRightClickEvent
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.util.general.id
import space.maxus.macrocosm.util.joml
import space.maxus.macrocosm.util.mutate

object InfernalGreatswordThrowAbility : AbilityBase(
    AbilityType.RIGHT_CLICK,
    "Throw",
    "Throw your sword, piercing <gold>infinitely<gray> until it hits a wall or does <red>3x your melee ${Statistic.DAMAGE.display}<gray>."
) {
    override val cost: AbilityCost = AbilityCost(150, cooldown = 1)

    override fun registerListeners() {
        listen<PlayerRightClickEvent> { e ->
            if (!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen

            val p = e.player.paper ?: return@listen
            val pos = p.eyeLocation
            val inc = pos.direction.multiply(2f)

            val display = p.world.spawnEntity(pos, EntityType.ITEM_DISPLAY) as ItemDisplay
            display.itemDisplayTransform = ItemDisplay.ItemDisplayTransform.THIRDPERSON_LEFTHAND
            display.interpolationDelay = 0
            display.interpolationDuration = 5
            display.itemStack = Registry.ITEM.find(id("infernal_greatsword")).build(e.player)

            val poseQuat = Quaternionf()
            poseQuat.rotationXYZ(Mth.DEG_TO_RAD * 180f, 0f, 0f)
            display.transformation = display.transformation.mutate(leftRot = poseQuat)

            val stats = e.player.stats()!!
            var (damage, crit) = DamageCalculator.calculateStandardDealt(stats.damage, stats)
            damage *= 3f

            sound(Sound.ENTITY_EGG_THROW) {
                pitch = 0f
                playAt(pos)
            }

            var tick = 0
            task(period = 1L) {
                tick++
                if (tick >= 80) {
                    // travelling for *too* long, despawn
                    display.remove()
                    it.cancel()
                    return@task
                }
                pos.add(inc)
                val playerRot = Quaternionf()
                playerRot.rotationTo(Vector3f(0f, 0f, 1f), p.eyeLocation.direction.joml().apply { y = 0f })
                display.interpolationDelay = 0
                display.interpolationDuration = 5
                display.transformation = display.transformation.mutate(translation = pos.toVector().subtract(display.location.toVector()).joml(), leftRot = playerRot, rightRot = poseQuat)

                if (pos.add(vec(y = -.3)).block.isSolid) {
                    removeDisplay(pos, display)
                    it.cancel()
                    return@task
                }

                val nearby = pos.getNearbyLivingEntities(1.5)
                    .filter { entity -> entity !is ArmorStand && entity !is Player && !entity.isDead }
                val entity = nearby.firstOrNull() ?: return@task
                val mc = entity.macrocosm!!
                val entityStats = mc.calculateStats()
                val ehp = DamageCalculator.calculateLightEffectiveHealth(mc.currentHealth, entityStats)

                if (damage > ehp) {
                    val hp = mc.currentHealth
                    DamageHandlers.summonDamageIndicator(pos, hp, if (crit) DamageType.CRITICAL else DamageType.DEFAULT)
                    particle(Particle.FLAME) {
                        amount = 4
                        extra = 0.3
                        spawnAt(pos)
                    }

                    mc.damage(hp, p)
                    damage -= ehp
                } else {
                    val dmg = damage * (entityStats.defense / (100 + entityStats.defense))
                    mc.damage(dmg, p)

                    removeDisplay(pos, display)
                    DamageHandlers.summonDamageIndicator(
                        pos,
                        dmg,
                        if (crit) DamageType.CRITICAL else DamageType.DEFAULT
                    )
                    particle(Particle.FLAME) {
                        amount = 4
                        extra = 0.3
                        spawnAt(pos)
                    }
                    it.cancel()
                    return@task
                }

                if (damage <= 0) {
                    removeDisplay(pos, display)
                    it.cancel()
                    return@task
                }
            }
        }
    }

    private fun removeDisplay(pos: Location, display: ItemDisplay) {
        sound(Sound.ENTITY_ITEM_BREAK) {
            pitch = 0f
            volume = 4f
            playAt(pos)
        }

        particle(Particle.REDSTONE) {
            data = DustOptions(org.bukkit.Color.fromRGB(0x888888), 1.5f)
            amount = 5
            extra = 0
            offset = Vector.getRandom()
            spawnAt(pos)
        }

        display.remove()
    }
}
