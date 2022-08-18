package space.maxus.macrocosm.util.data

import space.maxus.macrocosm.util.NULL
import space.maxus.macrocosm.util.general.ConditionalCallback
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

open class MutableContainer<V> protected constructor(var values: ConcurrentHashMap<UUID, V>) {
    companion object {
        fun <V> empty() = MutableContainer<V>(ConcurrentHashMap())
        fun trulyEmpty() = MutableContainer<NULL>(ConcurrentHashMap())

        fun MutableContainer<NULL>.setAllWith(with: List<UUID>) {
            for (w in with) {
                set(w, NULL)
            }
        }

        fun MutableContainer<NULL>.removeAllWith(with: List<UUID>) {
            for (w in with) {
                remove(w)
            }
        }
    }

    @Synchronized
    operator fun set(k: UUID, v: V) {
        values[k] = v
    }

    @Synchronized
    fun remove(k: UUID): V? {
        return values.remove(k)
    }

    @Synchronized
    operator fun contains(k: UUID) = values.containsKey(k)

    inline fun take(k: UUID, operator: (V) -> Unit): ConditionalCallback {
        return if (values.containsKey(k)) {
            operator(values[k]!!)
            ConditionalCallback.success()
        } else ConditionalCallback.fail()
    }

    inline fun revoke(k: UUID, operator: (V) -> Unit): ConditionalCallback {
        val value = values.remove(k)
        return if (value == null)
            ConditionalCallback.fail()
        else {
            operator(value)
            ConditionalCallback.success()
        }
    }

    @OptIn(ExperimentalContracts::class)
    inline fun filterAll(operator: (Pair<UUID, V>) -> Boolean) {
        contract {
            callsInPlace(operator)
        }
        this.values = ConcurrentHashMap(this.values.filter { operator(it.toPair()) })
    }

    inline fun iter(operator: (V) -> Unit) {
        for ((_, v) in values.iterator()) {
            operator(v)
        }
    }

    inline fun iterFull(operator: (Pair<UUID, V>) -> Unit) {
        for ((k, v) in values.iterator()) {
            operator(Pair(k, v))
        }
    }

    inline fun iterMut(operator: (V) -> V) {
        for ((k, v) in values.iterator()) {
            values[k] = operator(v)
        }
    }

    inline fun setOrTakeMut(k: UUID, operator: (V?) -> V) {
        if (values.containsKey(k)) {
            values[k] = operator(values[k]!!)
        } else {
            values[k] = operator(null)
        }
    }

    inline fun takeMut(k: UUID, operator: (V) -> V): ConditionalCallback {
        return if (values.containsKey(k)) {
            values[k] = operator(values[k]!!)
            ConditionalCallback.success()
        } else ConditionalCallback.fail()
    }

    inline fun takeMutOrRemove(k: UUID, operator: (V) -> Pair<V, TakeResult>) {
        if (values.containsKey(k)) {
            val (new, result) = operator(values[k]!!)
            if (result == TakeResult.REVOKE) {
                values.remove(k)
            } else {
                values[k] = new
            }
        }
    }

    override fun toString(): String {
        return "MutableContainer { values= { ${values.map { (k, v) -> k.toString() + ":" + v.toString() }} } }"
    }

    enum class TakeResult {
        RETAIN,
        REVOKE
    }
}

class ExpiringContainer<V> private constructor(val expirationMillis: Long, values: ConcurrentHashMap<UUID, V>): MutableContainer<V>(values) {
    companion object {
        fun <V> empty(expiry: Long) = ExpiringContainer<V>(expiry, ConcurrentHashMap())
    }
    @JvmSynthetic
    var lastModification = -1L

    inline fun trySetExpiring(key: UUID, operator: () -> V): ConditionalCallback {
        return if(System.currentTimeMillis() > lastModification + expirationMillis) {
            this[key] = operator()
            lastModification = System.currentTimeMillis()
            ConditionalCallback.success()
        } else ConditionalCallback.fail()
    }
}
