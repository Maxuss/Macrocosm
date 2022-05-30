package space.maxus.macrocosm.generators

abstract class SingletonResGenerator(val desiredPath: String) : ResGenerator {
    abstract fun generate(): String

    final override fun yieldGenerate(): Map<String, String> {
        return mapOf(desiredPath to generate())
    }
}
