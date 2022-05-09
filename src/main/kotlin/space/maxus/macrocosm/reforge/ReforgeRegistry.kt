@file:Suppress("unused")

package space.maxus.macrocosm.reforge

import net.axay.kspigot.extensions.server
import org.bukkit.event.Listener
import space.maxus.macrocosm.Macrocosm
import space.maxus.macrocosm.util.Identifier

object ReforgeRegistry {
    val reforges: HashMap<Identifier, Reforge> = hashMapOf()

    fun register(name: Identifier, reforge: Reforge): Reforge {
        if (reforges.containsKey(name))
            return reforge
        reforges[name] = reforge
        if (reforge.abilityName != null) {
            server.pluginManager.registerEvents(reforge as Listener, Macrocosm)
        }
        return reforge
    }

    fun nameOf(ref: Reforge) = reforges.filter { (_, v) -> v == ref }.map { (k, _) -> k }.firstOrNull()

    fun find(name: Identifier) = reforges[name]
}
