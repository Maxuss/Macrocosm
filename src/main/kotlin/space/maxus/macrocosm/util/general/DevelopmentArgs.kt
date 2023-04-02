package space.maxus.macrocosm.util.general

import space.maxus.macrocosm.util.annotations.DevelopmentOnly
import java.util.concurrent.ConcurrentHashMap

@DevelopmentOnly
object DevelopmentArgs {
    private val values: ConcurrentHashMap<String, Any?> = ConcurrentHashMap()
    val required: List<String> get() = values.keys().toList()

    private fun <V: Any> require(key: String, withDefault: V) {
        values[key] = withDefault
    }

    operator fun <V> set(key: String, value: V) {
        values[key] = value
    }

    @Suppress("UNCHECKED_CAST")
    operator fun <V> get(key: String): V {
        return values[key] as V
    }

    fun num(key: String): Number {
        return values[key] as Number
    }

    fun str(key: String): String {
        return values[key] as String
    }

    fun bool(key: String): Boolean {
        return values[key] as Boolean
    }

    init {
        // Put `require`s here
    }
}
