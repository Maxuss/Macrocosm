package space.maxus.macrocosm.slayer.zombie

import net.axay.kspigot.runnables.task
import space.maxus.macrocosm.slayer.SlayerAbility
import space.maxus.macrocosm.stats.Statistic
import kotlin.math.min

object ZombieAbilities {
    val REGENERATION = SlayerAbility(
        "regeneration",
        "revenant_horror",
        "Regeneration",
        "Boss rapidly regenerates <red>[50|150|1000|2500] ${Statistic.HEALTH.display}<gray> each second"
    ) {
        val healths = listOf(50, 150, 1000, 2500)
        task(period = 20L) {
            applyToBosses { mc, living, lvl ->
                println("APPLYING")
                if(lvl > 4)
                    return@applyToBosses
                val healthToRegen = healths[lvl - 1]
                mc.currentHealth = min(mc.currentHealth + healthToRegen, mc.calculateStats().health)
                mc.loadChanges(living)
            }
        }
    }
}
