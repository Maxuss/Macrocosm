package space.maxus.macrocosm.ability.types.armor

import net.axay.kspigot.event.listen
import net.axay.kspigot.particles.particle
import net.axay.kspigot.runnables.taskRunLater
import net.axay.kspigot.sound.sound
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import space.maxus.macrocosm.ability.FullSetBonus
import space.maxus.macrocosm.damage.DamageCalculator
import space.maxus.macrocosm.damage.DamageKind
import space.maxus.macrocosm.damage.DamageType
import space.maxus.macrocosm.entity.macrocosm
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.events.PlayerKillEntityEvent
import space.maxus.macrocosm.listeners.DamageHandlers
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.util.along
import space.maxus.macrocosm.util.data.MutableContainer
import space.maxus.macrocosm.util.game.Fmt
import space.maxus.macrocosm.util.superCritMod
import space.maxus.macrocosm.util.ticks
import space.maxus.macrocosm.util.unreachable
import java.util.concurrent.ThreadLocalRandom
import kotlin.time.Duration.Companion.seconds

object ConfluxPolarity: FullSetBonus("<rainbow>Primordial Grace</rainbow>", "Killing enemies results in the release of <gradient:#DCFDF4:#FDE6DC:#F9DCFD>Elemental Spark</gradient>. The sparks combust, dealing <red>[5000:0.1] ${Statistic.DAMAGE.display}<gray> to nearby enemies and <aqua>marks<gray> them for the next <green>2 seconds<gray>.<br>Attacking <aqua>marked<gray> enemies results in ${Fmt.SUPER_CRIT} ${Statistic.DAMAGE}<gray>.") {
    val marked = MutableContainer.trulyEmpty()

    override fun registerListeners() {
        listen<PlayerKillEntityEvent> { e ->
            if(!ensureSetRequirement(e.player))
                return@listen

            sound(Sound.BLOCK_BEACON_ACTIVATE) {
                pitch = 2f
                volume = 3f
                playAt(e.killed.location)
            }

            spawnParticles(e.killed.location, e.player, DamageCalculator.calculateMagicDamage(5000, .1f, e.player.stats()!!))
        }
        listen<PlayerDealDamageEvent> { e ->
            if(!ensureSetRequirement(e.player))
                return@listen
            marked.takeMutOrRemove(e.damaged.uniqueId) {
                e.damage *= superCritMod(e.player)
                e.isSuperCrit = true
                Pair(Unit, MutableContainer.TakeResult.REVOKE)
            }
        }
    }

    fun spawnParticles(at: Location, player: MacrocosmPlayer, dmg: Float) {
        val color = when(ThreadLocalRandom.current().nextInt(5)) {
            0 -> 0xFDE6DC
            1 -> 0xFDF7DC
            2 -> 0xDCFDEF
            3 -> 0xF4DCFD
            4 -> 0xFDDCEF
            else -> unreachable()
        }
        for(i in 0..5) {
            val vec = Vector.getRandom().multiply(5)
            vec.along(at.clone(), 10) {
                particle(Particle.REDSTONE) {
                    data = DustOptions(Color.fromRGB(color), 2f)
                    extra = 0f
                    amount = 5
                    spawnAt(it)
                }
            }
            val end = at.clone().add(vec)
            for(entity in end.getNearbyLivingEntities(1.5)) {
                if(entity !is Player && entity !is ArmorStand) {
                    entity.macrocosm?.damage(dmg, player.paper, DamageKind.MAGIC)
                    DamageHandlers.summonDamageIndicator(entity.location, dmg, DamageType.FIRE)
                    marked[entity.uniqueId] = Unit
                    taskRunLater(5.seconds.ticks, false) {
                        marked.remove(entity.uniqueId)
                    }
                }
            }
        }
    }
}
