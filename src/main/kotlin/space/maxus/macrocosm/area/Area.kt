package space.maxus.macrocosm.area

import org.bukkit.Location
import space.maxus.macrocosm.registry.Identified
import space.maxus.macrocosm.registry.Identifier
import java.util.function.Predicate

abstract class Area(override val id: Identifier, val name: String): Identified {
    companion object {
        fun impl(id: Identifier, name: String, handler: Predicate<Location>): Area = Impl(id, name, handler)
    }

    abstract fun contains(location: Location): Boolean

    private class Impl(id: Identifier, name: String, val handler: Predicate<Location>) : Area(id, name) {
        override fun contains(location: Location): Boolean = handler.test(location)
    }
}
