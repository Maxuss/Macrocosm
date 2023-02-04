package space.maxus.macrocosm.area

import org.bukkit.Location
import space.maxus.macrocosm.registry.Registry

/**
 * A helper object for areas
 */
object Areas {
    /**
     * Finds all matching areas that contain this location
     */
    fun matching(location: Location) = Registry.AREA.iter().filterValues { it.contains(location) }
}
