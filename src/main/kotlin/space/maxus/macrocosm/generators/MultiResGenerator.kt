package space.maxus.macrocosm.generators

typealias FnResGenerator = () -> String

class MultiResGenerator(
    val generators: HashMap<String, FnResGenerator>
) : ResGenerator {
    override fun yieldGenerate(): Map<String, String> {
        return generators.mapValues { (_, v) -> v() }
    }
}
