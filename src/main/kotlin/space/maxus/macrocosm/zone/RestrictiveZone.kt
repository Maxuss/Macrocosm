package space.maxus.macrocosm.zone

import org.bukkit.Location

class RestrictiveZone(val inner: PolygonalZone, val exit: Location): Zone(inner.id, inner.name) {
    override fun contains(location: Location): Boolean {
        return inner.contains(location)
    }
}
