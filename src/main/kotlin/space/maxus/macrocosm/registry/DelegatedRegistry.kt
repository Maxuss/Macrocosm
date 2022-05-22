package space.maxus.macrocosm.registry

class DelegatedRegistry<R>(
    name: Identifier,
    private val delegate: DelegatedRegistry<R>.(id: Identifier, value: R) -> Unit
) : DefaultedRegistry<R>(name) {
    override fun register(id: Identifier, value: R): R {
        if(!iter().containsKey(id))
            delegate(this, id, value)
        return super.register(id, value)
    }
}
