package space.maxus.macrocosm.area

import org.bukkit.Location
import space.maxus.macrocosm.registry.Registry

object Areas {
    fun matching(location: Location) = Registry.AREA.iter().filterValues { it.contains(location) }
}
