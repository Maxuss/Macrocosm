package space.maxus.macrocosm.ability.types.armor

import net.axay.kspigot.event.listen
import space.maxus.macrocosm.ability.FullSetBonus
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.stats.Statistic

object ProtectorDragonBonus : FullSetBonus(
    "Protector Blood",
    "Increases your <green>${Statistic.DEFENSE.display}<gray> by <green>5%<gray> for every percent of your ${Statistic.HEALTH.display}<gray> missing."
) {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            if (!ensureSetRequirement(e.player))
                return@listen
            val ratio = (2 - (e.player.currentHealth / e.player.stats()!!.health))
            e.stats.defense *= ratio
        }
    }
}
