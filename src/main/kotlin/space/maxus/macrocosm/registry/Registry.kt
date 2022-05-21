package space.maxus.macrocosm.registry

import space.maxus.macrocosm.util.Identifier
import space.maxus.macrocosm.util.id
import java.util.concurrent.ConcurrentHashMap

interface Registry<T> {
    fun iter(): ConcurrentHashMap<Identifier, T>
    fun register(id: Identifier, value: T): T
    fun byValue(value: T) = iter().filter { it.value == value }.toList().firstOrNull()
    fun find(id: Identifier): T = iter()[id]!!
    fun findOrNull(id: Identifier): T? = iter()[id]

    companion object {
        fun <V> register(registry: Registry<V>, id: Identifier, value: V) = registry.register(id, value)
        fun <V> register(registry: Registry<V>, id: String, value: V) = registry.register(id(id), value)
    }
}
