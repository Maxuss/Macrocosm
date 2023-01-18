package space.maxus.macrocosm.ability.types.accessory

import net.axay.kspigot.event.listen
import space.maxus.macrocosm.ability.AccessoryAbility
import space.maxus.macrocosm.events.PlayerCalculateSpecialStatsEvent
import space.maxus.macrocosm.stats.Statistic

object FireTalisman : AccessoryAbility(
    "fire_talisman",
    "Decreases all ${Statistic.DAMAGE.display}<gray> taken from <gold>Fire<gray> and <gold>Lava<gray> by <red>20%<gray>."
) {
    override fun registerListeners() {
        listen<PlayerCalculateSpecialStatsEvent> { e ->
            if (!hasAccs(e.player))
                return@listen
            e.stats.fireResistance += .2f
        }
    }
}
