package space.maxus.macrocosm.zone

import org.bukkit.Location
import space.maxus.macrocosm.util.Identifier
import java.util.function.Predicate

abstract class Zone(val id: Identifier, val name: String) {
    companion object {
        fun impl(id: Identifier, name: String, handler: Predicate<Location>): Zone = Impl(id, name, handler)
    }

    abstract fun contains(location: Location): Boolean

    private class Impl(id: Identifier, name: String, val handler: Predicate<Location>): Zone(id, name) {
        override fun contains(location: Location): Boolean = handler.test(location)
    }
}
