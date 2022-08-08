package space.maxus.macrocosm.spell.types

import net.axay.kspigot.particles.particle
import net.axay.kspigot.runnables.async
import net.axay.kspigot.runnables.task
import net.axay.kspigot.runnables.taskRunLater
import net.axay.kspigot.sound.sound
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import space.maxus.macrocosm.ability.AbilityCost
import space.maxus.macrocosm.global.GlobalVariables
import space.maxus.macrocosm.item.Rarity
import space.maxus.macrocosm.players.MacrocosmPlayer
import space.maxus.macrocosm.spell.Spell
import space.maxus.macrocosm.util.advanceInstantly
import space.maxus.macrocosm.util.data.MutableContainer.Companion.removeAllWith
import space.maxus.macrocosm.util.data.MutableContainer.Companion.setAllWith
import java.util.*

object SparklesSpell : Spell(
    "Sparks",
    "Shoots out some sparks, setting enemies on fire for <green>5 seconds<gray>, but dealing <red>no<gray> damage.",
    AbilityCost(100, cooldown = 2),
    Rarity.UNCOMMON,
    5
) {
    override fun rightClick(mc: MacrocosmPlayer, p: Player) {
        renderSparkles(p.eyeLocation)
    }

    private fun renderSparkles(at: Location) {
        async {
            val dir = at.direction
            val hit = mutableListOf<UUID>()
            sound(Sound.ENTITY_BLAZE_SHOOT) {
                pitch = 0f
                volume = 5f
                playAt(at)
            }
            for (i in 0..4) {
                val cross = Vector.getRandom()
                cross.y = cross.y.coerceIn(-0.2..0.2)
                cross.z = cross.z.coerceIn((-.5 * dir.z)..(.5 * dir.z))
                cross.x = cross.x.coerceIn((-.5 * dir.x)..(.5 * dir.x))
                task {
                    cross.advanceInstantly(at, .5f, 5) {
                        particle(Particle.FLAME) {
                            extra = 0f
                            amount = 2

                            spawnAt(it)
                        }
                        it.getNearbyLivingEntities(1.5) { e -> e !is ArmorStand && e !is Player && !hit.contains(e.uniqueId) }
                            .forEach { e ->
                                hit.add(e.uniqueId)
                                e.isVisualFire = true
                            }
                    }
                }
            }

            GlobalVariables.enemiesOnFire.setAllWith(hit)

            taskRunLater(5 * 20L) {
                GlobalVariables.enemiesOnFire.removeAllWith(hit)
                hit.forEach { h ->
                    at.world.getEntity(h)?.isVisualFire = false
                }
            }
        }
    }
}
