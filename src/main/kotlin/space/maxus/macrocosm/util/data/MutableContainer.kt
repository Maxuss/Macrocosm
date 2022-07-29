package space.maxus.macrocosm.util.data

import space.maxus.macrocosm.util.NULL
import space.maxus.macrocosm.util.generic.ConditionalCallback
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class MutableContainer<V> private constructor(val values: ConcurrentHashMap<UUID, V>) {
    companion object {
        fun <V> empty() = MutableContainer<V>(ConcurrentHashMap())
        fun trulyEmpty() = MutableContainer<NULL>(ConcurrentHashMap())
    }

    operator fun set(k: UUID, v: V) {
        values[k] = v
    }

    fun remove(k: UUID): V? {
        return values.remove(k)
    }

    operator fun contains(k: UUID) = values.containsKey(k)

    inline fun take(k: UUID, operator: (V) -> Unit): ConditionalCallback {
        return if (values.containsKey(k)) {
            operator(values[k]!!)
            ConditionalCallback.success()
        }
        else ConditionalCallback.fail()
    }

    inline fun revoke(k: UUID, operator: (V) -> Unit): ConditionalCallback {
        val value = values.remove(k)
        return if(value == null)
            ConditionalCallback.fail()
        else {
            operator(value)
            ConditionalCallback.success()
        }
    }

    inline fun iter(operator: (V) -> Unit) {
        for((_, v) in values) {
            operator(v)
        }
    }

    inline fun iterMut(operator: (V) -> V) {
        for((k, v) in values) {
            values[k] = operator(v)
        }
    }

    inline fun setOrTakeMut(k: UUID, operator: (V?) -> V) {
        if(values.containsKey(k)) {
            values[k] = operator(values[k]!!)
        } else {
            values[k] = operator(null)
        }
    }

    inline fun takeMut(k: UUID, operator: (V) -> V): ConditionalCallback {
        return if (values.containsKey(k)) {
            values[k] = operator(values[k]!!)
            ConditionalCallback.success()
        }
        else ConditionalCallback.fail()
    }

    override fun toString(): String {
        return "MutableContainer { values= { ${values.map { (k, v) -> k.toString() + ":" + v.toString() }} } }"
    }
}
