package space.maxus.macrocosm.ability.types.accessory

import net.axay.kspigot.event.listen
import net.axay.kspigot.runnables.task
import space.maxus.macrocosm.ability.AccessoryAbility
import space.maxus.macrocosm.events.PlayerKillEntityEvent
import space.maxus.macrocosm.stats.Statistic

object ReaperOrb : AccessoryAbility(
    "reaper_orb",
    "Gain <red>+2${Statistic.STRENGTH.display}<gray> for <green>5 seconds<gray> after killing a mob."
) {
    override fun registerListeners() {
        listen<PlayerKillEntityEvent> { e ->
            if (!hasAccs(e.player))
                return@listen
            e.player.tempStats.strength += 5
            task(delay = 5 * 20L) {
                e.player.tempStats.strength -= 5
            }
        }
    }
}
