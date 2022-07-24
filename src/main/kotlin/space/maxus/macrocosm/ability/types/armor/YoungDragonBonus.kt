package space.maxus.macrocosm.ability.types.armor

import net.axay.kspigot.event.listen
import space.maxus.macrocosm.ability.FullSetBonus
import space.maxus.macrocosm.events.PlayerCalculateSpecialStatsEvent
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.stats.Statistic

object YoungDragonBonus : FullSetBonus(
    "Young Blood",
    "Gain <white>+100 ${Statistic.SPEED}<gray> while above <green>50%<gray> Health. Boosts your <green>Speed Cap<gray> by <yellow>100<gray>."
) {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            if (!ensureSetRequirement(e.player))
                return@listen
            e.stats.speed += 100f
        }

        listen<PlayerCalculateSpecialStatsEvent> { e ->
            if (!ensureSetRequirement(e.player))
                return@listen
            e.stats.speedCapBoost += 100f
        }
    }
}
