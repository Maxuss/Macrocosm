package space.maxus.macrocosm.registry

import java.util.concurrent.ConcurrentHashMap

class CloseableRegistry<R>(name: Identifier): Registry<R>(name) {
    private var accepting: Boolean = false

    private val values: ConcurrentHashMap<Identifier, R> = ConcurrentHashMap()

    override fun iter(): ConcurrentHashMap<Identifier, R> {
        return values
    }

    override fun register(id: Identifier, value: R): R {
        if(!accepting) {
            logger.warn("Tried to register key '$id' while registry was closed!")
            return value
        }
        if(values.containsKey(id)) {
            logger.warn("Tried to register duplicate key '$id'!")
            return values[id]!!
        }
        values[id] = value
        return value
    }

    fun open() {
        accepting = true
    }

    fun close() {
        accepting = false
    }
}
