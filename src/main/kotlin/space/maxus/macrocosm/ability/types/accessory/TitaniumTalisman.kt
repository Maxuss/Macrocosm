package space.maxus.macrocosm.ability.types.accessory

import net.axay.kspigot.event.listen
import space.maxus.macrocosm.ability.AccessoryAbility
import space.maxus.macrocosm.events.PlayerCalculateStatsEvent
import space.maxus.macrocosm.stats.Statistic

class TitaniumTalisman(applicable: String, private val amount: Int) : AccessoryAbility(
    applicable,
    "Increases your ${Statistic.MINING_FORTUNE.display} by <gold>+$amount<gray> when below <blue>Y 30<gray>."
) {
    override fun registerListeners() {
        listen<PlayerCalculateStatsEvent> { e ->
            if (!hasAccs(e.player) || (e.player.paper?.location?.y ?: 60.0) > 20)
                return@listen
            e.stats.miningFortune += amount
        }
    }
}
