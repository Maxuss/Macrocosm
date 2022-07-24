package space.maxus.macrocosm.util.generic

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
