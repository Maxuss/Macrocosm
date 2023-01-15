package space.maxus.macrocosm.ability.types.accessory

import net.axay.kspigot.event.listen
import space.maxus.macrocosm.ability.AccessoryAbility
import space.maxus.macrocosm.events.PlayerCalculateSpecialStatsEvent
import kotlin.math.roundToInt

class FeatherTalisman(applicable: String, private val amount: Float): AccessoryAbility(applicable, "Decreases all <blue>Fall Damage<gray> taken <gold>Fire<gray> and by <red>${(amount * 100f).roundToInt()}%<gray>.") {
    override fun registerListeners() {
        listen<PlayerCalculateSpecialStatsEvent> { e ->
            if(!hasAccs(e.player))
                return@listen
            e.stats.fallResistance += amount
        }
    }
}
