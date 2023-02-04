package space.maxus.macrocosm.area

import org.bukkit.Location

/**
 * A polygonal area that has an exit position and can teleport player out, if the PlayerEnterAreaEvent is cancelled
 */
class RestrictedArea(val inner: PolygonalArea, val exit: Location): Area(inner.id) {
    override fun contains(location: Location): Boolean {
        return inner.contains(location)
    }
}
