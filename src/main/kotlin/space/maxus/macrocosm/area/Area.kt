package space.maxus.macrocosm.area

import org.bukkit.Location
import space.maxus.macrocosm.registry.Identified
import space.maxus.macrocosm.registry.Identifier
import space.maxus.macrocosm.registry.Registry
import java.util.function.Predicate

abstract class Area(override val id: Identifier): Identified {
    val model get() = Registry.AREA_MODEL.find(id)

    companion object {
        fun impl(id: Identifier, handler: Predicate<Location>): Area = Impl(id, handler)
    }

    abstract fun contains(location: Location): Boolean

    private class Impl(id: Identifier, val handler: Predicate<Location>) : Area(id) {
        override fun contains(location: Location): Boolean = handler.test(location)
    }

    object Null: Area(Identifier.parse("null")) {
        override fun contains(location: Location): Boolean = false
    }
}
