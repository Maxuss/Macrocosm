package space.maxus.macrocosm.ability.types.armor

import net.axay.kspigot.event.listen
import space.maxus.macrocosm.ability.FullSetBonus
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent

object SuperiorDragonBonus: FullSetBonus("Superior Blood", "Increases <green>all<gray> of your stats by <gold>10%<gray>.") {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            if(!ensureSetRequirement(e.player))
                return@listen
            e.stats.multiply(1.1f)
        }
    }
}
