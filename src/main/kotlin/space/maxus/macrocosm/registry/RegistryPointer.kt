package space.maxus.macrocosm.registry

class RegistryPointer(val registry: Identifier, pointer: Identifier) {
    var pointer: Identifier = pointer; private set

    @Suppress("UNCHECKED_CAST")
    fun <T> trySet(v: T): Boolean {
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
}
