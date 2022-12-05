@file:OptIn(ExperimentalContracts::class)

package space.maxus.macrocosm.util.general

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@JvmInline
value class Callback(val chain: () -> Unit) {
    @OptIn(ExperimentalContracts::class)
    inline fun <R> then(runnable: () -> R): R {
        contract {
            callsInPlace(runnable, InvocationKind.EXACTLY_ONCE)
        }
        chain()
        return runnable()
    }

    fun exec() = chain()
}

data class ConditionalValueCallback<V>(val producer: () -> V?) {
    companion object {
        fun <V> success(value: V) = ConditionalValueCallback { value }
        fun <V> fail() = ConditionalValueCallback<V> { null }
    }

    inline fun then(crossinline runnable: (V) -> Unit): ConditionalValueCallback<V> {
        return ConditionalValueCallback {
            val value = producer()
            if (value != null)
                runnable(value)
            value
        }
    }

    inline fun otherwise(crossinline runnable: () -> Unit): ConditionalValueCallback<V> {
        return ConditionalValueCallback {
            val value = producer()
            if (value == null)
                runnable()
            value
        }
    }

    fun call(): V? {
        return producer()
    }

    operator fun invoke(): V? {
        return producer()
    }
}

data class ConditionalCallback(val condition: () -> Boolean) {
    companion object {
        fun success() = ConditionalCallback { true }
        fun fail() = ConditionalCallback { false }
    }

    inline fun then(crossinline runnable: () -> Unit): ConditionalCallback {
        return ConditionalCallback {
            val stored = condition()
            if (stored)
                runnable()
            stored
        }
    }

    inline fun otherwise(crossinline runnable: () -> Unit): ConditionalCallback {
        return ConditionalCallback {
            val stored = condition()
            if (!stored)
                runnable()
            stored
        }
    }

    fun call(): Boolean {
        return condition()
    }

    operator fun invoke(): Boolean {
        return condition()
    }
}

data class SuspendConditionalCallback(val condition: suspend () -> Boolean) {
    companion object {
        fun suspendSuccess() = SuspendConditionalCallback { true }
        fun suspendFail() = SuspendConditionalCallback { false }
    }

    suspend inline fun then(crossinline runnable: suspend () -> Unit): SuspendConditionalCallback {
        return SuspendConditionalCallback {
            val stored = condition()
            if (stored)
                runnable()
            stored
        }
    }

    suspend inline fun otherwise(crossinline runnable: suspend () -> Unit): SuspendConditionalCallback {
        return SuspendConditionalCallback {
            val stored = condition()
            if (!stored)
                runnable()
            stored
        }
    }

    suspend fun call(): Boolean {
        return condition()
    }

    suspend operator fun invoke(): Boolean {
        return condition()
    }
}

data class DeferredAction<T>(val self: T, val deferred: (T) -> Unit) {
    fun done() {
        this.deferred(self)
    }

    operator fun invoke() {
        this.deferred(self)
    }

    inline fun <R> first(action: T.() -> R): R {
        val ret = action(self)
        done()
        return ret
    }
}

inline fun <reified T> T.defer(noinline action: T.() -> Unit): DeferredAction<T> {
    return DeferredAction(this, action)
}
