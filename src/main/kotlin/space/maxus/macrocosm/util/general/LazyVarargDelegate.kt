package space.maxus.macrocosm.util.general

import space.maxus.macrocosm.util.identity
import kotlin.reflect.KProperty

class LazyVarargDelegate<V, O>(varargs: Array<out V>, private val mapping: (V) -> O) {
    private val listValue by lazy { listOf(*varargs).map(mapping) }
    operator fun getValue(thisRef: Any?, prop: KProperty<*>): List<O> {
        return this.listValue
    }
}

fun <V> varargs(argv: Array<out V>) = LazyVarargDelegate(argv, identity())

fun <V, O> varargs(argv: Array<out V>, mapping: (V) -> O) = LazyVarargDelegate(argv, mapping)
