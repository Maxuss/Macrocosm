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
}
