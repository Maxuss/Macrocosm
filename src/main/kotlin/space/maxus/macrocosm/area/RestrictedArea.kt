package space.maxus.macrocosm.area

import org.bukkit.Location

class RestrictedArea(val inner: PolygonalArea, val exit: Location): Area(inner.id) {
    override fun contains(location: Location): Boolean {
        return inner.contains(location)
    }
}
