package space.maxus.macrocosm.area.spawning

import net.axay.kspigot.extensions.worlds
import net.minecraft.nbt.CompoundTag
import org.bukkit.Location
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import space.maxus.macrocosm.util.general.getId
import space.maxus.macrocosm.util.general.putId
import java.util.*

/**
 * Represents a single position at which a mob can spawn
 */
data class SpawningPosition(
    /**
     * Location at which the entity spawns
     */
    val location: Location,
    /**
     * Entity that spawns at this position
     */
    val entity: Identifier) {

    companion object {
        fun read(from: CompoundTag): SpawningPosition {
            val x = from.getInt("X")
            val y = from.getInt("Y")
            val z = from.getInt("Z")
            val entity = from.getId("Entity")
            return SpawningPosition(Location(worlds[0], x.toDouble(), y.toDouble(), z.toDouble()), entity)
        }
    }

    /**
     * Spawns a new entity at this position
     */
    fun spawn(): UUID? {
        if(location.getNearbyEntities(7.0, 7.0, 7.0).size >= 5) {
            // Extra check not to spawn too many entities
            return null
        }
        val e = Registry.ENTITY.find(entity).spawn(location)
        return e.uniqueId
    }

    /**
     * Saves this position to compound
     */
    fun save(): CompoundTag {
        val tag = CompoundTag()
        tag.putInt("X", location.x.toInt())
        tag.putInt("Y", location.y.toInt())
        tag.putInt("Z", location.z.toInt())
        tag.putId("Entity", entity)
        return tag
    }
}
