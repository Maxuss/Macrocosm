@file:OptIn(ExperimentalContracts::class)

package space.maxus.macrocosm.util.generic

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@JvmInline
value class Callback(val chain: () -> Unit) {
    inline fun <R> then(runnable: () -> R): R {
        contract {
            callsInPlace(runnable, InvocationKind.EXACTLY_ONCE)
        }
        chain()
        return runnable()
    }

    fun exec() = chain()
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
}
