package space.maxus.macrocosm.registry

import space.maxus.macrocosm.util.Identifier
import java.util.concurrent.ConcurrentHashMap

open class DefaultedRegistry<R>(name: Identifier): Registry<R>(name) {
    private val values: ConcurrentHashMap<Identifier, R> = ConcurrentHashMap()

    override fun iter(): ConcurrentHashMap<Identifier, R> { return values }

    override fun register(id: Identifier, value: R): R {
        if(values.containsKey(id)) {
            logger.warn("Tried to register duplicate key '$id'!")
            return values[id]!!
        }
        values[id] = value
        return value
    }
}
