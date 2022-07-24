package space.maxus.macrocosm.slayer

import net.axay.kspigot.extensions.bukkit.toComponent
import net.axay.kspigot.extensions.geometry.vec
import net.axay.kspigot.particles.particle
import net.axay.kspigot.runnables.task
import net.axay.kspigot.sound.sound
import net.kyori.adventure.text.Component
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import space.maxus.macrocosm.display.RenderComponent
import space.maxus.macrocosm.enchants.roman
import space.maxus.macrocosm.registry.Clone
import space.maxus.macrocosm.util.stripTags
import kotlin.math.min

class SlayerQuest(val type: SlayerType, val tier: Int, val collectedExp: Float, val status: SlayerStatus) : Clone {
    override fun clone(): Clone {
        return SlayerQuest(type, tier, collectedExp, status)
    }

    fun render(): RenderComponent {
        return RenderComponent.fixed(
            "Slayer Quest".toComponent(),
            listOf(
                Component.text("${type.slayer.name.stripTags()} ${roman(tier)}").color(colorFromTier(tier)),
                status.display(this)
            )
        )
    }

    fun summonBoss(by: Player) {
        val at = by.location
        var timer = 0L
        var extraPitch = 0f
        task(period = 1L) {
            if (timer >= 20) {
                sound(Sound.ENTITY_WITHER_SPAWN) {
                    pitch = 2f
                    volume = 4f
                    playAt(at)
                }
                sound(Sound.ENTITY_GENERIC_EXPLODE) {
                    pitch = 0f
                    volume = 1f
                    playAt(at)
                }
                particle(Particle.EXPLOSION_HUGE) {
                    amount = 3
                    spawnAt(at)
                }
                type.slayer.bossForTier(tier).summonBy(at, by)
                it.cancel()
                return@task
            }
            timer++
            extraPitch += .05f
            sound(Sound.ENTITY_WITHER_SHOOT) {
                pitch = min(.8f + extraPitch, 2f)
                volume = 2f
                playAt(at)
            }
            particle(Particle.SPELL_WITCH) {
                offset = Vector.getRandom()
                amount = 12
                spawnAt(at)
            }
            particle(Particle.SPELL_INSTANT) {
                offset = Vector.getRandom().add(vec(y = timer / 3.0))
                amount = 8
                spawnAt(at)
            }
        }
    }
}
