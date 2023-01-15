package space.maxus.macrocosm.ability.types.accessory

import net.axay.kspigot.event.listen
import space.maxus.macrocosm.ability.AccessoryAbility
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.stats.Statistic

class HasteAbility(applicable: String, private val amount: Int): AccessoryAbility(applicable, "Increases your ${Statistic.MINING_SPEED.display}<gray> by <gold>+$amount<gray>.") {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            if(hasAccs(e.player))
                e.stats.miningSpeed += amount
        }
    }
}
