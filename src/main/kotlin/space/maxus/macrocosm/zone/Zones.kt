package space.maxus.macrocosm.zone

import org.bukkit.Location
import space.maxus.macrocosm.registry.Registry

object Zones {
    fun matching(location: Location) = Registry.ZONE.iter().filterValues { it.contains(location) }
}
