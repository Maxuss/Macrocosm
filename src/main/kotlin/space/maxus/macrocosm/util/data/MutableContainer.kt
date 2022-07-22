package space.maxus.macrocosm.util.data

import java.util.*
import java.util.concurrent.ConcurrentHashMap

class MutableContainer<V> private constructor(private val values: ConcurrentHashMap<UUID, V>) {
    companion object {
        fun <V> empty() = MutableContainer<V>(ConcurrentHashMap())
    }

    operator fun set(k: UUID, v: V) {
        values[k] = v
    }

    fun remove(k: UUID): V? {
        return values.remove(k)
    }

    operator fun contains(k: UUID) = values.containsKey(k)

    fun take(k: UUID, operator: (V) -> Unit) {
        if(values.containsKey(k)) {
            operator(values[k]!!)
        }
    }

    fun takeMut(k: UUID, operator: (V) -> V) {
        if(values.containsKey(k)) {
            values[k] = operator(values[k]!!)
        }
    }
}
