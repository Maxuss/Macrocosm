package space.maxus.macrocosm.ability.types.item

import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.geometry.vec
import net.axay.kspigot.particles.particle
import net.axay.kspigot.runnables.async
import net.axay.kspigot.runnables.taskRunLater
import net.axay.kspigot.sound.sound
import net.minecraft.util.Mth
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.EventPriority
import org.bukkit.inventory.EquipmentSlot
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerReceiveDamageEvent
import space.maxus.macrocosm.events.PlayerSneakEvent
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.util.data.MutableContainer
import space.maxus.macrocosm.util.runNTimes
import java.util.*

object ArmorOfDamnedPassive : AbilityBase(
    AbilityType.PASSIVE,
    "Cursed Reliquary",
    "You take take <red>100%<gray> more ${Statistic.DAMAGE.display}<gray> from <blue>Undead<gray> mobs."
) {
    override fun registerListeners() {
        listen<PlayerReceiveDamageEvent>(priority = EventPriority.HIGHEST) { e ->
            if (!ensureRequirements(e.player, EquipmentSlot.CHEST))
                return@listen
            e.damage *= 2f
        }
    }
}

object ArmorOfDamnedActive : AbilityBase(
    AbilityType.SNEAK,
    "Unholy Binding",
    "For the next <green>5 seconds<gray> accumulate all ${Statistic.DAMAGE.display}<gray> you take from <blue>Undead<gray> mobs, then rapidly shoot a volley of <#67EAA9>Soul Fireballs<gray>.<br>Each fireball deals <red>10x<gray> the damage you took."
) {
    override val cost: AbilityCost = AbilityCost(500, cooldown = 20)

    private val accumulated = MutableContainer.empty<Float>()
    override fun registerListeners() {
        listen<PlayerReceiveDamageEvent> { e ->
            accumulated.takeMut(e.player.ref) {
                if (e.damager.isUndead())
                    it + e.damage
                else it
            }
        }
        listen<PlayerSneakEvent> { e ->
            if (!ensureRequirements(e.player, EquipmentSlot.CHEST))
                return@listen

            accumulated[e.player.ref] = 0f

            taskRunLater(5 * 20L) {
                val amount = accumulated.remove(e.player.ref)!!
                if (amount <= 0f)
                    return@taskRunLater
                e.player.sendMessage(
                    "<gray>Your <aqua>Unholy Binding<gray> accumulated <red>${
                        Formatting.withCommas(
                            amount.toBigDecimal()
                        )
                    } ${Statistic.DAMAGE.display}<gray>."
                )

                val p = e.player.paper ?: return@taskRunLater
                runNTimes(5, 10) {
                    sound(Sound.ENTITY_PHANTOM_DEATH) {
                        pitch = 2f
                        volume = 3f

                        playAt(p.location)
                    }
                    shoot(p, amount * 10f)
                }
            }
        }
    }

    private fun shoot(player: Player, damage: Float) {
        val dir = player.eyeLocation.direction.multiply(.5f)
        val pos = player.eyeLocation.add(vec(y = -.7f))
        val hit = mutableListOf<UUID>()
        runNTimes(10, 2) {
            pos.add(dir)

            pos.getNearbyLivingEntities(1.43) { it !is Player && it !is ArmorStand && !hit.contains(it.uniqueId) }
                .forEach {
                    it.macrocosm?.damage(damage); DamageHandlers.summonDamageIndicator(
                    it.location,
                    damage
                ); hit.add(it.uniqueId)
                }

            renderBall(pos)
        }
    }

    private fun renderBall(pos: Location) {
        async {
            var i = 0f
            val at = pos.clone()
            while (i < Mth.PI) {
                val radius = Mth.sin(i) * .5
                val y = Mth.cos(i) * .5

                var a = 0f
                while (a < Mth.PI * 2) {
                    val x = Mth.cos(a) * radius
                    val z = Mth.sin(a) * radius

                    val vecConst = vec(x, y, z)

                    at.add(vecConst)

                    particle(Particle.REDSTONE) {
                        data = Particle.DustOptions(Color.TEAL, 1.2f)
                        amount = 2

                        spawnAt(at)
                    }

                    at.subtract(vecConst)

                    a += Mth.PI / 7f
                }

                i += Mth.PI / 7f
            }
        }
    }


}
