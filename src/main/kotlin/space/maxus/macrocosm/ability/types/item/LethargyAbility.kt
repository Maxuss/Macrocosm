package space.maxus.macrocosm.ability.types.item

import net.axay.kspigot.event.listen
import net.axay.kspigot.extensions.bukkit.title
import net.axay.kspigot.extensions.geometry.increase
import net.axay.kspigot.extensions.geometry.reduce
import net.axay.kspigot.extensions.geometry.vec
import net.axay.kspigot.particles.particle
import net.axay.kspigot.runnables.async
import net.axay.kspigot.runnables.taskRunLater
import net.axay.kspigot.sound.sound
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.util.Vector
import space.maxus.macrocosm.ability.AbilityBase
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.ability.AbilityType
import space.maxus.macrocosm.damage.relativeLocation
import space.maxus.macrocosm.events.PlayerDealDamageEvent
import space.maxus.macrocosm.events.PlayerRightClickEvent
import space.maxus.macrocosm.stats.Statistic
import space.maxus.macrocosm.text.text
import java.util.*
import kotlin.random.Random
import kotlin.random.nextInt

object LethargyAbility : AbilityBase(
    AbilityType.RIGHT_CLICK,
    "Lethargy",
    "Hit enemies for the next <red>5 seconds<gray> to accumulate <gradient:#AB6C08:#B77A1A:#291A01>Hatred Energy<gray>.<br>After 5 seconds converts energy to ${Statistic.STRENGTH.display}<gray> and applies it to your next hit."
) {
    override val cost: AbilityCost = AbilityCost(500, 250, 10)

    private val hits: HashMap<UUID, Int> = hashMapOf()
    private val disable: HashMap<UUID, Float> = hashMapOf()

    override fun registerListeners() {
        listen<PlayerDealDamageEvent> { e ->
            val u = e.player.paper?.uniqueId ?: return@listen
            if (hits.containsKey(u)) {
                hits[u] = hits[u]!! + 1
                e.player.paper?.title(
                    subText = text("<gold>${hits[u]!!}")
                )
                sound(Sound.ENTITY_PHANTOM_BITE) {
                    pitch = 1f
                    volume = 2f

                    playAt(e.player.paper?.location ?: return@sound)
                }

                val entity = e.player.paper ?: return@listen
                val vector = entity.eyeLocation.direction.clone() reduce vec(1) increase vec(y = 1)
                for (i in 0 until 12) {
                    entity.world.spawnParticle(
                        Particle.REDSTONE,
                        vector.relativeLocation(entity.location),
                        2 + Random.nextInt(0..2),
                        Particle.DustOptions(Color.fromRGB(0xD67E0A), 0.6f)
                    )
                    vector increase vec(x = .2)
                }
            } else if (disable.containsKey(u)) {
                val a = disable.remove(u)!!
                e.player.tempStats.strength -= a
            }
        }
        listen<PlayerRightClickEvent> { e ->
            if (!ensureRequirements(e.player, EquipmentSlot.HAND))
                return@listen
            val player = e.player
            val p = player.paper ?: return@listen

            if (hits.contains(p.uniqueId))
                return@listen

            sound(Sound.ENTITY_PHANTOM_DEATH) {
                pitch = 0f
                volume = 5f

                playAt(p.location)
            }

            particle(Particle.REDSTONE) {
                data = Particle.DustOptions(Color.fromRGB(0xD67E0A), 2f)
                offset = Vector.getRandom()
                amount = 20
                extra = 2f

                spawnAt(p.location)
            }

            if (!ensureRequirements(player, EquipmentSlot.HAND))
                return@listen

            hits[p.uniqueId] = 0

            taskRunLater(20 * 5) {
                sound(Sound.ENTITY_BLAZE_AMBIENT) {
                    pitch = 2f
                    volume = 5f

                    playAt(p.location)
                }

                async {
                    for (i in 0..20) {
                        val color = when (Random.nextInt(0, 4)) {
                            0 -> 0xD67E0A
                            1 -> 0x2D2315
                            else -> 0xD67E0A
                        }

                        particle(Particle.REDSTONE) {
                            data = Particle.DustOptions(Color.fromRGB(color), 2f)
                            offset = Vector.getRandom()
                            amount = 5
                            extra = 2f

                            spawnAt(p.location)
                        }
                    }
                }

                disableAbility(e)
            }
        }
    }

    private fun disableAbility(e: PlayerRightClickEvent) {
        val p = e.player.paper ?: return

        val amount = hits.remove(p.uniqueId)!! * 13f
        e.player.tempStats.strength += amount
        disable[p.uniqueId] = amount
    }
}
