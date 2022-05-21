package space.maxus.macrocosm.zone

import org.bukkit.Location
import space.maxus.macrocosm.util.Identifier
import java.util.concurrent.ConcurrentHashMap

object ZoneRegistry {
    private val zones: ConcurrentHashMap<Identifier, Zone> = ConcurrentHashMap(hashMapOf())

    fun register(id: Identifier, zone: Zone): Zone {
        if(zones.contains(id))
            return zone

        zones[id] = zone
        return zone
    }

    fun matching(location: Location) = zones.filterValues { it.contains(location) }

    fun find(id: Identifier) = zones[id]!!
}
