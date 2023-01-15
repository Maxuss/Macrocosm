package space.maxus.macrocosm.accessory.power

import org.bukkit.entity.Player
import space.maxus.macrocosm.players.macrocosm
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.stats.Statistics

interface AccessoryPower {
    val name: String
    val tier: String
    val id: Identifier
    val stats: Statistics

    fun registerListeners()

    fun ensureRequirements(player: Player): Boolean {
        return player.macrocosm?.accessoryBag?.power == this.id
    }
}
