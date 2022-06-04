package space.maxus.macrocosm.slayer.zombie

import net.axay.kspigot.particles.particle
import net.axay.kspigot.runnables.task
import net.axay.kspigot.sound.sound
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.slayer.SlayerAbility
import space.maxus.macrocosm.stats.Statistic
import kotlin.math.min

object ZombieAbilities {
    val REGENERATION = SlayerAbility(
        "regeneration",
        "revenant_horror",
        "<red>Regeneration",
        "Boss rapidly regenerates <red>[50/150/1000/2500] ${Statistic.HEALTH.display}<gray> every second."
    ) {
        val healths = listOf(50, 150, 1000, 2500)
        task(period = 20L) {
            applyToBosses { mc, living, lvl ->
                if(lvl > 4)
                    return@applyToBosses
                val healthToRegen = healths[lvl - 1]
                mc.currentHealth = min(mc.currentHealth + healthToRegen, mc.calculateStats().health)
                sound(Sound.ENTITY_ARROW_HIT_PLAYER) {
                    volume = 1f
                    pitch = 2f
                    playAt(living.location)
                }
                particle(Particle.VILLAGER_HAPPY) {
                    offset = Vector.getRandom()
                    amount = 7
                    spawnAt(living.location)
                }
                mc.loadChanges(living)
            }
        }
    }

    val CRUMBLING_TOUCH = SlayerAbility(
        "crumbling_touch",
        "revenant_horror",
        "<yellow>Crumbling Touch",
        "Every <green>5 seconds<gray> boss will shatter your armor, <yellow>decreasing<gray> your ${Statistic.DEFENSE.display}<gray> by <red>[10/20/30/50]%<gray>."
    ) {
        val multiples = listOf(.1, .2, .3, .5)
        task(period = 5 * 20L) {
            applyToBosses { _, living, lvl ->
                if(lvl > 4 || lvl < 2)
                    return@applyToBosses
                val amount = multiples[lvl - 2]

                val nearby = living.getNearbyEntities(6.0, 6.0, 6.0).filterIsInstance<Player>()
                for(player in nearby) {
                    val m = player.macrocosm!!
                    val defense = amount * m.stats()!!.defense
                    m.tempStats.defense -= defense.toFloat()
                    task(delay = 10 * 20L) {
                        m.tempStats.defense += defense.toFloat()
                    }
                    particle(Particle.BLOCK_CRACK) {
                        data = Material.NETHERITE_BLOCK.createBlockData()
                        this@particle.amount = 8
                        spawnAt(player.location)
                    }
                    sound(Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR) {
                        pitch = 0f
                        playAt(player.location)
                    }
                }
            }
        }
    }
}
