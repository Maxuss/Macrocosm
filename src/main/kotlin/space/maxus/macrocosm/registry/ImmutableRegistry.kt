package space.maxus.macrocosm.registry

@Suppress("UNCHECKED_CAST")
class ImmutableRegistry<R : Clone>(name: Identifier, delegate: DelegatedRegistry<R>.(Identifier, R) -> Unit) :
    DelegatedRegistry<R>(name, delegate) {
    override fun findOrNull(id: Identifier): R? {
        return super.findOrNull(id)?.clone() as? R
    }

    override fun find(id: Identifier): R {
        return super.find(id).clone() as R
    }
}
