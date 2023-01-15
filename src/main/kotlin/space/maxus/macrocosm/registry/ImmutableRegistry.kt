package space.maxus.macrocosm.registry

@Suppress("UNCHECKED_CAST")
open class ImmutableRegistry<R : Clone>(
    name: Identifier,
    delegate: DelegatedRegistry<R>.(Identifier, R) -> Unit,
    expose: Boolean = true
) :
    DelegatedRegistry<R>(name, delegate, expose) {
    override fun findOrNull(id: Identifier?): R? {
        return super.findOrNull(id)?.clone() as? R
    }

    override fun find(id: Identifier): R {
        return super.find(id).clone() as R
    }
}
