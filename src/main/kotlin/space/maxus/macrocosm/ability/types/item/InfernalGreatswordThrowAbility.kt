package space.maxus.macrocosm.ability.types.item

import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.geometry.vec
import net.axay.kspigot.particles.particle
import net.axay.kspigot.runnables.task
import net.axay.kspigot.sound.sound
import net.minecraft.util.Mth
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.EulerAngle
import org.bukkit.util.Vector
import space.maxus.macrocosm.Macrocosm
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
import space.maxus.macrocosm.util.generic.id

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
            val pos = p.eyeLocation.add(vec(y = -1.4))
                .add(p.eyeLocation.direction.rotateAroundY(Mth.DEG_TO_RAD * 90.0).multiply(1.1f).normalize()).add(
                    vec(y = 0.4)
                )
            val inc = pos.direction.multiply(2f)

            val stand = p.world.spawnEntity(pos, EntityType.ARMOR_STAND) as ArmorStand
            stand.isVisible = false
            stand.isMarker = true
            stand.isInvulnerable = true
            stand.setGravity(false)
            stand.persistentDataContainer.set(NamespacedKey(Macrocosm, "ignore_damage"), PersistentDataType.BYTE, 0)
            stand.rightArmPose = EulerAngle(.0, -p.eyeLocation.pitch.toDouble() * Mth.DEG_TO_RAD, 90.0 * Mth.DEG_TO_RAD)
            stand.setItem(EquipmentSlot.HAND, Registry.ITEM.find(id("infernal_greatsword")).build(e.player))

            var tick = 0

            val stats = e.player.stats()!!
            var (damage, crit) = DamageCalculator.calculateStandardDealt(stats.damage, stats)
            damage *= 3f

            sound(Sound.ENTITY_EGG_THROW) {
                pitch = 0f
                playAt(pos)
            }

            task(period = 1L) {
                tick++
                if (tick >= 80) {
                    // travelling for *too* long, despawn
                    stand.remove()
                    it.cancel()
                    return@task
                }
                pos.add(inc)
                stand.teleport(pos)

                if (stand.eyeLocation.add(vec(y = -.3)).block.isSolid) {
                    removeStand(pos, stand)
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

                    removeStand(pos, stand)
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
                    removeStand(pos, stand)
                    it.cancel()
                    return@task
                }
            }
        }
    }

    private fun removeStand(pos: Location, stand: ArmorStand) {
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

        stand.remove()
    }
}
