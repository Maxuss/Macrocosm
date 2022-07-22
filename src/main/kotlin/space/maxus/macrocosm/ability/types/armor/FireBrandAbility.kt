package space.maxus.macrocosm.ability.types.armor

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
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.FullSetBonus
import space.maxus.macrocosm.chat.Formatting
import space.maxus.macrocosm.damage.DamageType
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerReceiveDamageEvent
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.util.math.MathHelper

class FireBrandAbility(name: String, val dmg: Float): FullSetBonus(name, "Shoot firebrands out of yourself when you are hit, the firebrands deal <red>${Formatting.withCommas(dmg.toBigDecimal())} ${Statistic.DAMAGE.display}<gray>.") {
    override val cost: AbilityCost = AbilityCost(cooldown = 5)

    override fun registerListeners() {
        listen<PlayerReceiveDamageEvent> { e ->
            if(!this.ensureSetRequirement(e.player))
                return@listen
            if(!this.cost.ensureRequirements(e.player, id, true))
                return@listen
            val loc = e.player.paper?.location ?: return@listen
            for(i in 1..(1..3).random()) {
                task(delay = i * 5L) {
                    summonBrand(loc.clone(), Vector.getRandom(), e.player)
                    sound(Sound.ENTITY_BLAZE_SHOOT) {
                        pitch = 0f
                        volume = 2f

                        playAt(loc)
                    }
                }
            }
        }
    }

    private fun summonBrand(start: Location, direction: Vector, by: MacrocosmPlayer) {
        val mov = direction.multiply(5f)

        val end = start.clone().add(mov)

        val parabolic = MathHelper.parabola(start, end, 12)

        val p = by.paper
        for(pos in parabolic) {
            particle(Particle.FLAME) {
                extra = 0f
                amount = 3

                spawnAt(pos)
            }

            particle(Particle.REDSTONE) {
                data = DustOptions(Color.BLACK, .9f)
                extra = 0f

                amount = 3

                spawnAt(pos)
            }

            for(entity in pos.getNearbyLivingEntities(1.5) { it !is Player && it !is ArmorStand }) {
                entity.macrocosm?.damage(dmg, p)
                DamageHandlers.summonDamageIndicator(pos, dmg, DamageType.FIRE)
            }
        }
    }
}
