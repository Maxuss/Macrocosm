package space.maxus.macrocosm.registry

import java.util.concurrent.ConcurrentHashMap

open class DefaultedRegistry<R>(name: Identifier, expose: Boolean) : Registry<R>(name, expose) {
    private val values: ConcurrentHashMap<Identifier, R> = ConcurrentHashMap()

    override fun iter(): ConcurrentHashMap<Identifier, R> {
        return values
    }

    override fun register(id: Identifier, value: R): R {
        if (values.containsKey(id)) {
            logger.warn("Tried to register duplicate key '$id'!")
            return values[id]!!
        }
        values[id] = value
        return value
    }
}
