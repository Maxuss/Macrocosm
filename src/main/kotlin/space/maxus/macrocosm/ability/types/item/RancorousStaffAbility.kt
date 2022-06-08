package space.maxus.macrocosm.ability.types.item

import net.axay.kspigot.event.listen
import net.axay.kspigot.particles.particle
import net.axay.kspigot.runnables.task
import net.axay.kspigot.sound.sound
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerReceiveDamageEvent
import space.maxus.macrocosm.events.PlayerRightClickEvent
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.stats.Statistic
import java.util.UUID

object RancorousStaffAbility: AbilityBase(AbilityType.RIGHT_CLICK, "Pure Hatred", "Begin a channel for <green>5 seconds<gray>. After that time ends, all damage you have taken will be <red>viciously<gray> reflected to your enemies.") {
    override val cost: AbilityCost = AbilityCost(400, cooldown = 15)

    private val amountTaken = hashMapOf<UUID, Float>()

    override fun registerListeners() {
        listen<PlayerRightClickEvent> { e ->
            if(!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen

            amountTaken[e.player.ref] = 0f
            sound(Sound.ENTITY_IRON_GOLEM_HURT) {
                pitch = 0f
                playAt(e.player.paper!!.location)
            }

            var ticks = 0
            task(period = 10L) {
                ticks++
                if(ticks >= 10) {
                    val damage = amountTaken.remove(e.player.ref)!!
                    val toReflect = damage * 2
                    val p = e.player.paper!!
                    for (entity in p.getNearbyEntities(10.0, 10.0, 10.0)) {
                        if (entity !is LivingEntity || entity is Player || entity is ArmorStand)
                            continue
                        entity.macrocosm!!.damage(toReflect, p)
                        particleRay(p.eyeLocation, entity.location)
                        DamageHandlers.summonDamageIndicator(entity.location, toReflect)

                    }
                    sound(Sound.ENTITY_IRON_GOLEM_DEATH) {
                        pitch = 2f
                        playAt(p.location)
                    }
                    e.player.sendMessage(
                        "<yellow>Your <red>Pure Hatred<yellow> reflected <red>${
                            Formatting.stats(
                                toReflect.toBigDecimal()
                            )
                        } ${Statistic.DAMAGE.display}<yellow> to your enemies!"
                    )
                    it.cancel()
                } else {
                    particle(Particle.VILLAGER_ANGRY) {
                        amount = 10
                        offset = Vector.getRandom()
                        spawnAt(e.player.paper!!.location)
                    }
                }
            }
        }

        listen<PlayerReceiveDamageEvent>(priority = EventPriority.LOWEST) { e ->
            if(amountTaken.containsKey(e.player.ref)) {
                amountTaken[e.player.ref] = amountTaken[e.player.ref]!! + e.damage
            }
        }
    }

    private fun particleRay(from: Location, to: Location) {
        val direction = to.subtract(from).toVector().normalize()
        val loc = from.clone()
        val constMul = direction.multiply(0.1).normalize()

        var tick = 0
        task(sync = false, period = 1L) {
            tick++
            if(tick >= 10) {
                it.cancel()
                return@task
            }
            loc.add(constMul)
            particle(Particle.REDSTONE) {
                data = DustOptions(Color.RED, 1.5f)
                amount = 4
                extra = 0
                spawnAt(loc)
            }
        }
    }
}
