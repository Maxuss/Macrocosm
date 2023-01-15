package space.maxus.macrocosm.ability.types.accessory

import net.axay.kspigot.runnables.task
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.ability.AccessoryAbility

class EmeraldAbility(applicable: String, private val amount: Int): AccessoryAbility(applicable, "Get <gold>+$amount coin<gray> every minute!") {
    override fun registerListeners() {
        task(period = 60 * 20L, sync = false) {
            for(player in Macrocosm.loadedPlayers.values) {
                if(hasAccs(player))
                    player.purse += amount.toBigDecimal()
            }
        }
    }
}
