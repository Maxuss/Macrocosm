package space.maxus.macrocosm.registry

data class RegistryPointer(val registry: Identifier, val pointer: Identifier) {
    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> get(): T {
        val actualRegistry: Registry<T> = (Registry.find(registry) as? Registry<T>)!!
        return actualRegistry.find(pointer)
    }

    @Suppress("UNCHECKED_CAST")
    inline fun <reified T> tryGet(): T? {
        return try {
            val actualRegistry = Registry.find(registry) as Registry<T>
            actualRegistry.findOrNull(pointer)
        } catch (e: ClassCastException) {
            null
        }
    }
}
