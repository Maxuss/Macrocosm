package space.maxus.macrocosm.area

import org.bukkit.Location
import space.maxus.macrocosm.area.spawning.SpawningPosition
import space.maxus.macrocosm.registry.Identified
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import java.util.function.Predicate
import kotlin.math.roundToInt

/**
 * An abstract class for areas that hold locations. Not to be confused with area models that hold actual area data
 */
abstract class Area(override val id: Identifier, val spawns: MutableList<SpawningPosition>) : Identified {
    /**
     * Area model for this area
     */
    val model get() = Registry.AREA_MODEL.find(id)

    companion object {
        /**
         * Constructs a simple area implementation
         */
        fun impl(id: Identifier, handler: Predicate<Location>): Area = Impl(id, handler)
    }

    /**
     * Checks if the provided location is in this area
     */
    abstract fun contains(location: Location): Boolean

    /**
     * Does a single spawn pass
     */
    fun doSpawnPass() {
        // Too many entities alive in this area, ignore a spawn pass
        if (this.spawns.isEmpty() || AreaLevelDbAdapter.spawnsPerArea.get(id).size >= (this.spawns.size.toDouble() * 1.5).roundToInt())
            return
        for (spawn in spawns) {
            val id = spawn.spawn() ?: continue
            AreaLevelDbAdapter.spawnsPerArea.put(this.id, id)
        }
    }

    private class Impl(id: Identifier, val handler: Predicate<Location>) : Area(id, mutableListOf()) {
        override fun contains(location: Location): Boolean = handler.test(location)
    }

    /**
     * A NULL Area
     */
    object Null : Area(Identifier.parse("null"), mutableListOf()) {
        override fun contains(location: Location): Boolean = false
    }
}
