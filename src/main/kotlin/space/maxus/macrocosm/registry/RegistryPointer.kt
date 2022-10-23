package space.maxus.macrocosm.registry

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import space.maxus.macrocosm.util.general.ConditionalCallback

class RegistryPointer(val registry: Identifier, pointer: Identifier) {
    var pointer: Identifier = pointer; private set

    /**
     * Attempts to set value inside this pointer.
     */
    fun <T> trySet(v: T): ConditionalCallback {
        val success = this.set(v)
        return ConditionalCallback { success }
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> set(v: T): Boolean {
        val actualRegistry: Registry<T> = (Registry.find(registry) as? Registry<T>) ?: return false
        this.pointer = actualRegistry.byValue(v) ?: return false
        return true
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(): T {
        val actualRegistry: Registry<T> = (Registry.find(registry) as? Registry<T>)!!
        return actualRegistry.find(pointer)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> tryGet(): T? {
        return try {
            val actualRegistry = Registry.find(registry) as Registry<T>
            actualRegistry.findOrNull(pointer)
        } catch (e: ClassCastException) {
            null
        }
    }

    operator fun component1(): Identifier {
        return this.registry
    }

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

    override fun toString(): String {
        return "$registry@$pointer"
    }
}

object RegistryPointerTypeAdapter: TypeAdapter<RegistryPointer>() {
    override fun write(out: JsonWriter, value: RegistryPointer) {
        out.value("${value.registry}@${value.pointer}")
    }

    override fun read(reader: JsonReader): RegistryPointer {
        val str = reader.nextString()
        val (reg, id) = str.split("@".toRegex(), 2)
        return RegistryPointer(Identifier.parse(reg), Identifier.parse(id))
    }
}
