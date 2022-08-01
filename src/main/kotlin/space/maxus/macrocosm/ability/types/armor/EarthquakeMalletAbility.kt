package space.maxus.macrocosm.ability.types.armor

import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.geometry.vec
import net.axay.kspigot.particles.particle
import net.axay.kspigot.sound.sound
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.events.PlayerRightClickEvent
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.util.NULL
import space.maxus.macrocosm.util.data.MutableContainer
import space.maxus.macrocosm.util.game.Fmt
import space.maxus.macrocosm.util.math.MathHelper
import space.maxus.macrocosm.util.metrics.report
import space.maxus.macrocosm.util.runNTimes

object EarthquakeMalletAbility: AbilityBase(AbilityType.RIGHT_CLICK, "Earth-shattering Jump", "Jump into air and land, dealing <red>[1000:0.1] ${Statistic.DAMAGE.display}<gray> and <yellow>Stunning<gray> all enemies within <green>4<gray> blocks.") {
    override val cost: AbilityCost = AbilityCost(300, cooldown = 5)

    val stunned = MutableContainer.trulyEmpty()

    override fun registerListeners() {
        listen<PlayerRightClickEvent> { e ->
            if(!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen

            val p = e.player.paper ?: report("Player in RightClickEvent was null!") { return@listen }

            val trajectory = MathHelper.parabola(p.location, p.getTargetBlock(8)?.location ?: p.eyeLocation.add(p.eyeLocation.direction.multiply(5f)), 20)

            val dmg = DamageCalculator.calculateMagicDamage(1000, .1f, e.player.stats()!!)

            sound(Sound.ENTITY_FIREWORK_ROCKET_LAUNCH) {
                pitch = 0f
                volume = 4f

                playAt(p.location)
            }

            val midpoint = trajectory[8]
            val midVec = midpoint.toVector().subtract(p.location.toVector()).normalize().multiply(1.3f)
            val end = trajectory.last()
            val endVec = end.toVector().subtract(midpoint.toVector()).normalize().multiply(1.21f)

            p.velocity = midVec

            var iter = 0
            runNTimes(10L, 1L, {
                p.velocity = endVec
                iter = 0
                runNTimes(10L, 1L, {
                    sound(Sound.BLOCK_ANVIL_LAND) {
                        pitch = 0f
                        volume = 4f

                        playAt(p.location)
                    }
                    particle(Particle.EXPLOSION_HUGE) {
                        amount = 10

                        spawnAt(p.location)
                    }
                    p.location.getNearbyLivingEntities(4.0) { entity -> entity !is Player && entity !is ArmorStand }.forEach { living ->
                        living.macrocosm?.damage(dmg)
                        DamageHandlers.summonDamageIndicator(living.location, dmg)
                        stunned[living.uniqueId] = NULL
                        val prevAiState = living.hasAI()
                        living.setAI(false)
                        runNTimes(20, 2L, {
                            stunned.remove(living.uniqueId)
                            living.setAI(prevAiState)
                        }) {
                            particle(Particle.REDSTONE) {
                                data = Particle.DustOptions(Color.YELLOW, 1f)
                                amount = 8
                                offset = Vector.getRandom()

                                spawnAt(living.eyeLocation)
                            }
                        }
                    }
                }) {
                    iter++
                    if(iter >= 4 && p.location.add(vec(y = -1)).block.isSolid)
                        it.cancel()
                }
            }) {
                iter++
                if(iter >= 4 && p.location.add(vec(y = -1)).block.isSolid)
                    it.cancel()
            }

        }
    }
}

object BeatingBagAbility: AbilityBase(AbilityType.PASSIVE, "Beating Bag", "Deal ${Fmt.SUPER_CRIT} damage when attacking <yellow>Stunned<gray> enemies.") {
    override fun registerListeners() {
        listen<PlayerDealDamageEvent> { e ->
            if(!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen
            EarthquakeMalletAbility.stunned.take(e.damaged.uniqueId) {
                e.isSuperCrit = true
            }
        }
    }
}
