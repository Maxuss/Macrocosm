package space.maxus.macrocosm.registry

interface AutoRegister<R> {
    fun register(registry: Registry<R>)
}
