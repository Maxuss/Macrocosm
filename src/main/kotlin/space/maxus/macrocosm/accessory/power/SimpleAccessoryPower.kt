package space.maxus.macrocosm.accessory.power

import org.bukkit.Material
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.stats.Statistics
import space.maxus.macrocosm.util.general.id

/**
 * A simple accessory power without any specific bonuses
 */
class SimpleAccessoryPower(id: String,
                           override val displayItem: Material, override val name: String, override val stats: Statistics, override val tier: String = "Starter"): AccessoryPower {
    override val id: Identifier = id(id)

    override fun registerListeners() {
        // Empty because simple APs do not have special abilities
    }
}
