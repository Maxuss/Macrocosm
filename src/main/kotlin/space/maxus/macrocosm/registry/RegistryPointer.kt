package space.maxus.macrocosm.registry

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import space.maxus.macrocosm.util.general.ConditionalCallback
import kotlin.reflect.KProperty

/**
 * A pointer to a value in certain registry
 *
 * @param registry registry this pointer's element belongs to
 * @param pointer ID of the element
 *
 * @property registry registry this pointer's element belongs to
 * @property pointer ID of the element
 */
class RegistryPointer(val registry: Identifier, pointer: Identifier) {
    var pointer: Identifier = pointer; private set

    /**
     * Attempts to set value inside this pointer.
     */
    fun <T : Any> trySet(v: T?): ConditionalCallback {
        val success = this.set(v)
        return ConditionalCallback { success }
    }

    /**
     * Writes a value to this pointer.
     *
     * @return false if failed to write value, true if succeeded
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> set(v: T?): Boolean {
        val actualRegistry = Registry.findOrNull(registry) as? Registry<Any> ?: return false
        this.pointer = if (v == null) Identifier.NULL else actualRegistry.byValue(v) ?: return false
        return true
    }

    /**
     * Reads a value from this pointer
     *
     * @throws ClassCastException if [T] is not the type of value in this pointer
     * @throws NullPointerException if it could not find element of ID [pointer] inside the [registry] registry
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> get(): T? {
        if (pointer.isNull())
            return null
        val actualRegistry = (Registry.find(registry) as? Registry<Any>)!!
        return actualRegistry.find(pointer) as T
    }

    /**
     * Attempts to read value from this pointer
     *
     * @return null if failed to read the value, value of the pointer otherwise
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : Any> tryGet(): T? {
        if (pointer.isNull())
            return null
        return try {
            val actualRegistry = Registry.find(registry) as Registry<Any>
            actualRegistry.findOrNull(pointer) as? T
        } catch (e: ClassCastException) {
            null
        }
    }

    /**
     * Gets the ID of the registry
     */
    operator fun component1(): Identifier {
        return this.registry
    }

    /**
     * Gets the ID of the pointer
     */
    operator fun component2(): Identifier {
        return this.pointer
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is RegistryPointer && other.registry == this.registry && other.pointer == this.pointer
    }

    override fun hashCode(): Int {
        var result = registry.hashCode()
        result = 31 * result + pointer.hashCode()
        return result
    }

    /**
     * Converts this pointer to the string representation in format of `registry@pointer`
     */
    override fun toString(): String {
        return "$registry@$pointer"
    }

    operator fun <T : Any> getValue(self: Any?, prop: KProperty<*>): T? {
        return tryGet()
    }

    operator fun <T : Any> setValue(self: Any?, prop: KProperty<*>, value: T?) {
        this.set(value)
    }
}

@Suppress("UNCHECKED_CAST")
fun <T> Iterable<RegistryPointer>.anyPoints(to: T): Boolean {
    val id = if (to is Identified) to.id else (Registry.findOrNull(
        firstOrNull()?.registry ?: return false
    ) as? Registry<T>)?.byValue(to)
        ?: return false
    return any { it.pointer == id }
}

@Suppress("UNCHECKED_CAST")
fun <T> registryPointer(registry: Identifier, value: T): RegistryPointer {
    return RegistryPointer(registry, (Registry.find(registry) as Registry<T>).byValue(value)!!)
}

fun registryPointer(registry: Identifier): RegistryPointer {
    return RegistryPointer(registry, Identifier.NULL)
}

object RegistryPointerTypeAdapter : TypeAdapter<RegistryPointer>() {
    override fun write(out: JsonWriter, value: RegistryPointer) {
        out.value("${value.registry}@${value.pointer}")
    }

    override fun read(reader: JsonReader): RegistryPointer {
        val str = reader.nextString()
        val (reg, id) = str.split("@".toRegex(), 2)
        return RegistryPointer(Identifier.parse(reg), Identifier.parse(id))
    }
}
