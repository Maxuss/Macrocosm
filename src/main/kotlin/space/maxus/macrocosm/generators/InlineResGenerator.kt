package space.maxus.macrocosm.generators

fun generate(at: String, generator: FnResGenerator) = InlineResGenerator(at, generator)

class InlineResGenerator(path: String, private val generator: FnResGenerator) : SingletonResGenerator(path) {
    override fun generate(): String {
        return generator()
    }
}
