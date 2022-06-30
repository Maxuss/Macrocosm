package space.maxus.macrocosm.util.generic

inline fun <reified T> collect(vararg iters: Iterable<T>) = mutableListOf<T>().apply { iters.forEach { addAll(it) } }
